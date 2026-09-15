package net.maxtyson.jfxhr.loader;

import net.maxtyson.jfxhr.Reloadable;
import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Objects;

import static net.maxtyson.jfxhr.compiler.FullyQualifiedName.sharesSourcePacakage;
import static net.maxtyson.jfxhr.compiler.ReflectionHelpers.*;

//@credit https://stackoverflow.com/questions/4133709/is-it-possible-in-java-to-create-blank-instance-of-class-without-no-arg-constr

public class ClassLoader {


    public static Class<?> load(Path compiledOutput, String className) throws Exception {

        // Open the compiled path
        File file = new File(compiledOutput.toAbsolutePath().toString());
        URL url = file.toURI().toURL();

        // Load the class
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        return classLoader.loadClass(className);
    }

    public static Object replaceInstance(Object oldInstance, Class<?> sourceClass) throws Exception {
        return replaceInstance(oldInstance, null, sourceClass);
    }

    public static Object replaceInstance(Object oldInstance, Object newInstance, Class<?> sourceClass) throws Exception {

        // Target instance is either the one created by parent constructor or freshly instantiated
        Object targetInstance = newInstance;
        if (targetInstance == null) {

             // Create a new instance of the class
            Constructor<?> constructor = getConstructorUsed(oldInstance, null, sourceClass);
            Object[] args = getArgs(oldInstance, null, constructor);
            targetInstance = constructor.newInstance(args);
        }

        Class<?> oldClass = oldInstance.getClass();
        Class<?> current = sourceClass;

        // Let the instance perform any local preloads
        if (targetInstance instanceof Reloadable)
            ((Reloadable) targetInstance).beforeReload();

        // Get the that were used to construct the new and old element
        Object[] oldArgsTypes = internalReadArgsTypes(oldInstance);
        Object[] oldArgValues = internalReadArgs(oldInstance);
        Object[] newArgsTypes = internalReadArgsTypes(targetInstance);

        boolean argsChanged = oldArgsTypes != null && newArgsTypes != null && !Arrays.equals(oldArgsTypes, newArgsTypes);

        // Inject old state into object
        while (current != null && sharesSourcePacakage(current.getName(), oldClass.getPackageName())) {
            for (Field newField : current.getDeclaredFields()) {

                // Static stored elsewhere (@todo verify existence of bss in java)
                if (Modifier.isStatic(newField.getModifiers()))
                    continue;

                // Skip any internal state
                if (newField.getName().startsWith("__jfxhr_") || newField.isSynthetic())
                    continue;

                // Make sure new field is same name and type
                Field oldField = oldClass.getDeclaredField(newField.getName());
                if (!oldField.getType().getName().equals(newField.getType().getName()))
                    continue;

                // Force fields to be reflectively visible
                oldField.setAccessible(true);
                newField.setAccessible(true);

                Object oldValue = oldField.get(oldInstance);
                Object newValue = newField.get(targetInstance);

                // If the args were updated then don't copy the old arg over
                if (argsChanged && isArgValue(oldValue, oldArgValues))
                    continue;

                // Any field that is uninitialised will be set up during construction, so don't copy the old constructed values
                if (newInstance != null && !isDefaultValue(newValue, newField.getType()) && !Objects.equals(oldValue, newValue))
                    continue;

                newField.set(targetInstance, oldValue);
            }

            // Ensure superclasses are inited aswell
            current = current.getSuperclass();
        }

        // Let the instance perform any local reloads
        if (targetInstance instanceof Reloadable)
            ((Reloadable) targetInstance).afterReload();

        return targetInstance;
    }

    private static Constructor<?> getConstructorUsed(Object oldInstance, Object newInstance, Class<?> sourceClass) throws NoSuchFieldException, NoSuchMethodException {

        // The new code may have added/removed args so try get the new constructor first
        Class<?>[] args = internalReadArgsTypes(newInstance);
        if (args == null)
            args = internalReadArgsTypes(oldInstance);

        // Args are yet to be attached as meta-data (there
        if (args == null)
            return sourceClass.getDeclaredConstructors()[0];

        // Sytax enforces that constructors must not be arbitrary, so it is known that if the types match then it MUST be the same
        for (Constructor<?> c : sourceClass.getDeclaredConstructors())
            if (Arrays.equals(c.getParameterTypes(), args))
                return c;

        // Neither the old constructor nor the new one exists
        throw new NoSuchMethodException("No constructor on " + sourceClass.getName() + " matches previously used signature " + Arrays.toString(args));
    }

}

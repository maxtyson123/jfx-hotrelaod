package net.maxtyson.jfxhr.loader;

import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

//@credit https://stackoverflow.com/questions/4133709/is-it-possible-in-java-to-create-blank-instance-of-class-without-no-arg-constr

public class ClassLoader {

    public static String fullQualifiedNameFromPath(Path file){

        String fqnPackage = file.getParent().toString().replace("/", ".");
        String fqnClass = file.getFileName().toString().replace(".java", "");

        return fqnPackage + "." + fqnClass;
    }

    public static Class<?> load(Path compiledOutput, String className) throws Exception {

        // Open the compiled path
        File file = new File(compiledOutput.toAbsolutePath().toString());
        URL url = file.toURI().toURL();

        // Load the class
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        return classLoader.loadClass(className);
    }

     private static boolean sharesTopLevel(String className, String packageScope){
         return className.startsWith(packageScope.split("\\.")[0]);
    }

    public static Object replaceInstance(Object oldInstance, Class<?> sourceClass) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException, NoSuchFieldException {

        // Create a new instance of the class
        Object newInstance = sourceClass.getDeclaredConstructor().newInstance();

        Class<?> oldClass = oldInstance.getClass();
        Class<?> current = sourceClass;

        // Inject old state into object
        while (current != null && sharesTopLevel(current.getName(), oldClass.getPackageName())) {
            for (Field newField : current.getDeclaredFields()) {

                // Static stored elsewhere (@todo verify existence of bss in java)
                if (Modifier.isStatic(newField.getModifiers()))
                    continue;

                // Make sure new field is same name and type
                Field oldField = oldClass.getDeclaredField(newField.getName());
                if (!oldField.getType().getName().equals(newField.getType().getName()))
                    continue;


                // Force fields to be reflectively visible
                oldField.setAccessible(true);
                newField.setAccessible(true);

                // Populate with old data
                newField.set(newInstance, oldField.get(oldInstance));
            }


            // Ensure superclasses are inited aswell
            current = current.getSuperclass();
        }

        return newInstance;
    }
}

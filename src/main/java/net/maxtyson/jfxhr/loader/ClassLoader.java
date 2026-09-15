package net.maxtyson.jfxhr.loader;

import sun.reflect.ReflectionFactory;

import java.io.File;
import java.lang.reflect.*;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.Arrays;

//@credit https://stackoverflow.com/questions/4133709/is-it-possible-in-java-to-create-blank-instance-of-class-without-no-arg-constr

public class ClassLoader {

    public static String fullQualifiedNameFromFile(Path file){

        String fqnPackage = file.getParent().toString().replace("/", ".");
        String fqnClass = file.getFileName().toString().replace(".java", "");

        return fqnPackage + "." + fqnClass;
    }

       public static String fullQualifiedNameFromDir(Path dir){

        String fqnPackage = dir.toString().replace("/", ".");
        return fqnPackage;
    }

    public static Class<?> load(Path compiledOutput, String className) throws Exception {

        // Open the compiled path
        File file = new File(compiledOutput.toAbsolutePath().toString());
        URL url = file.toURI().toURL();

        // Load the class
        URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
        return classLoader.loadClass(className);
    }

     public static boolean sharesTopLevel(String className, String packageScope){
         return className.startsWith(packageScope.split("\\.")[0]);
    }

    public static Object replaceInstance(Object oldInstance, Class<?> sourceClass) throws Exception {

        // Create a new instance of the class
        Constructor<?> constructoror = sourceClass.getDeclaredConstructors()[0];
        Object newInstance = constructoror.newInstance(getOldArgs(oldInstance, constructoror.getParameters()));

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

    private static Object[] getOldArgs(Object oldInstance, Parameter[] params) throws Exception {


        Object[] args = new Object[params.length];

        for (int i = 0; i < params.length; i++) {

            // Get the stored old arg
            Field f = oldInstance.getClass().getDeclaredField(params[i].getName());
            f.setAccessible(true);

            // Copy the old arg
            args[i] = f.get(oldInstance);
        }

        return args;
    }

}

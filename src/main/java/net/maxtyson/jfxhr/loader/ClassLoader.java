package net.maxtyson.jfxhr.loader;

import java.io.File;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;

public class ClassLoader {

   public static Class<?> load(Path compiledOutput, String className) throws Exception {

       // Open the compiled path
       File file = new File(compiledOutput.toAbsolutePath().toString());
       URL url = file.toURI().toURL();

       // Load the class
       URLClassLoader classLoader = new URLClassLoader(new URL[]{url});
       return classLoader.loadClass(className);
   }
}

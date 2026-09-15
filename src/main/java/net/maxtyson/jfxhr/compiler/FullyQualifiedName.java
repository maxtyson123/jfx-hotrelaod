package net.maxtyson.jfxhr.compiler;

import java.nio.file.Path;

public class FullyQualifiedName {

     public static String fromFile(Path file) {

        String fqnPackage = file.getParent().toString().replace("/", ".");
        String fqnClass = file.getFileName().toString().replace(".java", "");

        return fqnPackage + "." + fqnClass;
    }


    public static String fromDirectory(Path dir) {

        String fqnPackage = dir.toString().replace("/", ".");
        return fqnPackage;
    }

    public static boolean sharesSourcePacakage(String className, String packageScope) {
        return className.startsWith(packageScope.split("\\.")[0]);
    }

    public static Path getFQNPart(Path watchDir, Path entry) {

         return entry.subpath(watchDir.getNameCount(), entry.getNameCount());
    }

}

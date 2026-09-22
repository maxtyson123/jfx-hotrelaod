# JavaFX Hot Reloader (jfxhr)
A project to recompile and inject java classes on the fly as they are changed. Made mainly to speed up development on GUI code so that you dont have to recompile, re run and re setup state for small changes.

Started when working on a project and realised how good I had with react's hot realoading so decided to port it to a java project. 

Works by watching classes in the project source directory, and compiling them to java byte code and then loading them on to the JVM. From there, it walks the scene graph finding any instance of the class and switches it for a new instance of the freshly compiled code. This instance will have any common fields copied over and will only re run the constructor if it has been changed (ie tries to preserve state).  

In the future it will rely more the injected metadata to make integraion with prjects easier.

# Demo
[![demovid](https://i.imgur.com/W2RJ5nH.png)](https://www.youtube.com/watch?v=Wwt0KOYOsQo "demovid")

# Usage
Still requires a bit of work to remove the friction with intergration into projects, usage will be posted here when that is done.

For now checkout the watched-test, and look at demo package in main source tree. Also see Reloadable class for some helpers to intergrate if you want to try.

# Todo
- proper readme
- walk entire project for any references to the class instead of just fx scene
- shadow args for entire project injected as gradle build step
- figure out way to auto do the afterReload() calls instead of making it the dev's problem
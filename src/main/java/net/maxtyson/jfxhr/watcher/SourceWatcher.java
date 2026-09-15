package net.maxtyson.jfxhr.watcher;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import static java.nio.file.StandardWatchEventKinds.*;

public class SourceWatcher {


    /// SourceWatcher specific logger
    private static final Logger log = LogManager.getLogger(SourceWatcher.class);

    private Consumer<Path> onFileChanged;
    private PauseTransition debouncer;
    private Path changeEvent;

    private Path watchDirectory;
    private WatchService watcher;
    private ExecutorService watchingThread;
    private final Map<WatchKey, Path> subDirectories = new HashMap<>();


    public SourceWatcher(Path dir, Consumer<Path> onFileChanged) {

        this.watchDirectory = dir;
        this.onFileChanged = onFileChanged;

        // Ensure files arent compiled mid write to disk
        debouncer = new PauseTransition(Duration.millis(300));
        debouncer.setOnFinished(e -> onFileChanged.accept(changeEvent));

    }

     private void watchDirectoryContents(Path start) throws IOException {

        Files.walkFileTree(start, new SimpleFileVisitor<Path>() {


            @Override
            public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {

                // Add the directory to the list of directories to watch
                WatchKey key = dir.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);
                subDirectories.put(key, dir);

                return FileVisitResult.CONTINUE;
            }
        });
    }

    private void spinUpWatchers() {

         watchingThread = Executors.newSingleThreadExecutor();
         watchingThread.submit(() -> {

            log.info("Started watching '{}' on tid {}", watchDirectory, Thread.currentThread().getName());

            try {

                watcher = FileSystems.getDefault().newWatchService();
                watchDirectoryContents(watchDirectory);

                while (!Thread.currentThread().isInterrupted()) {

                    // Wait for directory events
                    WatchKey key = watcher.take();

                    // Look up the actual directory this event was watched for on
                    Path directory = subDirectories.get(key);
                    if (directory == null) {
                        System.err.println("WatchKey not recognized!");
                        continue;
                    }

                    for (WatchEvent<?> event : key.pollEvents()) {

                        // Skip malformed event
                        if (event.kind() == OVERFLOW)
                            continue;

                        // Parse event
                        WatchEvent<Path> fileEvent = (WatchEvent<Path>) event;
                        Path filename = fileEvent.context();
                        Path file = directory.resolve(filename);
                        boolean isDirectory = Files.isDirectory(file);

                        // Add any new directories to the directories to watch
                        if (event.kind() == ENTRY_CREATE)
                            if (isDirectory)
                                watchDirectoryContents(file);

                        // Pass event to reloader pipeline
                        if(!isDirectory){
                            changeEvent = file;
                            debouncer.playFromStart();
                        }

                        log.info("Event {} on {} '{}'", fileEvent.kind().name(),  isDirectory ? "directory" : "file", fileEvent.context());
                    }

                    // Signal that event has been handled and wait for next if safe to continue polling
                    if (key.reset())
                        continue;

                    // Directory must have been deleted so stop watching it
                    subDirectories.remove(key);

                    // Nothing left to watch
                    if (subDirectories.isEmpty())
                        break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.info("Log thread safely stopped");
            }
        });

    }

    public void start() {

        // Start a new thread to handle watching
        spinUpWatchers();
    }


    public void stop() {

    }
}

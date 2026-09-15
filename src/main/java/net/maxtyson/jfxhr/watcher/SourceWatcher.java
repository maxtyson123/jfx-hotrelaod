package net.maxtyson.jfxhr.watcher;

import javafx.animation.PauseTransition;
import javafx.util.Duration;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static java.nio.file.StandardWatchEventKinds.*;

public class SourceWatcher {


   /// SourceWatcher specific logger
   private static final Logger log = LogManager.getLogger(SourceWatcher.class);

   private Path watchDirectory;
   private Runnable onChangeDebounced;

   private PauseTransition debouncer;
   private ExecutorService watchingThread;


   public SourceWatcher(Path dir, Runnable onChangeDebounced) {

      this.watchDirectory = dir;
      this.onChangeDebounced = onChangeDebounced;

      // Ensure files arent compiled mid write to disk
      debouncer = new PauseTransition(Duration.millis(300));
      debouncer.setOnFinished(e -> onChangeDebounced.run());

   }

   public void start() {

      // Start a new thread to handle watching
      watchingThread = Executors.newSingleThreadExecutor();
      watchingThread.submit(() ->{

            log.info("Started watching '{}' on tid {}", watchDirectory, Thread.currentThread().getName());

            try (WatchService watcher = FileSystems.getDefault().newWatchService()) {
                watchDirectory.register(watcher, ENTRY_CREATE, ENTRY_MODIFY, ENTRY_DELETE);

                while (!Thread.currentThread().isInterrupted()) {

                    // Wait for directory events
                    WatchKey key = watcher.take();
                    for (WatchEvent<?> event : key.pollEvents()) {

                        // Skip malformed event
                        if (event.kind() == OVERFLOW)
                           continue;

                        WatchEvent<Path> fileEvent = (WatchEvent<Path>) event;
                        log.info("Event {} on file {}", fileEvent.kind().name(), fileEvent.context());
                    }

                    // Signal that event has been handled and wait for next if safe to continue polling
                    if (!key.reset())
                       break;
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (InterruptedException e) {
                log.info("Log thread safley stopped");
            }
      });
   }

   public void stop() {

   }
}

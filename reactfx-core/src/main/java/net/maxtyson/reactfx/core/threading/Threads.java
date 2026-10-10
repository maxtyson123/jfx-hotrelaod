package net.maxtyson.reactfx.core.threading;

import javafx.application.Platform;

import java.util.concurrent.CountDownLatch;

/**
 * Handles running functions on the javafx thread
 */
public class Threads {

    /**
     * Check if the current function is running on the java fx thread
     *
     * @return True if on the application thread, false otherwise
     */
    public static boolean isOnFX() {
        return Platform.isFxApplicationThread();
    }

    /**
     * Run a function on the fx thread, will run now if already on the fx thread,
     * otherwise will enqueue to run on the thread at a later time
     *
     * @param function The function to run
     * @see #runOnFX(Runnable, boolean)
     */
    public static void runOnFX(Runnable function) {
        runOnFX(function, false);
    }

    /**
     * Run a function on the fx thread, will run now if already on the fx thread,
     * otherwise will enqueue to run on the thread at a later time. If blocking is
     * true, it will wait until it is complete
     *
     * @param function The function to run
     * @see #runOnFX(Runnable, boolean)
     */
    public static void runOnFX(Runnable function, boolean block) {

        // No need to enqueue
        if(isOnFX()){
            function.run();
            return;
        }

        CountDownLatch latch = new CountDownLatch(1);

        // Enqueue the function
        Platform.runLater(() -> {
            try {
                function.run();
            } finally {
                latch.countDown();
            }
        });

        // Not blocking so don't need to wait
        if(!block)
            return;

        // Wait for the thread mark finish via the countdown
        try {
            latch.await();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
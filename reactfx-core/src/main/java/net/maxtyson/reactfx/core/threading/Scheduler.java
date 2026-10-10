package net.maxtyson.reactfx.core.threading;

import javafx.application.Platform;
import net.maxtyson.reactfx.core.state.Effect;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedDeque;
import java.util.concurrent.atomic.AtomicBoolean;

public class Scheduler {

    /// Scheduler specific logger
    private static final Logger log = LogManager.getLogger(Scheduler.class);

    /// The list of effects that need to run
    private static Deque<Effect> queue = new ConcurrentLinkedDeque<>();

    /// Whether a flush call is currently waiting to be picked up by JavaFX
    private static AtomicBoolean flushScheduled = new AtomicBoolean(false);

    public static void enqueue(Effect effect){

        queue.add(effect);

        // Start a flush batch (note: doesn't matter if already on fx thread here, using javafx's internal queue
        // to batch queue runs to the next frame)
        if(flushScheduled.compareAndSet(false, true))
            Platform.runLater(() -> flush());

    }

    public static void flush(){

        flushScheduled.set(false);

        int iterations = 0;
        while(!queue.isEmpty()){

            // Potential self causing update
            if(iterations > 100){
                handleCycle();
                return;
            }

            processNextBatch();
            iterations++;
        }
    }

    public static void handleCycle(){

        log.warn("Cyclic update detected");

        // Remove from queue
        for(Effect effect : queue)
            effect.clearQueueState();

        queue.clear();
    }

     private static void processNextBatch() {

        List<Effect> batch = new ArrayList<>();

        // Copy the queue into a local copy
        Effect effect;
        while ((effect = queue.poll()) != null)
            batch.add(effect);

        // Run each effect
        for (Effect e : batch)
            e.execute();

    }
}
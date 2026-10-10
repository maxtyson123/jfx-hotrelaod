package net.maxtyson.reactfx.core.state;

import net.maxtyson.reactfx.core.scope.Scope;
import net.maxtyson.reactfx.core.state.dependant.StateDependantFunction;
import net.maxtyson.reactfx.core.threading.Scheduler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A function to run when a state variable changes
 */
public class Effect extends StateDependantFunction {

    /// Effect specific logger
    private static final Logger log = LogManager.getLogger(Effect.class);

    /// The identifier of the effect
    private String name;

    /// The function code to run when a state changes
    private Runnable function;

    /// Whether the function has already been scheduled to run
    private boolean queued = false;

    /// Whether the scope this effect was in has been cleaned up
    private boolean disposed = false;

    /// The scope to use for clean up functions in the effect
    private Scope scope = Scope.root();

    public Effect(String name, Runnable function){
        this.name = name;
        this.function = function;

        // Delete this effect when it goes out of scope
        Scope current = Scope.getCurrentScope();
        if(current == null){
            log.warn("Effect made out side of scope, will leak resources with no clean up");
            return;
        }

        current.registerCleanUpCallback(() -> dispose());

        //  Discover deps
        try{
            scope.run(() -> run(function));
        } catch (Exception e) {
            log.error(name, e);
        }
    }

    /**
     * Run the effect
     */
    public void execute(){

        // Dont run effects that are no longer active
        if(disposed)
            return;

        // Now that this effect has been dispatched, it can be enqueued again whilst this runs async (for example
        // if this changes a state that requires it to be enqueued again)
        queued = false;

        // Run any clean up for the old effect
        scope.dispose();
        scope = Scope.root();

        // Run the effect, catching any clean-up triggers
        try{
            scope.run(() -> run(function));
        } catch (Exception e) {
            log.error(name, e);
        }

    }

    @Override
    protected void makeDirty() {

        // Dont run effects that are no longer active
        if(disposed)
            return;

        // Effect doesn't needed to be re-enqueued if it is already queued as when it runs it will fetch the latest data
        if(queued)
            return;

        queued = true;
        Scheduler.enqueue(this);
    }

    public void dispose(){

        disposed = true;
        scope.dispose();
        clean();

    }

    public void clearQueueState() {
        queued = false;
    }
}

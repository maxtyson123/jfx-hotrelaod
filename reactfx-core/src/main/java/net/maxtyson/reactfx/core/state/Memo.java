package net.maxtyson.reactfx.core.state;

import net.maxtyson.reactfx.core.scope.Scope;
import net.maxtyson.reactfx.core.state.dependant.StateDependantFunction;
import net.maxtyson.reactfx.core.state.dependant.Subscribers;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Deque;
import java.util.function.Supplier;

/**
 * A cached calculated function that updates when its dependent state values change
 *
 * @param <T> The type of value the function calculates
 */
public class Memo<T> extends State<T> {

    private static final Logger log = LogManager.getLogger(Memo.class);

    /// Whether the memo function is in the middle of running
    private boolean running = false;

    /// Whether the cached memo result is up to date
    private boolean dirty = true;

    /// The function to calculate the memo value
    private Supplier<T> function;

    /// Whether the scope this effect was in has been cleaned up
    private boolean disposed = false;

    /// The runner to track dependencies as the memo is calculated
    private StateDependantFunction runner = new StateDependantFunction() {
        @Override
        protected void makeDirty() {
            markDirty();
        }
    };

    /**
     * Construct a new Memo
     *
     * @param function The function to run to calculate the value
     */
    public Memo(Supplier<T> function){
        super(null);

        this.function = function;

        // Delete this memo when it goes out of scope
        Scope current = Scope.getCurrentScope();
        if(current == null){
            log.warn("Memo made out side of scope, will leak resources with no clean up");
            return;
        }

        current.registerCleanUpCallback(() -> dispose());
    }

    /**
     * Get the result of the function
     *
     * @return The result of the function
     */
    @Override
    public T get() {

        if(disposed)
            throw new IllegalStateException("Cannot get from a disposed memo");

        // Store that the caller just became a dependacny
        subscribers.track();

        return run(true);
    }

     /**
     * Get the result of the function (without tracking dependencies)
     *
     * @return The result of the function
     */
    @Override
    public T peek() {

        if(disposed)
            throw new IllegalStateException("Cannot peek at a disposed memo");

        return run(false);
    }

    private T run(boolean tracked){

        // Cached value is up to date so don't need to re run
        if(!dirty)
            return value;

        // If already running then that means it is caling itself
        if(running)
            throw new IllegalStateException("Circular dependency in memo function");

        try{
            running = true;
            value = tracked ? runner.run(function) : runner.runUntracked(function);
        } finally {
            running = false;
        }

         // Untracked cant mark as not dirty as doesn't track dependency
        if(tracked)
            dirty = false;

        return value;

    }

    public void markDirty(){

        // Nothing to do
        if(dirty || disposed)
            return;

        dirty = true;
        subscribers.notifyDirty();
        fireValueChangedEvent();

    }

    @Override
    public void set(T newValue) {
        throw new UnsupportedOperationException("Cannot call set() on a memo");
    }

    public void dispose(){

        disposed = true;

        // Remove state listenter
        runner.clean();

    }
}

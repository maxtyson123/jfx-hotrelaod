package net.maxtyson.reactfx.core.state;

import net.maxtyson.reactfx.core.state.dependant.StateDependantFunction;
import net.maxtyson.reactfx.core.state.dependant.Subscribers;

import java.util.function.Supplier;

/**
 * A cached calculated function that updates when its dependent state values change
 *
 * @param <T> The type of value the function calculates
 */
public class Memo<T> extends State<T> {

    /// Whether the memo function is in the middle of running
    private boolean running = false;

    /// Whether the cached memo result is up to date
    private boolean dirty = true;

    /// The function to calculate the memo value
    private Supplier<T> function;

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
    }

    /**
     * Get the result of the function
     *
     * @return The result of the function
     */
    @Override
    public T get() {
        return run(true);
    }

     /**
     * Get the result of the function (without tracking dependencies)
     *
     * @return The result of the function
     */
    @Override
    public T peek() {
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
            dirty = false;
        }

        return value;

    }

    public void markDirty(){

        // Nothing to do
        if(dirty)
            return;

        dirty = true;
        subscribers.notifyDirty();
        fireValueChangedEvent();

    }

    @Override
    public void set(T newValue) {
        throw new UnsupportedOperationException("Cannot call set() on a memo");
    }
}

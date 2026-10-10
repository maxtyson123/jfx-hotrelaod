package net.maxtyson.reactfx.core.state.dependant;

import net.maxtyson.reactfx.core.scope.Scope;
import net.maxtyson.reactfx.core.state.State;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * An executable function that needs to be re-ran each time a specific state updates
 */
public abstract class StateDependantFunction {

    /// The current function that is executing or null if not inside one
    private static StateDependantFunction current;

    /// The list of state variables the function is currently a subscriber of
    private final Set<Subscribers> sources = new HashSet<>();

    /// Mark this function as needing to be re ran
    protected abstract void makeDirty();

    /**
     * Register that this become a subscriber of a state variable
     *
     * @param subscriberOf The state this is now subscribed to
     */
    public void registerSource(Subscribers subscriberOf){
        sources.add(subscriberOf);
    }

    /**
     * Unsubscribe from everything this is currently subscribed to
     */
    public void clean(){

        for(Subscribers subscriberOf : sources)
            subscriberOf.remove(this);

        sources.clear();
    }

    /**
     * Run the function
     *
     * @param function The function to run
     */
    public void run(Runnable function){
        handleRun(function, false);
    }


    /**
     * Run the function
     *
     * @param function The function to run
     */
    public <T> T run(Supplier<T> function){
        return handleRun(function, true);
    }

    /**
     * Run the function (without tracking state)
     *
     * @param function The function to run
     */
    public void runUntracked(Runnable function){
        handleRun(function, false);
    }

     /**
     * Run the function (without tracking state)
     *
     * @param function The function to run
     */
    public <T> T runUntracked(Supplier<T> function){
        return handleRun(function, false);
    }

    private void handleRun(Runnable function, boolean tracked){

        // Remove stale subscriptions (ie x.get() might be in a condition that isnt true any more so dont leak x)
        if(tracked)
            clean();

        // This function is now running
        StateDependantFunction previous = current;
        current = tracked ? this : null;

        // Run the function
        try {
            function.run();
        } finally {
            current = previous;
        }
    }

    private <T> T handleRun(Supplier<T> function, boolean tracked){

        // Remove stale subscriptions (ie x.get() might be in a condition that isnt true any more so dont leak x)
        if(tracked)
            clean();

        // This function is now running
        StateDependantFunction previous = current;
        current = tracked ? this : null;

        // Run the function
        T value;
        try {
            value = function.get();
        } finally {
            current = previous;
        }

        return value;
    }



    /**
     * Get the current executing function if it exists
     *
     * @return The current executing function or null if none
     */
    public static StateDependantFunction getCurrent(){
        return current;
    }
}

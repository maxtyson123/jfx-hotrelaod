package net.maxtyson.reactfx.core.state.dependant;

import java.util.ArrayList;
import java.util.List;

/**
 * Handles a list of dependencies on a state
 */
public class Subscribers {

    /// The list of functions dependent on this state
    private List<StateDependantFunction> dependants = new ArrayList<>();

    /**
     * Register an ew dependency
     *
     * @param subscriber The new dependency
     */
    public void add(StateDependantFunction subscriber){
        dependants.add(subscriber);

    }

    /**
     * Remove a registered dependency
     *
     * @param subscriber The dependency to remove
     */
    public void remove(StateDependantFunction subscriber){
        dependants.remove(subscriber);

    }

    /**
     * Mark all subscribers as needing to be reran
     */
    public void notifyDirty(){

        for(StateDependantFunction dependantFunction : dependants)
            dependantFunction.makeDirty();

    }

    /**
     * Add the current execution state as a dependency
     */
     public void track(){

        // Nothing to track
        StateDependantFunction currentExecution = StateDependantFunction.getCurrent();
        if(currentExecution == null)
            return;

        add(currentExecution);
        currentExecution.registerSource(this);
    }
}

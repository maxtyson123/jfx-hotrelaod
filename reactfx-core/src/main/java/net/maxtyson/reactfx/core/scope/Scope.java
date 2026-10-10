package net.maxtyson.reactfx.core.scope;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.function.Function;

/**
 * Handles the lifetime and ownership of state nodes
 */
public class Scope {

    /// Scope specific logger
    private static final Logger log = LogManager.getLogger(Scope.class);

    /// A list of handlers to run when this state is disposed
    private Stack<Runnable> cleanUpCallbacks = new Stack<>();

    /// A list of sub-scopes that was created in this scope
    private List<Scope> children = new ArrayList<>();

    /// The scope that this subscope was created in
    private Scope parent;

    /// Has this scope already done its clean up
    private boolean isDisposedOf = false;

    /// The currently executing active scope in the scope call stack (ie rsp)
    private static Scope current;

    /**
     * Create a new child scope
     *
     * @param parent The scope that contains this scope
     */
    public Scope(Scope parent){
        this.parent = parent;
    }

    /**
     * Create a new scope tree with no parent
     *
     * @return The new scope
     */
    public static Scope root(){
        return new Scope(null);
    }


    /**
     * Create a new scope that is part of this scope
     *
     * @return The new child scope
     */
    public Scope child(){

        if (isDisposedOf)
            throw new IllegalStateException("Cannot create child scope from a disposed parent scope");


        Scope newChild = new Scope(this);
        children.add(newChild);

        return newChild;

    }

    /**
     * Run a function within this scope
     *
     * @param function The function to run
     */
    public void run(Runnable function){

        if (isDisposedOf)
            throw new IllegalStateException("Cannot run within a disposed scope");

        // The function now runs within this scope
        Scope previous = current;
        current = this;

        try {
            function.run();
        } finally {
            current = previous;
        }
    }

    /**
     * Clean up the current scope, disposing of children then running registered call backs
     */
    public void dispose(){

        // Already disposed of
        if(isDisposedOf)
            return;

        isDisposedOf = true;

        // Clean up children
        List<Scope> childrenToDispose = new ArrayList<>(children);
        for(Scope child : childrenToDispose)
            child.dispose();

        // Run handlers (reverse so that things above each other arent cleaned up before things below
        // them (that might still refernce them) get a chance to)
        while (!cleanUpCallbacks.isEmpty()) {
            Runnable handler = cleanUpCallbacks.pop();
            try {
                handler.run();
            } catch (Exception e) {
                 log.error("Error executing cleanup action during scope disposal", e);
            }
        }

        if(parent != null)
            parent.unRegisterAsChild(this);

        children.clear();
    }

    /**
     * Remove a child from this scope
     *
     * @param scope The scope to remove
     */
    private void unRegisterAsChild(Scope scope) {

        children.remove(scope);
    }

    /**
     * Get the current execution scope
     *
     * @return The scope that this is currently executing in
     */
    public static Scope getCurrentScope() {
        return current;
    }

    /**
     * Register a new clean up handler to run within this scope
     *
     * @param function The handler to run on disposal
     */
    public void registerCleanUpCallback(Runnable function){
        cleanUpCallbacks.add(function);
    }
}

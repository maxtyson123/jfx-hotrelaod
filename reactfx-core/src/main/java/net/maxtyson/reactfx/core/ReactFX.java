package net.maxtyson.reactfx.core;

import net.maxtyson.reactfx.core.scope.Scope;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import static net.maxtyson.reactfx.core.scope.Scope.getCurrentScope;

/**
 * Facade to abastract API
 */
public class ReactFX {


    /// Scope specific logger
    private static final Logger log = LogManager.getLogger(ReactFX.class);


    /**
     * Register a new clean up task for the current scope
     *
     * @param function The function to run when this scope get disposed of
     */
    public static void onCleanup(Runnable function) {

        // No scope
        Scope currentScope = getCurrentScope();
        if (currentScope == null) {
            log.warn("onCleanup called outside of an active Scope (ignoring)");
            return;
        }

        currentScope.registerCleanUpCallback(function);
    }
}

package net.maxtyson.reactfx.core;

import net.maxtyson.reactfx.core.scope.Scope;
import net.maxtyson.reactfx.core.state.Memo;
import net.maxtyson.reactfx.core.state.State;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.function.Supplier;

import static net.maxtyson.reactfx.core.scope.Scope.getCurrentScope;

/**
 * Facade to abstract API
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

    /**
     * Create a new state tracked variable
     *
     * @param value The value of the variable
     * @return The new state
     * @param <T> The type of the variable
     */
    public static <T> State<T> useState(T value){
        return State.of(value);
    }

    /**
     * Creat a new state tracked function
     *
     * @param function The function to run
     * @return The new state
     * @param <T> The type of the variable
     */
    public static <T> Memo<T> useMemo(Supplier<T> function){
        return new Memo<>(function);
    }
}

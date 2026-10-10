package net.maxtyson.reactfx.core.state;

import javafx.beans.value.ObservableValueBase;
import net.maxtyson.reactfx.core.ReactFX;
import net.maxtyson.reactfx.core.threading.Threads;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Objects;
import java.util.function.Function;
import java.util.function.UnaryOperator;

import static net.maxtyson.reactfx.core.threading.Threads.isOnFX;
import static net.maxtyson.reactfx.core.threading.Threads.runOnFX;

/**
 * Container for a value that notifies on change
 *
 * @param <T> The type of value
 */
public class State<T> extends ObservableValueBase<T> {

    /// State specific logger
    private static final Logger log = LogManager.getLogger(State.class);

    /// The current value of the variable this state holds
    volatile T value;

    /**
     * Setup a new state
     *
     * @param value The initial value of the state
     */
    public State(T value){
        this.value = value;
    }

    /**
     * Get the current value and subscribe to changes
     *
     * @return The value
     */
    public T get() {
        return value;
    }

    /**
     * Get the current value but don't subscribe to changes
     *
     * @return The value
     */
    public T peek() {
        return value;
    }

    /**
     * Get the current value and subscribe to changes
     *
     * @return The value
     */
    @Override
    public T getValue() {
        return get();
    }

    /**
     * Update the value in this state if different
     *
     * @param newValue The new value
     */
    public void set(T newValue){

        // Make sure runs on application thread
        if(!isOnFX()){
            runOnFX(() -> set(newValue));
            return;
        }

        // Don't waste a rerender of the same data
        if(Objects.equals(value, newValue))
            return;

        // Rerender with new values
        value = newValue;
        fireValueChangedEvent();
    }

    public void update(Function<T, T> function){

        // Make sure runs on application thread
        if(!isOnFX()){
            runOnFX(() -> update(function));
            return;
        }

        try{

            // Run the update and store the result
            T newValue = function.apply(value);
            set(newValue);


        } catch (Exception e) {
            log.warn("Failed to update state", e);
        }
    }

    /**
     * Create a new state with a given value
     *
     * @param value The state to create
     * @return The created state
     * @param <T> The state value
     */
    public static <T> State<T> of(T value){
        return new State<T>(value);
    }
}

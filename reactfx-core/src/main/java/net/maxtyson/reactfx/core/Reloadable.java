package net.maxtyson.reactfx.core;

/**
 * An element that can be reloaded and receive reload events
 */
public interface Reloadable {
    default void beforeReload() {}
    default void afterReload() {}

    default void onUnload() {}
}

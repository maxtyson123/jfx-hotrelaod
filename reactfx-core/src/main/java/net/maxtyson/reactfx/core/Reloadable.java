package net.maxtyson.reactfx.core;

public interface Reloadable {
    default void beforeReload() {}
    default void afterReload() {}

    default void onUnload() {}
}

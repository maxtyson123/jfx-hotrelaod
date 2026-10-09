package net.maxtyson.reactfx;

public interface Reloadable {
    default void beforeReload() {}
    default void afterReload() {}

    default void onUnload() {}
}

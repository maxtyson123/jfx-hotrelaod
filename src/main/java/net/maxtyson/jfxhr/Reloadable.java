package net.maxtyson.jfxhr;

public interface Reloadable {
    default void beforeReload() {}
    default void afterReload() {}

    default void onUnload() {}
}

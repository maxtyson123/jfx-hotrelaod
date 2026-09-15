package net.maxtyson.jfxhr;

import javafx.scene.layout.Pane;

public class HotReloader {

    private final ReloaderConfig config;
    private final Pane mountPoint;

    public static HotReloader attach(ReloaderConfig config, Pane mountPoint) {
        // construct

        // first load

        // setup watch
    }

    private void onSourceChanged() {
        // compile
        // load
        // reflective newInstance
        // UiSwapper.swap

        // on compile failure log
    }

}

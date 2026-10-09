package net.maxtyson.reactfx.demo;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import net.maxtyson.reactfx.demo.Log;
import net.maxtyson.reactfx.demo.UI;
import net.maxtyson.reactfx.core.Threads;

public class DemoScene extends VBox {
    public DemoScene() {
        super(10);
        setPadding(new Insets(12));

        Log log = new Log();
        log.log("test");

        Label created = new Label("constructed on FX thread: (v3) " + Platform.isFxApplicationThread());
        Node hotreloader = UI.section("Hotloader reloading from thread:", created);

        Node b1 = UI.button("from a background thread", () -> new Thread(() ->
                Threads.runOnFX(() -> log.log("ran, on FX thread = " + Platform.isFxApplicationThread()))
        ).start());

        Node b2 = UI.button("from the FX thread", () -> {
            log.log("before");
            Threads.runOnFX(() -> log.log("inside"));
            log.log("after");
        });

        Node buttons = UI.row(b1, b2);

        Node runOnFXTest = UI.section("runOnFX tests", buttons);


        getChildren().addAll(hotreloader, runOnFXTest, log);
    }
}
package net.maxtyson.reactfx.demo;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import net.maxtyson.reactfx.demo.Log;
import net.maxtyson.reactfx.demo.UI;
import net.maxtyson.reactfx.core.threading.Threads;
import net.maxtyson.reactfx.core.scope.Scope;
import static net.maxtyson.reactfx.core.ReactFX.onCleanup;

public class DemoScene extends VBox {
    public DemoScene() {
        super(10);
        setPadding(new Insets(12));

        Log log = new Log();
        log.log("test");

        Scope[] parent = {null};
        Scope[] child = {null};


        Node createBtn = UI.button("create parent and child", () -> {

            if (parent[0] != null) {
                log.log("need reset");
                return;
            }

            Scope p = Scope.root();
            parent[0] = p;
            p.run(() -> {
                onCleanup(() -> log.log("cleanup P1"));
                child[0] = p.child();
                child[0].run(() -> onCleanup(() -> log.log("cleanup C1")));
                onCleanup(() -> log.log("cleanup P2"));
            });
            log.log("created");
        });

        Node disposeParentBtn = UI.button("dispose parent", () -> {
            if (parent[0] != null)
                parent[0].dispose();
        });

        Node disposeChildBtn = UI.button("dispose child", () -> {
            if (child[0] != null)
                child[0].dispose();
        });

        Node resetBtn = UI.button("reset", () -> {
            parent[0] = null;
            child[0] = null;
            log.log("reset");
        });

        Node disposalRow = UI.row(createBtn, disposeParentBtn, disposeChildBtn, resetBtn);
        Node errorTest = UI.section("Disposal", disposalRow);

        Node errorBtn = UI.button("run() in a disposed scope", () -> {
            Scope s = Scope.root();
            s.dispose();
            try {
                s.run(() -> {
                });
            } catch (IllegalStateException ex) {
                log.log(ex.getMessage());
            }
        });

        Node disposaltest = UI.section("Errors", errorBtn);

        getChildren().addAll(disposaltest, errorTest, log);
    }
}
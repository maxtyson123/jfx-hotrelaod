package net.maxtyson.reactfx.demo;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.Button;
import javafx.scene.Node;
import javafx.scene.layout.VBox;
import net.maxtyson.reactfx.demo.Log;
import net.maxtyson.reactfx.demo.UI;
import net.maxtyson.reactfx.core.threading.Threads;
import net.maxtyson.reactfx.core.scope.Scope;
import net.maxtyson.reactfx.core.state.State;
import net.maxtyson.reactfx.core.state.Effect;
import net.maxtyson.reactfx.core.state.State;

import static net.maxtyson.reactfx.core.ReactFX.*;

public class DemoScene extends VBox {
    public DemoScene() {
        super(10);
        setPadding(new Insets(12));
        Log log = new Log();
        Scope scope = Scope.root();
        State<Integer> count = useState(0);

        scope.run(() -> {
            useEffect("A", () -> {
                log.log("A ran, count=" + count.get());
                onCleanup(() -> log.log("cleanup A"));
            });
        });

        getChildren().addAll(
                UI.row(
                        UI.button("count + 1", () -> count.update(c -> c + 1)),
                        UI.button("dispose scope", () -> {
                            scope.dispose();
                            log.log("scope disposed");
                        })),
                log);
    }
}
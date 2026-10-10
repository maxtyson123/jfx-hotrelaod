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

        State<Integer> count = useState(0);
        State<Integer> other = useState(0);
        State<Integer> doubled = useMemo(() -> count.get() * 2);

        useEffect("first", () -> log.log("useEffect[first] count=" + count.get() + " doubled=" + doubled.get()));
        useEffect("second", () -> log.log("useEffect[second] count=" + count.get()));
        useEffect("both", () -> log.log("useEffect[both] count=" + count.get() + " other=" + other.get()));
        useEffect("cleanup", () -> {
            int c = count.get();
            onCleanup(() -> log.log(" cleanup for c=" + c));
            log.log("useEffect[cleanup] body c=" + c);
        });

        Effect[] manual = {null};

        Node driveRow = UI.row(
            UI.button("count + 1", () -> count.update(c -> c + 1)),
            UI.button("other + 1", () -> other.update(c -> c + 1)),
            UI.button("count AND other in one click", () -> {
                count.update(c -> c + 1);
                other.update(c -> c + 1);
            })
        );
        Node sectionDrive = UI.section("Drive them", driveRow);

        Node disposeRow = UI.row(
            UI.button("create manual useEffect", () -> {
                if (manual[0] != null) return;
                manual[0] = useEffect("manual", () -> log.log("useEffect[manual] count=" + count.get()));
            }),
            UI.button("dispose manual useEffect", () -> {
                if (manual[0] != null) {
                    manual[0].dispose();
                    manual[0] = null;
                    log.log("manual disposed");
                }
            })
        );
        Node sectionDispose = UI.section("Dispose", disposeRow);

        getChildren().addAll(
            sectionDrive,
            sectionDispose,
            log
        );
    }
}
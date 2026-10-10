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
import net.maxtyson.reactfx.core.state.State;

import static net.maxtyson.reactfx.core.ReactFX.*;

public class DemoScene extends VBox {
    public DemoScene() {
        super(10);
        setPadding(new Insets(12));

        Log log = new Log();

        State<Integer> a = useState(1);
        State<Integer> b = useState(2);
        Label aLabel = new Label();
        aLabel.textProperty().bind(a.map(x -> "a = " + x));
        Label bLabel = new Label();
        bLabel.textProperty().bind(b.map(x -> "b = " + x));

        int[] sumRuns = {0};
        State<Integer> sum = useMemo(() -> {
            sumRuns[0]++;
            return a.get() + b.get();
        });

        Node inputsRow = UI.row(aLabel, bLabel);
        Node inputsBtnRow = UI.row(
            UI.button("a + 1", () -> a.update(x -> x + 1)),
            UI.button("b + 1", () -> b.update(x -> x + 1))
        );
        Node sectionInputs = UI.section("Inputs", inputsRow, inputsBtnRow);

        Node sumBtn = UI.button("read sum", () -> log.log("sum = " + sum.get() + "   ran " + sumRuns[0] + "x"));
        Node sectionSum = UI.section("lazy + cached", sumBtn);

        getChildren().addAll(sectionInputs, sectionSum, log);
    }
}
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

import static net.maxtyson.reactfx.core.ReactFX.*;

public class DemoScene extends VBox {
    public DemoScene() {
        super(10);
        setPadding(new Insets(12));

        Log log = new Log();
        log.log("test");

        State<Integer> count = useState(0);
        Label countLabel = new Label();
        countLabel.textProperty().bind(count.map(c -> "count = " + c));
        count.addListener((obs, was, now) -> log.log("change listener: " + was + " -> " + now));

        Button countPlusBtn = UI.button("count + 1", () -> count.update(c -> c + 1));
        Button set100Btn = UI.button("set to 100", () -> count.set(100));
        Node section1Row = UI.row(countPlusBtn, set100Btn);
        Node section1 = UI.section("updates", countLabel, section1Row);

        State<Integer> tick = useState(0);
        Label tickLabel = new Label();
        tickLabel.textProperty().bind(tick.map(t -> "tick = " + t));
        Thread[] ticker = {null};

        Button startTickerBtn = UI.button("start", () -> {
            if (ticker[0] != null) return;
            Thread t = new Thread(() -> {
                try {
                    while (!Thread.currentThread().isInterrupted()) {
                        Thread.sleep(400);
                        tick.update(x -> x + 1);
                    }
                } catch (InterruptedException ignored) {
                }
            }, "demo-ticker");
            t.setDaemon(true);
            ticker[0] = t;
            t.start();
        });

        Button stopTickerBtn = UI.button("stop", () -> {
            if (ticker[0] != null) {
                ticker[0].interrupt();
                ticker[0] = null;
            }
        });
        Node section2Row = UI.row(startTickerBtn, stopTickerBtn);
        Node section2 = UI.section("works from bg", tickLabel, section2Row);

        State<Integer> race = useState(0);
        Button raceBtn = UI.button("2 threads x 1000 updates", () -> {
            race.set(0);
            Runnable work = () -> {
                for (int i = 0; i < 1000; i++) {
                    race.update(x -> x + 1);
                }
            };
            Thread t1 = new Thread(work), t2 = new Thread(work);
            new Thread(() -> {
                try {
                    t1.start();
                    t2.start();
                    t1.join();
                    t2.join();
                } catch (InterruptedException ignored) {
                }
                Platform.runLater(() -> log.log("race finished: " + race.peek() + "=2000"));
            }).start();
        });
        Node section3 = UI.section("update is atomic", raceBtn);
        getChildren().addAll(section1, section2, section3, log);
    }
}
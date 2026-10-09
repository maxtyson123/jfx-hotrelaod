package net.maxtyson.reactfx.demo;

import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

public final class UI {

    public static Button button(String text, Runnable handler) {
        Button b = new Button(text);
        b.setOnAction(e -> handler.run());
        return b;
    }

    public static HBox row(Node... nodes) { return new HBox(8, nodes); }

    public static VBox section(String title, Node... nodes) {
        Label heading = new Label(title);
        heading.setStyle("-fx-font-weight: bold;");
        VBox box = new VBox(4, heading);
        box.getChildren().addAll(nodes);
        return box;
    }
}
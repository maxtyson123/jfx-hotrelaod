package net.maxtyson.jfxhr.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;

public class IconButton extends StackPane {
    private int clicks = 0;
    private final Label label = new Label();

    public IconButton(String name) {
        updateLabel(name);
        setOnMouseClicked(e -> { clicks++; updateLabel(name); });
    }

    private void updateLabel(String name) {
        label.setText(name + ": " + clicks);
        getChildren().setAll(label);
    }

}
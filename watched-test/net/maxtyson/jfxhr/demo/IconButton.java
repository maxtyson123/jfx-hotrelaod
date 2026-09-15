package net.maxtyson.jfxhr.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.geometry.Insets;
import net.maxtyson.jfxhr.Reloadable;

public class IconButton extends StackPane implements Reloadable {
        private int clicks = 0;
        private final Label label = new Label();
        private String name;

    public IconButton(String name) {
        this.name = name;

        updateLabel(name);
        setOnMouseClicked(e -> { clicks++; updateLabel(name); });
    }

    private void updateLabel(String name) {
        label.setText(name + " - " + clicks);
        getChildren().setAll(label);
    }

    @Override
    public void afterReload() {
        updateLabel(name);
    }
}
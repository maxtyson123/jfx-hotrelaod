package net.maxtyson.reactfx.demo;

import javafx.scene.layout.HBox;
import net.maxtyson.reactfx.demo.IconButton;

public class ToolBar extends HBox {
    public ToolBar() {
        super(50);
        getChildren().addAll(
            new IconButton("A"),
            new IconButton("B"),
            new IconButton("v24")
        );
    }
}
package net.maxtyson.reactfx.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.maxtyson.reactfx.demo.ToolBar;


public class DemoScene extends VBox {
    public DemoScene() {
        getChildren().addAll(new Label("version 40"), new ToolBar());
    }
}
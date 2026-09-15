package net.maxtyson.jfxhr.demo;

import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import net.maxtyson.jfxhr.demo.ToolBar;


public class DemoScene extends VBox {
    public DemoScene() {
        getChildren().addAll(new Label("version 32"), new ToolBar());
    }
}
package net.maxtyson.reactfx.core.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.maxtyson.reactfx.hotreload.HotReloader;
import net.maxtyson.reactfx.hotreload.ReloaderConfig;

import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        ReloaderConfig config = new ReloaderConfig(
            stage,
            Path.of("watched"),
            Path.of("target/classes-jfxhr")
        );

        StackPane root = new StackPane();
        stage.setScene(new Scene(root, 400, 300));
        stage.show();

        HotReloader reloader = HotReloader.attach(config, root);
        reloader.mountRoot("net.maxtyson.reactfx.demo.DemoScene");
    }

    public static void main(String[] args) {
        launch(args);
    }

}
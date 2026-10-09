package net.maxtyson.reactfx.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.maxtyson.reactfx.HotReloader;
import net.maxtyson.reactfx.ReloaderConfig;

import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        ReloaderConfig config = new ReloaderConfig(
            stage,
            Path.of("watched-test"),
            Path.of("target/classes-jfxhr")
        );

        StackPane root = new StackPane();
        stage.setScene(new Scene(root, 400, 300));
        stage.show();

        HotReloader reloader = HotReloader.attach(config, root);
        reloader.loadDemo();
    }

    public static void main(String[] args) {
        launch(args);
    }

}
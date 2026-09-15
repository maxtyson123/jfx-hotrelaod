package net.maxtyson.jfxhr.demo;

import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;
import net.maxtyson.jfxhr.HotReloader;
import net.maxtyson.jfxhr.ReloaderConfig;

import java.nio.file.Path;

public class App extends Application {

    @Override
    public void start(Stage stage) {

        ReloaderConfig config = new ReloaderConfig(
            stage,
            Path.of("watched-test"),
            Path.of("target/classes-jfxhr"),

            "net.maxtyson.jfxhr.demo.DemoButton",
            new Class<?>[]{},
            new Object[]{}
        );


        StackPane root = new StackPane();
        stage.setScene(new Scene(root, 400, 300));
        stage.show();

        HotReloader.attach(config, root);
    }

    public static void main(String[] args) {
        launch(args);
    }

}
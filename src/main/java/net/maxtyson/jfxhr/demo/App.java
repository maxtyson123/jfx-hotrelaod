package net.maxtyson.jfxhr.demo;

import javafx.application.Application;
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

            "net.maxtyson.jfxhr.demo.DemoView",
            new Class<?>[]{},
            new Object[]{}
        );


        HotReloader.attach(config, pane);
    }

}
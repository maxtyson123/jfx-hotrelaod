package net.maxtyson.reactfx;

import javafx.stage.Stage;

import java.nio.file.Path;

public record ReloaderConfig(
    Stage stage,
    Path watchDir,
    Path binDir
) {}
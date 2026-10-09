package net.maxtyson.reactfx.hotreload;

import javafx.stage.Stage;

import java.nio.file.Path;

public record ReloaderConfig(
    Stage stage,
    Path watchDir,
    Path binDir
) {}
package net.maxtyson.jfxhr;

import javafx.stage.Stage;

import java.nio.file.Path;

public record ReloaderConfig(
    Stage stage,
    Path watchDir,
    Path binDir,

    String rootClassName,
    Class<?>[] argTypes,
    Object[] argValues
) {}
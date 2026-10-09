package net.maxtyson.reactfx.hotreload;

import javafx.stage.Stage;

import java.nio.file.Path;

/**
 * Represents information about what the HotReloader needs to know to run
 *
 * @param stage The UI to hotswap into
 * @param watchDir The source code directory
 * @param binDir The compiled code output directory
 */
public record ReloaderConfig(
    Stage stage,
    Path watchDir,
    Path binDir
) {}
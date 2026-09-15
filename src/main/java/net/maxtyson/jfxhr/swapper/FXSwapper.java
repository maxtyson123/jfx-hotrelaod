package net.maxtyson.jfxhr.swapper;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

public class FXSwapper {

  public static void swap(Pane mountPoint, Node newNode) {
        Platform.runLater(() -> mountPoint.getChildren().setAll(newNode));
  }
}

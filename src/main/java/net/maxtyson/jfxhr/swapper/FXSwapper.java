package net.maxtyson.jfxhr.swapper;

import javafx.application.Platform;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.loader.ClassLoader;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class FXSwapper {

    private final Pane root;

    public FXSwapper(Pane root){
        this.root = root;
    }

    public void swapAll(String fqn, Class<?> sourceClass) throws NoSuchFieldException, InvocationTargetException, NoSuchMethodException, InstantiationException, IllegalAccessException {

        // Get all the old instances
        List<Node> references = findInstances(root, fqn);

        // Replace each reference with a new one
        for(Node old : references)
            swap(old, (Node)ClassLoader.replaceInstance(old, sourceClass));

    }

    public void swap(Node oldInstance, Node newInstance) {

        // Get where in the old instance's parent it was located
        Pane parent = (Pane) oldInstance.getParent();
        int index = parent.getChildren().indexOf(oldInstance);

        // Send updation on the render thread
        Platform.runLater(() -> parent.getChildren().set(index, (Node) newInstance));
    }

    private static List<Node> findInstances(Parent root, String targetClassName) {
        List<Node> found = new ArrayList<>();
        for (Node child : root.getChildrenUnmodifiable()) {

            // Store every refernce to the class getting reloaded
            if (child.getClass().getName().equals(targetClassName))
                found.add(child);

            // Add any references found in subcomponents
            if (child instanceof Parent p)
                found.addAll(findInstances(p, targetClassName));
        }
        return found;
    }

     public void setInRoot(Node newNode) {
        Platform.runLater(() -> root.getChildren().setAll(newNode));
  }

}

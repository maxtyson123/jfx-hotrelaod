package net.maxtyson.jfxhr.swapper;

import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.layout.Pane;
import net.maxtyson.jfxhr.Reloadable;
import net.maxtyson.jfxhr.loader.ClassLoader;
import net.maxtyson.jfxhr.watcher.SourceWatcher;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import static net.maxtyson.jfxhr.loader.ClassLoader.replaceInstance;

public class FXSwapper {

    private final Pane root;
    private final SourceWatcher watcher;

    public FXSwapper(Pane root, SourceWatcher watcher) {
        this.root = root;
        this.watcher = watcher;

    }

    public void swapAll(String fqn, Class<?> sourceClass) throws Exception {

        // Get all the old instances
        List<Node> references = findInstances(root, fqn);

        // Replace each reference with a new one
        for (Node old : references)
            swap(old, (Node) replaceInstance(old, sourceClass));

    }

    private void moveChildren(Node oldInstance, Node newInstance) throws Exception {

        // Let the instance perform any local cleanups
        if (oldInstance instanceof Reloadable)
            ((Reloadable)oldInstance).onUnload();

        // Wasn't a parent
        if (!(newInstance instanceof Parent newParent) || !(oldInstance instanceof Parent oldParent))
            return;

        ObservableList<Node> newChildrenList = (ObservableList<Node>) getChildrenReflectively(newParent);
        List<Node> newChildren = new ArrayList<>(newChildrenList);
        List<Node> oldChildren = new ArrayList<>(getChildrenReflectively(oldParent));

        // Copy each child
        for (int i = 0; i < newChildren.size() && i < oldChildren.size(); i++) {
            Node oldChild = oldChildren.get(i);
            Node newChild = newChildren.get(i);

            // Child order or count or type must have changed so state is broken
            if (!oldChild.getClass().getName().equals(newChild.getClass().getName()))
                continue;

            // JavaFX nodes can be reconstructed as they dont contain application state
            if(!watcher.getWatchedClasses().contains(newChild.getClass().getPackageName()))
                continue;

             // Create a new clone of the old child to add to the new parent
             Object merged = replaceInstance(oldChild, newChild.getClass());
             newChildren.set(i, (Node)merged);

             // Ensure subchildren are also copied onto the new node
            moveChildren(oldChild, (Node)merged);
        }

        newChildrenList.setAll(newChildren);
    }

    public void swap(Node oldInstance, Node newInstance) throws Exception {

        // Get where in the old instance's parent it was located
        Pane parent = (Pane) oldInstance.getParent();
        int index = parent.getChildren().indexOf(oldInstance);

        // Parent's children nodes need to be copied
        moveChildren(oldInstance, newInstance);

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

    private static List<Node> getChildrenReflectively(Parent parent) throws Exception {

        // Force access to getChildren
        Method m = Parent.class.getDeclaredMethod("getChildren");
        m.setAccessible(true);

        // Can now add to parent without jc throwing a fit
        ObservableList<Node> children = (ObservableList<Node>) m.invoke(parent);
        return children;
    }
}

package be.kdg.talenten.view;

import javafx.scene.Parent;
import javafx.scene.Scene;

public class SceneManager {
    private final Scene scene;

    public SceneManager(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("Scene mag niet null zijn");
        this.scene = scene;
    }

    public void toon(Parent view) {
        if (view == null) throw new IllegalArgumentException("View mag niet null zijn");
        scene.setRoot(view);
    }
}

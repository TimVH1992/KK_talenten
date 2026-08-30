package be.kdg.talenten.view.theme;

import javafx.scene.Scene;

import java.net.URL;
import java.util.prefs.Preferences;

public class ThemeManager {
    public enum Theme { LIGHT, DARK }

    private static final String PREF_KEY = "theme";
    private final Preferences preferences = Preferences.userNodeForPackage(ThemeManager.class);
    private Theme theme;

    public ThemeManager() {
        String opgeslagen = preferences.get(PREF_KEY, Theme.LIGHT.name());
        try {
            theme = Theme.valueOf(opgeslagen);
        } catch (IllegalArgumentException e) {
            theme = Theme.LIGHT;
        }
    }

    public void apply(Scene scene) {
        if (scene == null) throw new IllegalArgumentException("Scene mag niet null zijn");

        URL baseCss = getClass().getResource("/css/base.css");
        URL themeCss = getClass().getResource(theme == Theme.DARK ? "/css/dark.css" : "/css/light.css");

        if (baseCss == null || themeCss == null) {
            throw new IllegalStateException("CSS-bestanden voor het thema konden niet gevonden worden.");
        }

        scene.getStylesheets().setAll(baseCss.toExternalForm(), themeCss.toExternalForm());
    }

    public void toggle(Scene scene) {
        theme = theme == Theme.LIGHT ? Theme.DARK : Theme.LIGHT;
        preferences.put(PREF_KEY, theme.name());
        apply(scene);
    }

    public boolean isDark() {
        return theme == Theme.DARK;
    }
}

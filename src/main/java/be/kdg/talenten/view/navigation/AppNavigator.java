package be.kdg.talenten.view.navigation;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.ingerichttalent.IngerichtTalentPresenter;
import be.kdg.talenten.view.ingerichttalent.IngerichtTalentView;
import be.kdg.talenten.view.leerling.LeerlingPresenter;
import be.kdg.talenten.view.leerling.LeerlingView;
import be.kdg.talenten.view.leerkracht.LeerkrachtPresenter;
import be.kdg.talenten.view.leerkracht.LeerkrachtView;
import be.kdg.talenten.view.main.MainPresenter;
import be.kdg.talenten.view.main.MainView;
import be.kdg.talenten.view.talent.TalentPresenter;
import be.kdg.talenten.view.talent.TalentView;
import be.kdg.talenten.view.verdeling.VerdelingPresenter;
import be.kdg.talenten.view.verdeling.VerdelingView;
import be.kdg.talenten.view.voorkeuren.VoorkeurenPresenter;
import be.kdg.talenten.view.voorkeuren.VoorkeurenView;
import javafx.scene.control.Alert;

public class AppNavigator {
    private final ApplicationConfig config;
    private final SceneManager sceneManager;

    public AppNavigator(ApplicationConfig config, SceneManager sceneManager) {
        if (config == null || sceneManager == null) {
            throw new IllegalArgumentException("Config en sceneManager mogen niet null zijn");
        }

        this.config = config;
        this.sceneManager = sceneManager;
    }

    public void koppelSidebar(AppSidebar sidebar) {
        if (sidebar == null) {
            throw new IllegalArgumentException("Sidebar mag niet null zijn");
        }

        sidebar.getDashboardButton().setOnAction(event -> toonDashboard());
        sidebar.getLeerlingenButton().setOnAction(event -> toonLeerlingen());
        sidebar.getKlassenButton().setOnAction(event -> toonNietBeschikbaar("Klassen beheren"));
        sidebar.getLeerkrachtenButton().setOnAction(event -> toonLeerkrachten());
        sidebar.getTalentenButton().setOnAction(event -> toonTalenten());
        sidebar.getTalentenperiodesButton().setOnAction(event -> toonNietBeschikbaar("Talentenperiodes beheren"));
        sidebar.getIngerichteTalentenButton().setOnAction(event -> toonIngerichteTalenten());
        sidebar.getVoorkeurenButton().setOnAction(event -> toonVoorkeuren());
        sidebar.getAutomatischeVerdelingButton().setOnAction(event -> toonVerdeling());
        sidebar.getVerdelingBekijkenButton().setOnAction(event -> toonVerdeling());
        sidebar.getManueleToewijzingenButton().setOnAction(event -> toonVerdeling());
    }

    public void toonDashboard() {
        MainView view = new MainView();
        new MainPresenter(config, view, sceneManager);
        sceneManager.toon(view);
    }

    public void toonLeerlingen() {
        LeerlingView view = new LeerlingView();
        new LeerlingPresenter(config, view, sceneManager);
        sceneManager.toon(view);
    }

    public void toonLeerkrachten() {
        LeerkrachtView view = new LeerkrachtView();
        new LeerkrachtPresenter(config, view, sceneManager, this::toonDashboard);
        sceneManager.toon(view);
    }

    public void toonTalenten() {
        TalentView view = new TalentView();
        new TalentPresenter(config, view, sceneManager, this::toonDashboard);
        sceneManager.toon(view);
    }

    public void toonIngerichteTalenten() {
        IngerichtTalentView view = new IngerichtTalentView();
        new IngerichtTalentPresenter(config, view, sceneManager);
        sceneManager.toon(view);
    }

    public void toonVoorkeuren() {
        VoorkeurenView view = new VoorkeurenView();
        new VoorkeurenPresenter(config, view, sceneManager, this::toonDashboard);
        sceneManager.toon(view);
    }

    public void toonVerdeling() {
        VerdelingView view = new VerdelingView();
        new VerdelingPresenter(config, view, sceneManager, this::toonDashboard);
        sceneManager.toon(view);
    }

    private void toonNietBeschikbaar(String onderdeel) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Nog niet beschikbaar");
        alert.setHeaderText(onderdeel);
        alert.setContentText("Dit beheerscherm is nog niet geïmplementeerd.");
        alert.showAndWait();
    }
}

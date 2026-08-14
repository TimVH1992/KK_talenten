package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.leerkracht.LeerkrachtPresenter;
import be.kdg.talenten.view.leerkracht.LeerkrachtView;
import be.kdg.talenten.view.talent.TalentPresenter;
import be.kdg.talenten.view.talent.TalentView;
import be.kdg.talenten.view.verdeling.VerdelingPresenter;
import be.kdg.talenten.view.verdeling.VerdelingView;
import be.kdg.talenten.view.voorkeuren.VoorkeurenPresenter;
import be.kdg.talenten.view.voorkeuren.VoorkeurenView;

public class MainPresenter {
    private final ApplicationConfig config;
    private final MainView view;
    private final SceneManager sceneManager;

    public MainPresenter(ApplicationConfig config, MainView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }
        this.config = config;
        this.view = view;
        this.sceneManager = sceneManager;
        addEventHandlers();
        laadContext();
    }

    private void addEventHandlers() {
        view.getVoorkeurenButton().setOnAction(event -> toonVoorkeuren());
        view.getSnelVoorkeurenButton().setOnAction(event -> toonVoorkeuren());

        view.getAutomatischeVerdelingButton().setOnAction(event -> toonVerdeling());
        view.getVerdelingBekijkenButton().setOnAction(event -> toonVerdeling());
        view.getManueleToewijzingenButton().setOnAction(event -> toonVerdeling());
        view.getSnelVerdelingButton().setOnAction(event -> toonVerdeling());
        view.getSnelOverzichtButton().setOnAction(event -> toonVerdeling());

        view.getLeerlingenButton().setOnAction(event -> view.toonNietBeschikbaar("Leerlingen beheren"));
        view.getKlassenButton().setOnAction(event -> view.toonNietBeschikbaar("Klassen beheren"));
        view.getLeerkrachtenButton().setOnAction(event -> toonLeerkrachten());
        view.getTalentenButton().setOnAction(event -> toonTalenten());
        view.getTalentenBeheerButton().setOnAction(event -> toonTalenten());
        view.getLeerkrachtenBeheerButton().setOnAction(event -> toonLeerkrachten());
        view.getTalentenperiodesButton().setOnAction(event -> view.toonNietBeschikbaar("Talentenperiodes beheren"));
        view.getIngerichteTalentenButton().setOnAction(event -> view.toonNietBeschikbaar("Ingerichte talenten beheren"));

        view.getAfsluitenButton().setOnAction(event -> sceneManager.sluit());
    }

    private void laadContext() {
        try {
            String schooljaar = config.getSchooljaarService()
                    .zoekActiefSchooljaar()
                    .map(Schooljaar::getNaam)
                    .orElse("Geen actief schooljaar");
            view.setActiefSchooljaar("Schooljaar " + schooljaar);
        } catch (RuntimeException exception) {
            view.setActiefSchooljaar("Schooljaar niet beschikbaar");
        }
    }

    private void toonVoorkeuren() {
        VoorkeurenView voorkeurenView = new VoorkeurenView();
        new VoorkeurenPresenter(config, voorkeurenView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(voorkeurenView);
    }

    private void toonLeerkrachten() {
        LeerkrachtView leerkrachtView = new LeerkrachtView();
        new LeerkrachtPresenter(config, leerkrachtView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(leerkrachtView);
    }

    private void toonTalenten() {
        TalentView talentView = new TalentView();
        new TalentPresenter(config, talentView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(talentView);
    }

    private void toonVerdeling() {
        VerdelingView verdelingView = new VerdelingView();
        new VerdelingPresenter(config, verdelingView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(verdelingView);
    }

    private void toonHoofdmenu() {
        MainView mainView = new MainView();
        new MainPresenter(config, mainView, sceneManager);
        sceneManager.toon(mainView);
    }
}

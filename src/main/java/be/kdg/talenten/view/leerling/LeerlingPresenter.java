package be.kdg.talenten.view.leerling;

import be.kdg.talenten.view.navigation.AppNavigator;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.view.SceneManager;
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

import java.util.Comparator;
import java.util.List;

public class LeerlingPresenter {
    private final ApplicationConfig config;
    private final LeerlingView view;
    private final SceneManager sceneManager;

    private final LeerlingService leerlingService;
    private final SchooljaarService schooljaarService;
    private final KlasService klasService;

    private Leerling geselecteerdeLeerling;
    private boolean schooljarenWordenGeladen;

    public LeerlingPresenter(ApplicationConfig config, LeerlingView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }

        this.config = config;
        this.view = view;
        this.sceneManager = sceneManager;
        this.leerlingService = config.getLeerlingService();
        this.schooljaarService = config.getSchooljaarService();
        this.klasService = config.getKlasService();

        new AppNavigator(config, sceneManager).koppelSidebar(view.getSidebar());

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getDashboardButton().setOnAction(event -> toonHoofdmenu());
        view.getLeerkrachtenButton().setOnAction(event -> toonLeerkrachten());
        view.getTalentenButton().setOnAction(event -> toonTalenten());
        view.getVoorkeurenButton().setOnAction(event -> toonVoorkeuren());
        view.getAutomatischeVerdelingButton().setOnAction(event -> toonVerdeling());
        view.getVerdelingBekijkenButton().setOnAction(event -> toonVerdeling());
        view.getManueleToewijzingenButton().setOnAction(event -> toonVerdeling());

        view.getKlassenButton().setOnAction(event -> view.toonNietBeschikbaar("Klassen beheren"));
        view.getTalentenperiodesButton().setOnAction(event -> view.toonNietBeschikbaar("Talentenperiodes beheren"));
        view.getIngerichteTalentenButton().setOnAction(event -> view.toonNietBeschikbaar("Ingerichte talenten beheren"));

        view.getNieuweLeerlingButton().setOnAction(event -> startNieuweLeerling());
        view.getAnnulerenButton().setOnAction(event -> startNieuweLeerling());
        view.getOpslaanButton().setOnAction(event -> slaLeerlingOp());

        view.getSchooljaarComboBox().setOnAction(event -> {
            if (!schooljarenWordenGeladen) {
                schooljaarGewijzigd();
            }
        });

        view.getLeerlingenTable().getSelectionModel().selectedItemProperty().addListener((observable, oudeLeerling, nieuweLeerling) -> {
            if (nieuweLeerling != null) {
                toonLeerling(nieuweLeerling);
            }
        });
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen = true;
            List<Schooljaar> schooljaren = schooljaarService.zoekSelecteerbareSchooljaren();
            view.setSchooljaren(schooljaren);

            if (schooljaren.isEmpty()) {
                view.setKlassen(List.of());
                view.setLeerlingen(List.of());
                view.setStatus("Er zijn nog geen selecteerbare schooljaren.");
                return;
            }

            Schooljaar standaardSchooljaar = schooljaarService.zoekActiefSchooljaar()
                    .filter(schooljaren::contains)
                    .orElse(schooljaren.getFirst());

            view.getSchooljaarComboBox().getSelectionModel().select(standaardSchooljaar);
            laadGegevensVoorSchooljaar(standaardSchooljaar);
        } catch (RuntimeException exception) {
            view.toonFout("De leerlingen konden niet geladen worden: " + veiligBericht(exception));
        } finally {
            schooljarenWordenGeladen = false;
        }
    }

    private void schooljaarGewijzigd() {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();

        if (schooljaar == null) {
            view.setKlassen(List.of());
            view.setLeerlingen(List.of());
            view.setStatus("Selecteer eerst een schooljaar.");
            return;
        }

        laadGegevensVoorSchooljaar(schooljaar);
    }

    private void laadGegevensVoorSchooljaar(Schooljaar schooljaar) {
        try {
            List<Klas> klassen = klasService.geefAlleKlassen().stream()
                    .filter(klas -> klas.getSchooljaar().equals(schooljaar))
                    .sorted(Comparator.comparing(Klas::getNaam))
                    .toList();

            view.setKlassen(klassen);
            view.setLeerlingen(leerlingService.geefLeerlingenVoorSchooljaar(schooljaar));
            startNieuweLeerling();

            if (klassen.isEmpty()) {
                view.setStatus("Voor schooljaar " + schooljaar.getNaam() + " zijn nog geen klassen opgeslagen.");
            }
        } catch (RuntimeException exception) {
            view.toonFout("De gegevens voor schooljaar " + schooljaar.getNaam() + " konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void startNieuweLeerling() {
        geselecteerdeLeerling = null;
        view.toonNieuweLeerlingFormulier();
    }

    private void toonLeerling(Leerling leerling) {
        geselecteerdeLeerling = leerling;
        view.toonLeerlingFormulier(leerling);
    }

    private void slaLeerlingOp() {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        Klas klas = view.getKlasComboBox().getValue();
        String voornaam = view.getVoornaamField().getText();
        String achternaam = view.getAchternaamField().getText();

        if (schooljaar == null) {
            view.toonFout("Selecteer eerst een schooljaar.");
            return;
        }
        if (klas == null) {
            view.toonFout("Selecteer een klas voor de leerling.");
            return;
        }

        try {
            if (geselecteerdeLeerling == null) {
                Leerling nieuweLeerling = leerlingService.maakLeerling(voornaam, achternaam, klas);
                herlaadLeerlingen(schooljaar);
                startNieuweLeerling();
                view.toonSucces("Leerling " + nieuweLeerling + " is toegevoegd aan " + klas.getNaam() + ".");
            } else {
                String oudeNaam = geselecteerdeLeerling.toString();
                String oudeKlas = geselecteerdeLeerling.getKlas().getNaam();

                leerlingService.wijzigLeerling(geselecteerdeLeerling, voornaam, achternaam, klas);
                herlaadLeerlingen(schooljaar);
                startNieuweLeerling();

                if (!oudeKlas.equals(klas.getNaam())) {
                    view.toonSucces("Leerling " + oudeNaam + " is aangepast en verplaatst van " + oudeKlas + " naar " + klas.getNaam() + ".");
                } else {
                    view.toonSucces("Leerling " + oudeNaam + " is aangepast.");
                }
            }
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void herlaadLeerlingen(Schooljaar schooljaar) {
        view.setLeerlingen(leerlingService.geefLeerlingenVoorSchooljaar(schooljaar));
    }

    private void toonHoofdmenu() {
        MainView mainView = new MainView();
        new MainPresenter(config, mainView, sceneManager);
        sceneManager.toon(mainView);
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

    private void toonVoorkeuren() {
        VoorkeurenView voorkeurenView = new VoorkeurenView();
        new VoorkeurenPresenter(config, voorkeurenView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(voorkeurenView);
    }

    private void toonVerdeling() {
        VerdelingView verdelingView = new VerdelingView();
        new VerdelingPresenter(config, verdelingView, sceneManager, this::toonHoofdmenu);
        sceneManager.toon(verdelingView);
    }

    private String veiligBericht(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getMessage();
    }
}

package be.kdg.talenten.view.leerkracht;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.service.beheer.LeerkrachtService;
import be.kdg.talenten.view.SceneManager;

public class LeerkrachtPresenter {
    private final LeerkrachtView view;
    private final Runnable terugNaarHoofdmenu;
    private final LeerkrachtService leerkrachtService;

    private Leerkracht geselecteerdeLeerkracht;

    public LeerkrachtPresenter(ApplicationConfig config, LeerkrachtView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }

        this.view = view;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.leerkrachtService = config.getLeerkrachtService();

        addEventHandlers();
        laadLeerkrachten();
        startNieuweLeerkracht();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());
        view.getNieuweLeerkrachtButton().setOnAction(event -> startNieuweLeerkracht());
        view.getAnnulerenButton().setOnAction(event -> startNieuweLeerkracht());
        view.getOpslaanButton().setOnAction(event -> slaLeerkrachtOp());

        view.getLeerkrachtenTable().getSelectionModel().selectedItemProperty().addListener((observable, oudeLeerkracht, nieuweLeerkracht) -> {
            if (nieuweLeerkracht != null) {
                toonLeerkracht(nieuweLeerkracht);
            }
        });
    }

    private void laadLeerkrachten() {
        try {
            view.setLeerkrachten(leerkrachtService.geefAlleLeerkrachten());
        } catch (RuntimeException exception) {
            view.toonFout("De leerkrachten konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void startNieuweLeerkracht() {
        geselecteerdeLeerkracht = null;
        view.toonNieuweLeerkrachtFormulier();
    }

    private void toonLeerkracht(Leerkracht leerkracht) {
        geselecteerdeLeerkracht = leerkracht;
        view.toonLeerkrachtFormulier(leerkracht);
    }

    private void slaLeerkrachtOp() {
        String voornaam = view.getVoornaamField().getText();
        String achternaam = view.getAchternaamField().getText();

        try {
            if (geselecteerdeLeerkracht == null) {
                Leerkracht nieuweLeerkracht = leerkrachtService.maakLeerkracht(voornaam, achternaam);
                laadLeerkrachten();
                startNieuweLeerkracht();
                view.toonSucces("Leerkracht " + nieuweLeerkracht.getVoornaam() + " " + nieuweLeerkracht.getAchternaam() + " is toegevoegd.");
            } else {
                String oudeNaam = geselecteerdeLeerkracht.getVoornaam() + " " + geselecteerdeLeerkracht.getAchternaam();
                leerkrachtService.wijzigLeerkracht(geselecteerdeLeerkracht, voornaam, achternaam);
                laadLeerkrachten();
                startNieuweLeerkracht();
                view.toonSucces("Leerkracht " + oudeNaam + " is aangepast.");
            }
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private String veiligBericht(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getMessage();
    }
}

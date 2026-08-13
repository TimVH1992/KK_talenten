package be.kdg.talenten.view.talent;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.service.beheer.TalentService;
import be.kdg.talenten.view.SceneManager;

public class TalentPresenter {
    private final TalentView view;
    private final Runnable terugNaarHoofdmenu;
    private final TalentService talentService;

    private Talent geselecteerdTalent;

    public TalentPresenter(ApplicationConfig config, TalentView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }

        this.view = view;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.talentService = config.getTalentService();

        addEventHandlers();
        laadTalenten();
        startNieuwTalent();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());
        view.getNieuwTalentButton().setOnAction(event -> startNieuwTalent());
        view.getAnnulerenButton().setOnAction(event -> startNieuwTalent());
        view.getOpslaanButton().setOnAction(event -> slaTalentOp());

        view.getTalentenTable().getSelectionModel().selectedItemProperty().addListener((observable, oudTalent, nieuwTalent) -> {
            if (nieuwTalent != null) {
                toonTalent(nieuwTalent);
            }
        });
    }

    private void laadTalenten() {
        try {
            view.setTalenten(talentService.geefAlleTalenten());
        } catch (RuntimeException exception) {
            view.toonFout("De talenten konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void startNieuwTalent() {
        geselecteerdTalent = null;
        view.toonNieuwTalentFormulier();
    }

    private void toonTalent(Talent talent) {
        geselecteerdTalent = talent;
        view.toonTalentFormulier(talent);
    }

    private void slaTalentOp() {
        String naam = view.getNaamField().getText();
        String beschrijving = view.getBeschrijvingArea().getText();

        try {
            if (geselecteerdTalent == null) {
                Talent nieuwTalent = talentService.maakTalent(naam, beschrijving);
                laadTalenten();
                startNieuwTalent();
                view.toonSucces("Talent " + nieuwTalent.getNaam() + " is toegevoegd.");
            } else {
                String oudeNaam = geselecteerdTalent.getNaam();
                talentService.wijzigTalent(geselecteerdTalent, naam, beschrijving);
                laadTalenten();
                startNieuwTalent();
                view.toonSucces("Talent " + oudeNaam + " is aangepast.");
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

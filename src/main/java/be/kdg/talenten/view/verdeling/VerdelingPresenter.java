package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import be.kdg.talenten.service.ManueleToewijzingService;
import be.kdg.talenten.service.TalentenPeriodeService;
import be.kdg.talenten.service.VerdelingBekijkenService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import be.kdg.talenten.view.SceneManager;

import java.util.List;

public class VerdelingPresenter {
    private final VerdelingView view;
    private final SceneManager sceneManager;
    private final Runnable terugNaarHoofdmenu;
    private final TalentenPeriodeService talentenPeriodeService;
    private final VerdelingBekijkenService verdelingBekijkenService;
    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;

    public VerdelingPresenter(ApplicationConfig config, VerdelingView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }
        this.view = view;
        this.sceneManager = sceneManager;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.verdelingBekijkenService = config.getVerdelingBekijkenService();
        this.automatischeVerdelingService = config.getAutomatischeVerdelingService();
        this.manueleToewijzingService = config.getManueleToewijzingService();

        addEventHandlers();
        laadPeriodes();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());
        view.getOverzichtLadenButton().setOnAction(event -> laadOverzicht());
        view.getPeriodeComboBox().setOnAction(event -> laadOverzicht());
        view.getTalentenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> toonToewijzingen(nieuw));
        view.getAutomatischeVerdelingButton().setOnAction(event -> voerAutomatischeVerdelingUit());
        view.getVerplaatsLeerlingButton().setOnAction(event -> verplaatsLeerling());
    }

    private void laadPeriodes() {
        try {
            List<TalentenPeriode> periodes = talentenPeriodeService.zoekAlle();
            view.setPeriodes(periodes);

            if (periodes.isEmpty()) {
                view.toonMelding("Er zijn nog geen talentenperiodes opgeslagen.");
                return;
            }

            view.getPeriodeComboBox().getSelectionModel().selectFirst();
            laadOverzicht();
        } catch (RuntimeException exception) {
            view.toonFout("De talentenperiodes konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void laadOverzicht() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (periode == null) {
            view.setOverzichten(List.of());
            view.setToewijzingen(List.of());
            view.toonMelding("Selecteer eerst een talentenperiode.");
            return;
        }

        try {
            List<IngerichtTalentOverzicht> overzichten = verdelingBekijkenService.bekijkPerIngerichtTalent(periode);
            view.setOverzichten(overzichten);
            view.setToewijzingen(List.of());

            if (overzichten.isEmpty()) {
                view.toonMelding("Voor deze periode zijn nog geen talenten ingericht.");
                return;
            }

            view.getTalentenTable().getSelectionModel().selectFirst();
            view.toonMelding("Overzicht geladen voor " + periode.getNaam() + ".");
        } catch (RuntimeException exception) {
            view.toonFout("Het overzicht kon niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void toonToewijzingen(IngerichtTalentOverzicht overzicht) {
        if (overzicht == null) {
            view.setToewijzingen(List.of());
            return;
        }
        view.setToewijzingen(overzicht.toewijzingen());
    }

    private void voerAutomatischeVerdelingUit() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (periode == null) {
            view.toonFout("Selecteer eerst een talentenperiode.");
            return;
        }

        boolean bevestigd = view.vraagBevestiging("Automatische verdeling", "De automatische verdeling voor " + periode.getNaam() + " wordt uitgevoerd en opgeslagen. Doorgaan?");
        if (!bevestigd) return;

        try {
            VerdelingsResultaat resultaat = automatischeVerdelingService.voerAutomatischeVerdelingUit(periode);
            laadOverzicht();
            view.toonSucces(resultaat.getAantalToewijzingen() + " leerlingen toegewezen. Niet toegewezen: " + resultaat.getNietToegewezenLeerlingen().size() + ".");
        } catch (RuntimeException exception) {
            view.toonFout("De automatische verdeling is niet uitgevoerd: " + veiligBericht(exception));
        }
    }

    private void verplaatsLeerling() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Toewijzing geselecteerdeToewijzing = view.getLeerlingenTable().getSelectionModel().getSelectedItem();
        IngerichtTalent doelTalent = view.getDoelTalentComboBox().getValue();

        if (periode == null) {
            view.toonFout("Selecteer eerst een talentenperiode.");
            return;
        }
        if (geselecteerdeToewijzing == null) {
            view.toonFout("Selecteer eerst een leerling in de rechtertabel.");
            return;
        }
        if (doelTalent == null) {
            view.toonFout("Kies het talent waarnaar de leerling verplaatst moet worden.");
            return;
        }

        try {
            manueleToewijzingService.wijzigToewijzing(periode, geselecteerdeToewijzing.getLeerling(), doelTalent);
            String leerlingNaam = geselecteerdeToewijzing.getLeerling().toString();
            laadOverzicht();
            view.toonSucces(leerlingNaam + " werd verplaatst naar " + doelTalent.getTalent().getNaam() + ".");
        } catch (RuntimeException exception) {
            view.toonFout("De leerling kon niet verplaatst worden: " + veiligBericht(exception));
        }
    }

    private String veiligBericht(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank() ? exception.getClass().getSimpleName() : exception.getMessage();
    }
}

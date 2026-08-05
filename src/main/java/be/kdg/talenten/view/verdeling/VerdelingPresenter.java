package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.overzicht.KlasOverzicht;
import be.kdg.talenten.overzicht.LeerlingDetailsOverzicht;
import be.kdg.talenten.overzicht.LeerlingToewijzingOverzicht;
import be.kdg.talenten.service.AutomatischeVerdelingService;
import be.kdg.talenten.service.KlasService;
import be.kdg.talenten.service.LeerlingDetailsService;
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
    private final KlasService klasService;
    private final VerdelingBekijkenService verdelingBekijkenService;
    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final LeerlingDetailsService leerlingDetailsService;

    private Leerling geselecteerdeLeerling;
    private Toewijzing huidigeToewijzingVanGeselecteerdeLeerling;

    public VerdelingPresenter(ApplicationConfig config, VerdelingView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }

        this.view = view;
        this.sceneManager = sceneManager;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.klasService = config.getKlasService();
        this.verdelingBekijkenService = config.getVerdelingBekijkenService();
        this.automatischeVerdelingService = config.getAutomatischeVerdelingService();
        this.manueleToewijzingService = config.getManueleToewijzingService();
        this.leerlingDetailsService = config.getLeerlingDetailsService();

        addEventHandlers();
        laadPeriodes();
        laadKlassen();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());
        view.getOverzichtLadenButton().setOnAction(event -> laadOverzicht());
        view.getPeriodeComboBox().setOnAction(event -> laadOverzicht());
        view.getKlasComboBox().setOnAction(event -> laadKlasOverzicht());
        view.getTalentenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> toonToewijzingen(nieuw));
        view.getLeerlingenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> selecteerToewijzing(nieuw));
        view.getKlasLeerlingenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> selecteerLeerlingUitKlasOverzicht(nieuw));
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

    private void laadKlassen() {
        try {
            List<Klas> klassen = klasService.zoekAlle();
            view.setKlassen(klassen);

            if (klassen.isEmpty()) {
                view.setKlasOverzicht(null);
                return;
            }

            view.getKlasComboBox().getSelectionModel().selectFirst();
            laadKlasOverzicht();
        } catch (RuntimeException exception) {
            view.toonFout("De klassen konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void laadOverzicht() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        wisGeselecteerdeLeerling();

        if (periode == null) {
            view.setOverzichten(List.of());
            view.setToewijzingen(null, List.of());
            view.setKlasOverzicht(null);
            view.toonMelding("Selecteer eerst een talentenperiode.");
            return;
        }

        try {
            List<IngerichtTalentOverzicht> overzichten = verdelingBekijkenService.bekijkPerIngerichtTalent(periode);
            view.setOverzichten(overzichten);
            view.setToewijzingen(null, List.of());

            if (overzichten.isEmpty()) {
                view.setKlasOverzicht(null);
                view.toonMelding("Voor deze periode zijn nog geen talenten ingericht.");
                return;
            }

            view.getTalentenTable().getSelectionModel().selectFirst();
            laadKlasOverzicht();

            int totaalToegewezen = overzichten.stream().mapToInt(IngerichtTalentOverzicht::aantalToegewezen).sum();
            view.toonMelding("Overzicht geladen voor " + periode.getNaam() + ". " + totaalToegewezen + " leerlingen toegewezen in totaal.");
        } catch (RuntimeException exception) {
            view.toonFout("Het overzicht kon niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void laadKlasOverzicht() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Klas klas = view.getKlasComboBox().getValue();

        if (periode == null || klas == null) {
            view.setKlasOverzicht(null);
            return;
        }

        try {
            KlasOverzicht overzicht = verdelingBekijkenService.bekijkVoorKlas(periode, klas);
            view.setKlasOverzicht(overzicht);
        } catch (RuntimeException exception) {
            view.setKlasOverzicht(null);
            view.toonFout("Het klasoverzicht kon niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void toonToewijzingen(IngerichtTalentOverzicht overzicht) {
        wisGeselecteerdeLeerling();

        if (overzicht == null) {
            view.setToewijzingen(null, List.of());
            return;
        }

        String talentNaam = overzicht.ingerichtTalent().getTalent().getNaam();
        view.setToewijzingen(talentNaam, overzicht.toewijzingen());
    }

    private void selecteerToewijzing(Toewijzing toewijzing) {
        if (toewijzing == null) return;

        view.getKlasLeerlingenTable().getSelectionModel().clearSelection();
        selecteerLeerling(toewijzing.getLeerling(), toewijzing);
    }

    private void selecteerLeerlingUitKlasOverzicht(LeerlingToewijzingOverzicht overzicht) {
        if (overzicht == null) return;

        view.getLeerlingenTable().getSelectionModel().clearSelection();
        selecteerLeerling(overzicht.leerling(), overzicht.toewijzing());
    }

    private void selecteerLeerling(Leerling leerling, Toewijzing huidigeToewijzing) {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (leerling == null || periode == null) {
            wisGeselecteerdeLeerling();
            return;
        }

        try {
            LeerlingDetailsOverzicht details = leerlingDetailsService.bekijk(leerling, periode);
            geselecteerdeLeerling = leerling;
            huidigeToewijzingVanGeselecteerdeLeerling = huidigeToewijzing;
            view.setLeerlingDetails(details, huidigeToewijzing);
        } catch (RuntimeException exception) {
            wisGeselecteerdeLeerling();
            view.toonFout("De leerlinginformatie kon niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void wisGeselecteerdeLeerling() {
        geselecteerdeLeerling = null;
        huidigeToewijzingVanGeselecteerdeLeerling = null;
        view.setLeerlingDetails(null, null);
    }

    private void voerAutomatischeVerdelingUit() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        if (periode == null) {
            view.toonFout("Selecteer eerst een talentenperiode.");
            return;
        }

        try {
            boolean bestaandeToewijzingen = automatischeVerdelingService.heeftBestaandeToewijzingen(periode);
            String boodschap = bestaandeToewijzingen
                    ? "De bestaande automatische toewijzingen voor " + periode.getNaam() + " worden opnieuw berekend. Manuele toewijzingen blijven behouden. Doorgaan?"
                    : "De automatische verdeling voor " + periode.getNaam() + " wordt uitgevoerd en opgeslagen. Doorgaan?";

            if (!view.vraagBevestiging("Automatische verdeling", boodschap)) return;

            VerdelingsResultaat resultaat = automatischeVerdelingService.voerAutomatischeVerdelingUit(periode);
            laadOverzicht();

            String melding = resultaat.getAantalToewijzingen() + " leerlingen automatisch toegewezen. Niet toegewezen: "
                    + resultaat.getNietToegewezenLeerlingen().size() + ".";
            if (bestaandeToewijzingen) {
                melding += " De manuele toewijzingen zijn behouden.";
            }
            view.toonSucces(melding);
        } catch (RuntimeException exception) {
            view.toonFout("De automatische verdeling is niet uitgevoerd: " + veiligBericht(exception));
        }
    }

    private void verplaatsLeerling() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        IngerichtTalent doelTalent = view.getDoelTalentComboBox().getValue();

        if (periode == null) {
            view.toonFout("Selecteer eerst een talentenperiode.");
            return;
        }
        if (geselecteerdeLeerling == null) {
            view.toonFout("Selecteer eerst een leerling in het overzicht per talent of per klas.");
            return;
        }
        if (doelTalent == null) {
            view.toonFout("Kies het talent waarnaar de leerling verplaatst moet worden.");
            return;
        }

        try {
            String leerlingNaam = geselecteerdeLeerling.toString();
            manueleToewijzingService.wijzigToewijzing(periode, geselecteerdeLeerling, doelTalent);
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

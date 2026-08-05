package be.kdg.talenten.view.verdeling;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
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
import be.kdg.talenten.service.SchooljaarService;
import be.kdg.talenten.service.TalentenPeriodeService;
import be.kdg.talenten.service.VerdelingBekijkenService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import be.kdg.talenten.view.SceneManager;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class VerdelingPresenter {
    private final VerdelingView view;
    private final SceneManager sceneManager;
    private final Runnable terugNaarHoofdmenu;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final KlasService klasService;
    private final VerdelingBekijkenService verdelingBekijkenService;
    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final LeerlingDetailsService leerlingDetailsService;

    private Leerling geselecteerdeLeerling;
    private Toewijzing huidigeToewijzingVanGeselecteerdeLeerling;
    private boolean schooljarenWordenGeladen;

    public VerdelingPresenter(ApplicationConfig config, VerdelingView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }

        this.view = view;
        this.sceneManager = sceneManager;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.schooljaarService = config.getSchooljaarService();
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.klasService = config.getKlasService();
        this.verdelingBekijkenService = config.getVerdelingBekijkenService();
        this.automatischeVerdelingService = config.getAutomatischeVerdelingService();
        this.manueleToewijzingService = config.getManueleToewijzingService();
        this.leerlingDetailsService = config.getLeerlingDetailsService();

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());
        view.getSchooljaarComboBox().setOnAction(event -> schooljaarGewijzigd());
        view.getOverzichtLadenButton().setOnAction(event -> laadOverzicht());
        view.getPeriodeComboBox().setOnAction(event -> laadOverzicht());
        view.getKlasComboBox().setOnAction(event -> laadKlasOverzicht());
        view.getTalentenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> toonToewijzingen(nieuw));
        view.getLeerlingenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> selecteerToewijzing(nieuw));
        view.getKlasLeerlingenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> selecteerLeerlingUitKlasOverzicht(nieuw));
        view.getAutomatischeVerdelingButton().setOnAction(event -> voerAutomatischeVerdelingUit());
        view.getVerplaatsLeerlingButton().setOnAction(event -> verplaatsLeerling());
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen = true;
            List<Schooljaar> schooljaren = schooljaarService.zoekSelecteerbareSchooljaren();
            view.setSchooljaren(schooljaren);

            if (schooljaren.isEmpty()) {
                view.setPeriodes(List.of());
                view.setKlassen(List.of());
                view.toonMelding("Er is geen selecteerbaar schooljaar opgeslagen.");
                return;
            }

            Schooljaar actiefSchooljaar = schooljaarService.zoekActiefSchooljaar()
                    .filter(schooljaren::contains)
                    .orElse(schooljaren.getFirst());
            view.getSchooljaarComboBox().getSelectionModel().select(actiefSchooljaar);
            laadGegevensVoorSchooljaar(actiefSchooljaar);
        } catch (RuntimeException exception) {
            view.toonFout("De schooljaren konden niet geladen worden: " + veiligBericht(exception));
        } finally {
            schooljarenWordenGeladen = false;
        }
    }

    private void schooljaarGewijzigd() {
        if (schooljarenWordenGeladen) return;

        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        if (schooljaar == null) {
            view.setPeriodes(List.of());
            view.setKlassen(List.of());
            view.toonMelding("Selecteer eerst een schooljaar.");
            return;
        }

        try {
            schooljaarService.maakActief(schooljaar);
            laadGegevensVoorSchooljaar(schooljaar);
            view.toonMelding("Schooljaar " + schooljaar.getNaam() + " is geselecteerd en wordt bij de volgende opstart opnieuw gebruikt.");
        } catch (RuntimeException exception) {
            view.toonFout("Het schooljaar kon niet geselecteerd worden: " + veiligBericht(exception));
        }
    }

    private void laadGegevensVoorSchooljaar(Schooljaar schooljaar) {
        laadPeriodes(schooljaar);
        laadKlassen(schooljaar);
    }

    private void laadPeriodes(Schooljaar schooljaar) {
        List<TalentenPeriode> periodes = talentenPeriodeService.zoekVoorSchooljaar(schooljaar);
        view.setPeriodes(periodes);

        if (periodes.isEmpty()) {
            view.setOverzichten(List.of());
            view.setToewijzingen(null, List.of());
            view.toonMelding("Voor schooljaar " + schooljaar.getNaam() + " zijn nog geen talentenperiodes opgeslagen.");
            return;
        }

        view.getPeriodeComboBox().getSelectionModel().select(bepaalStandaardPeriode(periodes));
        laadOverzicht();
    }

    private TalentenPeriode bepaalStandaardPeriode(List<TalentenPeriode> periodes) {
        LocalDate vandaag = LocalDate.now();

        return periodes.stream()
                .filter(periode -> !vandaag.isBefore(periode.getStartDatum()) && !vandaag.isAfter(periode.getEindDatum()))
                .findFirst()
                .orElseGet(() -> periodes.stream()
                        .filter(periode -> !periode.getStartDatum().isBefore(vandaag))
                        .min(Comparator.comparing(TalentenPeriode::getStartDatum))
                        .orElse(periodes.getLast()));
    }

    private void laadKlassen(Schooljaar schooljaar) {
        List<Klas> klassen = klasService.zoekAlle().stream()
                .filter(klas -> klas.getSchooljaar().equals(schooljaar.getNaam()))
                .toList();
        view.setKlassen(klassen);

        if (klassen.isEmpty()) {
            view.setKlasOverzicht(null);
            return;
        }

        view.getKlasComboBox().getSelectionModel().selectFirst();
        laadKlasOverzicht();
    }

    private void laadOverzicht() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        pasWijzigMogelijkhedenAan(periode);
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
            String melding = "Overzicht geladen voor " + periode.getNaam() + ". " + totaalToegewezen + " leerlingen toegewezen in totaal.";
            if (isAfgelopenPeriode(periode)) melding += " Deze periode is afgelopen en kan alleen bekeken worden.";
            view.toonMelding(melding);
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
            view.setKlasOverzicht(verdelingBekijkenService.bekijkVoorKlas(periode, klas));
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
        view.setToewijzingen(overzicht.ingerichtTalent().getTalent().getNaam(), overzicht.toewijzingen());
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

            String melding = resultaat.getAantalToewijzingen() + " leerlingen automatisch toegewezen. Niet toegewezen: " + resultaat.getNietToegewezenLeerlingen().size() + ".";
            if (bestaandeToewijzingen) melding += " De manuele toewijzingen zijn behouden.";
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

    private void pasWijzigMogelijkhedenAan(TalentenPeriode periode) {
        view.setWijzigingenToegestaan(periode != null && !isAfgelopenPeriode(periode));
    }

    private boolean isAfgelopenPeriode(TalentenPeriode periode) {
        return periode.getEindDatum().isBefore(LocalDate.now());
    }
}

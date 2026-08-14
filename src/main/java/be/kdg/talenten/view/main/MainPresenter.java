package be.kdg.talenten.view.main;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.overzicht.OverzichtGegevens;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.overzicht.OverzichtService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class MainPresenter {
    private final MainView view;
    private final AppNavigator navigator;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final OverzichtService overzichtService;

    public MainPresenter(ApplicationConfig config, MainView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }

        this.view = view;
        this.navigator = new AppNavigator(config, sceneManager);
        this.schooljaarService = config.getSchooljaarService();
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.overzichtService = config.getOverzichtService();

        navigator.koppelSidebar(view.getSidebar());
        laadOverzicht();
    }

    private void laadOverzicht() {
        try {
            Schooljaar schooljaar = schooljaarService.zoekActiefSchooljaar().orElse(null);

            if (schooljaar == null) {
                toonLeegOverzicht("Er is geen actief schooljaar ingesteld.");
                return;
            }

            view.setActiefSchooljaar(schooljaar.getNaam());

            List<TalentenPeriode> periodes = talentenPeriodeService.geefPeriodesVoorSchooljaar(schooljaar);
            TalentenPeriode relevantePeriode = bepaalRelevantePeriode(periodes);
            OverzichtGegevens gegevens = overzichtService.geefOverzicht(schooljaar, relevantePeriode);

            vulKerncijfers(gegevens);
            vulPeriode(gegevens.talentenPeriode());
            vulAandachtspunten(gegevens);
            configureerVolgendeActie(gegevens);
        } catch (RuntimeException exception) {
            toonLeegOverzicht("Het overzicht kon niet volledig geladen worden.");
        }
    }

    private TalentenPeriode bepaalRelevantePeriode(List<TalentenPeriode> periodes) {
        if (periodes == null || periodes.isEmpty()) {
            return null;
        }

        LocalDate vandaag = LocalDate.now();

        return periodes.stream()
                .filter(periode -> !vandaag.isBefore(periode.getStartDatum()) && !vandaag.isAfter(periode.getEindDatum()))
                .findFirst()
                .orElseGet(() -> periodes.stream()
                        .filter(periode -> !periode.getStartDatum().isBefore(vandaag))
                        .min(Comparator.comparing(TalentenPeriode::getStartDatum))
                        .orElseGet(() -> periodes.stream()
                                .max(Comparator.comparing(TalentenPeriode::getEindDatum))
                                .orElse(null)));
    }

    private void vulKerncijfers(OverzichtGegevens gegevens) {
        view.setAantalLeerlingen(gegevens.aantalLeerlingen());
        view.setAantalIngerichteTalenten(gegevens.aantalIngerichteTalenten());
        view.setVoorkeurenStatus(gegevens.aantalLeerlingenMetVolledigeVoorkeuren(), gegevens.aantalLeerlingen());
        view.setToewijzingenStatus(gegevens.aantalToewijzingen(), gegevens.aantalLeerlingen());
    }

    private void vulPeriode(TalentenPeriode periode) {
        if (periode == null) {
            view.setHuidigePeriode(
                    "Geen talentenperiode",
                    "—",
                    "Voor het actieve schooljaar is nog geen talentenperiode aangemaakt."
            );
            return;
        }

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale.forLanguageTag("nl-BE"));
        String datums = periode.getStartDatum().format(formatter) + " — " + periode.getEindDatum().format(formatter);
        LocalDate vandaag = LocalDate.now();

        String status;
        if (vandaag.isBefore(periode.getStartDatum())) {
            status = "Deze periode start op " + periode.getStartDatum().format(formatter) + ".";
        } else if (vandaag.isAfter(periode.getEindDatum())) {
            status = "Deze periode is afgelopen.";
        } else {
            status = "Deze periode loopt momenteel.";
        }

        view.setHuidigePeriode(periode.getNaam(), datums, status);
    }

    private void vulAandachtspunten(OverzichtGegevens gegevens) {
        List<String> aandachtspunten = new ArrayList<>();

        if (gegevens.talentenPeriode() == null) {
            aandachtspunten.add("Maak eerst een talentenperiode aan voor het actieve schooljaar.");
            view.setAandachtspunten(aandachtspunten);
            return;
        }

        if (gegevens.aantalIngerichteTalenten() == 0) {
            aandachtspunten.add("Voor deze periode zijn nog geen talenten ingericht.");
        }

        int onvolledigeVoorkeuren = gegevens.aantalLeerlingen() - gegevens.aantalLeerlingenMetVolledigeVoorkeuren();
        if (onvolledigeVoorkeuren > 0) {
            aandachtspunten.add(onvolledigeVoorkeuren + " leerlingen hebben nog geen volledige voorkeuren.");
        }

        if (gegevens.aantalImportProblemen() > 0) {
            aandachtspunten.add(gegevens.aantalImportProblemen() + " problemen gevonden bij de voorkeurenimport.");
        }

        int nietToegewezen = gegevens.aantalLeerlingen() - gegevens.aantalToewijzingen();
        if (nietToegewezen > 0 && gegevens.aantalLeerlingenMetVolledigeVoorkeuren() == gegevens.aantalLeerlingen()) {
            aandachtspunten.add(nietToegewezen + " leerlingen zijn nog niet toegewezen.");
        }

        view.setAandachtspunten(aandachtspunten);
    }

    private void configureerVolgendeActie(OverzichtGegevens gegevens) {
        TalentenPeriode periode = gegevens.talentenPeriode();
        boolean kanVerdelen = periode != null
                && !periode.getEindDatum().isBefore(LocalDate.now())
                && gegevens.aantalLeerlingen() > 0
                && gegevens.aantalIngerichteTalenten() > 0
                && gegevens.aantalLeerlingenMetVolledigeVoorkeuren() == gegevens.aantalLeerlingen()
                && gegevens.aantalToewijzingen() < gegevens.aantalLeerlingen();

        if (!kanVerdelen) {
            view.verbergVolgendeActie();
            return;
        }

        view.toonVolgendeActie("Naar automatische verdeling");
        view.getVolgendeActieButton().setOnAction(event -> navigator.toonVerdeling());
    }

    private void toonLeegOverzicht(String aandachtspunt) {
        view.setActiefSchooljaar(null);
        view.setAantalLeerlingen(0);
        view.setAantalIngerichteTalenten(0);
        view.setVoorkeurenStatus(0, 0);
        view.setToewijzingenStatus(0, 0);
        view.setHuidigePeriode("Geen talentenperiode", "—", "Er zijn nog geen gegevens beschikbaar.");
        view.setAandachtspunten(List.of(aandachtspunt));
        view.verbergVolgendeActie();
    }
}

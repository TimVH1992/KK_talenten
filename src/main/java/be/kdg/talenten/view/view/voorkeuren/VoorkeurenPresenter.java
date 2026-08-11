package be.kdg.talenten.view.view.voorkeuren;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportResultaat;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.voorkeuren.VoorkeurenView;

import java.nio.file.Path;
import java.util.List;

public class VoorkeurenPresenter {
    private final be.kdg.talenten.view.voorkeuren.VoorkeurenView view;
    private final Runnable terugNaarHoofdmenu;

    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final VoorkeurenExcelService excelService;
    private final VoorkeurenImportService importService;

    private boolean schooljarenWordenGeladen;

    public VoorkeurenPresenter(ApplicationConfig config, VoorkeurenView view, SceneManager sceneManager, Runnable terugNaarHoofdmenu) {
        if (config == null || view == null || sceneManager == null || terugNaarHoofdmenu == null) {
            throw new IllegalArgumentException("Config, view, sceneManager en terugactie mogen niet null zijn");
        }

        this.view = view;
        this.terugNaarHoofdmenu = terugNaarHoofdmenu;
        this.schooljaarService = config.getSchooljaarService();
        this.talentenPeriodeService = config.getTalentenPeriodeService();
        this.excelService = config.getVoorkeurenExcelService();
        this.importService = config.getVoorkeurenImportService();

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getTerugButton().setOnAction(event -> terugNaarHoofdmenu.run());

        view.getSchooljaarComboBox().setOnAction(event -> {
            if (!schooljarenWordenGeladen) {
                schooljaarGewijzigd();
            }
        });

        view.getGenereerButton().setOnAction(event -> genereerTemplate());
        view.getImporteerButton().setOnAction(event -> importeerBestand());
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen = true;
            List<Schooljaar> schooljaren = schooljaarService.zoekSelecteerbareSchooljaren();
            view.setSchooljaren(schooljaren);

            if (schooljaren.isEmpty()) {
                view.setPeriodes(List.of());
                view.toonFout("Er is geen selecteerbaar schooljaar opgeslagen.");
                return;
            }

            Schooljaar standaard = schooljaarService.zoekActiefSchooljaar()
                    .filter(schooljaren::contains)
                    .orElse(schooljaren.getFirst());

            view.getSchooljaarComboBox().getSelectionModel().select(standaard);
            laadPeriodes(standaard);
        } catch (RuntimeException exception) {
            view.toonFout("De schooljaren konden niet geladen worden: " + veiligBericht(exception));
        } finally {
            schooljarenWordenGeladen = false;
        }
    }

    private void schooljaarGewijzigd() {
        Schooljaar schooljaar = view.getSchooljaarComboBox().getValue();
        if (schooljaar == null) {
            view.setPeriodes(List.of());
            return;
        }

        try {
            schooljaarService.maakActief(schooljaar);
            laadPeriodes(schooljaar);
            view.toonMelding("Schooljaar " + schooljaar.getNaam() + " geselecteerd.");
        } catch (RuntimeException exception) {
            view.toonFout("Het schooljaar kon niet geselecteerd worden: " + veiligBericht(exception));
        }
    }

    private void laadPeriodes(Schooljaar schooljaar) {
        List<TalentenPeriode> periodes = talentenPeriodeService.zoekVoorSchooljaar(schooljaar);
        view.setPeriodes(periodes);

        if (!periodes.isEmpty()) {
            view.getPeriodeComboBox().getSelectionModel().selectFirst();
        }
    }

    private void genereerTemplate() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();

        if (periode == null || doelgroep == null) {
            view.toonFout("Selecteer eerst een talentenperiode en doelgroep.");
            return;
        }

        String doelgroepNaam = doelgroep == Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB ? "observatie" : "kwalificatie";
        String bestandsNaam = "voorkeuren_" + periode.getNaam().toLowerCase().replace(' ', '_') + "_" + doelgroepNaam + ".xlsx";
        Path bestand = view.kiesOpslagBestand(bestandsNaam);
        if (bestand == null) return;

        try {
            excelService.genereerTemplate(periode, doelgroep, bestand);
            view.toonSucces("Exceltemplate aangemaakt: " + bestand.getFileName());
        } catch (RuntimeException exception) {
            view.toonFout("Het Exceltemplate kon niet aangemaakt worden: " + veiligBericht(exception));
        }
    }

    private void importeerBestand() {
        TalentenPeriode periode = view.getPeriodeComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();

        if (periode == null || doelgroep == null) {
            view.toonFout("Selecteer eerst een talentenperiode en doelgroep.");
            return;
        }

        Path bestand = view.kiesImportBestand();
        if (bestand == null) return;

        boolean bevestigd = view.vraagBevestiging(
                "Voorkeuren importeren",
                "Bestaande voorkeuren en importproblemen van leerlingen die in dit bestand voorkomen, worden vervangen door de nieuwe inhoud. Doorgaan?"
        );
        if (!bevestigd) return;

        try {
            VoorkeurenImportResultaat resultaat = importService.importeer(bestand, periode, doelgroep);
            view.setProblemen(resultaat.getProblemen());

            if (resultaat.getProblemen().isEmpty()) {
                view.toonSucces("Import voltooid zonder problemen. Alle leerlingen in het bestand hebben drie geldige voorkeuren.");
            } else {
                view.toonWaarschuwing("Import voltooid. " + resultaat.getProblemen().size() + " importprobleem/problemen gevonden. Geldige voorkeuren zijn wel opgeslagen.");
            }
        } catch (RuntimeException exception) {
            view.toonFout("Het voorkeurenbestand kon niet geïmporteerd worden: " + veiligBericht(exception));
        }
    }

    private String veiligBericht(RuntimeException exception) {
        if (exception.getMessage() == null || exception.getMessage().isBlank()) {
            return exception.getClass().getSimpleName();
        }
        return exception.getMessage();
    }
}

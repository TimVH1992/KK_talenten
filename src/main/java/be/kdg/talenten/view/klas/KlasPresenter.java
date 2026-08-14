package be.kdg.talenten.view.klas;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.leerling.LeerlingPlakRegel;
import be.kdg.talenten.service.leerling.LeerlingenPlakResultaat;
import be.kdg.talenten.service.leerling.LeerlingenPlakService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class KlasPresenter {
    private final KlasView view;
    private final KlasService klasService;
    private final LeerlingService leerlingService;
    private final SchooljaarService schooljaarService;
    private final LeerlingenPlakService leerlingenPlakService;

    private Schooljaar actiefSchooljaar;
    private Klas geselecteerdeKlas;
    private LeerlingenPlakResultaat huidigPlakResultaat;

    public KlasPresenter(ApplicationConfig config, KlasView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }

        this.view = view;
        this.klasService = config.getKlasService();
        this.leerlingService = config.getLeerlingService();
        this.schooljaarService = config.getSchooljaarService();
        this.leerlingenPlakService = new LeerlingenPlakService(leerlingService);

        new AppNavigator(config, sceneManager).koppelSidebar(view.getSidebar());

        addEventHandlers();
        laadActiefSchooljaar();
    }

    private void addEventHandlers() {
        view.getNieuweKlasButton().setOnAction(event -> startNieuweKlas());
        view.getAnnulerenButton().setOnAction(event -> startNieuweKlas());
        view.getOpslaanButton().setOnAction(event -> slaKlasOp());
        view.getVerwijderenButton().setOnAction(event -> verwijderKlas());
        view.getPreviewButton().setOnAction(event -> analyseerGeplakteLeerlingen());
        view.getLeerlingenToevoegenButton().setOnAction(event -> slaGeplakteLeerlingenOp());

        view.getPlakArea().textProperty().addListener((observable, oud, nieuw) -> {
            huidigPlakResultaat = null;
            view.setPreview("", false);
        });

        view.getKlassenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> {
            if (nieuw != null) {
                toonKlas(nieuw);
            }
        });
    }

    private void laadActiefSchooljaar() {
        try {
            actiefSchooljaar = schooljaarService.zoekActiefSchooljaar().orElse(null);
            view.setActiefSchooljaar(actiefSchooljaar);

            if (actiefSchooljaar == null) {
                view.setKlassen(List.of(), Map.of());
                view.toonNieuweKlasFormulier(null);
                view.setStatus("Er is geen actief schooljaar. Maak of activeer eerst een schooljaar.");
                return;
            }

            laadKlassen();
            startNieuweKlas();
        } catch (RuntimeException exception) {
            view.toonFout("De klassen konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void laadKlassen() {
        if (actiefSchooljaar == null) return;

        List<Klas> klassen = klasService.geefAlleKlassen().stream()
                .filter(klas -> klas.getSchooljaar().equals(actiefSchooljaar))
                .sorted(Comparator.comparingInt(Klas::getLeerjaar).thenComparing(Klas::getNaam))
                .toList();

        List<Leerling> leerlingen = leerlingService.geefLeerlingenVoorSchooljaar(actiefSchooljaar);
        Map<Long, Integer> aantallen = new LinkedHashMap<>();

        for (Leerling leerling : leerlingen) {
            Long klasId = leerling.getKlas().getId();
            if (klasId != null) {
                aantallen.merge(klasId, 1, Integer::sum);
            }
        }

        view.setKlassen(klassen, aantallen);
    }

    private void startNieuweKlas() {
        geselecteerdeKlas = null;
        huidigPlakResultaat = null;
        view.toonNieuweKlasFormulier(actiefSchooljaar);
    }

    private void toonKlas(Klas klas) {
        geselecteerdeKlas = klas;
        huidigPlakResultaat = null;
        view.toonKlasFormulier(klas);
        laadLeerlingenVoorGeselecteerdeKlas();
    }

    private void laadLeerlingenVoorGeselecteerdeKlas() {
        if (geselecteerdeKlas == null || actiefSchooljaar == null) {
            view.setLeerlingen(null, List.of());
            return;
        }

        List<Leerling> leerlingen = leerlingService.geefLeerlingenVoorSchooljaar(actiefSchooljaar).stream()
                .filter(leerling -> zelfdeKlas(leerling.getKlas(), geselecteerdeKlas))
                .sorted(Comparator.comparing(Leerling::getAchternaam).thenComparing(Leerling::getVoornaam))
                .toList();

        view.setLeerlingen(geselecteerdeKlas, leerlingen);
    }

    private void slaKlasOp() {
        if (actiefSchooljaar == null) {
            view.toonFout("Er is geen actief schooljaar.");
            return;
        }

        String naam = view.getNaamField().getText();
        Integer leerjaar = view.getLeerjaarComboBox().getValue();
        Doelgroep doelgroep = view.getDoelgroepComboBox().getValue();

        if (leerjaar == null) {
            view.toonFout("Selecteer een leerjaar.");
            return;
        }
        if (doelgroep == null) {
            view.toonFout("Selecteer een doelgroep.");
            return;
        }

        try {
            if (geselecteerdeKlas == null) {
                Klas nieuweKlas = klasService.maakKlas(naam, actiefSchooljaar, leerjaar, doelgroep);
                laadKlassen();
                selecteerKlas(nieuweKlas.getId());
                view.toonSucces("Klas " + nieuweKlas.getNaam() + " is aangemaakt. Je kunt nu leerlingen uit Excel plakken.");
            } else {
                Long klasId = geselecteerdeKlas.getId();
                klasService.wijzigKlas(geselecteerdeKlas, naam, leerjaar, doelgroep);
                laadKlassen();
                selecteerKlas(klasId);
                view.toonSucces("Klas " + naam + " is aangepast.");
            }
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void verwijderKlas() {
        if (geselecteerdeKlas == null) {
            view.toonFout("Selecteer eerst een klas.");
            return;
        }

        String naam = geselecteerdeKlas.getNaam();
        if (!view.vraagBevestiging("Klas verwijderen", "Wil je klas " + naam + " verwijderen? Een klas met leerlingen kan niet verwijderd worden.")) {
            return;
        }

        try {
            klasService.verwijderKlas(geselecteerdeKlas);
            laadKlassen();
            startNieuweKlas();
            view.toonSucces("Klas " + naam + " is verwijderd.");
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void analyseerGeplakteLeerlingen() {
        if (geselecteerdeKlas == null) {
            view.toonFout("Sla de klas eerst op voordat je leerlingen toevoegt.");
            return;
        }

        try {
            huidigPlakResultaat = leerlingenPlakService.analyseer(view.getPlakArea().getText());
            view.setPreview(maakPreviewTekst(huidigPlakResultaat), !huidigPlakResultaat.heeftProblemen() && huidigPlakResultaat.getAantalGeldigeLeerlingen() > 0);

            if (huidigPlakResultaat.heeftProblemen()) {
                view.setStatus("De geplakte gegevens bevatten fouten. Corrigeer ze en bekijk opnieuw het voorbeeld.");
            } else {
                view.setStatus(huidigPlakResultaat.getAantalGeldigeLeerlingen() + " leerlingen klaar om toe te voegen aan " + geselecteerdeKlas.getNaam() + ".");
            }
        } catch (RuntimeException exception) {
            huidigPlakResultaat = null;
            view.setPreview("", false);
            view.toonFout(veiligBericht(exception));
        }
    }

    private void slaGeplakteLeerlingenOp() {
        if (geselecteerdeKlas == null) {
            view.toonFout("Selecteer eerst een opgeslagen klas.");
            return;
        }
        if (huidigPlakResultaat == null) {
            view.toonFout("Bekijk eerst het voorbeeld van de geplakte leerlingen.");
            return;
        }

        try {
            List<Leerling> opgeslagen = leerlingenPlakService.slaLeerlingenOp(geselecteerdeKlas, huidigPlakResultaat);
            Long klasId = geselecteerdeKlas.getId();
            laadKlassen();
            selecteerKlas(klasId);
            view.wisPlakInvoer();
            huidigPlakResultaat = null;
            view.toonSucces(opgeslagen.size() + " leerlingen toegevoegd aan " + geselecteerdeKlas.getNaam() + ".");
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private String maakPreviewTekst(LeerlingenPlakResultaat resultaat) {
        StringBuilder tekst = new StringBuilder();

        for (LeerlingPlakRegel regel : resultaat.getGeldigeRegels()) {
            tekst.append("✓ ").append(regel.voornaam()).append(" ").append(regel.achternaam()).append(System.lineSeparator());
        }

        for (String probleem : resultaat.getProblemen()) {
            tekst.append("⚠ ").append(probleem).append(System.lineSeparator());
        }

        if (tekst.isEmpty()) {
            return "Geen leerlingen gevonden.";
        }

        return tekst.toString().trim();
    }

    private void selecteerKlas(Long klasId) {
        if (klasId == null) return;

        for (Klas klas : view.getKlassenTable().getItems()) {
            if (klasId.equals(klas.getId())) {
                view.getKlassenTable().getSelectionModel().select(klas);
                view.getKlassenTable().scrollTo(klas);
                toonKlas(klas);
                return;
            }
        }
    }

    private boolean zelfdeKlas(Klas eerste, Klas tweede) {
        if (eerste == null || tweede == null) return false;
        if (eerste.getId() != null && tweede.getId() != null) {
            return eerste.getId().equals(tweede.getId());
        }
        return eerste.equals(tweede);
    }

    private String veiligBericht(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
    }
}

package be.kdg.talenten.view.talentenperiode;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class TalentenPeriodePresenter {
    private final TalentenPeriodeView view;
    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;

    private Schooljaar geselecteerdSchooljaar;
    private TalentenPeriode geselecteerdePeriode;
    private boolean schooljarenWordenGeladen;

    public TalentenPeriodePresenter(ApplicationConfig config, TalentenPeriodeView view, SceneManager sceneManager) {
        if (config == null || view == null || sceneManager == null) {
            throw new IllegalArgumentException("Config, view en sceneManager mogen niet null zijn");
        }

        this.view = view;
        this.schooljaarService = config.getSchooljaarService();
        this.talentenPeriodeService = config.getTalentenPeriodeService();

        new AppNavigator(config, sceneManager).koppelSidebar(view.getSidebar());

        addEventHandlers();
        laadSchooljaren(null);
    }

    private void addEventHandlers() {
        view.getVolgendSchooljaarButton().setOnAction(event -> voegVolgendSchooljaarToe());
        view.getActiefMakenButton().setOnAction(event -> maakGeselecteerdSchooljaarActief());
        view.getNieuwePeriodeButton().setOnAction(event -> startNieuwePeriode());
        view.getAnnulerenPeriodeButton().setOnAction(event -> startNieuwePeriode());
        view.getOpslaanPeriodeButton().setOnAction(event -> slaPeriodeOp());
        view.getVerwijderenPeriodeButton().setOnAction(event -> verwijderPeriode());

        view.getSchooljarenTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> {
            if (!schooljarenWordenGeladen && nieuw != null) {
                toonSchooljaar(nieuw);
            }
        });

        view.getPeriodesTable().getSelectionModel().selectedItemProperty().addListener((observable, oud, nieuw) -> {
            if (nieuw != null) {
                toonPeriode(nieuw);
            }
        });
    }

    private void laadSchooljaren(Long schooljaarIdOmTeSelecteren) {
        try {
            List<Schooljaar> schooljaren = new ArrayList<>(schooljaarService.zoekAlleSchooljaren());
            schooljaren.sort(Comparator.comparing(Schooljaar::getStartDatum).reversed());

            Schooljaar actiefSchooljaar = schooljaarService.zoekActiefSchooljaar().orElse(null);
            view.setActiefSchooljaar(actiefSchooljaar);

            schooljarenWordenGeladen = true;
            view.setSchooljaren(schooljaren);

            if (schooljaren.isEmpty()) {
                schooljarenWordenGeladen = false;
                geselecteerdSchooljaar = null;
                geselecteerdePeriode = null;
                view.setPeriodes(null, List.of());
                view.toonNieuwePeriodeFormulier(null);
                view.setStatus("Er zijn nog geen schooljaren. Voeg eerst een schooljaar toe voordat je talentenperiodes kunt beheren.");
                return;
            }

            Schooljaar teSelecteren = zoekSchooljaar(schooljaren, schooljaarIdOmTeSelecteren);
            if (teSelecteren == null) {
                teSelecteren = actiefSchooljaar != null ? zoekSchooljaar(schooljaren, actiefSchooljaar.getId()) : schooljaren.getFirst();
            }
            if (teSelecteren == null) {
                teSelecteren = schooljaren.getFirst();
            }

            view.getSchooljarenTable().getSelectionModel().select(teSelecteren);
            view.getSchooljarenTable().scrollTo(teSelecteren);
            schooljarenWordenGeladen = false;
            toonSchooljaar(teSelecteren);
        } catch (RuntimeException exception) {
            schooljarenWordenGeladen = false;
            view.toonFout("De schooljaren konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private Schooljaar zoekSchooljaar(List<Schooljaar> schooljaren, Long id) {
        if (id == null) return null;

        return schooljaren.stream()
                .filter(schooljaar -> id.equals(schooljaar.getId()))
                .findFirst()
                .orElse(null);
    }

    private void toonSchooljaar(Schooljaar schooljaar) {
        geselecteerdSchooljaar = schooljaar;
        geselecteerdePeriode = null;
        laadPeriodes();
        startNieuwePeriode();
    }

    private void laadPeriodes() {
        if (geselecteerdSchooljaar == null) {
            view.setPeriodes(null, List.of());
            return;
        }

        try {
            List<TalentenPeriode> periodes = new ArrayList<>(talentenPeriodeService.geefPeriodesVoorSchooljaar(geselecteerdSchooljaar));
            periodes.sort(Comparator.comparing(TalentenPeriode::getStartDatum).thenComparing(TalentenPeriode::getNaam));
            view.setPeriodes(geselecteerdSchooljaar, periodes);
        } catch (RuntimeException exception) {
            view.toonFout("De talentenperiodes konden niet geladen worden: " + veiligBericht(exception));
        }
    }

    private void voegVolgendSchooljaarToe() {
        try {
            Schooljaar nieuwSchooljaar = schooljaarService.voegVolgendSchooljaarToe();
            laadSchooljaren(nieuwSchooljaar.getId());
            view.toonSucces("Schooljaar " + nieuwSchooljaar.getNaam() + " is toegevoegd als gepland schooljaar.");
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void maakGeselecteerdSchooljaarActief() {
        if (geselecteerdSchooljaar == null) {
            view.toonFout("Selecteer eerst een schooljaar.");
            return;
        }
        if (geselecteerdSchooljaar.isActief()) {
            view.setStatus("Schooljaar " + geselecteerdSchooljaar.getNaam() + " is al actief.");
            return;
        }

        String bericht = "Wil je schooljaar " + geselecteerdSchooljaar.getNaam() + " actief maken? Het huidige actieve schooljaar wordt automatisch inactief.";
        if (!view.vraagBevestiging("Schooljaar activeren", bericht)) {
            return;
        }

        try {
            Long schooljaarId = geselecteerdSchooljaar.getId();
            String naam = geselecteerdSchooljaar.getNaam();
            schooljaarService.maakActief(geselecteerdSchooljaar);
            laadSchooljaren(schooljaarId);
            view.toonSucces("Schooljaar " + naam + " is nu actief.");
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void startNieuwePeriode() {
        geselecteerdePeriode = null;
        view.toonNieuwePeriodeFormulier(geselecteerdSchooljaar);
    }

    private void toonPeriode(TalentenPeriode periode) {
        geselecteerdePeriode = periode;
        view.toonWijzigPeriodeFormulier(periode);
    }

    private void slaPeriodeOp() {
        if (geselecteerdSchooljaar == null) {
            view.toonFout("Selecteer eerst een schooljaar.");
            return;
        }

        String naam = view.getNaamField().getText();
        LocalDate startDatum = view.getStartDatumPicker().getValue();
        LocalDate eindDatum = view.getEindDatumPicker().getValue();

        if (startDatum == null) {
            view.toonFout("Selecteer een startdatum.");
            return;
        }
        if (eindDatum == null) {
            view.toonFout("Selecteer een einddatum.");
            return;
        }

        try {
            if (geselecteerdePeriode == null) {
                TalentenPeriode nieuwePeriode = talentenPeriodeService.maakPeriode(naam, startDatum, eindDatum, geselecteerdSchooljaar);
                laadPeriodesEnSelecteer(nieuwePeriode.getId());
                view.toonSucces("Talentenperiode " + nieuwePeriode.getNaam() + " is toegevoegd.");
            } else {
                Long periodeId = geselecteerdePeriode.getId();
                talentenPeriodeService.wijzigPeriode(geselecteerdePeriode, naam, startDatum, eindDatum);
                laadPeriodesEnSelecteer(periodeId);
                view.toonSucces("Talentenperiode " + naam + " is aangepast.");
            }
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private void laadPeriodesEnSelecteer(Long periodeId) {
        laadPeriodes();

        if (periodeId == null) {
            startNieuwePeriode();
            return;
        }

        for (TalentenPeriode periode : view.getPeriodesTable().getItems()) {
            if (periodeId.equals(periode.getId())) {
                view.getPeriodesTable().getSelectionModel().select(periode);
                view.getPeriodesTable().scrollTo(periode);
                toonPeriode(periode);
                return;
            }
        }

        startNieuwePeriode();
    }

    private void verwijderPeriode() {
        if (geselecteerdePeriode == null) {
            view.toonFout("Selecteer eerst een talentenperiode.");
            return;
        }

        String naam = geselecteerdePeriode.getNaam();
        String bericht = "Wil je talentenperiode " + naam + " verwijderen? Dit kan alleen wanneer er nog geen voorkeuren of toewijzingen voor bestaan.";
        if (!view.vraagBevestiging("Talentenperiode verwijderen", bericht)) {
            return;
        }

        try {
            talentenPeriodeService.verwijderPeriode(geselecteerdePeriode);
            geselecteerdePeriode = null;
            laadPeriodes();
            startNieuwePeriode();
            view.toonSucces("Talentenperiode " + naam + " is verwijderd.");
        } catch (RuntimeException exception) {
            view.toonFout(veiligBericht(exception));
        }
    }

    private String veiligBericht(RuntimeException exception) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? exception.getClass().getSimpleName()
                : exception.getMessage();
    }
}

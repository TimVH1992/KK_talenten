package be.kdg.talenten.view.beheer.leerling.historiek;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.leerling.LeerlingHistoriekService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.beheer.leerling.LeerlingenKeuzeView;
import be.kdg.talenten.view.theme.ThemeManager;
import javafx.collections.FXCollections;
import javafx.scene.Scene;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class LeerlingHistoriekPresenter {
    private final LeerlingHistoriekView view;
    private final LeerlingenKeuzeView keuzeView;
    private final SceneManager sceneManager;
    private final ThemeManager themeManager;
    private final Scene scene;
    private final SchooljaarService schooljaarService;
    private final LeerlingService leerlingService;
    private final LeerlingHistoriekService historiekService;
    private List<Leerling> alleLeerlingen = List.of();

    public LeerlingHistoriekPresenter(
            LeerlingHistoriekView view, LeerlingenKeuzeView keuzeView,
            SceneManager sceneManager, ThemeManager themeManager, Scene scene,
            SchooljaarService schooljaarService, LeerlingService leerlingService,
            LeerlingHistoriekService historiekService
    ) {
        if (view == null || keuzeView == null || sceneManager == null || themeManager == null || scene == null
                || schooljaarService == null || leerlingService == null || historiekService == null) {
            throw new IllegalArgumentException("LeerlingHistoriekPresenter kreeg een null-afhankelijkheid");
        }
        this.view = view;
        this.keuzeView = keuzeView;
        this.sceneManager = sceneManager;
        this.themeManager = themeManager;
        this.scene = scene;
        this.schooljaarService = schooljaarService;
        this.leerlingService = leerlingService;
        this.historiekService = historiekService;
        configureer();
        laadLeerlingen();
    }

    private void configureer() {
        view.updateThemeIcon(themeManager.isDark());
        view.getTerugButton().setOnAction(event -> sceneManager.toon(keuzeView));
        view.getThemeButton().setOnAction(event -> {
            themeManager.toggle(scene);
            boolean dark = themeManager.isDark();
            view.updateThemeIcon(dark);
            keuzeView.updateThemeIcon(dark);
        });
        view.getZoekVeld().textProperty().addListener((observable, oud, nieuw) -> filterLeerlingen());
        view.getLeerlingTabel().getSelectionModel().selectedItemProperty()
                .addListener((observable, oud, leerling) -> laadHistoriek(leerling));
    }

    private void laadLeerlingen() {
        try {
            Map<Long, Leerling> uniekOpId = new LinkedHashMap<>();
            for (Schooljaar schooljaar : schooljaarService.zoekAlleSchooljaren()) {
                for (Leerling leerling : leerlingService.geefLeerlingenVoorSchooljaar(schooljaar)) {
                    if (leerling.getId() != null) uniekOpId.putIfAbsent(leerling.getId(), leerling);
                }
            }
            alleLeerlingen = uniekOpId.values().stream()
                    .sorted(Comparator.comparing(Leerling::getAchternaam, String.CASE_INSENSITIVE_ORDER)
                            .thenComparing(Leerling::getVoornaam, String.CASE_INSENSITIVE_ORDER))
                    .toList();
            filterLeerlingen();
        } catch (RuntimeException exception) {
            alleLeerlingen = List.of();
            view.getLeerlingTabel().getItems().clear();
            view.toonStatus(boodschap(exception, "De leerlingen konden niet geladen worden."));
        }
    }

    private void filterLeerlingen() {
        String zoekterm = view.getZoekVeld().getText() == null ? ""
                : view.getZoekVeld().getText().trim().toLowerCase(Locale.ROOT);
        List<Leerling> resultaat = alleLeerlingen.stream()
                .filter(leerling -> zoekterm.isBlank()
                        || leerling.getVoornaam().toLowerCase(Locale.ROOT).contains(zoekterm)
                        || leerling.getAchternaam().toLowerCase(Locale.ROOT).contains(zoekterm)
                        || (leerling.getVoornaam() + " " + leerling.getAchternaam())
                        .toLowerCase(Locale.ROOT).contains(zoekterm))
                .toList();
        view.getLeerlingTabel().setItems(FXCollections.observableArrayList(resultaat));
        view.getHistoriekTabel().getItems().clear();
        view.getHistoriekTitel().setText("Selecteer een leerling");
    }

    private void laadHistoriek(Leerling leerling) {
        if (leerling == null) {
            view.getHistoriekTabel().getItems().clear();
            return;
        }
        try {
            view.getHistoriekTitel().setText("Toewijzingen van " + leerling.getVoornaam() + " " + leerling.getAchternaam());
            view.getHistoriekTabel().setItems(FXCollections.observableArrayList(
                    historiekService.geefAlleToewijzingen(leerling)));
        } catch (RuntimeException exception) {
            view.getHistoriekTabel().getItems().clear();
            view.toonStatus(boodschap(exception, "De toewijzingshistoriek kon niet geladen worden."));
        }
    }

    private String boodschap(RuntimeException exception, String standaard) {
        return exception.getMessage() == null || exception.getMessage().isBlank()
                ? standaard : exception.getMessage();
    }
}

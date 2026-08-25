package be.kdg.talenten.view.leerkracht;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.service.beheer.LeerkrachtService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

public class LeerkrachtPresenter {
    private final LeerkrachtView view;
    private final LeerkrachtService leerkrachtService;

    private Leerkracht geselecteerdeLeerkracht;

    public LeerkrachtPresenter(
            ApplicationConfig config,
            LeerkrachtView view,
            SceneManager sceneManager,
            Runnable terugNaarHoofdmenu
    ) {
        if (
                config == null
                        || view == null
                        || sceneManager == null
                        || terugNaarHoofdmenu == null
        ) {
            throw new IllegalArgumentException(
                    "Config, view, sceneManager en terugactie mogen niet null zijn"
            );
        }

        this.view = view;
        this.leerkrachtService =
                config.getLeerkrachtService();

        new AppNavigator(
                config,
                sceneManager
        ).koppelSidebar(view.getSidebar());

        addEventHandlers();
        laadLeerkrachten();
        startNieuweLeerkracht();
    }

    private void addEventHandlers() {
        view.getNieuweLeerkrachtButton()
                .setOnAction(
                        event -> startNieuweLeerkracht()
                );

        view.getAnnulerenButton()
                .setOnAction(
                        event -> startNieuweLeerkracht()
                );

        view.getOpslaanButton()
                .setOnAction(
                        event -> slaLeerkrachtOp()
                );

        view.getStatusWijzigenButton()
                .setOnAction(
                        event -> wijzigActieveStatus()
                );

        view.getLeerkrachtenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oudeLeerkracht,
                                nieuweLeerkracht
                        ) -> {
                            if (nieuweLeerkracht != null) {
                                toonLeerkracht(
                                        nieuweLeerkracht
                                );
                            }
                        }
                );
    }

    private void laadLeerkrachten() {
        try {
            view.setLeerkrachten(
                    leerkrachtService
                            .geefAlleLeerkrachten()
            );
        } catch (RuntimeException exception) {
            view.toonFout(
                    "De leerkrachten konden niet geladen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void startNieuweLeerkracht() {
        geselecteerdeLeerkracht = null;
        view.toonNieuweLeerkrachtFormulier();
    }

    private void toonLeerkracht(
            Leerkracht leerkracht
    ) {
        geselecteerdeLeerkracht = leerkracht;
        view.toonLeerkrachtFormulier(leerkracht);
    }

    private void slaLeerkrachtOp() {
        String voornaam =
                view.getVoornaamField().getText();

        String achternaam =
                view.getAchternaamField().getText();

        try {
            if (geselecteerdeLeerkracht == null) {
                maakNieuweLeerkracht(
                        voornaam,
                        achternaam
                );
            } else {
                wijzigBestaandeLeerkracht(
                        voornaam,
                        achternaam
                );
            }

        } catch (RuntimeException exception) {
            view.toonFout(
                    veiligBericht(exception)
            );
        }
    }

    private void maakNieuweLeerkracht(
            String voornaam,
            String achternaam
    ) {
        Leerkracht nieuweLeerkracht =
                leerkrachtService.maakLeerkracht(
                        voornaam,
                        achternaam
                );

        laadLeerkrachten();
        startNieuweLeerkracht();

        view.toonSucces(
                "Leerkracht "
                        + nieuweLeerkracht.getVoornaam()
                        + " "
                        + nieuweLeerkracht.getAchternaam()
                        + " is toegevoegd."
        );
    }

    private void wijzigBestaandeLeerkracht(
            String voornaam,
            String achternaam
    ) {
        String oudeNaam =
                geselecteerdeLeerkracht.getVoornaam()
                        + " "
                        + geselecteerdeLeerkracht.getAchternaam();

        leerkrachtService.wijzigLeerkracht(
                geselecteerdeLeerkracht,
                voornaam,
                achternaam
        );

        laadLeerkrachten();
        startNieuweLeerkracht();

        view.toonSucces(
                "Leerkracht "
                        + oudeNaam
                        + " is aangepast."
        );
    }

    private void wijzigActieveStatus() {
        if (geselecteerdeLeerkracht == null) {
            return;
        }

        try {
            String volledigeNaam =
                    geselecteerdeLeerkracht.getVoornaam()
                            + " "
                            + geselecteerdeLeerkracht.getAchternaam();

            boolean wasActief =
                    geselecteerdeLeerkracht.isActief();

            if (wasActief) {
                leerkrachtService.deactiveerLeerkracht(
                        geselecteerdeLeerkracht
                );
            } else {
                leerkrachtService.activeerLeerkracht(
                        geselecteerdeLeerkracht
                );
            }

            laadLeerkrachten();
            startNieuweLeerkracht();

            if (wasActief) {
                view.toonSucces(
                        "Leerkracht "
                                + volledigeNaam
                                + " is op niet-actief gezet."
                );
            } else {
                view.toonSucces(
                        "Leerkracht "
                                + volledigeNaam
                                + " is opnieuw actief."
                );
            }

        } catch (RuntimeException exception) {
            view.toonFout(
                    veiligBericht(exception)
            );
        }
    }

    private String veiligBericht(
            RuntimeException exception
    ) {
        if (
                exception.getMessage() == null
                        || exception.getMessage().isBlank()
        ) {
            return exception
                    .getClass()
                    .getSimpleName();
        }

        return exception.getMessage();
    }
}
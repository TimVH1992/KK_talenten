package be.kdg.talenten.view.leerling;

import be.kdg.talenten.config.ApplicationConfig;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.LeerlingService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.view.SceneManager;
import be.kdg.talenten.view.navigation.AppNavigator;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

public class LeerlingPresenter {

    private final LeerlingView view;

    private final LeerlingService leerlingService;
    private final SchooljaarService schooljaarService;
    private final KlasService klasService;

    private Leerling geselecteerdeLeerling;
    private boolean schooljarenWordenGeladen;

    public LeerlingPresenter(
            ApplicationConfig config,
            LeerlingView view,
            SceneManager sceneManager
    ) {
        if (
                config == null
                        || view == null
                        || sceneManager == null
        ) {
            throw new IllegalArgumentException(
                    "Config, view en sceneManager mogen niet null zijn"
            );
        }

        this.view = view;

        this.leerlingService =
                config.getLeerlingService();

        this.schooljaarService =
                config.getSchooljaarService();

        this.klasService =
                config.getKlasService();

        new AppNavigator(
                config,
                sceneManager
        ).koppelSidebar(
                view.getSidebar()
        );

        addEventHandlers();
        laadSchooljaren();
    }

    private void addEventHandlers() {
        view.getNieuweLeerlingButton()
                .setOnAction(
                        event ->
                                startNieuweLeerling()
                );

        view.getAnnulerenButton()
                .setOnAction(
                        event ->
                                startNieuweLeerling()
                );

        view.getOpslaanButton()
                .setOnAction(
                        event ->
                                slaLeerlingOp()
                );

        view.getActiefButton()
                .setOnAction(
                        event ->
                                wijzigActiefStatus()
                );

        view.getSchooljaarComboBox()
                .setOnAction(event -> {
                    if (!schooljarenWordenGeladen) {
                        schooljaarGewijzigd();
                    }
                });

        view.getLeerlingenTable()
                .getSelectionModel()
                .selectedItemProperty()
                .addListener(
                        (
                                observable,
                                oudeLeerling,
                                nieuweLeerling
                        ) -> {
                            if (nieuweLeerling != null) {
                                toonLeerling(
                                        nieuweLeerling
                                );
                            }
                        }
                );
    }

    private void laadSchooljaren() {
        try {
            schooljarenWordenGeladen =
                    true;

            List<Schooljaar> schooljaren =
                    schooljaarService
                            .zoekSelecteerbareSchooljaren();

            view.setSchooljaren(
                    schooljaren
            );

            if (schooljaren.isEmpty()) {
                view.setKlassen(
                        List.of()
                );

                view.setLeerlingen(
                        List.of()
                );

                view.setStatus(
                        "Er zijn nog geen selecteerbare schooljaren."
                );

                return;
            }

            Schooljaar standaardSchooljaar =
                    schooljaarService
                            .zoekActiefSchooljaar()
                            .filter(
                                    schooljaren::contains
                            )
                            .orElse(
                                    schooljaren.getFirst()
                            );

            view.getSchooljaarComboBox()
                    .getSelectionModel()
                    .select(
                            standaardSchooljaar
                    );

            laadGegevensVoorSchooljaar(
                    standaardSchooljaar
            );

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De leerlingen konden niet geladen worden: "
                            + veiligBericht(exception)
            );

        } finally {
            schooljarenWordenGeladen =
                    false;
        }
    }

    private void schooljaarGewijzigd() {
        Schooljaar schooljaar =
                view.getSchooljaarComboBox()
                        .getValue();

        if (schooljaar == null) {
            view.setKlassen(
                    List.of()
            );

            view.setLeerlingen(
                    List.of()
            );

            view.setStatus(
                    "Selecteer eerst een schooljaar."
            );

            return;
        }

        laadGegevensVoorSchooljaar(
                schooljaar
        );
    }

    private void laadGegevensVoorSchooljaar(
            Schooljaar schooljaar
    ) {
        try {
            List<Klas> klassen =
                    klasService
                            .geefAlleKlassen()
                            .stream()
                            .filter(
                                    klas ->
                                            klas.getSchooljaar()
                                                    .equals(schooljaar)
                            )
                            .sorted(
                                    Comparator.comparing(
                                            Klas::getNaam
                                    )
                            )
                            .toList();

            view.setKlassen(
                    klassen
            );

            view.setLeerlingen(
                    leerlingService
                            .geefLeerlingenVoorSchooljaar(
                                    schooljaar
                            )
            );

            startNieuweLeerling();

            if (klassen.isEmpty()) {
                view.setStatus(
                        "Voor schooljaar "
                                + schooljaar.getNaam()
                                + " zijn nog geen klassen opgeslagen."
                );
            }

        } catch (RuntimeException exception) {
            view.toonFout(
                    "De gegevens voor schooljaar "
                            + schooljaar.getNaam()
                            + " konden niet geladen worden: "
                            + veiligBericht(exception)
            );
        }
    }

    private void startNieuweLeerling() {
        geselecteerdeLeerling =
                null;

        view.toonNieuweLeerlingFormulier();
    }

    private void toonLeerling(
            Leerling leerling
    ) {
        geselecteerdeLeerling =
                leerling;

        view.toonLeerlingFormulier(
                leerling
        );
    }

    private void slaLeerlingOp() {
        Schooljaar schooljaar =
                view.getSchooljaarComboBox()
                        .getValue();

        Klas klas =
                view.getKlasComboBox()
                        .getValue();

        String voornaam =
                view.getVoornaamField()
                        .getText();

        String achternaam =
                view.getAchternaamField()
                        .getText();

        if (schooljaar == null) {
            view.toonFout(
                    "Selecteer eerst een schooljaar."
            );

            return;
        }

        if (klas == null) {
            view.toonFout(
                    "Selecteer een klas voor de leerling."
            );

            return;
        }

        try {
            if (geselecteerdeLeerling == null) {
                maakNieuweLeerling(
                        schooljaar,
                        klas,
                        voornaam,
                        achternaam
                );

            } else {
                wijzigBestaandeLeerling(
                        schooljaar,
                        klas,
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

    private void maakNieuweLeerling(
            Schooljaar schooljaar,
            Klas klas,
            String voornaam,
            String achternaam
    ) {
        Leerling nieuweLeerling =
                leerlingService.maakLeerling(
                        voornaam,
                        achternaam,
                        klas
                );

        herlaadLeerlingen(
                schooljaar
        );

        startNieuweLeerling();

        view.toonSucces(
                "Leerling "
                        + nieuweLeerling
                        + " is toegevoegd aan "
                        + klas.getNaam()
                        + "."
        );
    }

    private void wijzigBestaandeLeerling(
            Schooljaar schooljaar,
            Klas klas,
            String voornaam,
            String achternaam
    ) {
        String oudeNaam =
                geselecteerdeLeerling.toString();

        Klas oudeKlas =
                geselecteerdeLeerling.getKlas();

        boolean klasGewijzigd =
                !oudeKlas.equals(klas);

        leerlingService.wijzigLeerling(
                geselecteerdeLeerling,
                voornaam,
                achternaam
        );

        if (klasGewijzigd) {
            leerlingService.wijzigKlas(
                    geselecteerdeLeerling,
                    klas,
                    LocalDate.now()
            );
        }

        herlaadLeerlingen(
                schooljaar
        );

        startNieuweLeerling();

        if (klasGewijzigd) {
            view.toonSucces(
                    "Leerling "
                            + oudeNaam
                            + " is aangepast en verplaatst van "
                            + oudeKlas.getNaam()
                            + " naar "
                            + klas.getNaam()
                            + "."
            );

        } else {
            view.toonSucces(
                    "Leerling "
                            + oudeNaam
                            + " is aangepast."
            );
        }
    }

    private void wijzigActiefStatus() {
        if (geselecteerdeLeerling == null) {
            return;
        }

        Schooljaar schooljaar =
                view.getSchooljaarComboBox()
                        .getValue();

        if (schooljaar == null) {
            view.toonFout(
                    "Selecteer eerst een schooljaar."
            );

            return;
        }

        try {
            boolean wasActief =
                    geselecteerdeLeerling.isActief();

            String naam =
                    geselecteerdeLeerling.toString();

            if (wasActief) {
                leerlingService
                        .deactiveerLeerling(
                                geselecteerdeLeerling
                        );

            } else {
                leerlingService
                        .activeerLeerling(
                                geselecteerdeLeerling
                        );
            }

            herlaadLeerlingen(
                    schooljaar
            );

            startNieuweLeerling();

            if (wasActief) {
                view.toonSucces(
                        "Leerling "
                                + naam
                                + " is op niet-actief gezet."
                );

            } else {
                view.toonSucces(
                        "Leerling "
                                + naam
                                + " is opnieuw actief."
                );
            }

        } catch (RuntimeException exception) {
            view.toonFout(
                    veiligBericht(exception)
            );
        }
    }

    private void herlaadLeerlingen(
            Schooljaar schooljaar
    ) {
        view.setLeerlingen(
                leerlingService
                        .geefLeerlingenVoorSchooljaar(
                                schooljaar
                        )
        );
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
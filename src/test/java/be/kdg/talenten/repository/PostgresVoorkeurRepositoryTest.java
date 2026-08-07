package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.postgres.*;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

public class PostgresVoorkeurRepositoryTest {

    private Schooljaar schooljaar2025_2026;
    private Schooljaar schooljaar2026_2027;

    private record TestData(
            Klas klas,
            Leerling leerling,
            Talent talent,
            TalentenPeriode periode,
            Leerkracht leerkracht,
            IngerichtTalent ingerichtTalent,
            Voorkeur voorkeur
    ) {}

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             Statement statement = connection.createStatement()) {

            statement.executeUpdate("""
                    TRUNCATE TABLE
                        ingericht_talent_leerkrachten,
                        voorkeuren,
                        toewijzingen,
                        leerlingen,
                        ingerichte_talenten,
                        leerkrachten,
                        talenten,
                        talenten_periodes,
                        schooljaren,
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }

        PostgresSchooljaarRepository schooljaarRepository =
                new PostgresSchooljaarRepository();

        schooljaar2025_2026 = schooljaarRepository.save(
                new Schooljaar(
                        "2025-2026",
                        LocalDate.of(2025, 7, 1),
                        LocalDate.of(2026, 6, 30)
                )
        );

        schooljaar2026_2027 = schooljaarRepository.save(
                new Schooljaar(
                        "2026-2027",
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2027, 6, 30),
                        true
                )
        );
    }

    @Test
    void saveSlaatVoorkeurOp() {
        // ARRANGE
        TestData data = initialiseTestData();

        VoorkeurRepository repository =
                new PostgresVoorkeurRepository();

        // ACT
        Voorkeur opgeslagenVoorkeur =
                repository.save(data.voorkeur());

        // ASSERT
        Assertions.assertNotNull(opgeslagenVoorkeur);
        Assertions.assertEquals(1, opgeslagenVoorkeur.getId());
    }

    @Test
    void zoekVoorPeriodeGeeftAlleenVoorkeurenVanGevraagdePeriodeTerug() {
        // ARRANGE
        KlasRepository klasRepository =
                new PostgresKlasRepository();

        LeerlingRepository leerlingRepository =
                new PostgresLeerlingRepository();

        TalentRepository talentRepository =
                new PostgresTalentRepository();

        TalentenPeriodeRepository periodeRepository =
                new PostgresTalentenPeriodeRepository();

        LeerkrachtRepository leerkrachtRepository =
                new PostgresLeerkrachtRepository();

        IngerichtTalentRepository ingerichtTalentRepository =
                new PostgresIngerichtTalentRepository();

        VoorkeurRepository voorkeurRepository =
                new PostgresVoorkeurRepository();

        Klas klas1AA = klasRepository.save(
                new Klas(
                        "1AA",
                        schooljaar2026_2027,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        Leerling tim = leerlingRepository.save(
                new Leerling(
                        "Tim",
                        "Van Herreweghe",
                        klas1AA
                )
        );

        TalentenPeriode herfst = periodeRepository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 10, 31),
                        schooljaar2026_2027
                )
        );

        TalentenPeriode winter = periodeRepository.save(
                new TalentenPeriode(
                        "Winter",
                        LocalDate.of(2026, 11, 1),
                        LocalDate.of(2026, 12, 20),
                        schooljaar2026_2027
                )
        );

        Talent schaken = talentRepository.save(
                new Talent(
                        "Schaken",
                        "Leren schaken"
                )
        );

        Talent voetbal = talentRepository.save(
                new Talent(
                        "Voetbal",
                        "Voetbaltraining"
                )
        );

        Leerkracht tom = leerkrachtRepository.save(
                new Leerkracht(
                        "Tom",
                        "Laforce"
                )
        );

        IngerichtTalent schakenHerfst =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                schaken,
                                herfst,
                                10,
                                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                                List.of(tom)
                        )
                );

        IngerichtTalent voetbalHerfst =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                voetbal,
                                herfst,
                                10,
                                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                                List.of(tom)
                        )
                );

        IngerichtTalent schakenWinter =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                schaken,
                                winter,
                                10,
                                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                                List.of(tom)
                        )
                );

        Voorkeur opgeslagenSchakenHerfst =
                voorkeurRepository.save(
                        new Voorkeur(
                                tim,
                                herfst,
                                schakenHerfst,
                                1
                        )
                );

        Voorkeur opgeslagenVoetbalHerfst =
                voorkeurRepository.save(
                        new Voorkeur(
                                tim,
                                herfst,
                                voetbalHerfst,
                                2
                        )
                );

        voorkeurRepository.save(
                new Voorkeur(
                        tim,
                        winter,
                        schakenWinter,
                        1
                )
        );

        // ACT
        List<Voorkeur> resultaat =
                voorkeurRepository.zoekVoorPeriode(herfst);

        // ASSERT
        Assertions.assertEquals(2, resultaat.size());

        Voorkeur eersteVoorkeur =
                resultaat.getFirst();

        Voorkeur tweedeVoorkeur =
                resultaat.get(1);

        Assertions.assertEquals(
                opgeslagenSchakenHerfst.getId(),
                eersteVoorkeur.getId()
        );

        Assertions.assertEquals(
                1,
                eersteVoorkeur.getVoorkeurNummer()
        );

        Assertions.assertEquals(
                tim.getId(),
                eersteVoorkeur.getLeerling().getId()
        );

        Assertions.assertEquals(
                herfst.getId(),
                eersteVoorkeur.getTalentenPeriode().getId()
        );

        Assertions.assertEquals(
                schakenHerfst.getId(),
                eersteVoorkeur.getIngerichtTalent().getId()
        );

        Assertions.assertEquals(
                opgeslagenVoetbalHerfst.getId(),
                tweedeVoorkeur.getId()
        );

        Assertions.assertEquals(
                2,
                tweedeVoorkeur.getVoorkeurNummer()
        );

        Assertions.assertEquals(
                tim.getId(),
                tweedeVoorkeur.getLeerling().getId()
        );

        Assertions.assertEquals(
                herfst.getId(),
                tweedeVoorkeur.getTalentenPeriode().getId()
        );

        Assertions.assertEquals(
                voetbalHerfst.getId(),
                tweedeVoorkeur.getIngerichtTalent().getId()
        );

        Assertions.assertTrue(
                resultaat.stream()
                        .allMatch(voorkeur ->
                                voorkeur.getTalentenPeriode()
                                        .getId()
                                        .equals(herfst.getId())
                        )
        );

        Assertions.assertFalse(
                resultaat.stream()
                        .anyMatch(voorkeur ->
                                voorkeur.getIngerichtTalent()
                                        .getId()
                                        .equals(schakenWinter.getId())
                        )
        );
    }

    private TestData initialiseTestData() {
        KlasRepository klasRepository =
                new PostgresKlasRepository();

        LeerlingRepository leerlingRepository =
                new PostgresLeerlingRepository();

        TalentRepository talentRepository =
                new PostgresTalentRepository();

        TalentenPeriodeRepository periodeRepository =
                new PostgresTalentenPeriodeRepository();

        LeerkrachtRepository leerkrachtRepository =
                new PostgresLeerkrachtRepository();

        IngerichtTalentRepository ingerichtTalentRepository =
                new PostgresIngerichtTalentRepository();

        Klas klas = klasRepository.save(
                new Klas(
                        "1AA",
                        schooljaar2026_2027,
                        1,
                        Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB
                )
        );

        Leerling leerling = leerlingRepository.save(
                new Leerling(
                        "Tim",
                        "VH",
                        klas
                )
        );

        TalentenPeriode periode = periodeRepository.save(
                new TalentenPeriode(
                        "Herfst",
                        LocalDate.of(2026, 9, 1),
                        LocalDate.of(2026, 12, 21),
                        schooljaar2026_2027
                )
        );

        Talent talent = talentRepository.save(
                new Talent(
                        "Voetbal",
                        "Balsport"
                )
        );

        Leerkracht leerkracht = leerkrachtRepository.save(
                new Leerkracht(
                        "Tom",
                        "Laforce"
                )
        );

        IngerichtTalent ingerichtTalent =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                talent,
                                periode,
                                10,
                                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB,
                                List.of(leerkracht)
                        )
                );

        Voorkeur voorkeur = new Voorkeur(
                leerling,
                periode,
                ingerichtTalent,
                1
        );

        return new TestData(
                klas,
                leerling,
                talent,
                periode,
                leerkracht,
                ingerichtTalent,
                voorkeur
        );
    }
}
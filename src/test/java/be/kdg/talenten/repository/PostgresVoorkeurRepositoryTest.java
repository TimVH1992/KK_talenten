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
                        klassen
                    RESTART IDENTITY CASCADE
                    """);
        }
    }

    @Test
    void saveSlaatVoorkeurOp(){
        TestData data = initialiseTestData();

        VoorkeurRepository repository = new PostgresVoorkeurRepository();

        Voorkeur opgeslagenVoorkeur = repository.save(data.voorkeur());

        Assertions.assertNotNull(opgeslagenVoorkeur);
        Assertions.assertEquals(1, opgeslagenVoorkeur.getId());
    }

    private TestData initialiseTestData() {
        KlasRepository klasRepository = new PostgresKlasRepository();
        LeerlingRepository leerlingRepository = new PostgresLeerlingRepository();
        TalentRepository talentRepository = new PostgresTalentRepository();
        TalentenPeriodeRepository periodeRepository = new PostgresTalentenPeriodeRepository();
        LeerkrachtRepository leerkrachtRepository = new PostgresLeerkrachtRepository();
        IngerichtTalentRepository ingerichtTalentRepository = new PostgresIngerichtTalentRepository();

        Klas klas = klasRepository.save(new Klas("1AA", "2026-2027", 1, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB));
        Leerling leerling = leerlingRepository.save(new Leerling("Tim", "VH", klas));
        TalentenPeriode periode = periodeRepository.save(new TalentenPeriode("Herfst", LocalDate.of(2026, 9, 1), LocalDate.of(2026, 12, 21)));
        Talent talent = talentRepository.save(new Talent("Voetbal", "Balsport"));
        Leerkracht leerkracht = leerkrachtRepository.save(new Leerkracht("Tom", "Laforce"));
        IngerichtTalent ingerichtTalent = ingerichtTalentRepository.save(new IngerichtTalent(talent, periode, 10, Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB, List.of(leerkracht)));
        Voorkeur voorkeur = new Voorkeur(leerling, periode, ingerichtTalent, 1);

        return new TestData(klas, leerling, talent, periode, leerkracht, ingerichtTalent, voorkeur);
    }
}

package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresVoorkeurRepository implements VoorkeurRepository {
    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;

    public PostgresVoorkeurRepository() {
        this(new PostgresLeerlingRepository(), new PostgresIngerichtTalentRepository());
    }

    public PostgresVoorkeurRepository(LeerlingRepository leerlingRepository, IngerichtTalentRepository ingerichtTalentRepository) {
        if (leerlingRepository == null || ingerichtTalentRepository == null) {
            throw new IllegalArgumentException("Repositories mogen niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
    }

    @Override
    public Voorkeur save(Voorkeur voorkeur) {
        if (voorkeur == null) {
            throw new IllegalArgumentException("Voorkeur mag niet null zijn");
        }

        if (voorkeur.getLeerling().getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }

        if (voorkeur.getTalentenPeriode().getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        if (voorkeur.getIngerichtTalent().getId() == null) {
            throw new IllegalArgumentException("Het ingerichte talent moet eerst opgeslagen zijn");
        }

        String sql = """
                INSERT INTO voorkeuren(
                voorkeur_nummer,
                leerling_id,
                talenten_periode_id,
                ingericht_talent_id
                )
                VALUES (?,?,?,?)
                RETURNING voorkeur_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setInt(1, voorkeur.getVoorkeurNummer());
            statement.setLong(2, voorkeur.getLeerling().getId());
            statement.setLong(3, voorkeur.getTalentenPeriode().getId());
            statement.setLong(4, voorkeur.getIngerichtTalent().getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen voorkeur_id terug");
                }
                long gegenereerdId = resultSet.getLong("voorkeur_id");
                return new Voorkeur(gegenereerdId, voorkeur.getLeerling(), voorkeur.getTalentenPeriode(), voorkeur.getIngerichtTalent(), voorkeur.getVoorkeurNummer());
            }

        } catch (SQLException e) {
            throw new IllegalStateException("De voorkeur kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<Voorkeur> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        if (periode.getId() == null) {
            throw new IllegalArgumentException("De periode moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT voorkeur_id, voorkeur_nummer,leerling_id, talenten_periode_id, ingericht_talent_id
                FROM voorkeuren
                WHERE talenten_periode_id = ?
                ORDER BY leerling_id, voorkeur_nummer
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Voorkeur> voorkeuren = new ArrayList<>();
                while (resultSet.next()) {
                    Voorkeur voorkeur = new Voorkeur(
                            resultSet.getLong("voorkeur_id"),
                            leerlingRepository.zoekOpId(resultSet.getLong("leerling_id")),
                            periode,
                            ingerichtTalentRepository.zoekOpId(resultSet.getLong("ingericht_talent_id")),
                            resultSet.getInt("voorkeur_nummer")
                    );
                    voorkeuren.add(voorkeur);
                }
                return voorkeuren;
            }
        } catch (SQLException e){
            throw new IllegalStateException("De voorkeuren konden niet opgehaald worden", e);
        }
    }
}

package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.KlasRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresVoorkeurRepository implements VoorkeurRepository {
    private LeerlingRepository leerlingRepository;
    private IngerichtTalentRepository ingerichtTalentRepository;

    public PostgresVoorkeurRepository(){
        leerlingRepository = new PostgresLeerlingRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();
    }

    @Override
    public Voorkeur save(Voorkeur voorkeur) {
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

            statement.setLong(1, voorkeur.getVoorkeurNummer());
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


        String sql = """
                SELECT voorkeur_id, voorkeur_nummer,leerling_id, talenten_periode_id, ingericht_talent_id
                FROM voorkeuren
                WHERE talenten_periode_id = ?
                ORDER BY ingericht_talent_id, voorkeur_nummer
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setLong(1, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Voorkeur> voorkeuren = new ArrayList<>();
                while (resultSet.next()) {
                    Voorkeur voorkeur = new Voorkeur(
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

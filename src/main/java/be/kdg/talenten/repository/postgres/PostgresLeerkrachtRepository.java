package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.repository.LeerkrachtRepository;

import javax.xml.crypto.Data;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresLeerkrachtRepository implements LeerkrachtRepository {
    @Override
    public Leerkracht save(Leerkracht leerkracht) {
        if (leerkracht == null){
        throw new IllegalArgumentException("Leerkracht mag niet null zijn");
        }
        String sql = """
                INSERT INTO leerkrachten(
                voornaam,
                achternaam
                )
                VALUES(?,?)
                RETURNING leerkracht_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leerkracht.getVoornaam());
            statement.setString(2, leerkracht.getAchternaam());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen leerkracht_id terug");
                }

                long gegenereerdId = resultSet.getLong("leerkracht_id");

                return new Leerkracht(gegenereerdId, leerkracht.getVoornaam(), leerkracht.getAchternaam());
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De leerkracht kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<Leerkracht> zoekAlle() {
        String sql = """
                SELECT leerkracht_id, voornaam, achternaam
                FROM leerkrachten
                ORDER BY achternaam, voornaam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            try (ResultSet resultSet = statement.executeQuery()) {
                List<Leerkracht> leerkrachten = new ArrayList<>();

                while (resultSet.next()) {
                    Leerkracht leerkracht = new Leerkracht(
                            resultSet.getLong("leerkracht_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam")
                    );

                    leerkrachten.add(leerkracht);
                }
                return leerkrachten;
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De leerkrachten konden niet opgehaald worden", e);
        }
    }

    @Override
    public Leerkracht zoekOpId(long id) {
        if (id <= 0) {
            throw new IllegalArgumentException("Id moet groter zijn dan 0");
        }

        String sql = """
                SELECT voornaam, achternaam, actief
                FROM leerkrachten
                WHERE leerkracht_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)){

            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    throw new IllegalStateException("Geen leerkracht gevonden met id: " + id);
                }
                 Leerkracht huidigeLeerkracht = new Leerkracht (id,
                        resultSet.getString("voornaam"),
                        resultSet.getString("achternaam"));
                if (huidigeLeerkracht.getActief() != resultSet.getBoolean("actief")){
                    huidigeLeerkracht.setInactief();
                }
                return huidigeLeerkracht;
            }

        } catch (SQLException e){
            throw new IllegalStateException("De leerkracht met id " + id +" kon niet opgehaald worden", e);
        }
    }

    @Override
    public void update(Leerkracht leerkracht) {
        if (leerkracht == null){
            throw new IllegalArgumentException("Leerkracht mag niet null zijn");
        }
        if (leerkracht.getId() <= 0){
            throw new IllegalStateException("De leerkracht heeft geen bestaand id");
        }

        String sql = """
                UPDATE leerkrachten
                SET voornaam = ?,
                achternaam = ?
                WHERE leerkracht_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
            PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, leerkracht.getVoornaam());
            statement.setString(2, leerkracht.getAchternaam());
            statement.setLong(3, leerkracht.getId());

                int aantalAangepasteRijen = statement.executeUpdate();

                if (aantalAangepasteRijen == 0){
                    throw new IllegalStateException("Geen leerkracht gevonden met id: " + leerkracht.getId());
                }
        }  catch (SQLException e){
            throw new IllegalStateException("De update van leerkracht " + leerkracht.getVoornaam() + " " + leerkracht.getAchternaam() + "is niet doorgevoerd. ", e);
    }
    }
}


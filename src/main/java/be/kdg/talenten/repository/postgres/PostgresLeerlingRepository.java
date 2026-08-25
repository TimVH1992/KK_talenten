package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresLeerlingRepository implements LeerlingRepository {

    @Override
    public List<Leerling> zoekVoorKlas(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }
        if (klas.getId() == null) {
            throw new IllegalArgumentException("De klas moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT leerling_id, voornaam, achternaam, actief
                FROM leerlingen
                WHERE klas_id = ?
                ORDER BY leerling_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, klas.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Leerling> leerlingenPerKlas = new ArrayList<>();

                while (resultSet.next()) {
                    Leerling leerling = new Leerling(
                            resultSet.getLong("leerling_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam"),
                            klas,
                            resultSet.getBoolean("actief")
                    );

                    leerlingenPerKlas.add(leerling);
                }

                return leerlingenPerKlas;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerlingen voor deze klas konden niet opgehaald worden.",
                    e
            );
        }
    }

    @Override
    public Leerling save(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (leerling.getKlas() == null || leerling.getKlas().getId() == null) {
            throw new IllegalArgumentException("De klas van de leerling moet eerst opgeslagen zijn");
        }

        String sql = """
                INSERT INTO leerlingen (
                    voornaam,
                    achternaam,
                    klas_id,
                    actief
                )
                VALUES (?, ?, ?, ?)
                RETURNING leerling_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leerling.getVoornaam());
            statement.setString(2, leerling.getAchternaam());
            statement.setLong(3, leerling.getKlas().getId());
            statement.setBoolean(4, leerling.isActief());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen leerling_id terug.");
                }

                long gegenereerdId = resultSet.getLong("leerling_id");

                return new Leerling(
                        gegenereerdId,
                        leerling.getVoornaam(),
                        leerling.getAchternaam(),
                        leerling.getKlas(),
                        leerling.isActief()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerling kon niet opgeslagen worden.",
                    e
            );
        }
    }

    @Override
    public Leerling zoekOpId(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("id moet groter zijn dan 0");
        }

        String sql = """
                SELECT
                    l.voornaam,
                    l.achternaam,
                    l.actief,

                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep,

                    s.schooljaar_id,
                    s.naam AS schooljaar_naam,
                    s.startdatum AS schooljaar_startdatum,
                    s.einddatum AS schooljaar_einddatum,
                    s.actief AS schooljaar_actief
                FROM leerlingen l
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                JOIN schooljaren s
                    ON s.naam = k.schooljaar
                WHERE l.leerling_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, id);

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("Geen leerling gevonden met id: " + id);
                }

                Schooljaar schooljaar = maakSchooljaar(resultSet);

                Klas klas = new Klas(
                        resultSet.getLong("klas_id"),
                        resultSet.getString("klas_naam"),
                        schooljaar,
                        resultSet.getInt("leerjaar"),
                        Doelgroep.valueOf(resultSet.getString("doelgroep"))
                );

                return new Leerling(
                        id,
                        resultSet.getString("voornaam"),
                        resultSet.getString("achternaam"),
                        klas,
                        resultSet.getBoolean("actief")
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerling kan niet op id gevonden worden",
                    e
            );
        }
    }

    @Override
    public List<Leerling> zoekVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Het schooljaar mag niet null zijn");
        }
        if (schooljaar.getId() == null) {
            throw new IllegalArgumentException("Het schooljaar moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT
                    l.leerling_id,
                    l.voornaam,
                    l.achternaam,
                    l.actief,
                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep
                FROM leerlingen l
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                WHERE k.schooljaar = ?
                ORDER BY k.klas_naam, l.achternaam, l.voornaam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, schooljaar.getNaam());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Leerling> leerlingen = new ArrayList<>();

                while (resultSet.next()) {
                    Klas klas = new Klas(
                            resultSet.getLong("klas_id"),
                            resultSet.getString("klas_naam"),
                            schooljaar,
                            resultSet.getInt("leerjaar"),
                            Doelgroep.valueOf(resultSet.getString("doelgroep"))
                    );

                    Leerling leerling = new Leerling(
                            resultSet.getLong("leerling_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam"),
                            klas,
                            resultSet.getBoolean("actief")
                    );

                    leerlingen.add(leerling);
                }

                return leerlingen;
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerlingen van het schooljaar konden niet opgehaald worden",
                    e
            );
        }
    }

    private Schooljaar maakSchooljaar(ResultSet resultSet) throws SQLException {
        return new Schooljaar(
                resultSet.getLong("schooljaar_id"),
                resultSet.getString("schooljaar_naam"),
                resultSet.getDate("schooljaar_startdatum").toLocalDate(),
                resultSet.getDate("schooljaar_einddatum").toLocalDate(),
                resultSet.getBoolean("schooljaar_actief")
        );
    }

    @Override
    public void update(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (leerling.getId() == null || leerling.getId() < 1) {
            throw new IllegalStateException("De leerling heeft geen bestaand id");
        }
        if (leerling.getKlas() == null || leerling.getKlas().getId() == null) {
            throw new IllegalStateException("De klas van de leerling moet opgeslagen zijn");
        }

        String sql = """
                UPDATE leerlingen
                SET voornaam = ?,
                    achternaam = ?,
                    klas_id = ?,
                    actief = ?
                WHERE leerling_id = ?
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, leerling.getVoornaam());
            statement.setString(2, leerling.getAchternaam());
            statement.setLong(3, leerling.getKlas().getId());
            statement.setBoolean(4, leerling.isActief());
            statement.setLong(5, leerling.getId());

            int aantalAangepasteRijen = statement.executeUpdate();

            if (aantalAangepasteRijen == 0) {
                throw new IllegalStateException(
                        "Geen leerling gevonden met id: " + leerling.getId()
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De leerling " + leerling + " kon niet aangepast worden",
                    e
            );
        }
    }
}
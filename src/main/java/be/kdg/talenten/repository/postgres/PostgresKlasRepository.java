package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.KlasRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresKlasRepository implements KlasRepository {

    @Override
    public Klas save(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }
        if (klas.getSchooljaar() == null) {
            throw new IllegalArgumentException("Het schooljaar van de klas mag niet null zijn");
        }
        if (klas.getSchooljaar().getId() == null) {
            throw new IllegalArgumentException("Het schooljaar moet eerst opgeslagen zijn");
        }

        String sql = """
                INSERT INTO klassen(
                    klas_naam,
                    schooljaar,
                    leerjaar,
                    doelgroep
                )
                VALUES (?, ?, ?, ?)
                RETURNING klas_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, klas.getNaam());
            statement.setString(2, klas.getSchooljaar().getNaam());
            statement.setInt(3, klas.getLeerjaar());
            statement.setString(4, klas.getDoelgroep().name());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen klas_id terug");
                }

                long gegenereerdId = resultSet.getLong("klas_id");

                return new Klas(
                        gegenereerdId,
                        klas.getNaam(),
                        klas.getSchooljaar(),
                        klas.getLeerjaar(),
                        klas.getDoelgroep()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException("De klas kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<Klas> zoekAlle() {
        String sql = """
                SELECT
                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep,
                    s.schooljaar_id,
                    s.naam AS schooljaar_naam,
                    s.startdatum AS schooljaar_startdatum,
                    s.einddatum AS schooljaar_einddatum,
                    s.actief AS schooljaar_actief
                FROM klassen k
                JOIN schooljaren s
                    ON s.naam = k.schooljaar
                ORDER BY k.leerjaar, k.klas_naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            List<Klas> klassen = new ArrayList<>();

            while (resultSet.next()) {
                Schooljaar schooljaar = new Schooljaar(
                        resultSet.getLong("schooljaar_id"),
                        resultSet.getString("schooljaar_naam"),
                        resultSet.getDate("schooljaar_startdatum").toLocalDate(),
                        resultSet.getDate("schooljaar_einddatum").toLocalDate(),
                        resultSet.getBoolean("schooljaar_actief")
                );

                Klas klas = new Klas(
                        resultSet.getLong("klas_id"),
                        resultSet.getString("klas_naam"),
                        schooljaar,
                        resultSet.getInt("leerjaar"),
                        Doelgroep.valueOf(resultSet.getString("doelgroep"))
                );

                klassen.add(klas);
            }

            return klassen;
        } catch (SQLException e) {
            throw new IllegalStateException("De klassen konden niet opgehaald worden.", e);
        }
    }
}
package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.VoorkeurImportProbleem;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresVoorkeurImportProbleemRepository implements VoorkeurImportProbleemRepository {

    @Override
    public void save(VoorkeurImportProbleem probleem) {
        if (probleem == null) {
            throw new IllegalArgumentException("Importprobleem mag niet null zijn");
        }
        if (probleem.getLeerling() == null || probleem.getLeerling().getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }
        if (probleem.getPeriode() == null || probleem.getPeriode().getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        String sql = """
                INSERT INTO voorkeur_import_problemen (
                    leerling_id,
                    talenten_periode_id,
                    reden
                )
                VALUES (?, ?, ?)
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, probleem.getLeerling().getId());
            statement.setLong(2, probleem.getPeriode().getId());
            statement.setString(3, probleem.getReden());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Het voorkeurimportprobleem kon niet opgeslagen worden.",
                    e
            );
        }
    }

    @Override
    public List<VoorkeurImportProbleem> zoekVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (leerling.getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }
        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT reden
                FROM voorkeur_import_problemen
                WHERE leerling_id = ?
                  AND talenten_periode_id = ?
                ORDER BY voorkeur_import_probleem_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, leerling.getId());
            statement.setLong(2, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<VoorkeurImportProbleem> problemen = new ArrayList<>();

                while (resultSet.next()) {
                    problemen.add(
                            new VoorkeurImportProbleem(
                                    leerling,
                                    periode,
                                    resultSet.getString("reden")
                            )
                    );
                }

                return problemen;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeurimportproblemen voor deze leerling konden niet opgehaald worden.",
                    e
            );
        }
    }

    @Override
    public List<VoorkeurImportProbleem> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }
        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        String sql = """
                SELECT
                    p.reden,

                    l.leerling_id,
                    l.voornaam,
                    l.achternaam,

                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep
                FROM voorkeur_import_problemen p
                JOIN leerlingen l
                    ON l.leerling_id = p.leerling_id
                JOIN klassen k
                    ON k.klas_id = l.klas_id
                WHERE p.talenten_periode_id = ?
                ORDER BY
                    k.klas_naam,
                    l.achternaam,
                    l.voornaam,
                    p.voorkeur_import_probleem_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, periode.getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                List<VoorkeurImportProbleem> problemen = new ArrayList<>();

                while (resultSet.next()) {
                    Klas klas = new Klas(
                            resultSet.getLong("klas_id"),
                            resultSet.getString("klas_naam"),
                            periode.getSchooljaar(),
                            resultSet.getInt("leerjaar"),
                            Doelgroep.valueOf(resultSet.getString("doelgroep"))
                    );

                    Leerling leerling = new Leerling(
                            resultSet.getLong("leerling_id"),
                            resultSet.getString("voornaam"),
                            resultSet.getString("achternaam"),
                            klas
                    );

                    problemen.add(
                            new VoorkeurImportProbleem(
                                    leerling,
                                    periode,
                                    resultSet.getString("reden")
                            )
                    );
                }

                return problemen;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeurimportproblemen voor deze talentenperiode konden niet opgehaald worden.",
                    e
            );
        }
    }
    @Override
    public void verwijderVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (leerling.getId() == null) {
            throw new IllegalArgumentException("De leerling moet eerst opgeslagen zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("Talentenperiode mag niet null zijn");
        }
        if (periode.getId() == null) {
            throw new IllegalArgumentException("De talentenperiode moet eerst opgeslagen zijn");
        }

        String sql = """
            DELETE FROM voorkeur_import_problemen
            WHERE leerling_id = ?
              AND talenten_periode_id = ?
            """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, leerling.getId());
            statement.setLong(2, periode.getId());

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De voorkeurimportproblemen van de leerling konden niet verwijderd worden",
                    e
            );
        }
    }
}
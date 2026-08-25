package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.LeerlingKlasHistoriekRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class PostgresLeerlingKlasHistoriekRepository
        implements LeerlingKlasHistoriekRepository {

    @Override
    public void startHistoriek(
            Leerling leerling,
            Klas klas,
            LocalDate vanaf
    ) {
        if (leerling == null || leerling.getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn."
            );
        }

        if (klas == null || klas.getId() == null) {
            throw new IllegalArgumentException(
                    "De klas moet eerst opgeslagen zijn."
            );
        }

        if (vanaf == null) {
            throw new IllegalArgumentException(
                    "Vanaf-datum mag niet null zijn."
            );
        }

        String sql = """
                INSERT INTO leerling_klas_historiek (
                    leerling_id,
                    klas_id,
                    vanaf,
                    tot
                )
                VALUES (?, ?, ?, NULL)
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, leerling.getId());
            statement.setLong(2, klas.getId());
            statement.setObject(3, vanaf);

            statement.executeUpdate();

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De klashistoriek kon niet gestart worden.",
                    e
            );
        }
    }

    @Override
    public void sluitHuidigeHistoriekAf(
            Leerling leerling,
            LocalDate tot
    ) {
        if (leerling == null || leerling.getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn."
            );
        }

        if (tot == null) {
            throw new IllegalArgumentException(
                    "Tot-datum mag niet null zijn."
            );
        }

        String sql = """
                UPDATE leerling_klas_historiek
                SET tot = ?
                WHERE leerling_id = ?
                  AND tot IS NULL
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setObject(1, tot);
            statement.setLong(2, leerling.getId());

            int aantalAangepasteRijen = statement.executeUpdate();

            if (aantalAangepasteRijen == 0) {
                throw new IllegalStateException(
                        "Er is geen huidige klashistoriek gevonden voor leerling "
                                + leerling.getId()
                );
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De huidige klashistoriek kon niet afgesloten worden.",
                    e
            );
        }
    }

    @Override
    public List<LeerlingKlasHistoriek> zoekVoorLeerling(
            Leerling leerling
    ) {
        if (leerling == null || leerling.getId() == null) {
            throw new IllegalArgumentException(
                    "De leerling moet eerst opgeslagen zijn."
            );
        }

        String sql = """
                SELECT
                    h.leerling_klas_historiek_id,
                    h.vanaf,
                    h.tot,

                    k.klas_id,
                    k.klas_naam,
                    k.leerjaar,
                    k.doelgroep,

                    s.schooljaar_id,
                    s.naam AS schooljaar_naam,
                    s.startdatum AS schooljaar_startdatum,
                    s.einddatum AS schooljaar_einddatum,
                    s.actief AS schooljaar_actief

                FROM leerling_klas_historiek h

                JOIN klassen k
                    ON k.klas_id = h.klas_id

                JOIN schooljaren s
                    ON s.naam = k.schooljaar

                WHERE h.leerling_id = ?

                ORDER BY h.vanaf
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setLong(1, leerling.getId());

            try (ResultSet resultSet = statement.executeQuery()) {

                List<LeerlingKlasHistoriek> historiek =
                        new ArrayList<>();

                while (resultSet.next()) {

                    Schooljaar schooljaar = new Schooljaar(
                            resultSet.getLong("schooljaar_id"),
                            resultSet.getString("schooljaar_naam"),
                            resultSet.getDate(
                                    "schooljaar_startdatum"
                            ).toLocalDate(),
                            resultSet.getDate(
                                    "schooljaar_einddatum"
                            ).toLocalDate(),
                            resultSet.getBoolean(
                                    "schooljaar_actief"
                            )
                    );

                    Klas klas = new Klas(
                            resultSet.getLong("klas_id"),
                            resultSet.getString("klas_naam"),
                            schooljaar,
                            resultSet.getInt("leerjaar"),
                            Doelgroep.valueOf(
                                    resultSet.getString("doelgroep")
                            )
                    );

                    LocalDate tot = resultSet.getDate("tot") == null
                            ? null
                            : resultSet.getDate("tot").toLocalDate();

                    LeerlingKlasHistoriek registratie =
                            new LeerlingKlasHistoriek(
                                    resultSet.getLong(
                                            "leerling_klas_historiek_id"
                                    ),
                                    leerling,
                                    klas,
                                    resultSet.getDate(
                                            "vanaf"
                                    ).toLocalDate(),
                                    tot
                            );

                    historiek.add(registratie);
                }

                return historiek;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De klashistoriek van de leerling kon niet opgehaald worden.",
                    e
            );
        }
    }
    @Override
    public List<LeerlingKlasHistoriek> zoekVoorKlasOpDatum(
            Klas klas,
            LocalDate datum
    ) {
        if (klas == null || klas.getId() == null) {
            throw new IllegalArgumentException(
                    "De klas moet eerst opgeslagen zijn."
            );
        }

        if (datum == null) {
            throw new IllegalArgumentException(
                    "Datum mag niet null zijn."
            );
        }

        String sql = """
            SELECT
                h.leerling_klas_historiek_id,
                h.vanaf,
                h.tot,

                l.leerling_id,
                l.voornaam,
                l.achternaam,
                l.actief

            FROM leerling_klas_historiek h

            JOIN leerlingen l
                ON l.leerling_id = h.leerling_id

            WHERE h.klas_id = ?
              AND h.vanaf <= ?
              AND (
                    h.tot IS NULL
                    OR h.tot > ?
              )

            ORDER BY
                l.achternaam,
                l.voornaam
            """;

        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement =
                     connection.prepareStatement(sql)) {

            statement.setLong(
                    1,
                    klas.getId()
            );

            statement.setObject(
                    2,
                    datum
            );

            statement.setObject(
                    3,
                    datum
            );

            try (ResultSet resultSet =
                         statement.executeQuery()) {

                List<LeerlingKlasHistoriek> resultaat =
                        new ArrayList<>();

                while (resultSet.next()) {

                    Leerling leerling =
                            new Leerling(
                                    resultSet.getLong(
                                            "leerling_id"
                                    ),
                                    resultSet.getString(
                                            "voornaam"
                                    ),
                                    resultSet.getString(
                                            "achternaam"
                                    ),
                                    klas,
                                    resultSet.getBoolean(
                                            "actief"
                                    )
                            );

                    LocalDate tot =
                            resultSet.getDate("tot") == null
                                    ? null
                                    : resultSet
                                    .getDate("tot")
                                    .toLocalDate();

                    resultaat.add(
                            new LeerlingKlasHistoriek(
                                    resultSet.getLong(
                                            "leerling_klas_historiek_id"
                                    ),
                                    leerling,
                                    klas,
                                    resultSet
                                            .getDate("vanaf")
                                            .toLocalDate(),
                                    tot
                            )
                    );
                }

                return resultaat;
            }

        } catch (SQLException e) {
            throw new IllegalStateException(
                    "De klashistoriek kon niet opgehaald worden.",
                    e
            );
        }
    }


}
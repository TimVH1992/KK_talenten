package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.TalentenPeriodeRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresTalentenPeriodeRepository implements TalentenPeriodeRepository {
    @Override
    public TalentenPeriode save(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }
        if (periode.getSchooljaar().getId() == null) {
            throw new IllegalArgumentException("Het schooljaar moet eerst opgeslagen zijn");
        }

        String sql = """
                INSERT INTO talenten_periodes (naam, startdatum, einddatum, schooljaar_id)
                VALUES (?, ?, ?, ?)
                RETURNING talenten_periode_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, periode.getNaam());
            statement.setDate(2, java.sql.Date.valueOf(periode.getStartDatum()));
            statement.setDate(3, java.sql.Date.valueOf(periode.getEindDatum()));
            statement.setLong(4, periode.getSchooljaar().getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen talenten_periode_id terug");
                }
                return new TalentenPeriode(resultSet.getLong("talenten_periode_id"), periode.getNaam(), periode.getStartDatum(), periode.getEindDatum(), periode.getSchooljaar());
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("De talentenperiode kon niet opgeslagen worden", exception);
        }
    }

    @Override
    public List<TalentenPeriode> zoekAlle() {
        String sql = basisSelect() + " ORDER BY tp.startdatum, tp.einddatum";
        return voerQueryUit(sql, null);
    }

    @Override
    public List<TalentenPeriode> zoekVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }
        if (schooljaar.getId() == null) {
            throw new IllegalArgumentException("Het schooljaar moet eerst opgeslagen zijn");
        }

        String sql = basisSelect() + " WHERE tp.schooljaar_id = ? ORDER BY tp.startdatum, tp.einddatum";
        return voerQueryUit(sql, schooljaar.getId());
    }

    private String basisSelect() {
        return """
                SELECT tp.talenten_periode_id,
                       tp.naam AS periode_naam,
                       tp.startdatum AS periode_startdatum,
                       tp.einddatum AS periode_einddatum,
                       sj.schooljaar_id,
                       sj.naam AS schooljaar_naam,
                       sj.startdatum AS schooljaar_startdatum,
                       sj.einddatum AS schooljaar_einddatum,
                       sj.actief
                FROM talenten_periodes tp
                JOIN schooljaren sj ON sj.schooljaar_id = tp.schooljaar_id
                """;
    }

    private List<TalentenPeriode> voerQueryUit(String sql, Long schooljaarId) {
        List<TalentenPeriode> periodes = new ArrayList<>();

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (schooljaarId != null) statement.setLong(1, schooljaarId);

            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    Schooljaar schooljaar = new Schooljaar(resultSet.getLong("schooljaar_id"), resultSet.getString("schooljaar_naam"), resultSet.getDate("schooljaar_startdatum").toLocalDate(), resultSet.getDate("schooljaar_einddatum").toLocalDate(), resultSet.getBoolean("actief"));
                    periodes.add(new TalentenPeriode(resultSet.getLong("talenten_periode_id"), resultSet.getString("periode_naam"), resultSet.getDate("periode_startdatum").toLocalDate(), resultSet.getDate("periode_einddatum").toLocalDate(), schooljaar));
                }
                return periodes;
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("De talentenperiodes konden niet opgehaald worden", exception);
        }
    }
}

package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
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
        if (periode == null){
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }
        String sql = """
                INSERT INTO talenten_periodes(
                                              naam,
                                              startdatum,
                                              einddatum
                )
                VALUES (?, ?, ?)
                RETURNING talenten_periode_id
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)){
            statement.setString(1, periode.getNaam());
            statement.setDate(
                    2,
                    java.sql.Date.valueOf(periode.getStartDatum())
            );

            statement.setDate(
                    3,
                    java.sql.Date.valueOf(periode.getEindDatum())
            );

            try(ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
                    throw new IllegalStateException("PostgreSQL gaf geen TalentenPeriodeID terug");
                }
                long gegenereerdId = resultSet.getLong("talenten_periode_id");
                return new TalentenPeriode(
                        gegenereerdId,
                        periode.getNaam(),
                        periode.getStartDatum(),
                        periode.getEindDatum()
                );
            }
        } catch (SQLException e){
            throw new IllegalStateException("Het talent kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<TalentenPeriode> zoekAlle() {
        List<TalentenPeriode> periodes = new ArrayList<>();

        String sql = """
                SELECT talenten_periode_id, naam, startdatum, einddatum
                FROM talenten_periodes
                ORDER BY startdatum, einddatum
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
        PreparedStatement statement = connection.prepareStatement(sql)){
            try (ResultSet resultSet = statement.executeQuery()){
                while (resultSet.next()){
                    TalentenPeriode periode = new TalentenPeriode(
                            resultSet.getLong("talenten_periode_id"),
                            resultSet.getString("naam"),
                            resultSet.getDate("startdatum").toLocalDate(),
                            resultSet.getDate("einddatum").toLocalDate()
                    );
                    periodes.add(periode);

                }
                return periodes;
            }
        }
        catch (SQLException e){
            throw new IllegalStateException("De talenten_periodes konden niet opgehaald worden", e);
        }
    }
}

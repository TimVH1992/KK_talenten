package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class PostgresIngerichtTalentRepository implements IngerichtTalentRepository {
    @Override
    public IngerichtTalent save(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }

        String sql = """
                INSERT INTO ingerichte_talenten(
                maximum_capaciteit,
                doelgroep,
                talent_id,
                talenten_periode_id
                )
                VALUES(?,?,?,?)
                RETURNING ingericht_talent_id
                """;
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);) {

            statement.setInt(1, ingerichtTalent.getMaxCapaciteit());
            statement.setObject(2, ingerichtTalent.getDoelgroep());
            statement.setLong(3, ingerichtTalent.getTalent().getId());
            statement.setLong(4, ingerichtTalent.getTalentenPeriode().getId());

            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    throw new IllegalStateException("PostgreSQL gaf geen ingericht_talent_id terug");
                }
                long gegenereerdId = resultSet.getLong("ingericht_talent_id");
                return new IngerichtTalent(
                        gegenereerdId,
                        ingerichtTalent.getTalent(),
                        ingerichtTalent.getTalentenPeriode(),
                        ingerichtTalent.getMaxCapaciteit(),
                        ingerichtTalent.getDoelgroep(),
                        ingerichtTalent.getLeerkrachten()
                );
            }
        } catch (SQLException e) {
            throw new IllegalStateException(
                    "Het ingerichte talent kon niet opgeslagen worden",
                    e
            );
        }
    }

    @Override
    public List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode) {
        return List.of();
    }
}

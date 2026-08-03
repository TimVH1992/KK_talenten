package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.TalentRepository;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class PostgresTalentRepository implements TalentRepository {


    @Override
    public Talent save(Talent talent) {
        if (talent == null){
            throw new IllegalArgumentException("Talent mag niet null zijn");
        }
        String sql = """
                INSERT INTO TALENTEN(
                naam,
                beschrijving
                )
                VALUES (?,?)
                RETURNING talent_id
                """;
        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql)
        ){
            statement.setString(1, talent.getNaam());
            statement.setString(2, talent.getBeschrijving());

            try (ResultSet resultSet = statement.executeQuery()){
                if(!resultSet.next()){
                    throw new IllegalStateException("PostgreSQL gaf geen talent_id terug.");
                }
                long gegenereerdId = resultSet.getLong("talent_id");

                return new Talent(gegenereerdId, talent.getNaam(), talent.getBeschrijving());
            }

        } catch (SQLException e) {
            throw new IllegalStateException("Het talent kon niet opgeslagen worden",e);
        }
    }

    @Override
    public List<Talent> zoekAlle() {
        String sql = """
                SELECT talent_id, naam, beschrijving
                FROM talenten
                ORDER BY naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
        PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            try (ResultSet resultSet = statement.executeQuery()){
                List<Talent> talenten = new ArrayList<>();
                while (resultSet.next()){
                    Talent talent = new Talent(
                            resultSet.getLong("talent_id"),
                            resultSet.getString("naam"),
                            resultSet.getString("beschrijving")
                    );
                    talenten.add(talent);
                }
                return talenten;
            }
        }
        catch (SQLException e){
            throw new IllegalStateException("De talenten konden niet opgehaald worden.", e);
        }
    }
}

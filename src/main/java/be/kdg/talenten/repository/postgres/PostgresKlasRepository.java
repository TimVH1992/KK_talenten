package be.kdg.talenten.repository.postgres;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.Klas;
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
        if (klas == null){
            throw new IllegalArgumentException("Klas mag niet null zijn");
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
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {
            statement.setString(1, klas.getNaam());
            statement.setString(2, klas.getSchooljaar());
            statement.setInt(3, klas.getLeerjaar());
            statement.setString(
                    4,
                    klas.getDoelgroep().name()
            );

            try (ResultSet resultSet = statement.executeQuery()){
                if (!resultSet.next()){
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
        } catch (SQLException e){
            throw new IllegalStateException("De klas kon niet opgeslagen worden", e);
        }
    }

    @Override
    public List<Klas> zoekAlle() {
        String sql = """
                SELECT klas_id, 
                klas_naam, 
                schooljaar, 
                leerjaar,
                doelgroep
                FROM klassen
                ORDER BY leerjaar, klas_naam
                """;

        try (Connection connection = DatabaseConnectionFactory.maakVerbinding();
             PreparedStatement statement = connection.prepareStatement(sql);
        ) {

            try (ResultSet resultSet = statement.executeQuery()) {
                List<Klas> klassen = new ArrayList<>();

                while (resultSet.next()) {
                    Klas klas = new Klas(
                            resultSet.getLong("klas_id"),
                            resultSet.getString("klas_naam"),
                            resultSet.getString("schooljaar"),
                            resultSet.getInt("leerjaar"),
                            Doelgroep.valueOf(resultSet.getString("doelgroep"))
                    );

                    klassen.add(klas);
                }

                return klassen;
            }
        }
        catch (SQLException e) {
            throw new IllegalStateException(
                    "De klassen konden niet opgehaald worden.",
                    e
            );
        }
    }
}

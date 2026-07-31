package be.kdg.talenten.database;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public final class DatabaseConnectionFactory {
    private static final Properties PROPERTIES = laadProperties();

    private DatabaseConnectionFactory() {
    }

    public static Connection maakVerbinding() throws SQLException {
        return DriverManager.getConnection(
                PROPERTIES.getProperty("db.url"),
                PROPERTIES.getProperty("db.user"),
                PROPERTIES.getProperty("db.password")
        );
    }

    private static Properties laadProperties() {
        Properties properties = new Properties();

        try (InputStream input =
                     DatabaseConnectionFactory.class
                             .getClassLoader()
                             .getResourceAsStream("db.properties")) {

            if (input == null) {
                throw new IllegalStateException(
                        "db.properties werd niet gevonden."
                );
            }

            properties.load(input);
            return properties;

        } catch (IOException e) {
            throw new IllegalStateException(
                    "Database-instellingen konden niet geladen worden.",
                    e
            );
        }
    }
}
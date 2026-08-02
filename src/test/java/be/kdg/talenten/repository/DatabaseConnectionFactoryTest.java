package be.kdg.talenten.repository;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.*;

class DatabaseConnectionFactoryTest {

    @Test
    void maaktVerbindingMetPostgreSQL() throws SQLException {
        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding()) {

            assertNotNull(connection);
            assertFalse(connection.isClosed());
            assertEquals(
                    "kk_talenten",
                    connection.getCatalog()
            );
        }
    }
}
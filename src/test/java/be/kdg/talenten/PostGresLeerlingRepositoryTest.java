package be.kdg.talenten;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.PostgresLeerlingRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


public class PostGresLeerlingRepositoryTest {
    @Test
    public void saveSlaatLeerlingOpEnGeeftDatabaseIdTerug(){
//        ARRANGE
        LeerlingRepository repository = new PostgresLeerlingRepository();

        Klas klas = new Klas(1L, "2AA", "2026-2027", 2);
        Leerling leerling = new Leerling("Test", "Leerling", klas);
//        ACT
        Leerling opgeslagenLeerling = repository.save(leerling);
//        ASSERT
        Assertions.assertNotNull(opgeslagenLeerling);
        Assertions.assertTrue(opgeslagenLeerling.getId() > 0);
    }
}

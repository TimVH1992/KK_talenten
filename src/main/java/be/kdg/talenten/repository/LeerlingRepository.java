package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;

import java.util.List;


public interface LeerlingRepository {
    List<Leerling> zoekVoorKlas(Klas klas);
    Leerling save(Leerling leerling);
    Leerling zoekOpId(long id);
    List<Leerling> zoekVoorSchooljaar(Schooljaar schooljaar);
    void update(Leerling leerling);
}

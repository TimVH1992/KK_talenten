package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;

import java.util.List;


public interface LeerlingRepository {
    List<Leerling> zoekVoorKlas(Klas klas);
}

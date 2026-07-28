package be.kdg.talenten.repository;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;

import java.util.ArrayList;
import java.util.List;

public class InMemoryLeerlingRepository implements  LeerlingRepository{
    private List<Leerling> leerlingen;

    public InMemoryLeerlingRepository(List<Leerling> leerlingen) {
        if (leerlingen == null){
            throw new IllegalArgumentException("Leerlingen mag niet null zijn");
        }
        this.leerlingen = leerlingen;
    }

    @Override
    public List<Leerling> zoekVoorKlas(Klas klas) {
        List<Leerling> leerlingenPerKlas = new ArrayList<>();
        if (klas == null){
            throw new IllegalArgumentException("De klas mag niet null zijn");
        }
        for (Leerling leerling : leerlingen){
            if (leerling.getKlas().equals(klas)){
                leerlingenPerKlas.add(leerling);
            }
        }
        return leerlingenPerKlas;
    }
}

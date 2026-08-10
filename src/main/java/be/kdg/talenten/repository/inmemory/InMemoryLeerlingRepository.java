package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryLeerlingRepository implements LeerlingRepository {
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

    @Override
    public Leerling save(Leerling leerling) {
        if (leerling == null){
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        leerlingen.add(leerling);
        return leerling;
    }

    @Override
    public Leerling zoekOpId(long id) {
        for (Leerling leerling : leerlingen) {
            if (leerling.getId() != null && leerling.getId() == id) {
                return leerling;
            }
        }

        throw new IllegalStateException("Geen leerling gevonden met ID " + id);
    }
    @Override
    public List<Leerling> zoekVoorSchooljaar(Schooljaar schooljaar) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Het schooljaar mag niet null zijn");
        }

        return leerlingen.stream()
                .filter(leerling -> leerling.getKlas().getSchooljaar().equals(schooljaar))
                .sorted(Comparator
                        .comparing((Leerling leerling) -> leerling.getKlas().getNaam())
                        .thenComparing(Leerling::getAchternaam)
                        .thenComparing(Leerling::getVoornaam))
                .toList();
    }
}

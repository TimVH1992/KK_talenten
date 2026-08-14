package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.repository.LeerlingRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryLeerlingRepository implements LeerlingRepository {

    private final List<Leerling> leerlingen;

    public InMemoryLeerlingRepository(List<Leerling> leerlingen) {
        if (leerlingen == null) {
            throw new IllegalArgumentException("Leerlingen mag niet null zijn");
        }
        this.leerlingen = leerlingen;
    }

    @Override
    public List<Leerling> zoekVoorKlas(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("De klas mag niet null zijn");
        }

        List<Leerling> leerlingenPerKlas = new ArrayList<>();

        for (Leerling leerling : leerlingen) {
            if (leerling.getKlas().equals(klas)) {
                leerlingenPerKlas.add(leerling);
            }
        }

        return leerlingenPerKlas;
    }

    @Override
    public Leerling save(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }

        leerlingen.add(leerling);
        return leerling;
    }

    @Override
    public Leerling zoekOpId(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id moet groter zijn dan 0");
        }

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

    @Override
    public void update(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException("Leerling mag niet null zijn");
        }
        if (leerling.getId() == null || leerling.getId() < 1) {
            throw new IllegalStateException("De leerling heeft geen bestaand id");
        }

        for (int i = 0; i < leerlingen.size(); i++) {
            Leerling opgeslagenLeerling = leerlingen.get(i);

            if (opgeslagenLeerling.getId() != null && opgeslagenLeerling.getId().equals(leerling.getId())) {
                leerlingen.set(i, leerling);
                return;
            }
        }

        throw new IllegalStateException("Geen leerling gevonden met id: " + leerling.getId());
    }
}
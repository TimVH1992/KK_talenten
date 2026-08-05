package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class InMemoryVoorkeurRepository implements VoorkeurRepository {
    private final List<Voorkeur> voorkeuren;

    public InMemoryVoorkeurRepository(List<Voorkeur> voorkeuren) {
        if(voorkeuren == null){
            throw new IllegalArgumentException("Voorkeuren mag niet null zijn");
        }
        this.voorkeuren = new ArrayList<>(voorkeuren);
    }

    @Override
    public Voorkeur save(Voorkeur voorkeur) {
        if (voorkeur == null){
            throw new IllegalArgumentException("Voorkeur mag niet null zijn");
        }
        voorkeuren.add(voorkeur);
        return voorkeur;
    }


    @Override
    public List<Voorkeur> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null){
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        List<Voorkeur> resultaat = new ArrayList<>();

        for (Voorkeur voorkeur : voorkeuren){
            if (voorkeur.getTalentenPeriode() == periode) {
                resultaat.add(voorkeur);
            }
        }
        return resultaat;
    }
    @Override
    public List<Voorkeur> zoekVoorLeerlingEnPeriode(Leerling leerling, TalentenPeriode periode) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn");
        }
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }

        return voorkeuren.stream()
                .filter(voorkeur -> voorkeur.getLeerling().equals(leerling))
                .filter(voorkeur -> voorkeur.getTalentenPeriode().equals(periode))
                .sorted(Comparator.comparingInt(Voorkeur::getVoorkeurNummer))
                .toList();
    }
}

package be.kdg.talenten.repository;

import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Voorkeur;

import java.util.ArrayList;
import java.util.List;

public class InMemoryVoorkeurRepository implements VoorkeurRepository{
    private List<Voorkeur> voorkeuren;

    public InMemoryVoorkeurRepository(List<Voorkeur> voorkeuren) {
        if(voorkeuren == null){
            throw new IllegalArgumentException("Voorkeuren mag niet null zijn");
        }
        this.voorkeuren = voorkeuren;
    }
    @Override
    public List<Voorkeur> zoekVoorPeriode(TalentenPeriode periode) {
        List<Voorkeur> resultaat = new ArrayList<>();

        for (Voorkeur voorkeur : voorkeuren){
            resultaat.add(voorkeur);
        }
        return resultaat;
    }
}

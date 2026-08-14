package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryIngerichtTalentRepository implements IngerichtTalentRepository {

    private final List<IngerichtTalent> ingerichteTalenten;

    public InMemoryIngerichtTalentRepository(List<IngerichtTalent> ingerichteTalenten) {
        if (ingerichteTalenten == null) {
            throw new IllegalArgumentException("De ingerichteTalenten mag niet null zijn");
        }

        this.ingerichteTalenten = new ArrayList<>(ingerichteTalenten);
    }

    @Override
    public IngerichtTalent save(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }

        ingerichteTalenten.add(ingerichtTalent);
        return ingerichtTalent;
    }

    @Override
    public List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }

        List<IngerichtTalent> ingerichteTalentenVoorPeriode = new ArrayList<>();

        for (IngerichtTalent ingerichtTalent : ingerichteTalenten) {
            if (ingerichtTalent.getTalentenPeriode().equals(periode)) {
                ingerichteTalentenVoorPeriode.add(ingerichtTalent);
            }
        }

        return ingerichteTalentenVoorPeriode;
    }

    @Override
    public List<IngerichtTalent> zoekActieveVoorPeriodeEnDoelgroep(TalentenPeriode periode, Doelgroep doelgroep) {
        if (periode == null) {
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        if (doelgroep == null) {
            throw new IllegalArgumentException("De doelgroep mag niet null zijn");
        }

        List<IngerichtTalent> resultaat = new ArrayList<>();

        for (IngerichtTalent ingerichtTalent : ingerichteTalenten) {
            if (ingerichtTalent.getTalentenPeriode().equals(periode)
                    && ingerichtTalent.isActief()
                    && ingerichtTalent.getDoelgroep() == doelgroep) {

                resultaat.add(ingerichtTalent);
            }
        }

        return resultaat;
    }

    @Override
    public IngerichtTalent zoekOpId(long id) {
        if (id < 1) {
            throw new IllegalArgumentException("Id mag niet kleiner zijn dan 1");
        }

        for (IngerichtTalent ingerichtTalent : ingerichteTalenten) {
            if (ingerichtTalent.getId() != null && ingerichtTalent.getId().equals(id)) {
                return ingerichtTalent;
            }
        }

        throw new IllegalStateException("Geen ingericht talent gevonden met id: " + id);
    }

    @Override
    public void update(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
        if (ingerichtTalent.getId() == null || ingerichtTalent.getId() < 1) {
            throw new IllegalStateException("Het ingerichte talent heeft geen bestaand id");
        }

        for (int i = 0; i < ingerichteTalenten.size(); i++) {
            IngerichtTalent opgeslagen = ingerichteTalenten.get(i);

            if (opgeslagen.getId() != null && opgeslagen.getId().equals(ingerichtTalent.getId())) {
                ingerichteTalenten.set(i, ingerichtTalent);
                return;
            }
        }

        throw new IllegalStateException(
                "Geen ingericht talent gevonden met id: " + ingerichtTalent.getId()
        );
    }
}
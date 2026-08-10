package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryIngerichtTalentRepository implements IngerichtTalentRepository {
    private List<IngerichtTalent> ingerichteTalenten;

    public InMemoryIngerichtTalentRepository(List<IngerichtTalent> ingerichteTalenten){
        if (ingerichteTalenten == null){
            throw new IllegalArgumentException("De ingerichteTalenten mag niet null zijn");
        }
        this.ingerichteTalenten = new ArrayList<>(ingerichteTalenten);
    }

    @Override
    public IngerichtTalent save(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException(
                    "Ingericht talent mag niet null zijn"
            );
        }

        ingerichteTalenten.add(ingerichtTalent);
        return ingerichtTalent;
    }

    @Override
    public List<IngerichtTalent> zoekVoorPeriode(TalentenPeriode periode){
        if (periode == null){
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        List<IngerichtTalent> ingerichteTalentenVoorPeriode = new ArrayList<>();
        for (IngerichtTalent ingerichtTalent : ingerichteTalenten){
            if (ingerichtTalent.getTalentenPeriode() == periode){
                ingerichteTalentenVoorPeriode.add(ingerichtTalent);
            }
        }
        return ingerichteTalentenVoorPeriode;
    }

    @Override
    public List<IngerichtTalent> zoekActieveVoorPeriodeEnDoelgroep(TalentenPeriode periode, Doelgroep doelgroep) {
        if (periode == null){
            throw new IllegalArgumentException("De periode mag niet null zijn");
        }
        if (doelgroep == null){
            throw new IllegalArgumentException("De doelgroep mag niet null zijn");
        }
        List<IngerichtTalent> actieveIngerichteTalentenVoorPeriode = new ArrayList<>();
        for (IngerichtTalent ingerichtTalent : ingerichteTalenten){
            if (ingerichtTalent.getTalentenPeriode() == periode && ingerichtTalent.isActief() && ingerichtTalent.getDoelgroep() == doelgroep){
                actieveIngerichteTalentenVoorPeriode.add(ingerichtTalent);
            }
        }
        return actieveIngerichteTalentenVoorPeriode;
    }

    @Override
    public IngerichtTalent zoekOpId(long id) {
        if (id < 1){
            throw new IllegalArgumentException("id mag niet kleiner zijn dan 1");
        }
        for (IngerichtTalent ingerichtTalent : ingerichteTalenten){
            if (ingerichtTalent.getId() != null && ingerichtTalent.getId().equals(id)){
                return ingerichtTalent;
            }
        }
        throw new IllegalStateException("Geen ingericht talent gevonden met id: " + id);
    }
}

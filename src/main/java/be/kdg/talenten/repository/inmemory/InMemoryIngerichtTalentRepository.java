package be.kdg.talenten.repository.inmemory;

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
}

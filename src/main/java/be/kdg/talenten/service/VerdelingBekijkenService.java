package be.kdg.talenten.service;

import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Toewijzing;
import be.kdg.talenten.overzicht.IngerichtTalentOverzicht;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.util.*;

public class VerdelingBekijkenService {
    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final ToewijzingRepository toewijzingRepository;

    public VerdelingBekijkenService(IngerichtTalentRepository ingerichtTalentRepository, ToewijzingRepository toewijzingRepository){
        if (ingerichtTalentRepository == null){
            throw new IllegalArgumentException("IngerichtTalentRepository mag niet null zijn");
        }
        if (toewijzingRepository == null){
            throw new IllegalArgumentException("ToewijzingRepository mag niet null zijn");
        }
        this.ingerichtTalentRepository = ingerichtTalentRepository;
        this.toewijzingRepository = toewijzingRepository;
    }

    public List<IngerichtTalentOverzicht> bekijkPerIngerichtTalent(TalentenPeriode periode){
        if (periode == null){
            throw new IllegalArgumentException("periode mag niet null zijn");
        }
        Map<IngerichtTalent, List<Toewijzing>> toewijzingenPerTalentMap = new LinkedHashMap<>();

        for (IngerichtTalent ingerichtTalent : ingerichtTalentRepository.zoekVoorPeriode(periode)){
            toewijzingenPerTalentMap.put(ingerichtTalent, new ArrayList<>());
        }

        for (Toewijzing toewijzing : toewijzingRepository.zoekVoorPeriode(periode)){
            toewijzingenPerTalentMap.get(toewijzing.getIngerichtTalent()).add(toewijzing);
        }

        List<IngerichtTalentOverzicht> ingerichtTalentOverzichten = new ArrayList<>();
        for (Map.Entry<IngerichtTalent, List<Toewijzing>> entry :toewijzingenPerTalentMap.entrySet()){
            IngerichtTalent huidigIngerichtTalent = entry.getKey();
            List<Toewijzing> huidigeToewijzingen = entry.getValue();

            ingerichtTalentOverzichten.add(new IngerichtTalentOverzicht(huidigIngerichtTalent, huidigeToewijzingen.size(),
                    huidigIngerichtTalent.getMaxCapaciteit() - huidigeToewijzingen.size(), huidigeToewijzingen));

        }
        return ingerichtTalentOverzichten;
    }


}

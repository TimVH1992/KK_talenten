package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.IngerichtTalentRepository;

import java.util.List;

public class IngerichtTalentService {

    private final IngerichtTalentRepository ingerichtTalentRepository;

    public IngerichtTalentService(IngerichtTalentRepository ingerichtTalentRepository) {
        if (ingerichtTalentRepository == null) {
            throw new IllegalArgumentException("IngerichtTalentRepository mag niet null zijn");
        }

        this.ingerichtTalentRepository = ingerichtTalentRepository;
    }

    public List<IngerichtTalent> geefIngerichteTalentenVoorPeriode(TalentenPeriode periode) {
        return ingerichtTalentRepository.zoekVoorPeriode(periode);
    }

    public IngerichtTalent maakIngerichtTalent(Talent talent, TalentenPeriode periode, String naam, String omschrijving, int maxCapaciteit, Doelgroep doelgroep, List<Leerkracht> leerkrachten) {
        if (talent == null) {
            throw new IllegalArgumentException("Talent mag niet null zijn");
        }

        String effectieveOmschrijving = omschrijving;

        if (omschrijving == null || omschrijving.isBlank()) {
            effectieveOmschrijving = talent.getBeschrijving();
        }

        IngerichtTalent ingerichtTalent = new IngerichtTalent(
                talent,
                periode,
                naam,
                effectieveOmschrijving,
                maxCapaciteit,
                doelgroep,
                leerkrachten
        );

        return ingerichtTalentRepository.save(ingerichtTalent);
    }

    public void wijzigIngerichtTalent(IngerichtTalent ingerichtTalent, String naam, String omschrijving, int maxCapaciteit) {
        valideerIngerichtTalent(ingerichtTalent);

        ingerichtTalent.wijzigGegevens(
                naam,
                omschrijving,
                maxCapaciteit
        );

        ingerichtTalentRepository.update(ingerichtTalent);
    }

    public void voegLeerkrachtToe(IngerichtTalent ingerichtTalent, Leerkracht leerkracht) {
        valideerIngerichtTalent(ingerichtTalent);

        ingerichtTalent.voegLeerkrachtToe(leerkracht);
        ingerichtTalentRepository.update(ingerichtTalent);
    }

    public void verwijderLeerkracht(IngerichtTalent ingerichtTalent, Leerkracht leerkracht) {
        valideerIngerichtTalent(ingerichtTalent);

        ingerichtTalent.verwijderLeerkracht(leerkracht);
        ingerichtTalentRepository.update(ingerichtTalent);
    }

    public void activeer(IngerichtTalent ingerichtTalent) {
        valideerIngerichtTalent(ingerichtTalent);

        ingerichtTalent.activeer();
        ingerichtTalentRepository.update(ingerichtTalent);
    }

    public void deactiveer(IngerichtTalent ingerichtTalent) {
        valideerIngerichtTalent(ingerichtTalent);

        ingerichtTalent.deactiveer();
        ingerichtTalentRepository.update(ingerichtTalent);
    }

    private void valideerIngerichtTalent(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
    }
}
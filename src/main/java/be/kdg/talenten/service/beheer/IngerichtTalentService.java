package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Doelgroep;
import be.kdg.talenten.domain.IngerichtTalent;
import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.util.ArrayList;
import java.util.List;

public class IngerichtTalentService {

    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final ToewijzingRepository toewijzingRepository;

    public IngerichtTalentService(
            IngerichtTalentRepository ingerichtTalentRepository,
            ToewijzingRepository toewijzingRepository
    ) {
        if (ingerichtTalentRepository == null
                || toewijzingRepository == null) {

            throw new IllegalArgumentException(
                    "Repositories mogen niet null zijn"
            );
        }

        this.ingerichtTalentRepository =
                ingerichtTalentRepository;

        this.toewijzingRepository =
                toewijzingRepository;
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

    public void deactiveer(
            IngerichtTalent ingerichtTalent
    ) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException(
                    "Ingericht talent mag niet null zijn"
            );
        }

        if (ingerichtTalent.getId() == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent moet eerst opgeslagen zijn"
            );
        }

        if (!ingerichtTalent.isActief()) {
            return;
        }

        toewijzingRepository.verwijderVoorIngerichtTalent(
                ingerichtTalent
        );

        List<Leerkracht> gekoppeldeLeerkrachten =
                new ArrayList<>(
                        ingerichtTalent.getLeerkrachten()
                );

        for (Leerkracht leerkracht : gekoppeldeLeerkrachten) {
            ingerichtTalent.verwijderLeerkracht(
                    leerkracht
            );
        }

        ingerichtTalent.deactiveer();

        ingerichtTalentRepository.update(
                ingerichtTalent
        );
    }

    private void valideerIngerichtTalent(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Ingericht talent mag niet null zijn");
        }
    }
}
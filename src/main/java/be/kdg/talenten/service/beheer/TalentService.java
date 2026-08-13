package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Talent;
import be.kdg.talenten.repository.TalentRepository;

import java.util.List;

public class TalentService {

    private final TalentRepository talentRepository;

    public TalentService(TalentRepository talentRepository) {
        if (talentRepository == null) {
            throw new IllegalArgumentException("TalentRepository mag niet null zijn");
        }
        this.talentRepository = talentRepository;
    }

    public List<Talent> geefAlleTalenten() {
        return talentRepository.zoekAlle();
    }

    public Talent maakTalent(String naam, String beschrijving) {
        Talent talent = new Talent(naam, beschrijving);
        return talentRepository.save(talent);
    }

    public void wijzigTalent(Talent talent, String naam, String beschrijving) {
        if (talent == null) {
            throw new IllegalArgumentException("Talent mag niet null zijn");
        }

        talent.wijzigGegevens(naam, beschrijving);
        talentRepository.update(talent);
    }
}
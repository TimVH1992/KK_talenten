package be.kdg.talenten.service.beheer;

import be.kdg.talenten.domain.Leerkracht;
import be.kdg.talenten.repository.LeerkrachtRepository;

import java.util.List;

public class LeerkrachtService {
    private final LeerkrachtRepository leerkrachtRepository;

    public LeerkrachtService(LeerkrachtRepository leerkrachtRepository){
        if (leerkrachtRepository == null){
            throw new IllegalArgumentException("leerkrachtRepository mag niet null zijn");
        }
        this.leerkrachtRepository = leerkrachtRepository;
    }

    public List<Leerkracht> geefAlleLeerkrachten(){
        return leerkrachtRepository.zoekAlle();
    }
    public Leerkracht maakLeerkracht(String voornaam, String achternaam){
        return leerkrachtRepository.save(new Leerkracht(voornaam, achternaam));
    }
    public void wijzigLeerkracht(Leerkracht leerkracht, String voornaam, String achternaam){
        if (leerkracht == null){
            throw new IllegalArgumentException("Leerkracht mag niet null zijn");
        }
        leerkracht.wijzigGegevens(voornaam, achternaam);
        leerkrachtRepository.update(leerkracht);
    }
}

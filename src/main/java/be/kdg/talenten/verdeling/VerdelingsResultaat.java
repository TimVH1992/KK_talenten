package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Toewijzing;

import java.util.ArrayList;
import java.util.List;

public class VerdelingsResultaat {
    private List<Toewijzing> toewijzingen;
    private List<Leerling> nietToegewezenLeerlingen;

    public VerdelingsResultaat() {
        toewijzingen = new ArrayList<>();
        nietToegewezenLeerlingen = new ArrayList<>();
    }

    public void voegToewijzingToe(Toewijzing toewijzing){
        if (toewijzing == null){
            throw new IllegalArgumentException("Toewijzing mag niet null zijn");
        } else {
            toewijzingen.add(toewijzing);
        }
    }

    public void voegNietToegewezenLeerlingToe(Leerling leerling){
        if (leerling == null){
            throw new IllegalArgumentException("Leerling die je wilt toevoegen aan niettoegewezen leerlingen mag niet null zijn");
        } else{
            nietToegewezenLeerlingen.add(leerling);
        }
    }

    public List<Toewijzing> getToewijzingen() {
        return toewijzingen;
    }

    public List<Leerling> getNietToegewezenLeerlingen() {
        return nietToegewezenLeerlingen;
    }

    public int getAantalToewijzingen(){
        return toewijzingen.size();
    }

    public boolean heeftNietToegewezenLeerlingen(){
        return !nietToegewezenLeerlingen.isEmpty();
    }
}

package be.kdg.talenten.service.leerling;

import java.util.List;

public class LeerlingenPlakResultaat {

    private final List<LeerlingPlakRegel> geldigeRegels;
    private final List<String> problemen;

    public LeerlingenPlakResultaat(List<LeerlingPlakRegel> geldigeRegels, List<String> problemen) {
        this.geldigeRegels = List.copyOf(geldigeRegels);
        this.problemen = List.copyOf(problemen);
    }

    public List<LeerlingPlakRegel> getGeldigeRegels() {
        return geldigeRegels;
    }

    public List<String> getProblemen() {
        return problemen;
    }

    public boolean heeftProblemen() {
        return !problemen.isEmpty();
    }

    public int getAantalGeldigeLeerlingen() {
        return geldigeRegels.size();
    }
}
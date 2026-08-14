package be.kdg.talenten.service.leerling;

import be.kdg.talenten.domain.Klas;
import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.service.beheer.LeerlingService;

import java.util.ArrayList;
import java.util.List;

public class LeerlingenPlakService {

    private final LeerlingService leerlingService;

    public LeerlingenPlakService(LeerlingService leerlingService) {
        if (leerlingService == null) {
            throw new IllegalArgumentException("LeerlingService mag niet null zijn");
        }

        this.leerlingService = leerlingService;
    }

    public LeerlingenPlakResultaat analyseer(String tekst) {
        if (tekst == null || tekst.isBlank()) {
            throw new IllegalArgumentException("Er zijn geen leerlinggegevens geplakt");
        }

        List<LeerlingPlakRegel> geldigeRegels = new ArrayList<>();
        List<String> problemen = new ArrayList<>();

        String[] regels = tekst.split("\\R");

        for (int i = 0; i < regels.length; i++) {
            String regel = regels[i];
            int regelNummer = i + 1;

            if (regel == null || regel.isBlank()) {
                continue;
            }

            String[] kolommen = regel.split("\\t", -1);

            if (isHoofding(kolommen)) {
                continue;
            }

            if (kolommen.length != 2) {
                problemen.add(
                        "Regel " + regelNummer
                                + " moet exact twee kolommen bevatten: voornaam en achternaam"
                );
                continue;
            }

            String voornaam = kolommen[0].trim();
            String achternaam = kolommen[1].trim();

            if (voornaam.isBlank()) {
                problemen.add(
                        "Regel " + regelNummer + ": voornaam ontbreekt"
                );
                continue;
            }

            if (achternaam.isBlank()) {
                problemen.add(
                        "Regel " + regelNummer + ": achternaam ontbreekt"
                );
                continue;
            }

            geldigeRegels.add(
                    new LeerlingPlakRegel(
                            regelNummer,
                            voornaam,
                            achternaam
                    )
            );
        }

        if (geldigeRegels.isEmpty() && problemen.isEmpty()) {
            problemen.add("Er werden geen leerlingen gevonden");
        }

        return new LeerlingenPlakResultaat(
                geldigeRegels,
                problemen
        );
    }

    public List<Leerling> slaLeerlingenOp(Klas klas, LeerlingenPlakResultaat resultaat) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }
        if (resultaat == null) {
            throw new IllegalArgumentException("Resultaat mag niet null zijn");
        }
        if (resultaat.heeftProblemen()) {
            throw new IllegalStateException(
                    "Los eerst de fouten in de geplakte leerlinggegevens op"
            );
        }

        List<Leerling> opgeslagenLeerlingen = new ArrayList<>();

        for (LeerlingPlakRegel regel : resultaat.getGeldigeRegels()) {
            Leerling leerling = leerlingService.maakLeerling(
                    regel.voornaam(),
                    regel.achternaam(),
                    klas
            );

            opgeslagenLeerlingen.add(leerling);
        }

        return opgeslagenLeerlingen;
    }

    private boolean isHoofding(String[] kolommen) {
        if (kolommen.length != 2) {
            return false;
        }

        return kolommen[0].trim().equalsIgnoreCase("Voornaam")
                && kolommen[1].trim().equalsIgnoreCase("Achternaam");
    }
}
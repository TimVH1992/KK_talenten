package be.kdg.talenten.domain;

public class Leerling {
    private static long volgendId = 1;

    private long id;
    private String voornaam;
    private String achternaam;
    private Klas klas;

    public Leerling(String voornaam, String achternaam, Klas klas) {
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("Voornaam mag niet leeg zijn.");
        }

        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("Achternaam mag niet leeg zijn.");
        }
        if (klas == null){
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }
        this.id = volgendId++;
        this.voornaam = voornaam;
        this.achternaam = achternaam;
        this.klas = klas;
    }

    public void wijsKlasToe(Klas klas) {
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn.");
        }
        this.klas = klas;
    }

    public Klas getKlas() {
        return klas;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    @Override
    public String toString() {
        return voornaam + " " + achternaam;
    }
}

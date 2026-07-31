package be.kdg.talenten.domain;

public class Leerling {
    private static long volgendId = 1;

    private final Long id;
    private final String voornaam;
    private final String achternaam;
    private Klas klas;

    public Leerling(String voornaam, String achternaam, Klas klas) {
        this(volgendId++,voornaam, achternaam, klas);
    }

    public Leerling(long id, String voornaam, String achternaam, Klas klas) {
        if (id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0.");
        }
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("Voornaam mag niet leeg zijn.");
        }

        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("Achternaam mag niet leeg zijn.");
        }
        if (klas == null){
            throw new IllegalArgumentException("Klas mag niet null zijn");
        }
        this.id = id;
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

    public long getId() {
        return id;
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

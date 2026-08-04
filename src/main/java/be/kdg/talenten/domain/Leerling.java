package be.kdg.talenten.domain;

import java.util.Objects;

public class Leerling {
    private final Long id;
    private final String voornaam;
    private final String achternaam;
    private Klas klas;

    public Leerling(String voornaam, String achternaam, Klas klas) {
        this(null ,voornaam, achternaam, klas);
    }

    public Leerling(Long id, String voornaam, String achternaam, Klas klas) {
        if (id != null && id < 1) {
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

    public Long getId() {
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
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Leerling leerling = (Leerling) o;
        return Objects.equals(id, leerling.id) && Objects.equals(voornaam, leerling.voornaam) && Objects.equals(achternaam, leerling.achternaam);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, voornaam, achternaam);
    }

    @Override
    public String toString() {
        return voornaam + " " + achternaam;
    }
}

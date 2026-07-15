package be.kdg.talenten.domain;

public class Leerkracht {
    private static long volgendeId = 1;

    private long id;
    private String voornaam;
    private String achternaam;

    public Leerkracht(String voornaam, String achternaam) {
        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("De achternaam van de leerkracht mag niet null of leeg zijn");
        }
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("De voornaam van de leerkracht mag niet leeg of null zijn");
        }
        this.id = volgendeId++;
        this.achternaam = achternaam;
        this.voornaam = voornaam;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return "Leerkracht{" +
                "id=" + id +
                ", voornaam='" + voornaam + '\'' +
                ", achternaam='" + achternaam + '\'' +
                '}';
    }
}

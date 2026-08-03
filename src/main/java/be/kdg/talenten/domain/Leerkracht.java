package be.kdg.talenten.domain;

public class Leerkracht {

    private Long id;
    private String voornaam;
    private String achternaam;

    public Leerkracht(String voornaam, String achternaam) {
        this(null, voornaam, achternaam);
    }

    public Leerkracht(Long id, String voornaam, String achternaam) {
        if (id != null && id <1)
        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("De achternaam van de leerkracht mag niet null of leeg zijn");
        }
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("De voornaam van de leerkracht mag niet leeg of null zijn");
        }
        this.id = id;
        this.achternaam = achternaam;
        this.voornaam = voornaam;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public Long getId() {
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

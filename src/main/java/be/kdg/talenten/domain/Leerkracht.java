package be.kdg.talenten.domain;

import java.util.Objects;

public class Leerkracht {

    private Long id;
    private String voornaam;
    private String achternaam;
    private boolean actief;

    public Leerkracht(String voornaam, String achternaam) {
        this(null, voornaam, achternaam);
    }

    public Leerkracht(Long id, String voornaam, String achternaam) {
        this(id, voornaam, achternaam, true);
    }

    public Leerkracht(Long id, String voornaam, String achternaam, Boolean actief) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("Id moet groter zijn dan 0.");
        }
        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("De achternaam van de leerkracht mag niet null of leeg zijn");
        }
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("De voornaam van de leerkracht mag niet leeg of null zijn");
        }
        this.id = id;
        this.voornaam = voornaam;
        this.achternaam = achternaam;
        this.actief = actief;
    }

    public void wijzigGegevens(String voornaam, String achternaam) {
        this.voornaam = voornaam;
        this.achternaam = achternaam;
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

    public boolean isActief() {
        return actief;
    }

    public void deactiveer() {
        this.actief = false;
    }

    public void activeer() {
        this.actief = true;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Leerkracht that = (Leerkracht) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
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

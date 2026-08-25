package be.kdg.talenten.domain;

public class Leerling {

    private final Long id;
    private String voornaam;
    private String achternaam;
    private Klas klas;
    private boolean actief;

    public Leerling(String voornaam, String achternaam, Klas klas) {
        this(null, voornaam, achternaam, klas, true);
    }

    public Leerling(Long id, String voornaam, String achternaam, Klas klas) {
        this(id, voornaam, achternaam, klas, true);
    }

    public Leerling(Long id, String voornaam, String achternaam, Klas klas, boolean actief) {
        valideerGegevens(voornaam, achternaam, klas);

        if (id != null && id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0.");
        }

        this.id = id;
        this.voornaam = voornaam;
        this.achternaam = achternaam;
        this.klas = klas;
        this.actief = actief;
    }

    public void wijzigGegevens(String voornaam, String achternaam, Klas klas) {
        valideerGegevens(voornaam, achternaam, klas);

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

    public void deactiveer() {
        actief = false;
    }

    public void activeer() {
        actief = true;
    }

    private void valideerGegevens(String voornaam, String achternaam, Klas klas) {
        if (voornaam == null || voornaam.isBlank()) {
            throw new IllegalArgumentException("Voornaam mag niet leeg zijn.");
        }
        if (achternaam == null || achternaam.isBlank()) {
            throw new IllegalArgumentException("Achternaam mag niet leeg zijn.");
        }
        if (klas == null) {
            throw new IllegalArgumentException("Klas mag niet null zijn.");
        }
    }

    public Long getId() {
        return id;
    }

    public String getVoornaam() {
        return voornaam;
    }

    public String getAchternaam() {
        return achternaam;
    }

    public Klas getKlas() {
        return klas;
    }

    public boolean isActief() {
        return actief;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Leerling leerling = (Leerling) o;

        if (id == null || leerling.id == null) {
            return false;
        }

        return id.equals(leerling.id);
    }

    @Override
    public int hashCode() {
        return id != null
                ? id.hashCode()
                : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return voornaam + " " + achternaam;
    }
}
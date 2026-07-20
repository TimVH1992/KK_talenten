package be.kdg.talenten.domain;

public class Talent {
    private static int volgendeId = 1;

    private long id;
    private String naam;
    private String beschrijving;

    public Talent(String naam, String beschrijving) {
        this.id = volgendeId++;

        this.naam = naam;
        this.beschrijving = beschrijving;
    }

    public String getNaam() {
        return naam;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public long getId() {
        return id;
    }

    @Override
    public String toString() {
        return naam;
    }
}

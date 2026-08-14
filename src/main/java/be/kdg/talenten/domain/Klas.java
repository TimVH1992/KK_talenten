package be.kdg.talenten.domain;

import java.util.Objects;

public class Klas {

    private final Long id;
    private String naam;
    private final Schooljaar schooljaar;
    private int leerjaar;
    private Doelgroep doelgroep;

    public Klas(String naam, Schooljaar schooljaar, int leerjaar, Doelgroep doelgroep) {
        this(null, naam, schooljaar, leerjaar, doelgroep);
    }

    public Klas(Long id, String naam, Schooljaar schooljaar, int leerjaar, Doelgroep doelgroep) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0");
        }

        valideerGegevens(naam, leerjaar, doelgroep);

        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar is ongeldig");
        }

        this.id = id;
        this.naam = naam;
        this.schooljaar = schooljaar;
        this.leerjaar = leerjaar;
        this.doelgroep = doelgroep;
    }

    public void wijzigGegevens(String naam, int leerjaar, Doelgroep doelgroep) {
        valideerGegevens(naam, leerjaar, doelgroep);

        this.naam = naam;
        this.leerjaar = leerjaar;
        this.doelgroep = doelgroep;
    }

    private void valideerGegevens(String naam, int leerjaar, Doelgroep doelgroep) {
        if (naam == null || naam.isBlank()) {
            throw new IllegalArgumentException("Naam van de klas mag niet leeg zijn");
        }
        if (leerjaar < 1 || leerjaar > 7) {
            throw new IllegalArgumentException("Het leerjaar moet tussen 1 en 7 liggen");
        }
        if (doelgroep == null) {
            throw new IllegalArgumentException("Doelgroep mag niet null zijn");
        }
    }

    public Long getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public Schooljaar getSchooljaar() {
        return schooljaar;
    }

    public int getLeerjaar() {
        return leerjaar;
    }

    public Doelgroep getDoelgroep() {
        return doelgroep;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Klas klas = (Klas) o;

        if (id != null && klas.id != null) {
            return id.equals(klas.id);
        }

        return Objects.equals(naam, klas.naam)
                && Objects.equals(schooljaar, klas.schooljaar);
    }

    @Override
    public int hashCode() {
        return id != null
                ? id.hashCode()
                : Objects.hash(naam, schooljaar);
    }

    @Override
    public String toString() {
        return naam;
    }
}
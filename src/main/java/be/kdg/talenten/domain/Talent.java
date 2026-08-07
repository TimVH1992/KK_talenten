package be.kdg.talenten.domain;

import java.util.Objects;

public class Talent {
    private Long id;
    private String naam;
    private String beschrijving;

    public Talent(String naam, String beschrijving) {
        this(null, naam, beschrijving);
    }

    public Talent(Long id, String naam, String beschrijving) {
        if (naam == null){
            throw new IllegalArgumentException("Naam mag niet null zijn");
        }
        if (beschrijving == null){
            throw new IllegalArgumentException("beschrijving mag niet null zijn");
        }
        this.id = id;
        this.naam = naam;
        this.beschrijving = beschrijving;
    }

    public String getNaam() {
        return naam;
    }

    public String getBeschrijving() {
        return beschrijving;
    }

    public Long getId() {
        return id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Talent talent = (Talent) o;

        if (id == null || talent.id == null) {
            return false;
        }

        return id.equals(talent.id);
    }

    @Override
    public int hashCode() {
        return id != null
                ? id.hashCode()
                : System.identityHashCode(this);
    }

    @Override
    public String toString() {
        return naam;
    }
}

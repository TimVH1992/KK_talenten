package be.kdg.talenten.domain;

import java.nio.file.FileStore;
import java.util.Objects;

public class Klas {

    private final Long id;
    private final String naam;
    private final Schooljaar schooljaar;
    private final int leerjaar;
    private final Doelgroep doelgroep;

    public Klas(String naam, Schooljaar schooljaar, int leerjaar, Doelgroep doelgroep) {
        this(null, naam, schooljaar, leerjaar, doelgroep);
    }

    public Doelgroep getDoelgroep() {
        return doelgroep;
    }

    public Klas(Long id, String naam, Schooljaar schooljaar, int leerjaar, Doelgroep doelgroep) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0");
        }
        if (naam == null || naam.isBlank()) {
            throw new IllegalArgumentException(
                    "Naam van de klas mag niet leeg zijn"
            );
        }
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar is ongeldig");
        }
        if (leerjaar < 1 || leerjaar > 7) {
            throw new IllegalArgumentException(
                    "Het leerjaar moet tussen 1 en 7 liggen"
            );
        }
        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "Doelgroep mag niet null zijn"
            );
        }

        this.id = id;
        this.naam = naam;
        this.schooljaar = schooljaar;
        this.leerjaar = leerjaar;
        this.doelgroep = doelgroep;
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


    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        Klas klas = (Klas) o;
        return Objects.equals(naam, klas.naam) && Objects.equals(schooljaar, klas.schooljaar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(naam, schooljaar);
    }

    @Override
    public String toString() {
        return "Klas{" +
                "id=" + id +
                ", naam='" + naam + '\'' +
                ", schooljaar='" + schooljaar + '\'' +
                '}';
    }

}

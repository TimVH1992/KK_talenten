package be.kdg.talenten.domain;

public class Klas {
    private static long volgendId = 1;

    private final Long id;
    private final String naam;
    private final String schooljaar;
    private final int leerjaar;

    public Klas(String naam, String schooljaar, int leerjaar) {
        this(volgendId++, naam, schooljaar, leerjaar);
    }

    public Klas(long id, String naam, String schooljaar, int leerjaar) {
        if (id < 1) {
            throw new IllegalArgumentException("ID moet groter zijn dan 0");
        }
        if (naam == null || naam.isBlank()) {
            throw new IllegalArgumentException(
                    "Naam van de klas mag niet leeg zijn"
            );
        }
        if (schooljaar == null || schooljaar.isBlank()) {
            throw new IllegalArgumentException("Schooljaar is ongeldig");
        }
        if (leerjaar < 1 || leerjaar > 7) {
            throw new IllegalArgumentException(
                    "Het leerjaar moet tussen 1 en 7 liggen"
            );
        }

        this.id = id;
        this.naam = naam;
        this.schooljaar = schooljaar;
        this.leerjaar = leerjaar;
    }

    public Doelgroep bepaalDoelgroep() {
        if (leerjaar <= 3) {
            return Doelgroep.EERSTE_TOT_EN_MET_DERDE_JAAR;
        }

        return Doelgroep.VANAF_VIERDE_JAAR;
    }

    public long getId() {
        return id;
    }

    public String getNaam() {
        return naam;
    }

    public String getSchooljaar() {
        return schooljaar;
    }

    public int getLeerjaar() {
        return leerjaar;
    }

    public static long getVolgendId() {
        return volgendId;
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

package be.kdg.talenten.domain;

public class Klas {
    private static long volgendId = 1;

    private final long id;
    private final String naam;
    private final String schooljaar;
    private final int leerjaar;

    public Klas(String naam, String schooljaar, int leerjaar){
        if (naam == null || naam.isBlank()){
            throw new IllegalArgumentException("Naam van de klas mag niet leeg zijn");
        }
        if (schooljaar == null || schooljaar.isEmpty()){
            throw new IllegalArgumentException("Schooljaar is ongeldig");
        }
        this.id = volgendId++;
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

    @Override
    public String toString() {
        return "Klas{" +
                "id=" + id +
                ", naam='" + naam + '\'' +
                ", schooljaar='" + schooljaar + '\'' +
                '}';
    }
}

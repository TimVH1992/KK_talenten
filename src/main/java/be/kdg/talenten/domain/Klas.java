package be.kdg.talenten.domain;

public class Klas {
    private static long volgendId = 1;

    private long id;
    private String naam;
    private String schooljaar;

    public Klas(String naam, String schooljaar){
        if (naam == null || naam.isBlank()){
            throw new IllegalArgumentException("Naam van de klas mag niet leeg zijn");
        }
        if (schooljaar == null || schooljaar.isEmpty()){
            throw new IllegalArgumentException("Schooljaar is ongeldig");
        }
        this.id = volgendId++;
        this.naam = naam;
        this.schooljaar = schooljaar;
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

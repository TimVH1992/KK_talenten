package be.kdg.talenten.domain;

public class Leerkracht {
    private static long volgendeId = 1;

    private long id;
    private String voornaam;
    private String achternaam;

    public Leerkracht(String achternaam, String voornaam) {
        this.id = volgendeId++;
        this.achternaam = achternaam;
        this.voornaam = voornaam;
    }
}

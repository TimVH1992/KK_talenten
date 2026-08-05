package be.kdg.talenten.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class IngerichtTalent {
    private final Long id;
    private final Talent talent;
    private final TalentenPeriode talentenPeriode;
    private int maxCapaciteit;
    private final List<Leerkracht> leerkrachten;
    private final Doelgroep doelgroep;

    public IngerichtTalent(
            Talent talent,
            TalentenPeriode talentenPeriode,
            int maxCapaciteit,
            Doelgroep doelgroep,
            List<Leerkracht> leerkrachten
    ) {
        this(
                null,
                talent,
                talentenPeriode,
                maxCapaciteit,
                doelgroep,
                leerkrachten
        );
    }

    public IngerichtTalent(
            Long id,
            Talent talent,
            TalentenPeriode talentenPeriode,
            int maxCapaciteit,
            Doelgroep doelgroep,
            List<Leerkracht> leerkrachten
    ) {
        if (id != null && id < 1) {
            throw new IllegalArgumentException(
                    "ID moet groter zijn dan 0."
            );
        }

        if (talent == null) {
            throw new IllegalArgumentException(
                    "Talent mag niet null zijn."
            );
        }

        if (talentenPeriode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn."
            );
        }

        if (maxCapaciteit < 1) {
            throw new IllegalArgumentException(
                    "De maximumcapaciteit moet minstens 1 zijn."
            );
        }

        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "Doelgroep mag niet null zijn."
            );
        }

        valideerLeerkrachten(leerkrachten);

        this.id = id;
        this.talent = talent;
        this.talentenPeriode = talentenPeriode;
        this.maxCapaciteit = maxCapaciteit;
        this.doelgroep = doelgroep;
        this.leerkrachten = new ArrayList<>(leerkrachten);
    }

    private static void valideerLeerkrachten(
            List<Leerkracht> leerkrachten
    ) {
        if (leerkrachten == null || leerkrachten.isEmpty()) {
            throw new IllegalArgumentException(
                    "Een ingericht talent moet minstens één leerkracht hebben."
            );
        }

        if (leerkrachten.size() > 2) {
            throw new IllegalArgumentException(
                    "Een ingericht talent mag maximaal twee leerkrachten hebben."
            );
        }

        if (leerkrachten.stream().anyMatch(leerkracht -> leerkracht == null)) {
            throw new IllegalArgumentException(
                    "De lijst van leerkrachten mag geen null-element bevatten."
            );
        }

        if (leerkrachten.stream().distinct().count() != leerkrachten.size()) {
            throw new IllegalArgumentException(
                    "Dezelfde leerkracht mag niet meermaals toegevoegd worden."
            );
        }
    }

    public void voegLeerkrachtToe(Leerkracht leerkracht) {
        if (leerkracht == null) {
            throw new IllegalArgumentException(
                    "Leerkracht mag niet null zijn."
            );
        }

        if (leerkrachten.contains(leerkracht)) {
            return;
        }

        if (leerkrachten.size() >= 2) {
            throw new IllegalStateException(
                    "Een ingericht talent mag maximaal twee leerkrachten hebben."
            );
        }

        leerkrachten.add(leerkracht);
    }

    public void setMaxCapaciteit(int maxCapaciteit) {
        if (maxCapaciteit < 1) {
            throw new IllegalArgumentException(
                    "De maximumcapaciteit moet minstens 1 zijn."
            );
        }

        this.maxCapaciteit = maxCapaciteit;
    }

    public boolean behoortTot(TalentenPeriode talentenPeriode) {
        if (talentenPeriode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn."
            );
        }

        return this.talentenPeriode.equals(talentenPeriode);
    }

    public boolean heeftVrijePlaats(int aantalToewijzingen) {
        if (aantalToewijzingen < 0) {
            throw new IllegalArgumentException(
                    "Het aantal toewijzingen mag niet negatief zijn."
            );
        }

        return aantalToewijzingen < maxCapaciteit;
    }

    public boolean isGeschiktVoor(Leerling leerling) {
        if (leerling == null) {
            throw new IllegalArgumentException(
                    "Leerling mag niet null zijn."
            );
        }

        return doelgroep == leerling.getKlas().getDoelgroep();
    }

    public Long getId() {
        return id;
    }

    public Talent getTalent() {
        return talent;
    }

    public TalentenPeriode getTalentenPeriode() {
        return talentenPeriode;
    }

    public int getMaxCapaciteit() {
        return maxCapaciteit;
    }

    public List<Leerkracht> getLeerkrachten() {
        return List.copyOf(leerkrachten);
    }

    public Doelgroep getDoelgroep() {
        return doelgroep;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        IngerichtTalent ander = (IngerichtTalent) object;
        return id != null && id.equals(ander.id);
    }

    @Override
    public int hashCode() {
        return id != null ? id.hashCode() : System.identityHashCode(this);
    }



    @Override
    public String toString() {
        return talent.getNaam()
                + " ("
                + talentenPeriode.getNaam()
                + ")";
    }
}
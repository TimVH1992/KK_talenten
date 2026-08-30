package be.kdg.talenten.repository.inmemory;

import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.ToewijzingRepository;

import java.util.ArrayList;
import java.util.List;

public class InMemoryToewijzingRepository implements ToewijzingRepository {
    private List<Toewijzing> historischeToewijzingen;
    private List<Toewijzing> opgeslagenToewijzingen;

    public InMemoryToewijzingRepository(List<Toewijzing> historischeToewijzingen) {
        this.historischeToewijzingen = historischeToewijzingen;
        this.opgeslagenToewijzingen = new ArrayList<>();
    }

    @Override
    public List<Toewijzing> zoekHistorischeToewijzingenVoorSchooljaar(Schooljaar schooljaar) {
        List<Toewijzing> toewijzingenDitSchooljaar = new ArrayList<>();
        if (schooljaar == null){
            throw new IllegalArgumentException("schooljaar mag niet null zijn");
        }

        for (Toewijzing t : historischeToewijzingen){
            if (t.getIngerichtTalent().getTalentenPeriode().getSchooljaar().equals(schooljaar)){
                toewijzingenDitSchooljaar.add(t);
            }
        }
        return toewijzingenDitSchooljaar;
    }

    @Override
    public void saveAll(List<Toewijzing> toewijzingen) {
        opgeslagenToewijzingen.addAll(toewijzingen);
    }

    @Override
    public void vervangAutomatischeToewijzingenVoorPeriode(TalentenPeriode periode, List<Toewijzing> nieuweToewijzingen) {
        if (periode == null) {
            throw new IllegalArgumentException("De talentenperiode mag niet null zijn");
        }
        if (nieuweToewijzingen == null) {
            throw new IllegalArgumentException("De nieuwe toewijzingen mogen niet null zijn");
        }

        for (Toewijzing toewijzing : nieuweToewijzingen) {
            if (toewijzing == null) {
                throw new IllegalArgumentException("De lijst mag geen null-toewijzingen bevatten");
            }
            if (toewijzing.getToewijzingsType() != ToewijzingsType.AUTOMATISCH) {
                throw new IllegalArgumentException("Alle nieuwe toewijzingen moeten automatisch zijn");
            }
            if (!toewijzing.getIngerichtTalent().getTalentenPeriode().equals(periode)) {
                throw new IllegalArgumentException("Alle nieuwe toewijzingen moeten tot de gekozen periode behoren");
            }
        }

        opgeslagenToewijzingen.removeIf(toewijzing -> toewijzing.getToewijzingsType() == ToewijzingsType.AUTOMATISCH
                && toewijzing.getIngerichtTalent().getTalentenPeriode().equals(periode));
        opgeslagenToewijzingen.addAll(nieuweToewijzingen);
    }

    public List<Toewijzing> getOpgeslagenToewijzingen() {
        return opgeslagenToewijzingen;
    }

    @Override
    public Toewijzing zoekToewijzingVoorLeerlingEnPeriode(
            Leerling leerling,
            TalentenPeriode talentenPeriode
    ) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn.");
        }

        if (talentenPeriode == null) {
            throw new IllegalArgumentException("De talentenperiode mag niet null zijn.");
        }

        for (Toewijzing toewijzing : opgeslagenToewijzingen) {
            boolean zelfdeLeerling = toewijzing.getLeerling() == leerling;

            boolean zelfdeTalentenPeriode =
                    toewijzing.getIngerichtTalent()
                            .getTalentenPeriode() == talentenPeriode;

            if (zelfdeLeerling && zelfdeTalentenPeriode) {
                return toewijzing;
            }
        }

        return null;
    }

    @Override
    public int telToewijzingenVoorIngerichtTalent(IngerichtTalent ingerichtTalent) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException("Het ingerichtTalent mag niet null zijn");
        }
        int aantalToewijzingen = 0;

        for (Toewijzing toewijzing : opgeslagenToewijzingen) {
            if (toewijzing.getIngerichtTalent() == ingerichtTalent) {
                aantalToewijzingen++;
            }
        }
        return aantalToewijzingen;
    }

    @Override
    public Toewijzing save(Toewijzing toewijzing) {
        if (toewijzing == null){
            throw new IllegalArgumentException("Toewijzing die je wilt opslaan mag niet null zijn");
        }
        opgeslagenToewijzingen.add(toewijzing);
        return toewijzing;
    }

    @Override
    public Toewijzing update(Toewijzing toewijzing) {
        if (toewijzing == null) {
            throw new IllegalArgumentException("Toewijzing die je wilt updaten mag niet null zijn.");
        }

        boolean gevonden = false;

        for (Toewijzing opgeslagenToewijzing : opgeslagenToewijzingen) {
            if (opgeslagenToewijzing == toewijzing) {
                gevonden = true;
                break;
            }
        }

        if (!gevonden) {
            throw new IllegalArgumentException("De toewijzing die je wilt updaten bestaat niet.");
        }
        return toewijzing;
    }

    @Override
    public List<Toewijzing> zoekVoorPeriode(TalentenPeriode periode) {
        if (periode == null){
            throw new IllegalArgumentException("Periode mag niet null zijn");
        }
        List<Toewijzing> toewijzingenVoorPeriode = new ArrayList<>();
        for (Toewijzing toewijzing : opgeslagenToewijzingen){
            if (toewijzing.getIngerichtTalent().getTalentenPeriode() == periode){
                toewijzingenVoorPeriode.add(toewijzing);
            }
        }
        return toewijzingenVoorPeriode;
    }
    @Override
    public List<Toewijzing> zoekHistorischeToewijzingenVoorLeerlingEnSchooljaar(Leerling leerling, Schooljaar schooljaar) {
        if (leerling == null) {
            throw new IllegalArgumentException("De leerling mag niet null zijn");
        }
        if (schooljaar == null) {
            throw new IllegalArgumentException("Het schooljaar mag niet null zijn");
        }

        return historischeToewijzingen.stream()
                .filter(toewijzing -> toewijzing.getLeerling().equals(leerling))
                .filter(toewijzing -> toewijzing.getIngerichtTalent()
                        .getTalentenPeriode()
                        .getSchooljaar()
                        .equals(schooljaar))
                .toList();
    }

    @Override
    public void verwijderVoorIngerichtTalent(
            IngerichtTalent ingerichtTalent
    ) {
        if (ingerichtTalent == null) {
            throw new IllegalArgumentException(
                    "Het ingerichte talent mag niet null zijn"
            );
        }

        opgeslagenToewijzingen.removeIf(
                toewijzing ->
                        toewijzing.getIngerichtTalent()
                                == ingerichtTalent
        );
    }

    @Override
    public void vervangAutomatischeToewijzingenVoorPeriodeEnDoelgroep(
            TalentenPeriode periode,
            Doelgroep doelgroep,
            List<Toewijzing> nieuweToewijzingen
    ) {
        if (periode == null) {
            throw new IllegalArgumentException(
                    "Talentenperiode mag niet null zijn"
            );
        }

        if (doelgroep == null) {
            throw new IllegalArgumentException(
                    "Doelgroep mag niet null zijn"
            );
        }

        if (nieuweToewijzingen == null) {
            throw new IllegalArgumentException(
                    "Nieuwe toewijzingen mogen niet null zijn"
            );
        }

        for (Toewijzing toewijzing : nieuweToewijzingen) {
            if (toewijzing == null) {
                throw new IllegalArgumentException(
                        "De lijst mag geen null-toewijzingen bevatten"
                );
            }

            if (toewijzing.getToewijzingsType()
                    != ToewijzingsType.AUTOMATISCH) {

                throw new IllegalArgumentException(
                        "Alle nieuwe toewijzingen moeten automatisch zijn"
                );
            }

            if (!toewijzing
                    .getIngerichtTalent()
                    .getTalentenPeriode()
                    .equals(periode)) {

                throw new IllegalArgumentException(
                        "Alle nieuwe toewijzingen moeten tot de gekozen periode behoren"
                );
            }

            if (toewijzing
                    .getIngerichtTalent()
                    .getDoelgroep()
                    != doelgroep) {

                throw new IllegalArgumentException(
                        "Alle nieuwe toewijzingen moeten tot de gekozen doelgroep behoren"
                );
            }
        }

        opgeslagenToewijzingen.removeIf(
                toewijzing ->
                        toewijzing.getToewijzingsType()
                                == ToewijzingsType.AUTOMATISCH
                                && toewijzing
                                .getIngerichtTalent()
                                .getTalentenPeriode()
                                .equals(periode)
                                && toewijzing
                                .getIngerichtTalent()
                                .getDoelgroep()
                                == doelgroep
        );

        opgeslagenToewijzingen.addAll(
                nieuweToewijzingen
        );
    }

}

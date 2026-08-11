package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;

import java.util.*;

public class AutomatischeVerdeler {
    private final List<Leerling> leerlingen;
    private final List<Voorkeur> voorkeuren;
    private final List<Toewijzing> historischeToewijzingen;
    private final List<Toewijzing> vasteToewijzingen;

    private static final int MAX_LEERLINGEN_PER_KLAS_PER_TALENT = 2;

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren) {
        this(voorkeuren, List.of(), List.of());
    }

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren, List<Toewijzing> historischeToewijzingen) {
        this(voorkeuren, historischeToewijzingen, List.of());
    }

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren, List<Toewijzing> historischeToewijzingen, List<Toewijzing> vasteToewijzingen) {
        this(haalLeerlingenUitVoorkeuren(voorkeuren), voorkeuren, historischeToewijzingen, vasteToewijzingen);
    }

    public AutomatischeVerdeler(List<Leerling> leerlingen, List<Voorkeur> voorkeuren, List<Toewijzing> historischeToewijzingen, List<Toewijzing> vasteToewijzingen) {
        if (leerlingen == null) {
            throw new IllegalArgumentException("Leerlingen mogen niet null zijn.");
        }
        if (voorkeuren == null) {
            throw new IllegalArgumentException("Voorkeuren mogen niet null zijn.");
        }
        if (historischeToewijzingen == null) {
            throw new IllegalArgumentException("Historische toewijzingen mogen niet null zijn.");
        }
        if (vasteToewijzingen == null) {
            throw new IllegalArgumentException("Vaste toewijzingen mogen niet null zijn.");
        }

        this.leerlingen = leerlingen;
        this.voorkeuren = voorkeuren;
        this.historischeToewijzingen = historischeToewijzingen;
        this.vasteToewijzingen = vasteToewijzingen;
    }

    public VerdelingsResultaat verdeel() {
        VerdelingsResultaat verdelingsResultaat = new VerdelingsResultaat();

        Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling = groepeerVoorkeurenPerLeerling();
        Map<IngerichtTalent, Integer> bezetting = new HashMap<>();
        Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas = new HashMap<>();

        registreerVasteToewijzingen(voorkeurenPerLeerling, bezetting, bezettingPerKlas);

        List<Leerling> leerlingenInVolgorde = bepaalLeerlingVolgorde(voorkeurenPerLeerling);

        for (Leerling leerling : leerlingenInVolgorde) {
            List<Voorkeur> voorkeurenVanLeerling = voorkeurenPerLeerling.get(leerling);

            if (voorkeurenVanLeerling.size() != 3) {
                verdelingsResultaat.voegNietToegewezenLeerlingToe(leerling);
                continue;
            }

            voorkeurenVanLeerling.sort(Comparator.comparingInt(Voorkeur::getVoorkeurNummer));

            Toewijzing toewijzing = zoekToewijzingZonderHistoriek(leerling, voorkeurenVanLeerling, bezetting, bezettingPerKlas);

            if (toewijzing == null) {
                toewijzing = zoekToewijzingMetHistoriekToegestaan(leerling, voorkeurenVanLeerling, bezetting, bezettingPerKlas);
            }

            if (toewijzing != null) {
                verdelingsResultaat.voegToewijzingToe(toewijzing);
                verhoogBezetting(toewijzing, bezetting, bezettingPerKlas);
            } else {
                verdelingsResultaat.voegNietToegewezenLeerlingToe(leerling);
            }
        }

        return verdelingsResultaat;
    }

    private void registreerVasteToewijzingen(Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling, Map<IngerichtTalent, Integer> bezetting, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        for (Toewijzing vasteToewijzing : vasteToewijzingen) {
            voorkeurenPerLeerling.remove(vasteToewijzing.getLeerling());
            verhoogBezetting(vasteToewijzing, bezetting, bezettingPerKlas);
        }
    }

    private void verhoogBezetting(Toewijzing toewijzing, Map<IngerichtTalent, Integer> bezetting, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        IngerichtTalent ingerichtTalent = toewijzing.getIngerichtTalent();

        int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);
        bezetting.put(ingerichtTalent, huidigAantal + 1);

        verhoogKlasBezetting(ingerichtTalent, toewijzing.getLeerling().getKlas(), bezettingPerKlas);
    }

    private Toewijzing zoekToewijzingZonderHistoriek(Leerling leerling, List<Voorkeur> voorkeurenVanLeerling, Map<IngerichtTalent, Integer> bezetting, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftLeerlingTalentAlGevolgd(leerling, ingerichtTalent.getTalent())) {
                continue;
            }

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)
                    && heeftKlasNogPlaatsVoorIngerichtTalent(ingerichtTalent, leerling.getKlas(), bezettingPerKlas)) {

                return new Toewijzing(leerling, ingerichtTalent, ToewijzingsType.AUTOMATISCH, voorkeur.getVoorkeurNummer());
            }
        }

        return null;
    }

    private Toewijzing zoekToewijzingMetHistoriekToegestaan(Leerling leerling, List<Voorkeur> voorkeurenVanLeerling, Map<IngerichtTalent, Integer> bezetting, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)
                    && heeftKlasNogPlaatsVoorIngerichtTalent(ingerichtTalent, leerling.getKlas(), bezettingPerKlas)) {

                return new Toewijzing(leerling, ingerichtTalent, ToewijzingsType.AUTOMATISCH, voorkeur.getVoorkeurNummer());
            }
        }

        return null;
    }

    private boolean heeftIngerichtTalentVrijePlaats(IngerichtTalent ingerichtTalent, Map<IngerichtTalent, Integer> bezetting) {
        int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);
        return ingerichtTalent.heeftVrijePlaats(huidigAantal);
    }

    private boolean heeftLeerlingTalentAlGevolgd(Leerling leerling, Talent talent) {
        for (Toewijzing historischeToewijzing : historischeToewijzingen) {
            boolean zelfdeLeerling = historischeToewijzing.getLeerling().equals(leerling);
            boolean zelfdeTalent = historischeToewijzing.getIngerichtTalent().getTalent().equals(talent);

            if (zelfdeLeerling && zelfdeTalent) {
                return true;
            }
        }

        return false;
    }

    private Map<Leerling, List<Voorkeur>> groepeerVoorkeurenPerLeerling() {
        Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling = new LinkedHashMap<>();

        for (Leerling leerling : leerlingen) {
            voorkeurenPerLeerling.put(leerling, new ArrayList<>());
        }

        for (Voorkeur voorkeur : voorkeuren) {
            Leerling leerling = voorkeur.getLeerling();
            voorkeurenPerLeerling.computeIfAbsent(leerling, l -> new ArrayList<>()).add(voorkeur);
        }

        return voorkeurenPerLeerling;
    }

    private List<Leerling> bepaalLeerlingVolgorde(Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling) {
        List<Leerling> leerlingen = new ArrayList<>(voorkeurenPerLeerling.keySet());

        leerlingen.sort((leerling1, leerling2) ->
                Integer.compare(berekenVerdelingsPrioriteit(leerling2), berekenVerdelingsPrioriteit(leerling1))
        );

        return leerlingen;
    }

    private int berekenVerdelingsPrioriteit(Leerling leerling) {
        Toewijzing laatsteToewijzing = zoekLaatsteHistorischeToewijzing(leerling);

        if (laatsteToewijzing == null || laatsteToewijzing.getVoorkeurNummer() == null) {
            return 2;
        }

        return laatsteToewijzing.getVoorkeurNummer();
    }

    private Toewijzing zoekLaatsteHistorischeToewijzing(Leerling leerling) {
        Toewijzing laatsteToewijzing = null;

        for (Toewijzing historischeToewijzing : historischeToewijzingen) {
            boolean zelfdeLeerling = historischeToewijzing.getLeerling().equals(leerling);

            if (zelfdeLeerling && (laatsteToewijzing == null || isNieuwerDan(historischeToewijzing, laatsteToewijzing))) {
                laatsteToewijzing = historischeToewijzing;
            }
        }

        return laatsteToewijzing;
    }

    private boolean isNieuwerDan(Toewijzing kandidaat, Toewijzing huidigeLaatste) {
        return kandidaat.getIngerichtTalent().getTalentenPeriode().getEindDatum()
                .isAfter(huidigeLaatste.getIngerichtTalent().getTalentenPeriode().getEindDatum());
    }

    private boolean heeftKlasNogPlaatsVoorIngerichtTalent(IngerichtTalent ingerichtTalent, Klas klas, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        Map<Klas, Integer> klasBezetting = bezettingPerKlas.get(ingerichtTalent);

        if (klasBezetting == null) {
            return true;
        }

        int huidigAantalVanKlas = klasBezetting.getOrDefault(klas, 0);

        return huidigAantalVanKlas < MAX_LEERLINGEN_PER_KLAS_PER_TALENT;
    }

    private void verhoogKlasBezetting(IngerichtTalent ingerichtTalent, Klas klas, Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        Map<Klas, Integer> klasBezetting = bezettingPerKlas.computeIfAbsent(ingerichtTalent, talent -> new HashMap<>());

        int huidigAantalVanKlas = klasBezetting.getOrDefault(klas, 0);
        klasBezetting.put(klas, huidigAantalVanKlas + 1);
    }

    private static List<Leerling> haalLeerlingenUitVoorkeuren(List<Voorkeur> voorkeuren) {
        if (voorkeuren == null) {
            throw new IllegalArgumentException("Voorkeuren mogen niet null zijn.");
        }

        List<Leerling> leerlingen = new ArrayList<>();

        for (Voorkeur voorkeur : voorkeuren) {
            if (!leerlingen.contains(voorkeur.getLeerling())) {
                leerlingen.add(voorkeur.getLeerling());
            }
        }

        return leerlingen;
    }
}
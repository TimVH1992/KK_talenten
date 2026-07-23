package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;

import java.util.*;

public class AutomatischeVerdeler {
    private List<Voorkeur> voorkeuren;
    private List<Toewijzing> historischeToewijzingen;
    private VerdelingsResultaat verdelingsResultaat;
    private static final int MAX_LEERLINGEN_PER_KLAS_PER_TALENT = 2;

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren) {
        this(voorkeuren, new ArrayList<>());
    }

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren, List<Toewijzing> historischeToewijzingen) {
        if (voorkeuren == null) {
            throw new IllegalArgumentException("Voorkeuren mogen niet null zijn.");
        }

        if (historischeToewijzingen == null) {
            throw new IllegalArgumentException("Historische toewijzingen mogen niet null zijn.");
        }

        this.voorkeuren = voorkeuren;
        this.historischeToewijzingen = historischeToewijzingen;
        this.verdelingsResultaat = new VerdelingsResultaat();
    }

    public VerdelingsResultaat verdeel() {
        Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling = groepeerVoorkeurenPerLeerling();
        Map<IngerichtTalent, Integer> bezetting = new HashMap<>();
        Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas = new HashMap<>();

        List<Leerling> leerlingenInVolgorde = bepaalLeerlingVolgorde(voorkeurenPerLeerling);

        for (Leerling leerling : leerlingenInVolgorde) {
            List<Voorkeur> voorkeurenVanLeerling = voorkeurenPerLeerling.get(leerling);
            voorkeurenVanLeerling.sort(Comparator.comparingInt(Voorkeur::getVoorkeurNummer));

            Toewijzing toewijzing = zoekToewijzingZonderHistoriek(leerling, voorkeurenVanLeerling, bezetting, bezettingPerKlas);

            if (toewijzing == null) {
                toewijzing = zoekToewijzingMetHistoriekToegestaan(leerling, voorkeurenVanLeerling, bezetting, bezettingPerKlas);
            }

            if (toewijzing != null) {
                verdelingsResultaat.voegToewijzingToe(toewijzing);

                IngerichtTalent ingerichtTalent = toewijzing.getIngerichtTalent();
                int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);
                bezetting.put(ingerichtTalent, huidigAantal + 1);
                verhoogKlasBezetting(ingerichtTalent, leerling.getKlas(), bezettingPerKlas);
            } else {
                verdelingsResultaat.voegNietToegewezenLeerlingToe(leerling);
            }
        }

        return verdelingsResultaat;
    }

    private Toewijzing zoekToewijzingZonderHistoriek(
            Leerling leerling,
            List<Voorkeur> voorkeurenVanLeerling,
            Map<IngerichtTalent, Integer> bezetting,
            Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftLeerlingTalentAlGevolgd(leerling, ingerichtTalent.getTalent())) {
                continue;
            }

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)
                    && heeftKlasNogPlaatsVoorIngerichtTalent(ingerichtTalent, leerling.getKlas(), bezettingPerKlas)) {
                return new Toewijzing(
                        leerling,
                        ingerichtTalent,
                        ToewijzingsType.AUTOMATISCH,
                        voorkeur.getVoorkeurNummer()
                );
            }
        }

        return null;
    }

    private Toewijzing zoekToewijzingMetHistoriekToegestaan(
            Leerling leerling,
            List<Voorkeur> voorkeurenVanLeerling,
            Map<IngerichtTalent, Integer> bezetting,
            Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)
                    && heeftKlasNogPlaatsVoorIngerichtTalent(ingerichtTalent, leerling.getKlas(), bezettingPerKlas)) {
                return new Toewijzing(
                        leerling,
                        ingerichtTalent,
                        ToewijzingsType.AUTOMATISCH,
                        voorkeur.getVoorkeurNummer()
                );
            }
        }
        return null;
    }

    private boolean heeftIngerichtTalentVrijePlaats(
            IngerichtTalent ingerichtTalent,
            Map<IngerichtTalent, Integer> bezetting
    ) {
        int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);
        return ingerichtTalent.heeftVrijePlaats(huidigAantal);
    }

    private boolean heeftLeerlingTalentAlGevolgd(Leerling leerling, Talent talent) {
        for (Toewijzing historischeToewijzing : historischeToewijzingen) {
            boolean zelfdeLeerling = historischeToewijzing.getLeerling() == leerling;
            boolean zelfdeTalent = historischeToewijzing.getIngerichtTalent().getTalent() == talent;

            if (zelfdeLeerling && zelfdeTalent) {
                return true;
            }
        }

        return false;
    }

    private Map<Leerling, List<Voorkeur>> groepeerVoorkeurenPerLeerling() {
        Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling = new LinkedHashMap<>();

        for (Voorkeur voorkeur : voorkeuren) {
            Leerling leerling = voorkeur.getLeerling();

            voorkeurenPerLeerling
                    .computeIfAbsent(leerling, l -> new ArrayList<>())
                    .add(voorkeur);
        }

        return voorkeurenPerLeerling;
    }
    private List<Leerling> bepaalLeerlingVolgorde(Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling) {
        List<Leerling> leerlingen = new ArrayList<>(voorkeurenPerLeerling.keySet());

        leerlingen.sort(new Comparator<Leerling>() {
            @Override
            public int compare(Leerling leerling1, Leerling leerling2) {
                int prioriteit1 = berekenVerdelingsPrioriteit(leerling1);
                int prioriteit2 = berekenVerdelingsPrioriteit(leerling2);

                return Integer.compare(prioriteit2, prioriteit1);
            }
        });

        return leerlingen;
    }
    private int berekenVerdelingsPrioriteit(Leerling leerling) {
        Toewijzing laatsteToewijzing = zoekLaatsteHistorischeToewijzing(leerling);

        if (laatsteToewijzing == null) {
            return 2;
        }

        Integer voorkeurNummer = laatsteToewijzing.getVoorkeurNummer();

        if (voorkeurNummer == null) {
            return 2;
        }

        if (voorkeurNummer == 1) {
            return 1;
        }

        if (voorkeurNummer == 2) {
            return 2;
        }

        if (voorkeurNummer == 3) {
            return 3;
        }

        return 2;
    }
    private Toewijzing zoekLaatsteHistorischeToewijzing(Leerling leerling) {
        Toewijzing laatsteToewijzing = null;

        for (Toewijzing historischeToewijzing : historischeToewijzingen) {
            boolean zelfdeLeerling = historischeToewijzing.getLeerling() == leerling;

            if (zelfdeLeerling) {
                if (laatsteToewijzing == null) {
                    laatsteToewijzing = historischeToewijzing;
                } else if (isNieuwerDan(historischeToewijzing, laatsteToewijzing)) {
                    laatsteToewijzing = historischeToewijzing;
                }
            }
        }

        return laatsteToewijzing;
    }
    private boolean isNieuwerDan(Toewijzing kandidaat, Toewijzing huidigeLaatste) {
        return kandidaat.getIngerichtTalent()
                .getTalentenPeriode()
                .getEindDatum()
                .isAfter(
                        huidigeLaatste.getIngerichtTalent()
                                .getTalentenPeriode()
                                .getEindDatum()
                );
    }
    private boolean heeftKlasNogPlaatsVoorIngerichtTalent(
            IngerichtTalent ingerichtTalent,
            Klas klas,
            Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas
    ) {
        Map<Klas, Integer> klasBezetting = bezettingPerKlas.get(ingerichtTalent);

        if (klasBezetting == null) {
            return true;
        }

        int huidigAantalVanKlas = klasBezetting.getOrDefault(klas, 0);

        return huidigAantalVanKlas < MAX_LEERLINGEN_PER_KLAS_PER_TALENT;
    }
    private void verhoogKlasBezetting(
            IngerichtTalent ingerichtTalent,
            Klas klas,
            Map<IngerichtTalent, Map<Klas, Integer>> bezettingPerKlas
    ) {
        Map<Klas, Integer> klasBezetting = bezettingPerKlas.get(ingerichtTalent);

        if (klasBezetting == null) {
            klasBezetting = new HashMap<>();
            bezettingPerKlas.put(ingerichtTalent, klasBezetting);
        }

        int huidigAantalVanKlas = klasBezetting.getOrDefault(klas, 0);
        klasBezetting.put(klas, huidigAantalVanKlas + 1);
    }
}
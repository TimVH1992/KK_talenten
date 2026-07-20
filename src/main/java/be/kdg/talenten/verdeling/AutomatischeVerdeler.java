package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;

import java.util.*;

public class AutomatischeVerdeler {
    private List<Voorkeur> voorkeuren;
    private List<Toewijzing> historischeToewijzingen;
    private VerdelingsResultaat verdelingsResultaat;

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

        for (Leerling leerling : voorkeurenPerLeerling.keySet()) {
            List<Voorkeur> voorkeurenVanLeerling = voorkeurenPerLeerling.get(leerling);
            voorkeurenVanLeerling.sort(Comparator.comparingInt(Voorkeur::getVoorkeurNummer));

            Toewijzing toewijzing = zoekToewijzingZonderHistoriek(leerling, voorkeurenVanLeerling, bezetting);

            if (toewijzing == null) {
                toewijzing = zoekToewijzingMetHistoriekToegestaan(leerling, voorkeurenVanLeerling, bezetting);
            }

            if (toewijzing != null) {
                verdelingsResultaat.voegToewijzingToe(toewijzing);

                IngerichtTalent ingerichtTalent = toewijzing.getIngerichtTalent();
                int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);
                bezetting.put(ingerichtTalent, huidigAantal + 1);
            } else {
                verdelingsResultaat.voegNietToegewezenLeerlingToe(leerling);
            }
        }

        return verdelingsResultaat;
    }

    private Toewijzing zoekToewijzingZonderHistoriek(
            Leerling leerling,
            List<Voorkeur> voorkeurenVanLeerling,
            Map<IngerichtTalent, Integer> bezetting
    ) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftLeerlingTalentAlGevolgd(leerling, ingerichtTalent.getTalent())) {
                continue;
            }

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)) {
                return new Toewijzing(leerling, ingerichtTalent, ToewijzingsType.AUTOMATISCH);
            }
        }

        return null;
    }

    private Toewijzing zoekToewijzingMetHistoriekToegestaan(
            Leerling leerling,
            List<Voorkeur> voorkeurenVanLeerling,
            Map<IngerichtTalent, Integer> bezetting
    ) {
        for (Voorkeur voorkeur : voorkeurenVanLeerling) {
            IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

            if (heeftIngerichtTalentVrijePlaats(ingerichtTalent, bezetting)) {
                return new Toewijzing(leerling, ingerichtTalent, ToewijzingsType.AUTOMATISCH);
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
}
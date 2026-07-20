package be.kdg.talenten.verdeling;

import be.kdg.talenten.domain.*;

import java.util.*;

public class AutomatischeVerdeler {
    private List<Voorkeur> voorkeuren;
    private VerdelingsResultaat verdelingsResultaat;

    public AutomatischeVerdeler(List<Voorkeur> voorkeuren) {
        if (voorkeuren == null) {
            throw new IllegalArgumentException("Om een automatische verdeling te kunnen aanmaken mogen de voorkeuren die meegegeven worden niet null zijn.");
        } else {
            this.voorkeuren = voorkeuren;
            this.verdelingsResultaat = new VerdelingsResultaat();
        }
    }

    public VerdelingsResultaat verdeel() {
        Map<Leerling, List<Voorkeur>> voorkeurenPerLeerling = groepeerVoorkeurenPerLeerling();
        Map<IngerichtTalent, Integer> bezetting = new HashMap<>();

        for (Leerling leerling : voorkeurenPerLeerling.keySet()) {
            List<Voorkeur> voorkeurenVanLeerling = voorkeurenPerLeerling.get(leerling);
            voorkeurenVanLeerling.sort(Comparator.comparingInt(Voorkeur::getVoorkeurNummer));

            boolean leerlingIsToegewezen = false;

            for (Voorkeur voorkeur : voorkeurenVanLeerling) {
                IngerichtTalent ingerichtTalent = voorkeur.getIngerichtTalent();

                int huidigAantal = bezetting.getOrDefault(ingerichtTalent, 0);

                if (ingerichtTalent.heeftVrijePlaats(huidigAantal)) {
                    Toewijzing toewijzing = new Toewijzing(
                            leerling,
                            ingerichtTalent,
                            ToewijzingsType.AUTOMATISCH
                    );
                    verdelingsResultaat.voegToewijzingToe(toewijzing);
                    bezetting.put(ingerichtTalent, huidigAantal + 1);
                    leerlingIsToegewezen = true;

                    break;
                }
            }
            if (!leerlingIsToegewezen) {
                verdelingsResultaat.voegNietToegewezenLeerlingToe(leerling);
            }
        }

        return verdelingsResultaat;
    }

    public Map<Leerling, List<Voorkeur>> groepeerVoorkeurenPerLeerling() {
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

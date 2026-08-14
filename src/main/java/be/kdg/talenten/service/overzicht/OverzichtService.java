package be.kdg.talenten.service.overzicht;

import be.kdg.talenten.domain.Leerling;
import be.kdg.talenten.domain.Schooljaar;
import be.kdg.talenten.domain.TalentenPeriode;
import be.kdg.talenten.domain.Voorkeur;
import be.kdg.talenten.overzicht.OverzichtGegevens;
import be.kdg.talenten.repository.IngerichtTalentRepository;
import be.kdg.talenten.repository.LeerlingRepository;
import be.kdg.talenten.repository.ToewijzingRepository;
import be.kdg.talenten.repository.VoorkeurImportProbleemRepository;
import be.kdg.talenten.repository.VoorkeurRepository;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class OverzichtService {
    private final LeerlingRepository leerlingRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;
    private final VoorkeurImportProbleemRepository voorkeurImportProbleemRepository;

    public OverzichtService(LeerlingRepository leerlingRepository, IngerichtTalentRepository ingerichtTalentRepository, VoorkeurRepository voorkeurRepository, ToewijzingRepository toewijzingRepository, VoorkeurImportProbleemRepository voorkeurImportProbleemRepository) {
        if (leerlingRepository == null || ingerichtTalentRepository == null || voorkeurRepository == null || toewijzingRepository == null || voorkeurImportProbleemRepository == null) {
            throw new IllegalArgumentException("Repositories voor het overzicht mogen niet null zijn");
        }

        this.leerlingRepository = leerlingRepository;
        this.ingerichtTalentRepository = ingerichtTalentRepository;
        this.voorkeurRepository = voorkeurRepository;
        this.toewijzingRepository = toewijzingRepository;
        this.voorkeurImportProbleemRepository = voorkeurImportProbleemRepository;
    }

    public OverzichtGegevens geefOverzicht(Schooljaar schooljaar, TalentenPeriode periode) {
        if (schooljaar == null) {
            throw new IllegalArgumentException("Schooljaar mag niet null zijn");
        }
        if (periode != null && !periode.getSchooljaar().equals(schooljaar)) {
            throw new IllegalArgumentException("Talentenperiode behoort niet tot het opgegeven schooljaar");
        }

        int aantalLeerlingen = leerlingRepository.zoekVoorSchooljaar(schooljaar).size();

        if (periode == null) {
            return new OverzichtGegevens(aantalLeerlingen, null, 0, 0, 0, 0);
        }

        List<Voorkeur> voorkeuren = voorkeurRepository.zoekVoorPeriode(periode);
        Map<Leerling, Long> aantalVoorkeurenPerLeerling = voorkeuren.stream()
                .collect(Collectors.groupingBy(Voorkeur::getLeerling, Collectors.counting()));

        int aantalLeerlingenMetVolledigeVoorkeuren = (int) aantalVoorkeurenPerLeerling.values().stream()
                .filter(aantal -> aantal == 3)
                .count();

        return new OverzichtGegevens(
                aantalLeerlingen,
                periode,
                ingerichtTalentRepository.zoekVoorPeriode(periode).size(),
                aantalLeerlingenMetVolledigeVoorkeuren,
                toewijzingRepository.zoekVoorPeriode(periode).size(),
                voorkeurImportProbleemRepository.zoekVoorPeriode(periode).size()
        );
    }
}

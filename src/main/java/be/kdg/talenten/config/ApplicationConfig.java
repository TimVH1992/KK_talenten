package be.kdg.talenten.config;

import be.kdg.talenten.repository.*;
import be.kdg.talenten.repository.postgres.*;
import be.kdg.talenten.service.beheer.KlasService;
import be.kdg.talenten.service.beheer.SchooljaarService;
import be.kdg.talenten.service.beheer.TalentenPeriodeService;
import be.kdg.talenten.service.leerling.LeerlingDetailsService;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.service.verdeling.ManueleToewijzingService;
import be.kdg.talenten.service.verdeling.VerdelingBekijkenService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportService;

public final class ApplicationConfig {
    private final KlasRepository klasRepository;
    private final LeerlingRepository leerlingRepository;
    private final LeerkrachtRepository leerkrachtRepository;
    private final TalentRepository talentRepository;
    private final SchooljaarRepository schooljaarRepository;
    private final TalentenPeriodeRepository talentenPeriodeRepository;
    private final IngerichtTalentRepository ingerichtTalentRepository;
    private final VoorkeurRepository voorkeurRepository;
    private final ToewijzingRepository toewijzingRepository;
    private final VoorkeurImportProbleemRepository voorkeurImportProbleemRepository;

    private final AutomatischeVerdelingService automatischeVerdelingService;
    private final ManueleToewijzingService manueleToewijzingService;
    private final VerdelingBekijkenService verdelingBekijkenService;

    private final SchooljaarService schooljaarService;
    private final TalentenPeriodeService talentenPeriodeService;
    private final KlasService klasService;
    private final LeerlingDetailsService leerlingDetailsService;

    private final VoorkeurenExcelService voorkeurenExcelService;
    private final VoorkeurenImportService voorkeurenImportService;

    public ApplicationConfig() {
        klasRepository = new PostgresKlasRepository();
        leerlingRepository = new PostgresLeerlingRepository();
        leerkrachtRepository = new PostgresLeerkrachtRepository();
        talentRepository = new PostgresTalentRepository();
        schooljaarRepository = new PostgresSchooljaarRepository();
        talentenPeriodeRepository = new PostgresTalentenPeriodeRepository();
        ingerichtTalentRepository = new PostgresIngerichtTalentRepository();

        voorkeurRepository = new PostgresVoorkeurRepository(leerlingRepository, ingerichtTalentRepository);
        toewijzingRepository = new PostgresToewijzingRepository(leerlingRepository, ingerichtTalentRepository);
        voorkeurImportProbleemRepository = new PostgresVoorkeurImportProbleemRepository();

        schooljaarService = new SchooljaarService(schooljaarRepository);
        talentenPeriodeService = new TalentenPeriodeService(talentenPeriodeRepository);
        klasService = new KlasService(klasRepository);
        leerlingDetailsService = new LeerlingDetailsService(voorkeurRepository, toewijzingRepository);

        voorkeurenExcelService = new VoorkeurenExcelService(leerlingRepository, ingerichtTalentRepository);
        voorkeurenImportService = new VoorkeurenImportService(leerlingRepository, ingerichtTalentRepository, voorkeurRepository, voorkeurImportProbleemRepository);

        automatischeVerdelingService = new AutomatischeVerdelingService(voorkeurRepository, toewijzingRepository, leerlingRepository, voorkeurImportProbleemRepository);
        manueleToewijzingService = new ManueleToewijzingService(toewijzingRepository);
        verdelingBekijkenService = new VerdelingBekijkenService(ingerichtTalentRepository, toewijzingRepository, leerlingRepository);
    }

    public VerdelingBekijkenService getVerdelingBekijkenService() {
        return verdelingBekijkenService;
    }

    public ManueleToewijzingService getManueleToewijzingService() {
        return manueleToewijzingService;
    }

    public AutomatischeVerdelingService getAutomatischeVerdelingService() {
        return automatischeVerdelingService;
    }

    public SchooljaarService getSchooljaarService() {
        return schooljaarService;
    }

    public TalentenPeriodeService getTalentenPeriodeService() {
        return talentenPeriodeService;
    }

    public KlasService getKlasService() {
        return klasService;
    }

    public LeerlingDetailsService getLeerlingDetailsService() {
        return leerlingDetailsService;
    }

    public VoorkeurenExcelService getVoorkeurenExcelService() {
        return voorkeurenExcelService;
    }

    public VoorkeurenImportService getVoorkeurenImportService() {
        return voorkeurenImportService;
    }
}
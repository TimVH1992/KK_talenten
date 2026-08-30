package be.kdg.talenten.integratie;

import be.kdg.talenten.database.DatabaseConnectionFactory;
import be.kdg.talenten.domain.*;
import be.kdg.talenten.repository.*;
import be.kdg.talenten.repository.postgres.*;
import be.kdg.talenten.service.verdeling.AutomatischeVerdelingService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenExcelService;
import be.kdg.talenten.service.voorkeuren.VoorkeurenImportService;
import be.kdg.talenten.verdeling.VerdelingsResultaat;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class VoorkeurenImportVerdelingIntegratieTest {

    private LeerlingRepository leerlingRepository;
    private TalentRepository talentRepository;
    private LeerkrachtRepository leerkrachtRepository;
    private KlasRepository klasRepository;
    private SchooljaarRepository schooljaarRepository;
    private TalentenPeriodeRepository periodeRepository;
    private IngerichtTalentRepository ingerichtTalentRepository;
    private VoorkeurRepository voorkeurRepository;
    private ToewijzingRepository toewijzingRepository;
    private VoorkeurImportProbleemRepository probleemRepository;

    @BeforeEach
    void setUp() throws SQLException {
        try (Connection connection =
                     DatabaseConnectionFactory.maakVerbinding();
             Statement statement =
                     connection.createStatement()) {

            statement.executeUpdate("""
                    TRUNCATE TABLE
                        voorkeur_import_problemen,
                        leerling_klas_historiek,
                        ingericht_talent_leerkrachten,
                        voorkeuren,
                        toewijzingen,
                        leerlingen,
                        ingerichte_talenten,
                        leerkrachten,
                        talenten,
                        talenten_periodes,
                        klassen,
                        schooljaren
                    RESTART IDENTITY CASCADE
                    """);
        }

        leerlingRepository =
                new PostgresLeerlingRepository();

        talentRepository =
                new PostgresTalentRepository();

        leerkrachtRepository =
                new PostgresLeerkrachtRepository();

        klasRepository =
                new PostgresKlasRepository();

        schooljaarRepository =
                new PostgresSchooljaarRepository();

        periodeRepository =
                new PostgresTalentenPeriodeRepository();

        ingerichtTalentRepository =
                new PostgresIngerichtTalentRepository();

        voorkeurRepository =
                new PostgresVoorkeurRepository(
                        leerlingRepository,
                        ingerichtTalentRepository
                );

        toewijzingRepository =
                new PostgresToewijzingRepository(
                        leerlingRepository,
                        ingerichtTalentRepository
                );

        probleemRepository =
                new PostgresVoorkeurImportProbleemRepository();
    }

    @Test
    void gecorrigeerdeHerimportWordtOpgeslagenEnLeerlingKanDaarnaAutomatischVerdeeldWorden(
            @TempDir Path tempDir
    ) throws IOException {

        // ARRANGE
        Schooljaar schooljaar =
                schooljaarRepository.save(
                        new Schooljaar(
                                "2099-2100",
                                LocalDate.of(2099, 7, 1),
                                LocalDate.of(2100, 6, 30),
                                true
                        )
                );

        Doelgroep doelgroep =
                Doelgroep.OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB;

        Klas klas =
                klasRepository.save(
                        new Klas(
                                "1AA",
                                schooljaar,
                                1,
                                doelgroep
                        )
                );

        Leerling sofie =
                leerlingRepository.save(
                        new Leerling(
                                "Sofie",
                                "Peeters",
                                klas
                        )
                );

        TalentenPeriode periode =
                periodeRepository.save(
                        new TalentenPeriode(
                                "Herfst",
                                LocalDate.of(2099, 9, 1),
                                LocalDate.of(2099, 12, 21),
                                schooljaar
                        )
                );

        Leerkracht tom =
                leerkrachtRepository.save(
                        new Leerkracht(
                                "Tom",
                                "Peeters"
                        )
                );

        Talent schaken =
                talentRepository.save(
                        new Talent(
                                "Schaken",
                                "Strategisch denkspel"
                        )
                );

        Talent koken =
                talentRepository.save(
                        new Talent(
                                "Koken",
                                "Leren koken"
                        )
                );

        Talent voetbal =
                talentRepository.save(
                        new Talent(
                                "Voetbal",
                                "Balsport"
                        )
                );

        IngerichtTalent schakenHerfst =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                schaken,
                                periode,
                                "Schaken observatie",
                                "Schaken voor observatie",
                                10,
                                doelgroep,
                                List.of(tom)
                        )
                );

        IngerichtTalent kokenHerfst =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                koken,
                                periode,
                                "Koken basis",
                                "Koken voor observatie",
                                10,
                                doelgroep,
                                List.of(tom)
                        )
                );

        IngerichtTalent voetbalHerfst =
                ingerichtTalentRepository.save(
                        new IngerichtTalent(
                                voetbal,
                                periode,
                                "Voetbal observatie",
                                "Voetbal voor observatie",
                                10,
                                doelgroep,
                                List.of(tom)
                        )
                );

        VoorkeurenExcelService excelService =
                new VoorkeurenExcelService(
                        leerlingRepository,
                        ingerichtTalentRepository
                );

        VoorkeurenImportService importService =
                new VoorkeurenImportService(
                        leerlingRepository,
                        ingerichtTalentRepository,
                        voorkeurRepository,
                        probleemRepository
                );

        AutomatischeVerdelingService verdelingService =
                new AutomatischeVerdelingService(
                        voorkeurRepository,
                        toewijzingRepository,
                        leerlingRepository,
                        probleemRepository
                );

        Path onvolledigBestand =
                tempDir.resolve(
                        "voorkeuren_onvolledig.xlsx"
                );

        excelService.genereerTemplate(
                periode,
                doelgroep,
                onvolledigBestand
        );

        vulKeuzesIn(
                onvolledigBestand,
                "1AA",
                "Schaken observatie",
                "Koken basis",
                null
        );

        // ACT 1
        importService.importeer(
                onvolledigBestand,
                periode,
                doelgroep
        );

        // ASSERT 1
        List<Voorkeur> voorkeurenNaEersteImport =
                voorkeurRepository
                        .zoekVoorLeerlingEnPeriode(
                                sofie,
                                periode
                        );

        List<VoorkeurImportProbleem> problemenNaEersteImport =
                probleemRepository
                        .zoekVoorLeerlingEnPeriode(
                                sofie,
                                periode
                        );

        assertEquals(
                2,
                voorkeurenNaEersteImport.size()
        );

        assertEquals(
                1,
                problemenNaEersteImport.size()
        );

        assertEquals(
                "Keuze 3 is niet ingevuld",
                problemenNaEersteImport
                        .getFirst()
                        .getReden()
        );

        // ARRANGE 2
        Path gecorrigeerdBestand =
                tempDir.resolve(
                        "voorkeuren_gecorrigeerd.xlsx"
                );

        excelService.genereerTemplate(
                periode,
                doelgroep,
                gecorrigeerdBestand
        );

        vulKeuzesIn(
                gecorrigeerdBestand,
                "1AA",
                "Schaken observatie",
                "Koken basis",
                "Voetbal observatie"
        );

        // ACT 2
        importService.importeer(
                gecorrigeerdBestand,
                periode,
                doelgroep
        );

        // ASSERT 2
        List<Voorkeur> voorkeurenNaHerimport =
                voorkeurRepository
                        .zoekVoorLeerlingEnPeriode(
                                sofie,
                                periode
                        );

        List<VoorkeurImportProbleem> problemenNaHerimport =
                probleemRepository
                        .zoekVoorLeerlingEnPeriode(
                                sofie,
                                periode
                        );

        assertEquals(
                3,
                voorkeurenNaHerimport.size()
        );

        assertEquals(
                1,
                voorkeurenNaHerimport
                        .get(0)
                        .getVoorkeurNummer()
        );

        assertEquals(
                schakenHerfst.getId(),
                voorkeurenNaHerimport
                        .get(0)
                        .getIngerichtTalent()
                        .getId()
        );

        assertEquals(
                2,
                voorkeurenNaHerimport
                        .get(1)
                        .getVoorkeurNummer()
        );

        assertEquals(
                kokenHerfst.getId(),
                voorkeurenNaHerimport
                        .get(1)
                        .getIngerichtTalent()
                        .getId()
        );

        assertEquals(
                3,
                voorkeurenNaHerimport
                        .get(2)
                        .getVoorkeurNummer()
        );

        assertEquals(
                voetbalHerfst.getId(),
                voorkeurenNaHerimport
                        .get(2)
                        .getIngerichtTalent()
                        .getId()
        );

        assertTrue(
                problemenNaHerimport.isEmpty()
        );

        // ACT 3
        VerdelingsResultaat resultaat =
                verdelingService
                        .voerAutomatischeVerdelingUit(
                                periode,
                                doelgroep
                        );

        // ASSERT 3
        assertEquals(
                1,
                resultaat.getAantalToewijzingen()
        );

        assertTrue(
                resultaat
                        .getNietToegewezenLeerlingen()
                        .isEmpty()
        );

        assertTrue(
                resultaat
                        .getImportProblemen()
                        .isEmpty()
        );

        Toewijzing toewijzing =
                resultaat
                        .getToewijzingen()
                        .getFirst();

        assertEquals(
                sofie.getId(),
                toewijzing
                        .getLeerling()
                        .getId()
        );

        assertEquals(
                schakenHerfst.getId(),
                toewijzing
                        .getIngerichtTalent()
                        .getId()
        );

        assertEquals(
                doelgroep,
                toewijzing
                        .getIngerichtTalent()
                        .getDoelgroep()
        );

        assertEquals(
                ToewijzingsType.AUTOMATISCH,
                toewijzing.getToewijzingsType()
        );

        assertEquals(
                1,
                toewijzing.getVoorkeurNummer()
        );

        List<Toewijzing> opgeslagenToewijzingen =
                toewijzingRepository
                        .zoekVoorPeriode(
                                periode
                        );

        assertEquals(
                1,
                opgeslagenToewijzingen.size()
        );

        Toewijzing opgeslagenToewijzing =
                opgeslagenToewijzingen.getFirst();

        assertEquals(
                sofie.getId(),
                opgeslagenToewijzing
                        .getLeerling()
                        .getId()
        );

        assertEquals(
                schakenHerfst.getId(),
                opgeslagenToewijzing
                        .getIngerichtTalent()
                        .getId()
        );

        assertEquals(
                doelgroep,
                opgeslagenToewijzing
                        .getIngerichtTalent()
                        .getDoelgroep()
        );
    }

    private void vulKeuzesIn(
            Path bestand,
            String sheetNaam,
            String keuze1,
            String keuze2,
            String keuze3
    ) throws IOException {

        XSSFWorkbook workbook;

        try (InputStream inputStream =
                     Files.newInputStream(
                             bestand
                     )) {

            workbook =
                    new XSSFWorkbook(
                            inputStream
                    );
        }

        try (workbook;
             OutputStream outputStream =
                     Files.newOutputStream(
                             bestand
                     )) {

            Sheet sheet =
                    workbook.getSheet(
                            sheetNaam
                    );

            Row leerlingRij =
                    sheet.getRow(
                            1
                    );

            leerlingRij
                    .getCell(2)
                    .setCellValue(
                            keuze1
                    );

            leerlingRij
                    .getCell(3)
                    .setCellValue(
                            keuze2
                    );

            if (keuze3 == null) {
                leerlingRij
                        .getCell(4)
                        .setBlank();
            } else {
                leerlingRij
                        .getCell(4)
                        .setCellValue(
                                keuze3
                        );
            }

            workbook.write(
                    outputStream
            );
        }
    }
}
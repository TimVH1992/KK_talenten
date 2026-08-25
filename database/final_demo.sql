-- =========================================================
-- KK TALENTEN - DEMODATA VOOR SNELLE END-TO-END TEST
-- =========================================================
-- Doel:
--   - applicatie testen zonder Excel-import
--   - automatische verdeling live uitvoeren
--   - resultaat per klas en per talent bekijken
--   - export per klas testen na verdeling
--   - export per ingericht talent testen na verdeling
--   - klashistoriek correct beschikbaar maken
--
-- Gebruik:
--   1. Voer eerst je normale database create/schema-script uit.
--   2. Voer daarna DIT script uit.
--   3. Start de JavaFX-app.
--
-- LET OP:
-- Dit script wist de DATA uit de betrokken tabellen.
-- De databasestructuur zelf wordt NIET verwijderd.
-- =========================================================

BEGIN;

-- =========================================================
-- 1. BESTAANDE DATA LEEGMAKEN
-- =========================================================

TRUNCATE TABLE
    ingericht_talent_leerkrachten,
    voorkeur_import_problemen,
    voorkeuren,
    toewijzingen,
    leerling_klas_historiek,
    leerlingen,
    ingerichte_talenten,
    leerkrachten,
    talenten,
    talenten_periodes,
    klassen,
    schooljaren
    RESTART IDENTITY CASCADE;


-- =========================================================
-- 2. SCHOOLJAAR
-- =========================================================

INSERT INTO schooljaren (
    naam,
    startdatum,
    einddatum,
    actief
)
VALUES (
           '2026-2027',
           '2026-09-01',
           '2027-06-30',
           TRUE
       );


-- =========================================================
-- 3. KLASSEN
-- =========================================================
-- 2 observatieklassen
-- 2 kwalificatieklassen

INSERT INTO klassen (
    klas_naam,
    schooljaar,
    leerjaar,
    doelgroep
)
VALUES
    ('1AA', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('1AB', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('3KA', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('3KB', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB');


-- =========================================================
-- 4. LEERLINGEN
-- =========================================================
-- 4 leerlingen per klas = 16 leerlingen.
-- Alle leerlingen nemen deel.
--
-- Bij het starten van de demo is nog geen enkele leerling toegewezen.
-- Zo kun je de automatische verdeling volledig live demonstreren.

INSERT INTO leerlingen (
    voornaam,
    achternaam,
    klas_id,
    actief
)
VALUES
    -- 1AA: id 1..4
    ('Jan',      'Peeters',       1, TRUE),
    ('Sofie',    'Janssens',      1, TRUE),
    ('Mohamed',  'El Amrani',     1, TRUE),
    ('Lotte',    'Vermeulen',     1, TRUE),

    -- 1AB: id 5..8
    ('Bram',     'Willems',       2, TRUE),
    ('Julie',    'Martens',       2, TRUE),
    ('Noah',     'Jacobs',        2, TRUE),
    ('Emma',     'De Smet',       2, TRUE),

    -- 3KA: id 9..12
    ('Milan',    'Aerts',         3, TRUE),
    ('Yara',     'Goossens',      3, TRUE),
    ('Amine',    'Yilmaz',        3, TRUE),
    ('Lina',     'De Ridder',     3, TRUE),

    -- 3KB: id 13..16
    ('Seppe',    'Vercammen',     4, TRUE),
    ('Nora',     'Van Looy',      4, TRUE),
    ('Cas',      'Michiels',      4, TRUE),
    ('Mila',     'Van Acker',     4, TRUE);


-- =========================================================
-- 5. KLAS_HISTORIEK
-- =========================================================
-- Iedere leerling krijgt een huidige klashistoriek vanaf
-- de start van het schooljaar.
--
-- Dit is nodig voor het historisch correcte klasoverzicht
-- en voor de Excel-export per ingericht talent.

INSERT INTO leerling_klas_historiek (
    leerling_id,
    klas_id,
    vanaf,
    tot
)
VALUES
    (1,  1, '2026-09-01', NULL),
    (2,  1, '2026-09-01', NULL),
    (3,  1, '2026-09-01', NULL),
    (4,  1, '2026-09-01', NULL),

    (5,  2, '2026-09-01', NULL),
    (6,  2, '2026-09-01', NULL),
    (7,  2, '2026-09-01', NULL),
    (8,  2, '2026-09-01', NULL),

    (9,  3, '2026-09-01', NULL),
    (10, 3, '2026-09-01', NULL),
    (11, 3, '2026-09-01', NULL),
    (12, 3, '2026-09-01', NULL),

    (13, 4, '2026-09-01', NULL),
    (14, 4, '2026-09-01', NULL),
    (15, 4, '2026-09-01', NULL),
    (16, 4, '2026-09-01', NULL);


-- =========================================================
-- 6. LEERKRACHTEN
-- =========================================================

INSERT INTO leerkrachten (
    voornaam,
    achternaam,
    actief
)
VALUES
    ('Tom',   'Laforce',        TRUE),
    ('Anke',  'Verhoeven',      TRUE),
    ('Sarah', 'De Winter',      TRUE),
    ('Koen',  'Van den Broeck', TRUE),
    ('Mark',  'Oud-Leerkracht', FALSE);


-- =========================================================
-- 7. BASIS TALENTEN
-- =========================================================
-- "actief" wordt bewust niet expliciet ingevuld.
-- Als die kolom in je schema bestaat, gebruikt PostgreSQL
-- gewoon de DEFAULT TRUE.

INSERT INTO talenten (
    naam,
    beschrijving
)
VALUES
    ('Schaken',        'Strategisch denken, plannen en leren schaken.'),
    ('Voetbal',        'Techniek, samenspel en spelinzicht.'),
    ('Koken',          'Basisvaardigheden in de keuken en eenvoudige gerechten.'),
    ('Houtbewerking',  'Veilig en praktisch leren werken met hout.'),
    ('Fietstechniek',  'Onderhoud en eenvoudige fietsherstellingen.'),
    ('Digitale Media', 'Creatief werken met foto, video en digitale media.');


-- =========================================================
-- 8. TALENTENPERIODE
-- =========================================================

INSERT INTO talenten_periodes (
    naam,
    startdatum,
    einddatum,
    schooljaar_id
)
VALUES (
           'Herfst 2026',
           '2026-09-01',
           '2026-10-31',
           1
       );


-- =========================================================
-- 9. INGERICHTE TALENTEN
-- =========================================================
-- id 1..3 = observatie
-- id 4..6 = kwalificatie

INSERT INTO ingerichte_talenten (
    naam,
    omschrijving,
    maximum_capaciteit,
    doelgroep,
    actief,
    talent_id,
    talenten_periode_id
)
VALUES
    ('Schaken - Herfst', 'Strategie en concentratie via schaken.', 5,
     'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', TRUE, 1, 1),
    ('Voetbal - Herfst', 'Voetbaltechniek en samenspel.', 5,
     'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', TRUE, 2, 1),
    ('Koken - Herfst', 'Eenvoudige gerechten leren bereiden.', 5,
     'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', TRUE, 3, 1),

    ('Houtatelier - Herfst', 'Praktische opdrachten met hout.', 5,
     'KWALIFICATIEFASE_TWEEDEGRAAD_AB', TRUE, 4, 1),
    ('Fietsatelier - Herfst', 'Onderhoud en eenvoudige fietsherstellingen.', 5,
     'KWALIFICATIEFASE_TWEEDEGRAAD_AB', TRUE, 5, 1),
    ('Digitale Media - Herfst', 'Foto, video en digitale creatie.', 5,
     'KWALIFICATIEFASE_TWEEDEGRAAD_AB', TRUE, 6, 1);


-- =========================================================
-- 10. LEERKRACHTEN KOPPELEN
-- =========================================================
-- Koken heeft bewust GEEN leerkracht.
-- Daarmee test je meteen dat een ingericht talent zonder
-- leerkracht correct geladen kan worden.

INSERT INTO ingericht_talent_leerkrachten (
    ingericht_talent_id,
    leerkracht_id
)
VALUES
    (1, 1),
    (2, 2),
    (4, 3),
    (5, 4),
    (6, 3),
    (6, 4);


-- =========================================================
-- 11. VOORKEUREN
-- =========================================================
-- Iedere leerling heeft exact 3 verschillende voorkeuren.

INSERT INTO voorkeuren (
    voorkeur_nummer,
    leerling_id,
    talenten_periode_id,
    ingericht_talent_id
)
VALUES
    -- 1AA
    (1, 1, 1, 1), (2, 1, 1, 2), (3, 1, 1, 3),
    (1, 2, 1, 2), (2, 2, 1, 3), (3, 2, 1, 1),
    (1, 3, 1, 3), (2, 3, 1, 1), (3, 3, 1, 2),
    (1, 4, 1, 1), (2, 4, 1, 3), (3, 4, 1, 2),

    -- 1AB
    (1, 5, 1, 2), (2, 5, 1, 1), (3, 5, 1, 3),
    (1, 6, 1, 3), (2, 6, 1, 2), (3, 6, 1, 1),
    (1, 7, 1, 1), (2, 7, 1, 3), (3, 7, 1, 2),
    (1, 8, 1, 2), (2, 8, 1, 3), (3, 8, 1, 1),

    -- 3KA
    (1, 9,  1, 4), (2, 9,  1, 5), (3, 9,  1, 6),
    (1, 10, 1, 5), (2, 10, 1, 6), (3, 10, 1, 4),
    (1, 11, 1, 6), (2, 11, 1, 4), (3, 11, 1, 5),
    (1, 12, 1, 4), (2, 12, 1, 6), (3, 12, 1, 5),

    -- 3KB
    (1, 13, 1, 5), (2, 13, 1, 4), (3, 13, 1, 6),
    (1, 14, 1, 6), (2, 14, 1, 5), (3, 14, 1, 4),
    (1, 15, 1, 4), (2, 15, 1, 6), (3, 15, 1, 5),
    (1, 16, 1, 5), (2, 16, 1, 6), (3, 16, 1, 4);


-- =========================================================
-- 12. TOEWIJZINGEN
-- =========================================================
-- Bewust GEEN toewijzingen invoegen.
--
-- De demo start dus met:
--   - leerlingen aanwezig
--   - 3 voorkeuren per leerling aanwezig
--   - ingerichte talenten aanwezig
--   - 0 toewijzingen
--
-- Tijdens de demonstratie voer je daarna in de applicatie
-- zelf de automatische verdeling uit.


COMMIT;


-- =========================================================
-- 13. SNELLE CONTROLES
-- =========================================================

SELECT COUNT(*) AS schooljaren FROM schooljaren;
SELECT COUNT(*) AS klassen FROM klassen;
SELECT COUNT(*) AS leerlingen FROM leerlingen;
SELECT COUNT(*) AS klashistoriek_regels FROM leerling_klas_historiek;
SELECT COUNT(*) AS leerkrachten FROM leerkrachten;
SELECT COUNT(*) AS talenten FROM talenten;
SELECT COUNT(*) AS ingerichte_talenten FROM ingerichte_talenten;
SELECT COUNT(*) AS voorkeuren FROM voorkeuren;
SELECT COUNT(*) AS toewijzingen FROM toewijzingen;

-- Verwacht:
--   4 klassen
--   16 leerlingen
--   16 klashistoriekregels
--   6 ingerichte talenten
--   48 voorkeuren
--   0 toewijzingen
--   16 niet-toegewezen leerlingen vóór automatische verdeling

SELECT
    k.klas_naam,
    COUNT(l.leerling_id) AS aantal_leerlingen,
    COUNT(tw.toewijzing_id) AS aantal_toegewezen
FROM klassen k
         LEFT JOIN leerlingen l
                   ON l.klas_id = k.klas_id
         LEFT JOIN toewijzingen tw
                   ON tw.leerling_id = l.leerling_id
                       AND tw.talenten_periode_id = 1
GROUP BY
    k.klas_id,
    k.klas_naam
ORDER BY
    k.klas_naam;

SELECT
    l.voornaam,
    l.achternaam,
    k.klas_naam
FROM leerlingen l
         JOIN klassen k
              ON k.klas_id = l.klas_id
         LEFT JOIN toewijzingen tw
                   ON tw.leerling_id = l.leerling_id
                       AND tw.talenten_periode_id = 1
WHERE tw.toewijzing_id IS NULL
ORDER BY
    k.klas_naam,
    l.achternaam,
    l.voornaam;

SELECT
    it.naam AS ingericht_talent,
    it.maximum_capaciteit,
    COUNT(tw.toewijzing_id) AS toegewezen,
    it.maximum_capaciteit - COUNT(tw.toewijzing_id) AS vrije_plaatsen
FROM ingerichte_talenten it
         LEFT JOIN toewijzingen tw
                   ON tw.ingericht_talent_id = it.ingericht_talent_id
                       AND tw.talenten_periode_id = it.talenten_periode_id
WHERE it.talenten_periode_id = 1
GROUP BY
    it.ingericht_talent_id,
    it.naam,
    it.maximum_capaciteit
ORDER BY
    it.naam;
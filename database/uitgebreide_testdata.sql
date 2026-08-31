-- =========================================================
-- KK TALENTEN - UITGEBREIDE TESTDATA
-- =========================================================
-- Vereisten:
--   1. Voer eerst database/CREATE_SCRIPT.sql uit.
--   2. Voer daarna dit script uit.
--
-- Dit script wist alle bestaande data, maar behoudt het schema.
-- Dataset:
--   - 1 actief schooljaar (2026-2027)
--   - 5 talentenperiodes
--   - 6 klassen, gelijk verdeeld over beide doelgroepen
--   - 8 actieve leerlingen per klas (48 totaal)
--   - 10 actieve leerkrachten
--   - 10 basistalenten
--   - dezelfde 10 ingerichte talenten in elk van de 5 periodes (50 totaal)
--   - 3 voorkeuren per leerling voor periodes 1 tot en met 4
--   - toewijzingen voor periodes 1 tot en met 4
--   - geen voorkeuren of toewijzingen voor periode 5, zodat de volledige
--     keuzelijst- en verdelingsflow kan worden getest
-- =========================================================

BEGIN;

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
-- 1. SCHOOLJAAR EN VIJF PERIODES
-- =========================================================

INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
VALUES ('2026-2027', DATE '2026-09-01', DATE '2027-06-30', TRUE);

INSERT INTO talenten_periodes (naam, startdatum, einddatum, schooljaar_id)
SELECT periode.naam, periode.startdatum, periode.einddatum, sj.schooljaar_id
FROM schooljaren sj
CROSS JOIN (VALUES
    ('Periode 1 - Najaar',   DATE '2026-09-01', DATE '2026-10-23'),
    ('Periode 2 - Winter',   DATE '2026-11-02', DATE '2026-12-18'),
    ('Periode 3 - Januari',  DATE '2027-01-04', DATE '2027-02-12'),
    ('Periode 4 - Voorjaar', DATE '2027-02-22', DATE '2027-04-02'),
    ('Periode 5 - Slot',     DATE '2027-04-19', DATE '2027-06-30')
) AS periode(naam, startdatum, einddatum)
WHERE sj.naam = '2026-2027';

-- =========================================================
-- 2. ZES KLASSEN
-- =========================================================

INSERT INTO klassen (klas_naam, schooljaar, leerjaar, doelgroep)
VALUES
    ('OBS1A', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS1B', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS2A', '2026-2027', 2, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('KWA3A', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA3B', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA4A', '2026-2027', 4, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB');

-- Acht herkenbare testleerlingen per klas.
INSERT INTO leerlingen (voornaam, achternaam, klas_id, actief)
SELECT
    'Leerling' || to_char(nummer, 'FM00'),
    'Klas' || klas.klas_naam,
    klas.klas_id,
    TRUE
FROM klassen klas
CROSS JOIN generate_series(1, 8) AS nummer
WHERE klas.schooljaar = '2026-2027'
ORDER BY klas.klas_id, nummer;

-- Iedere leerling krijgt een actuele klashistoriek.
INSERT INTO leerling_klas_historiek (leerling_id, klas_id, vanaf, tot)
SELECT leerling_id, klas_id, DATE '2026-09-01', NULL
FROM leerlingen;

-- =========================================================
-- 3. LEERKRACHTEN EN BASISTALENTEN
-- =========================================================

INSERT INTO leerkrachten (voornaam, achternaam, actief)
VALUES
    ('Anke',    'Verhoeven',      TRUE),
    ('Bram',    'De Smet',        TRUE),
    ('Caroline','Janssens',       TRUE),
    ('David',   'Peeters',        TRUE),
    ('Elise',   'Willems',        TRUE),
    ('Farid',   'El Amrani',      TRUE),
    ('Greet',   'Van den Broeck', TRUE),
    ('Hanne',   'Jacobs',         TRUE),
    ('Ilias',   'Yilmaz',         TRUE),
    ('Joris',   'Vermeulen',      TRUE);

INSERT INTO talenten (naam, beschrijving)
VALUES
    ('Schaken',         'Strategisch denken, vooruit plannen en schaken.'),
    ('Voetbal',         'Balvaardigheid, samenspel en spelinzicht.'),
    ('Koken',           'Basisvaardigheden en veilige technieken in de keuken.'),
    ('Houtbewerking',   'Ontwerpen en veilig werken met hout.'),
    ('Fietstechniek',   'Onderhoud en eenvoudige fietsherstellingen.'),
    ('Digitale media',  'Foto, video en digitale creatie.'),
    ('Muziek',          'Ritme, samenspel en muzikale expressie.'),
    ('Theater',         'Spel, presentatie en samenwerken op een podium.'),
    ('Tuinieren',       'Planten verzorgen en duurzaam tuinbeheer.'),
    ('Wetenschappen',   'Onderzoeken en experimenteren rond STEM-thema''s.');

-- =========================================================
-- 4. TIEN INGERICHTE TALENTEN PER PERIODE
-- =========================================================
-- Vijf talenten per doelgroep. Capaciteit 10 geeft per doelgroep
-- 50 plaatsen voor 24 leerlingen en dus voldoende totale capaciteit.

INSERT INTO ingerichte_talenten (
    naam, omschrijving, maximum_capaciteit, doelgroep,
    actief, talent_id, talenten_periode_id
)
SELECT
    inrichting.naam || ' - ' || periode.naam,
    talent.beschrijving,
    10,
    inrichting.doelgroep,
    TRUE,
    talent.talent_id,
    periode.talenten_periode_id
FROM (VALUES
    ('OBS Schaken',        'Schaken',        'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS Voetbal',        'Voetbal',        'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS Koken',          'Koken',          'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS Digitale media', 'Digitale media', 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('OBS Muziek',         'Muziek',         'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('KWA Houtbewerking',  'Houtbewerking',  'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA Fietstechniek',  'Fietstechniek',  'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA Theater',        'Theater',        'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA Tuinieren',      'Tuinieren',      'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('KWA Wetenschappen',  'Wetenschappen',  'KWALIFICATIEFASE_TWEEDEGRAAD_AB')
) AS inrichting(naam, talentnaam, doelgroep)
JOIN talenten talent ON talent.naam = inrichting.talentnaam
CROSS JOIN talenten_periodes periode;

-- Elk ingericht talent krijgt één primaire leerkracht. De tien
-- leerkrachten worden voor iedere periode opnieuw over de talenten verdeeld.
INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
SELECT
    ingericht.ingericht_talent_id,
    leerkracht.leerkracht_id
FROM (
    SELECT
        ingericht_talent_id,
        row_number() OVER (
            PARTITION BY talenten_periode_id
            ORDER BY ingericht_talent_id
        ) AS volgnummer
    FROM ingerichte_talenten
) ingericht
JOIN (
    SELECT
        leerkracht_id,
        row_number() OVER (ORDER BY leerkracht_id) AS volgnummer
    FROM leerkrachten
    WHERE actief = TRUE
) leerkracht USING (volgnummer);

-- =========================================================
-- 5. DRIE VOORKEUREN PER LEERLING VOOR PERIODES 1 TOT EN MET 4
-- =========================================================
-- De voorkeuren roteren over de vijf talenten van de doelgroep.
-- Hierdoor ontstaat een gevarieerde maar reproduceerbare verdeling.

WITH talenten_per_doelgroep AS (
    SELECT
        it.ingericht_talent_id,
        it.talenten_periode_id,
        it.doelgroep,
        row_number() OVER (
            PARTITION BY it.talenten_periode_id, it.doelgroep
            ORDER BY it.ingericht_talent_id
        ) AS talent_nummer
    FROM ingerichte_talenten it
    JOIN talenten_periodes tp
      ON tp.talenten_periode_id = it.talenten_periode_id
    WHERE tp.naam <> 'Periode 5 - Slot'
), leerlingen_genummerd AS (
    SELECT
        l.leerling_id,
        k.doelgroep,
        row_number() OVER (
            PARTITION BY k.doelgroep
            ORDER BY k.klas_naam, l.achternaam, l.voornaam
        ) AS leerling_nummer
    FROM leerlingen l
    JOIN klassen k ON k.klas_id = l.klas_id
    WHERE l.actief = TRUE
)
INSERT INTO voorkeuren (
    voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id
)
SELECT
    voorkeur_nummer,
    leerling.leerling_id,
    talent.talenten_periode_id,
    talent.ingericht_talent_id
FROM leerlingen_genummerd leerling
CROSS JOIN generate_series(1, 3) AS voorkeur_nummer
JOIN talenten_per_doelgroep talent
  ON talent.doelgroep = leerling.doelgroep
 AND talent.talent_nummer = ((leerling.leerling_nummer + voorkeur_nummer - 2) % 5) + 1;

-- =========================================================
-- 6. HISTORISCHE TOEWIJZINGEN VOOR PERIODES 1 TOT EN MET 4
-- =========================================================
-- Iedere leerling wordt automatisch toegewezen aan de eerste voorkeur.
-- Periode 5 blijft bewust volledig zonder toewijzingen.

INSERT INTO toewijzingen (
    toewijzings_type,
    voorkeur_nummer,
    leerling_id,
    talenten_periode_id,
    ingericht_talent_id,
    toegewezen_op
)
SELECT
    'AUTOMATISCH',
    1,
    voorkeur.leerling_id,
    voorkeur.talenten_periode_id,
    voorkeur.ingericht_talent_id,
    periode.einddatum::timestamp
FROM voorkeuren voorkeur
JOIN talenten_periodes periode
  ON periode.talenten_periode_id = voorkeur.talenten_periode_id
WHERE voorkeur.voorkeur_nummer = 1
  AND periode.naam <> 'Periode 5 - Slot';

-- =========================================================
-- 7. AUTOMATISCHE CONTROLES
-- =========================================================

DO $$
DECLARE
    aantal_schooljaren INTEGER;
    aantal_periodes INTEGER;
    aantal_klassen INTEGER;
    aantal_leerlingen INTEGER;
    aantal_talenten INTEGER;
    aantal_ingerichte_talenten INTEGER;
    aantal_voorkeuren INTEGER;
    aantal_toewijzingen INTEGER;
BEGIN
    SELECT COUNT(*) INTO aantal_schooljaren FROM schooljaren;
    SELECT COUNT(*) INTO aantal_periodes FROM talenten_periodes;
    SELECT COUNT(*) INTO aantal_klassen FROM klassen;
    SELECT COUNT(*) INTO aantal_leerlingen FROM leerlingen;
    SELECT COUNT(*) INTO aantal_talenten FROM talenten;
    SELECT COUNT(*) INTO aantal_ingerichte_talenten FROM ingerichte_talenten;
    SELECT COUNT(*) INTO aantal_voorkeuren FROM voorkeuren;
    SELECT COUNT(*) INTO aantal_toewijzingen FROM toewijzingen;

    IF aantal_schooljaren <> 1
       OR aantal_periodes <> 5
       OR aantal_klassen <> 6
       OR aantal_leerlingen <> 48
       OR aantal_talenten <> 10
       OR aantal_ingerichte_talenten <> 50
       OR aantal_voorkeuren <> 576
       OR aantal_toewijzingen <> 192 THEN
        RAISE EXCEPTION
            'Testdata onvolledig: schooljaren=%, periodes=%, klassen=%, leerlingen=%, talenten=%, ingerichte talenten=%, voorkeuren=%, toewijzingen=%',
            aantal_schooljaren, aantal_periodes, aantal_klassen,
            aantal_leerlingen, aantal_talenten, aantal_ingerichte_talenten,
            aantal_voorkeuren, aantal_toewijzingen;
    END IF;
END $$;

COMMIT;

SELECT
    k.klas_naam,
    k.doelgroep,
    COUNT(l.leerling_id) AS leerlingen
FROM klassen k
LEFT JOIN leerlingen l ON l.klas_id = k.klas_id
GROUP BY k.klas_id, k.klas_naam, k.doelgroep
ORDER BY k.klas_naam;

SELECT
    tp.naam AS periode,
    COUNT(DISTINCT it.ingericht_talent_id) AS ingerichte_talenten,
    COUNT(DISTINCT v.voorkeur_id) AS voorkeuren,
    COUNT(DISTINCT tw.toewijzing_id) AS toewijzingen
FROM talenten_periodes tp
LEFT JOIN ingerichte_talenten it
       ON it.talenten_periode_id = tp.talenten_periode_id
LEFT JOIN voorkeuren v
       ON v.talenten_periode_id = tp.talenten_periode_id
LEFT JOIN toewijzingen tw
       ON tw.talenten_periode_id = tp.talenten_periode_id
GROUP BY tp.talenten_periode_id, tp.naam, tp.startdatum
ORDER BY tp.startdatum;

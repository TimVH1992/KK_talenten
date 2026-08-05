BEGIN;

-- Let op: dit wist alle bestaande gegevens uit de tabellen.
TRUNCATE TABLE
    ingericht_talent_leerkrachten,
    voorkeuren,
    toewijzingen,
    leerlingen,
    ingerichte_talenten,
    leerkrachten,
    talenten,
    talenten_periodes,
    klassen
RESTART IDENTITY CASCADE;

-- =========================================================
-- KLASSEN
-- =========================================================
INSERT INTO klassen (klas_naam, schooljaar, leerjaar, doelgroep)
VALUES
    ('1AA', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('1AB', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB');

-- =========================================================
-- LEERLINGEN
-- =========================================================
INSERT INTO leerlingen (voornaam, achternaam, klas_id)
VALUES
    ('Alice',  'Janssens', 1),
    ('Bram',   'Peeters',  1),
    ('Celine', 'Willems',  1),
    ('Daan',   'Maes',     1),
    ('Emma',   'Claes',    2),
    ('Finn',   'Jacobs',   2),
    ('Gitte',  'Vermeulen',2),
    ('Hugo',   'De Smet',  2);

-- =========================================================
-- LEERKRACHTEN
-- =========================================================
INSERT INTO leerkrachten (voornaam, achternaam)
VALUES
    ('Sofie', 'De Clercq'),
    ('Tom',   'Goossens'),
    ('Anke',  'Verhoeven'),
    ('Pieter','Van den Broeck');

-- =========================================================
-- TALENTEN
-- =========================================================
INSERT INTO talenten (naam, beschrijving)
VALUES
    ('Schaken', 'Strategisch denken en leren schaken.'),
    ('Voetbal', 'Techniek, samenspel en spelinzicht.'),
    ('Koken',   'Basisvaardigheden in de keuken.'),
    ('Muziek',  'Samen ritme, zang en instrumenten verkennen.');

-- =========================================================
-- TALENTENPERIODE
-- =========================================================
INSERT INTO talenten_periodes (naam, startdatum, einddatum)
VALUES ('Herfst 2026', '2026-09-01', '2026-10-31');

-- =========================================================
-- INGERICHTE TALENTEN
-- ID's na RESTART IDENTITY:
-- 1 = Schaken, 2 = Voetbal, 3 = Koken, 4 = Muziek
-- =========================================================
INSERT INTO ingerichte_talenten (maximum_capaciteit, doelgroep, talent_id, talenten_periode_id)
VALUES
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 1, 1),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 2, 1),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 3, 1),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 4, 1);

INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
VALUES
    (1, 1),
    (2, 2),
    (3, 3),
    (4, 4);

-- =========================================================
-- VOORKEUREN: telkens exact drie verschillende keuzes
-- =========================================================
INSERT INTO voorkeuren (voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES
    -- Alice: Schaken, Koken, Muziek
    (1, 1, 1, 1), (2, 1, 1, 3), (3, 1, 1, 4),
    -- Bram: Schaken, Voetbal, Koken
    (1, 2, 1, 1), (2, 2, 1, 2), (3, 2, 1, 3),
    -- Celine: Schaken, Muziek, Voetbal
    (1, 3, 1, 1), (2, 3, 1, 4), (3, 3, 1, 2),
    -- Daan: Koken, Schaken, Voetbal
    (1, 4, 1, 3), (2, 4, 1, 1), (3, 4, 1, 2),
    -- Emma: Schaken, Koken, Muziek
    (1, 5, 1, 1), (2, 5, 1, 3), (3, 5, 1, 4),
    -- Finn: Voetbal, Schaken, Koken
    (1, 6, 1, 2), (2, 6, 1, 1), (3, 6, 1, 3),
    -- Gitte: Koken, Muziek, Schaken
    (1, 7, 1, 3), (2, 7, 1, 4), (3, 7, 1, 1),
    -- Hugo: Muziek, Voetbal, Koken
    (1, 8, 1, 4), (2, 8, 1, 2), (3, 8, 1, 3);

-- Bewust nog geen actuele toewijzingen:
-- die worden via de knop 'Automatische verdeling' aangemaakt.

COMMIT;

-- Snelle controles
SELECT COUNT(*) AS aantal_klassen FROM klassen;
SELECT COUNT(*) AS aantal_leerlingen FROM leerlingen;
SELECT COUNT(*) AS aantal_ingerichte_talenten FROM ingerichte_talenten;
SELECT COUNT(*) AS aantal_voorkeuren FROM voorkeuren;
SELECT COUNT(*) AS aantal_toewijzingen_voor_start FROM toewijzingen;

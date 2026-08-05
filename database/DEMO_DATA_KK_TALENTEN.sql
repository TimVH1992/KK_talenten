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
    schooljaren,
    klassen
RESTART IDENTITY CASCADE;

-- =========================================================
-- SCHOOLJAREN
-- 2025-2026 is bewaard als archief en verschijnt niet in de normale selectie.
-- 2026-2027 is het onthouden actieve schooljaar.
-- =========================================================
INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
VALUES
    ('2025-2026', '2025-09-01', '2026-06-30', FALSE),
    ('2026-2027', '2026-09-01', '2027-06-30', TRUE);

-- =========================================================
-- KLASSEN: 4 klassen van telkens 8 leerlingen
-- =========================================================
INSERT INTO klassen (klas_naam, schooljaar, leerjaar, doelgroep)
VALUES
    ('1AA', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('1AB', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('2AA', '2026-2027', 2, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('2AB', '2026-2027', 2, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB');

INSERT INTO leerlingen (voornaam, achternaam, klas_id)
VALUES
    ('Alice',   'Janssens',    1),
    ('Bram',    'Peeters',     1),
    ('Celine',  'Willems',     1),
    ('Daan',    'Maes',        1),
    ('Ella',    'Claes',       1),
    ('Finn',    'Jacobs',      1),
    ('Gitte',   'Vermeulen',   1),
    ('Hugo',    'De Smet',     1),

    ('Ilias',   'Verhoeven',   2),
    ('Jana',    'Wouters',     2),
    ('Kobe',    'Mertens',     2),
    ('Lina',    'De Vos',      2),
    ('Milan',   'Goossens',    2),
    ('Noor',    'De Clercq',   2),
    ('Oscar',   'Van Damme',   2),
    ('Phaedra', 'Martens',     2),

    ('Quinten', 'Aerts',       3),
    ('Rania',   'Jacobs',      3),
    ('Seppe',   'Peeters',     3),
    ('Tessa',   'Maes',        3),
    ('Umut',    'Yilmaz',      3),
    ('Viktor',  'Willems',     3),
    ('Wout',    'Claes',       3),
    ('Xena',    'De Smedt',    3),

    ('Yara',    'Janssens',    4),
    ('Zeno',    'Vermeulen',   4),
    ('Amelie',  'De Vos',      4),
    ('Bas',     'Mertens',     4),
    ('Chloe',   'Goossens',    4),
    ('Dries',   'Aerts',       4),
    ('Elise',   'Wouters',     4),
    ('Ferre',   'Van Damme',   4);

INSERT INTO leerkrachten (voornaam, achternaam)
VALUES
    ('Sofie',  'De Clercq'),
    ('Tom',    'Goossens'),
    ('Anke',   'Verhoeven'),
    ('Pieter', 'Van den Broeck');

INSERT INTO talenten (naam, beschrijving)
VALUES
    ('Schaken', 'Strategisch denken en leren schaken.'),
    ('Voetbal', 'Techniek, samenspel en spelinzicht.'),
    ('Koken',   'Basisvaardigheden in de keuken.'),
    ('Muziek',  'Samen ritme, zang en instrumenten verkennen.');

-- =========================================================
-- TALENTENPERIODES
-- Het aantal periodes is bewust niet vast: dit schooljaar bevat er vijf.
-- =========================================================
INSERT INTO talenten_periodes (naam, startdatum, einddatum, schooljaar_id)
VALUES
    ('Herfst 2026',   '2026-09-01', '2026-10-31', 2),
    ('Winter 2026',   '2026-11-09', '2026-12-18', 2),
    ('Lente 2027',    '2027-01-11', '2027-02-26', 2),
    ('Voorjaar 2027', '2027-03-08', '2027-04-30', 2),
    ('Zomer 2027',    '2027-05-10', '2027-06-25', 2),
    ('Lente 2026',    '2026-03-01', '2026-05-31', 1);

-- =========================================================
-- INGERICHTE TALENTEN VOOR HERFST 2026
-- Totale capaciteit = 27. Bij 32 leerlingen blijven er dus exact 5 over.
-- ID 1 Schaken (7), ID 2 Voetbal (7), ID 3 Koken (7), ID 4 Muziek (6)
-- =========================================================
INSERT INTO ingerichte_talenten (maximum_capaciteit, doelgroep, talent_id, talenten_periode_id)
VALUES
    (7, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 1, 1),
    (7, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 2, 1),
    (7, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 3, 1),
    (6, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 4, 1),

    -- Historische ingerichte talenten uit schooljaar 2025-2026
    (8, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 1, 6),
    (8, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 2, 6),
    (8, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 3, 6),
    (8, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 4, 6);

INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
VALUES
    (1, 1), (2, 2), (3, 3), (4, 4),
    (5, 1), (6, 2), (7, 3), (8, 4);

-- =========================================================
-- VOORKEUREN HERFST 2026
-- De gekozen volgorde zorgt met de huidige verdeler voor:
-- 19 eerste voorkeuren, 6 tweede voorkeuren, 2 derde voorkeuren en 5 niet toegewezen.
-- =========================================================
INSERT INTO voorkeuren (voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES
    -- 1AA
    (1, 1, 1, 3), (2, 1, 1, 4), (3, 1, 1, 2),
    (1, 2, 1, 2), (2, 2, 1, 1), (3, 2, 1, 3),
    (1, 3, 1, 1), (2, 3, 1, 3), (3, 3, 1, 2),
    (1, 4, 1, 2), (2, 4, 1, 3), (3, 4, 1, 4),
    (1, 5, 1, 4), (2, 5, 1, 1), (3, 5, 1, 3),
    (1, 6, 1, 3), (2, 6, 1, 4), (3, 6, 1, 2),
    (1, 7, 1, 1), (2, 7, 1, 2), (3, 7, 1, 4),
    (1, 8, 1, 3), (2, 8, 1, 1), (3, 8, 1, 4),

    -- 1AB
    (1, 9, 1, 3), (2, 9, 1, 1), (3, 9, 1, 4),
    (1, 10, 1, 4), (2, 10, 1, 3), (3, 10, 1, 1),
    (1, 11, 1, 3), (2, 11, 1, 4), (3, 11, 1, 2),
    (1, 12, 1, 3), (2, 12, 1, 4), (3, 12, 1, 1),
    (1, 13, 1, 2), (2, 13, 1, 1), (3, 13, 1, 3),
    (1, 14, 1, 2), (2, 14, 1, 4), (3, 14, 1, 3),
    (1, 15, 1, 4), (2, 15, 1, 3), (3, 15, 1, 2),
    (1, 16, 1, 2), (2, 16, 1, 1), (3, 16, 1, 4),

    -- 2AA
    (1, 17, 1, 2), (2, 17, 1, 1), (3, 17, 1, 4),
    (1, 18, 1, 2), (2, 18, 1, 4), (3, 18, 1, 1),
    (1, 19, 1, 2), (2, 19, 1, 1), (3, 19, 1, 4),
    (1, 20, 1, 3), (2, 20, 1, 1), (3, 20, 1, 4),
    (1, 21, 1, 3), (2, 21, 1, 4), (3, 21, 1, 1),
    (1, 22, 1, 2), (2, 22, 1, 1), (3, 22, 1, 3),
    (1, 23, 1, 4), (2, 23, 1, 1), (3, 23, 1, 2),
    (1, 24, 1, 1), (2, 24, 1, 3), (3, 24, 1, 2),

    -- 2AB
    (1, 25, 1, 4), (2, 25, 1, 1), (3, 25, 1, 3),
    (1, 26, 1, 4), (2, 26, 1, 1), (3, 26, 1, 2),
    (1, 27, 1, 4), (2, 27, 1, 1), (3, 27, 1, 3),
    (1, 28, 1, 2), (2, 28, 1, 1), (3, 28, 1, 3),
    (1, 29, 1, 1), (2, 29, 1, 2), (3, 29, 1, 3),
    (1, 30, 1, 2), (2, 30, 1, 4), (3, 30, 1, 1),
    (1, 31, 1, 2), (2, 31, 1, 4), (3, 31, 1, 3),
    (1, 32, 1, 1), (2, 32, 1, 3), (3, 32, 1, 2);

-- Historische gegevens uit het vorige schooljaar blijven bewaard,
-- maar worden niet in de normale schooljaarselectie getoond.
INSERT INTO toewijzingen (toewijzings_type, voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id, toegewezen_op)
VALUES
    ('AUTOMATISCH', 1, 1, 6, 7, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 2, 2, 6, 6, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 3, 6, 8, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 3, 4, 6, 5, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 5, 6, 7, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 2, 6, 6, 8, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 2, 7, 6, 5, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 8, 6, 6, '2026-03-01 09:00:00');

COMMIT;

-- Snelle controles vóór de automatische verdeling
SELECT COUNT(*) AS aantal_selecteerbare_schooljaren FROM schooljaren WHERE einddatum >= CURRENT_DATE OR actief = TRUE;
SELECT COUNT(*) AS aantal_klassen FROM klassen WHERE schooljaar = '2026-2027';
SELECT COUNT(*) AS aantal_leerlingen FROM leerlingen;
SELECT COUNT(*) AS aantal_voorkeuren_herfst FROM voorkeuren WHERE talenten_periode_id = 1;
SELECT COUNT(*) AS historische_toewijzingen FROM toewijzingen WHERE talenten_periode_id = 6;

-- Verwacht NA één automatische verdeling voor Herfst 2026:
-- voorkeur 1 = 19, voorkeur 2 = 6, voorkeur 3 = 2, totaal = 27 en 5 leerlingen niet toegewezen.
-- Controleer dit met:
-- SELECT voorkeur_nummer, COUNT(*) FROM toewijzingen WHERE talenten_periode_id = 1 GROUP BY voorkeur_nummer ORDER BY voorkeur_nummer;

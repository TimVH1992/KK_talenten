BEGIN;

TRUNCATE TABLE
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
RESTART IDENTITY CASCADE;

-- SCHOOLJAAR
INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
VALUES ('2026-2027', '2026-09-01', '2027-06-30', TRUE);

-- KLASSEN
INSERT INTO klassen (klas_naam, schooljaar, leerjaar, doelgroep)
VALUES
    ('1AA', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('1AB', '2026-2027', 1, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('2AA', '2026-2027', 2, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB'),
    ('3KA', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('3KB', '2026-2027', 3, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB'),
    ('4KA', '2026-2027', 4, 'KWALIFICATIEFASE_TWEEDEGRAAD_AB');

-- LEERLINGEN: 6 klassen x 5 leerlingen = 30
INSERT INTO leerlingen (voornaam, achternaam, klas_id)
VALUES
    ('Alice','Janssens',1), ('Bram','Peeters',1), ('Celine','Willems',1), ('Daan','Maes',1), ('Emma','Claes',1),
    ('Finn','Jacobs',2), ('Gitte','Vermeulen',2), ('Hugo','De Smet',2), ('Imani','Mertens',2), ('Jules','Van Damme',2),
    ('Kaat','Goossens',3), ('Liam','Verhoeven',3), ('Mila','De Vos',3), ('Noah','Hermans',3), ('Olivia','Vercammen',3),
    ('Pieter','Aerts',4), ('Quinten','Van Looy',4), ('Rani','De Ridder',4), ('Seppe','Vandenberghe',4), ('Tess','Smets',4),
    ('Umut','Yilmaz',5), ('Vince','Wouters',5), ('Wout','Van den Bossche',5), ('Xena','De Meyer',5), ('Yara','Verstraeten',5),
    ('Zeno','Cornelis',6), ('Amine','El Amrani',6), ('Bo','Lambrechts',6), ('Cas','Michiels',6), ('Dina','Van Acker',6);

-- LEERKRACHTEN
INSERT INTO leerkrachten (voornaam, achternaam, actief)
VALUES
    ('Sofie','De Clercq',TRUE),
    ('Tom','Goossens',TRUE),
    ('Anke','Verhoeven',TRUE),
    ('Pieter','Van den Broeck',TRUE),
    ('Sarah','De Winter',TRUE),
    ('Koen','Willems',TRUE),
    ('Nina','Peeters',TRUE),
    ('Mark','Janssens',FALSE);

-- TALENTEN
INSERT INTO talenten (naam, beschrijving, actief)
VALUES
    ('Schaken','Strategisch denken en leren schaken.',TRUE),
    ('Voetbal','Techniek, samenspel en spelinzicht.',TRUE),
    ('Koken','Basisvaardigheden in de keuken.',TRUE),
    ('Muziek','Ritme, zang en instrumenten verkennen.',TRUE),
    ('Houtbewerking','Veilig en praktisch leren werken met hout.',TRUE),
    ('Fietstechniek','Onderhoud, techniek en fietsvaardigheid.',TRUE),
    ('Tuin & Natuur','Praktisch werken rond natuur en groen.',TRUE),
    ('Digitale Media','Creatief werken met digitale media.',TRUE),
    ('Sportmix','Kennismaken met verschillende sporten.',TRUE),
    ('Creatief Atelier','Tekenen, schilderen en creatieve technieken.',FALSE);

-- TALENTENPERIODE
INSERT INTO talenten_periodes (naam, startdatum, einddatum, schooljaar_id)
VALUES ('Herfst 2026', '2026-09-01', '2026-10-31', 1);

-- INGERICHTE TALENTEN
-- 1..5 = actief observatie
-- 6..10 = actief kwalificatie
-- 11 = bewust inactief
INSERT INTO ingerichte_talenten
    (naam, omschrijving, maximum_capaciteit, doelgroep, actief, talent_id, talenten_periode_id)
VALUES
    ('Schakenatelier','Strategie en concentratie via schaken.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',TRUE,1,1),
    ('Voetbal - Basis','Basisvaardigheden voetbal en samenspel.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',TRUE,2,1),
    ('Voetbal - Techniek','Extra focus op balcontrole en techniek.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',TRUE,2,1),
    ('Kookatelier','Eenvoudige gerechten leren bereiden.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',TRUE,3,1),
    ('Muziekatelier','Ritme, zang en instrumenten.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',TRUE,4,1),
    ('Houtatelier','Praktische opdrachten met hout.',8,'KWALIFICATIEFASE_TWEEDEGRAAD_AB',TRUE,5,1),
    ('Fietsatelier','Onderhoud en eenvoudige fietsherstellingen.',8,'KWALIFICATIEFASE_TWEEDEGRAAD_AB',TRUE,6,1),
    ('Tuinatelier','Praktisch werken in tuin en groen.',8,'KWALIFICATIEFASE_TWEEDEGRAAD_AB',TRUE,7,1),
    ('Digitale Media','Foto, video en digitale creatie.',8,'KWALIFICATIEFASE_TWEEDEGRAAD_AB',TRUE,8,1),
    ('Sportmix','Afwisselend aanbod van sporten.',8,'KWALIFICATIEFASE_TWEEDEGRAAD_AB',TRUE,9,1),
    ('Creatief Atelier - niet aangeboden','Bewust inactief testtalent.',8,'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',FALSE,10,1);

-- LEERKRACHTEN PER INGERICHT TALENT
INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
VALUES
    (1,1),
    (2,2),(2,3),
    (3,2),
    (4,3),(4,5),
    (5,1),(5,5),
    (6,4),(6,6),
    (7,6),
    (8,7),
    (9,5),(9,7),
    (10,4),
    (11,1);

-- VOORKEUREN OBSERVATIE: leerlingen 1..15, talenten 1..5
INSERT INTO voorkeuren (voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES
    (1,1,1,1),(2,1,1,2),(3,1,1,4),
    (1,2,1,2),(2,2,1,3),(3,2,1,1),
    (1,3,1,3),(2,3,1,5),(3,3,1,2),
    (1,4,1,4),(2,4,1,1),(3,4,1,5),
    (1,5,1,5),(2,5,1,4),(3,5,1,3),
    (1,6,1,1),(2,6,1,3),(3,6,1,5),
    (1,7,1,2),(2,7,1,1),(3,7,1,4),
    (1,8,1,3),(2,8,1,2),(3,8,1,5),
    (1,9,1,4),(2,9,1,5),(3,9,1,1),
    (1,10,1,5),(2,10,1,3),(3,10,1,2),
    (1,11,1,1),(2,11,1,4),(3,11,1,2),
    (1,12,1,2),(2,12,1,5),(3,12,1,3),
    (1,13,1,3),(2,13,1,1),(3,13,1,4),
    (1,14,1,4),(2,14,1,2),(3,14,1,5),
    (1,15,1,5),(2,15,1,4),(3,15,1,1);

-- VOORKEUREN KWALIFICATIE: leerlingen 16..30, talenten 6..10
INSERT INTO voorkeuren (voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES
    (1,16,1,6),(2,16,1,7),(3,16,1,9),
    (1,17,1,7),(2,17,1,8),(3,17,1,6),
    (1,18,1,8),(2,18,1,10),(3,18,1,7),
    (1,19,1,9),(2,19,1,6),(3,19,1,10),
    (1,20,1,10),(2,20,1,9),(3,20,1,8),
    (1,21,1,6),(2,21,1,8),(3,21,1,10),
    (1,22,1,7),(2,22,1,6),(3,22,1,9),
    (1,23,1,8),(2,23,1,7),(3,23,1,10),
    (1,24,1,9),(2,24,1,10),(3,24,1,6),
    (1,25,1,10),(2,25,1,8),(3,25,1,7),
    (1,26,1,6),(2,26,1,9),(3,26,1,7),
    (1,27,1,7),(2,27,1,10),(3,27,1,8),
    (1,28,1,8),(2,28,1,6),(3,28,1,9),
    (1,29,1,9),(2,29,1,7),(3,29,1,10),
    (1,30,1,10),(2,30,1,9),(3,30,1,6);

-- Bewust geen actuele toewijzingen:
-- zo kan de automatische verdeling vanuit de applicatie getest worden.

COMMIT;

-- SNELLE CONTROLES
SELECT COUNT(*) AS aantal_schooljaren FROM schooljaren;
SELECT COUNT(*) AS aantal_klassen FROM klassen;
SELECT COUNT(*) AS aantal_leerlingen FROM leerlingen;
SELECT COUNT(*) AS aantal_leerkrachten FROM leerkrachten;
SELECT COUNT(*) AS aantal_talenten FROM talenten;
SELECT COUNT(*) AS aantal_talentenperiodes FROM talenten_periodes;
SELECT COUNT(*) AS aantal_ingerichte_talenten FROM ingerichte_talenten;
SELECT COUNT(*) AS aantal_actieve_ingerichte_talenten FROM ingerichte_talenten WHERE actief = TRUE;
SELECT COUNT(*) AS aantal_voorkeuren FROM voorkeuren;
SELECT COUNT(*) AS aantal_toewijzingen_voor_start FROM toewijzingen;

SELECT
    k.klas_naam,
    COUNT(l.leerling_id) AS aantal_leerlingen
FROM klassen k
LEFT JOIN leerlingen l ON l.klas_id = k.klas_id
GROUP BY k.klas_id, k.klas_naam
ORDER BY k.klas_naam;

SELECT
    doelgroep,
    COUNT(*) AS aantal_actief
FROM ingerichte_talenten
WHERE actief = TRUE
GROUP BY doelgroep
ORDER BY doelgroep;

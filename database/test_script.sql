-- Kleine handmatige rooktest voor het actuele databaseschema.
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

INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
VALUES ('2026-2027', '2026-09-01', '2027-06-30', TRUE);

INSERT INTO klassen (klas_naam, schooljaar, leerjaar, doelgroep)
VALUES ('2AA', '2026-2027', 2, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB');

INSERT INTO leerlingen (voornaam, achternaam, klas_id)
VALUES ('Jan', 'Peeters', 1), ('Julie', 'Martens', 1);

INSERT INTO leerkrachten (voornaam, achternaam)
VALUES ('Sofie', 'De Clercq');

INSERT INTO talenten (naam, beschrijving)
VALUES ('Schaken', 'Strategisch denkspel');

INSERT INTO talenten_periodes (naam, startdatum, einddatum, schooljaar_id)
VALUES ('Herfst', '2026-09-01', '2026-10-31', 1);

INSERT INTO ingerichte_talenten (maximum_capaciteit, doelgroep, talent_id, talenten_periode_id)
VALUES (10, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 1, 1);

INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
VALUES (1, 1);

INSERT INTO voorkeuren (voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES (1, 1, 1, 1), (1, 2, 1, 1);

INSERT INTO toewijzingen (toewijzings_type, voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id)
VALUES ('AUTOMATISCH', 1, 1, 1, 1), ('MANUEEL', NULL, 2, 1, 1);

SELECT * FROM schooljaren;
SELECT * FROM talenten_periodes;
SELECT * FROM klassen;
SELECT * FROM leerlingen;
SELECT * FROM ingerichte_talenten;
SELECT * FROM voorkeuren;
SELECT * FROM toewijzingen;

-- 1. Klas
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



INSERT INTO klassen (
    klas_naam,
    schooljaar,
    leerjaar
)
VALUES (
           '2AA',
           '2026-2027',
           2
       );

-- 2. Leerlingen
INSERT INTO leerlingen (
    voornaam,
    achternaam,
    klas_id
)
VALUES
    ('Jan', 'Peeters', 1),
    ('Julie', 'Martens', 1);

-- 3. Talent
INSERT INTO talenten (
    naam,
    beschrijving
)
VALUES (
           'Schaken',
           'Strategisch denkspel'
       );

-- 4. Talentenperiode
INSERT INTO talenten_periodes (
    naam,
    startdatum,
    einddatum
)
VALUES (
           'Herfst',
           '2026-09-01',
           '2026-10-31'
       );

-- 5. Ingericht talent
INSERT INTO ingerichte_talenten (
    maximum_capaciteit,
    doelgroep,
    talent_id,
    talenten_periode_id
)
VALUES (
           10,
           'EERSTE_TOT_EN_MET_DERDE_JAAR',
           1,
           1
       );

-- 6. Voorkeuren
INSERT INTO voorkeuren (
    voorkeur_nummer,
    leerling_id,
    talenten_periode_id,
    ingericht_talent_id
)
VALUES
    (1, 1, 1, 1),
    (1, 2, 1, 1);

-- 7. Toewijzingen
INSERT INTO toewijzingen (
    toewijzings_type,
    voorkeur_nummer,
    leerling_id,
    talenten_periode_id,
    ingericht_talent_id
)
VALUES
    ('AUTOMATISCH', 1, 1, 1, 1),
    ('MANUEEL', NULL, 2, 1, 1);



SELECT *
FROM klassen;

SELECT *
FROM leerlingen;

SELECT *
FROM ingerichte_talenten;

SELECT *
FROM voorkeuren;

SELECT *
FROM toewijzingen;

-- EEn ongeldige insert proberen om te bekijken of de check constraint werkt

INSERT INTO voorkeuren (
    voorkeur_nummer,
    leerling_id,
    talenten_periode_id,
    ingericht_talent_id
)
VALUES
    (4, 2, 1, 1);

UPDATE voorkeuren
SET voorkeur_nummer = 4
WHERE leerling_id = 2;

SELECT voornaam, achternaam, klas_id, leerling_id
FROM leerlingen
WHERE klas_id = 1;
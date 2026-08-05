-- Voer dit script uit nadat DEMO_DATA_KK_TALENTEN.sql is geladen.
-- Het voegt een afgelopen talentenperiode met historische toewijzingen toe.

BEGIN;

INSERT INTO talenten_periodes (naam, startdatum, einddatum)
VALUES ('Lente 2026', '2026-03-01', '2026-05-31');

INSERT INTO ingerichte_talenten (maximum_capaciteit, doelgroep, talent_id, talenten_periode_id)
VALUES
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 1, 2),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 2, 2),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 3, 2),
    (3, 'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB', 4, 2);

-- Na de oorspronkelijke demodata krijgen deze ingerichte talenten id 5 t.e.m. 8.
INSERT INTO ingericht_talent_leerkrachten (ingericht_talent_id, leerkracht_id)
VALUES (5, 1), (6, 2), (7, 3), (8, 4);

INSERT INTO toewijzingen (toewijzings_type, voorkeur_nummer, leerling_id, talenten_periode_id, ingericht_talent_id, toegewezen_op)
VALUES
    ('AUTOMATISCH', 1, 1, 2, 7, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 2, 2, 6, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 3, 2, 8, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 4, 2, 5, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 5, 2, 7, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 6, 2, 8, '2026-03-01 09:00:00'),
    ('MANUEEL', NULL, 7, 2, 5, '2026-03-01 09:00:00'),
    ('AUTOMATISCH', 1, 8, 2, 6, '2026-03-01 09:00:00');

COMMIT;

SELECT l.voornaam, l.achternaam, tp.naam AS periode, t.naam AS talent
FROM toewijzingen tw
JOIN leerlingen l ON l.leerling_id = tw.leerling_id
JOIN talenten_periodes tp ON tp.talenten_periode_id = tw.talenten_periode_id
JOIN ingerichte_talenten it ON it.ingericht_talent_id = tw.ingericht_talent_id
JOIN talenten t ON t.talent_id = it.talent_id
WHERE tp.naam = 'Lente 2026'
ORDER BY l.achternaam;

SELECT
    l.leerling_id,
    l.voornaam,
    l.achternaam,
    t.naam AS talent,
    tw.toewijzings_type,
    tw.voorkeur_nummer,
    tw.toegewezen_op,
    tw.gewijzigd_op
FROM toewijzingen tw
         JOIN leerlingen l ON l.leerling_id = tw.leerling_id
         JOIN ingerichte_talenten it ON it.ingericht_talent_id = tw.ingericht_talent_id
         JOIN talenten t ON t.talent_id = it.talent_id
         JOIN talenten_periodes tp ON tp.talenten_periode_id = tw.talenten_periode_id
WHERE tp.naam = 'Lente 2026'
  AND tw.toewijzings_type = 'MANUEEL'
ORDER BY l.achternaam;
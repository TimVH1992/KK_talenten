BEGIN;

-- =========================================================
-- BESTAANDE DATABASESTRUCTUUR VERWIJDEREN
-- =========================================================

DROP TABLE IF EXISTS
    ingericht_talent_leerkrachten,
    voorkeuren,
    toewijzingen,
    leerlingen,
    ingerichte_talenten,
    leerkrachten,
    talenten,
    talenten_periodes,
    schooljaren,
    klassen,
    voorkeur_import_problemen
    CASCADE;

DROP FUNCTION IF EXISTS controleer_talentenperiode_binnen_schooljaar();
DROP FUNCTION IF EXISTS controleer_schooljaar_datums();


-- =========================================================
-- SCHOOLJAREN
-- =========================================================

CREATE TABLE schooljaren
(
    schooljaar_id BIGINT GENERATED ALWAYS AS IDENTITY,
    naam          VARCHAR(9) NOT NULL,
    startdatum    DATE       NOT NULL,
    einddatum     DATE       NOT NULL,
    actief        BOOLEAN    NOT NULL DEFAULT FALSE,

    CONSTRAINT pk_schooljaren
        PRIMARY KEY (schooljaar_id),

    CONSTRAINT uq_schooljaren_naam
        UNIQUE (naam),

    CONSTRAINT uq_schooljaren_datums
        UNIQUE (startdatum, einddatum),

    CONSTRAINT chk_schooljaren_naam
        CHECK (naam ~ '^[0-9]{4}-[0-9]{4}$'),

    CONSTRAINT chk_schooljaren_datums
        CHECK (einddatum > startdatum)
);

-- Er kan maximaal één actief schooljaar zijn.
CREATE UNIQUE INDEX uq_schooljaren_een_actief
    ON schooljaren (actief)
    WHERE actief = TRUE;


-- =========================================================
-- KLASSEN
-- =========================================================

CREATE TABLE klassen
(
    klas_id    BIGINT GENERATED ALWAYS AS IDENTITY,
    klas_naam  VARCHAR(50) NOT NULL,
    schooljaar VARCHAR(9)  NOT NULL,
    leerjaar   SMALLINT    NOT NULL,
    doelgroep  VARCHAR(50) NOT NULL,

    CONSTRAINT pk_klassen
        PRIMARY KEY (klas_id),

    CONSTRAINT uq_klassen_naam_schooljaar
        UNIQUE (klas_naam, schooljaar),

    CONSTRAINT chk_klassen_naam
        CHECK (btrim(klas_naam) <> ''),

    CONSTRAINT chk_klassen_schooljaar
        CHECK (schooljaar ~ '^[0-9]{4}-[0-9]{4}$'),

    CONSTRAINT chk_klassen_leerjaar
        CHECK (leerjaar BETWEEN 1 AND 7),

    CONSTRAINT chk_klassen_doelgroep
        CHECK (
            doelgroep IN (
                          'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',
                          'KWALIFICATIEFASE_TWEEDEGRAAD_AB'
                )
            )
);


-- =========================================================
-- LEERLINGEN
-- =========================================================

CREATE TABLE leerlingen
(
    leerling_id BIGINT GENERATED ALWAYS AS IDENTITY,
    voornaam    VARCHAR(100) NOT NULL,
    achternaam  VARCHAR(100) NOT NULL,
    klas_id     BIGINT       NOT NULL,

    CONSTRAINT pk_leerlingen
        PRIMARY KEY (leerling_id),

    CONSTRAINT fk_leerlingen_klas
        FOREIGN KEY (klas_id)
            REFERENCES klassen (klas_id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_leerlingen_voornaam
        CHECK (btrim(voornaam) <> ''),

    CONSTRAINT chk_leerlingen_achternaam
        CHECK (btrim(achternaam) <> '')
);


-- =========================================================
-- LEERKRACHTEN
-- =========================================================

CREATE TABLE leerkrachten
(
    leerkracht_id BIGINT GENERATED ALWAYS AS IDENTITY,
    voornaam      VARCHAR(100) NOT NULL,
    achternaam    VARCHAR(100) NOT NULL,
    actief        BOOLEAN      NOT NULL DEFAULT TRUE,

    CONSTRAINT pk_leerkrachten
        PRIMARY KEY (leerkracht_id),

    CONSTRAINT chk_leerkrachten_voornaam
        CHECK (btrim(voornaam) <> ''),

    CONSTRAINT chk_leerkrachten_achternaam
        CHECK (btrim(achternaam) <> '')
);


-- =========================================================
-- TALENTEN
-- =========================================================

CREATE TABLE talenten
(
    talent_id    BIGINT GENERATED ALWAYS AS IDENTITY,
    naam         VARCHAR(100) NOT NULL,
    beschrijving TEXT         NOT NULL,

    CONSTRAINT pk_talenten
        PRIMARY KEY (talent_id),

    CONSTRAINT uq_talenten_naam
        UNIQUE (naam),

    CONSTRAINT chk_talenten_naam
        CHECK (btrim(naam) <> ''),

    CONSTRAINT chk_talenten_beschrijving
        CHECK (btrim(beschrijving) <> '')
);


-- =========================================================
-- TALENTENPERIODES
-- =========================================================

CREATE TABLE talenten_periodes
(
    talenten_periode_id BIGINT GENERATED ALWAYS AS IDENTITY,
    naam                VARCHAR(100) NOT NULL,
    startdatum          DATE         NOT NULL,
    einddatum           DATE         NOT NULL,
    schooljaar_id       BIGINT       NOT NULL,

    CONSTRAINT pk_talenten_periodes
        PRIMARY KEY (talenten_periode_id),

    CONSTRAINT fk_talenten_periodes_schooljaar
        FOREIGN KEY (schooljaar_id)
            REFERENCES schooljaren (schooljaar_id)
            ON DELETE RESTRICT,

    CONSTRAINT chk_talenten_periodes_naam
        CHECK (btrim(naam) <> ''),

    CONSTRAINT chk_talenten_periodes_datums
        CHECK (einddatum > startdatum),

    CONSTRAINT uq_talenten_periodes_datums
        UNIQUE (startdatum, einddatum),

    CONSTRAINT uq_talenten_periodes_naam_schooljaar
        UNIQUE (naam, schooljaar_id)
);


-- =========================================================
-- CONTROLE: PERIODE MOET BINNEN SCHOOLJAAR VALLEN
-- =========================================================

CREATE OR REPLACE FUNCTION controleer_talentenperiode_binnen_schooljaar()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
DECLARE
    schooljaar_startdatum DATE;
    schooljaar_einddatum  DATE;
BEGIN
    SELECT startdatum, einddatum
    INTO schooljaar_startdatum, schooljaar_einddatum
    FROM schooljaren
    WHERE schooljaar_id = NEW.schooljaar_id;

    IF NOT FOUND THEN
        RAISE EXCEPTION
            'Schooljaar met id % bestaat niet.',
            NEW.schooljaar_id;
    END IF;

    IF NEW.startdatum < schooljaar_startdatum
        OR NEW.einddatum > schooljaar_einddatum THEN

        RAISE EXCEPTION
            'Talentenperiode "%" (% tot %) valt niet volledig binnen het schooljaar (% tot %).',
            NEW.naam,
            NEW.startdatum,
            NEW.einddatum,
            schooljaar_startdatum,
            schooljaar_einddatum;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_talentenperiode_binnen_schooljaar
    BEFORE INSERT OR UPDATE OF startdatum, einddatum, schooljaar_id
    ON talenten_periodes
    FOR EACH ROW
EXECUTE FUNCTION controleer_talentenperiode_binnen_schooljaar();


-- =========================================================
-- CONTROLE: SCHOOLJAAR MAG BESTAANDE PERIODES NIET UITSLUITEN
-- =========================================================

CREATE OR REPLACE FUNCTION controleer_schooljaar_datums()
    RETURNS TRIGGER
    LANGUAGE plpgsql
AS
$$
BEGIN
    IF EXISTS (SELECT 1
               FROM talenten_periodes
               WHERE schooljaar_id = NEW.schooljaar_id
                 AND (
                   startdatum < NEW.startdatum
                       OR einddatum > NEW.einddatum
                   )) THEN
        RAISE EXCEPTION
            'De datums van schooljaar "%" kunnen niet gewijzigd worden omdat minstens één talentenperiode dan buiten het schooljaar valt.',
            NEW.naam;
    END IF;

    RETURN NEW;
END;
$$;

CREATE TRIGGER trg_schooljaar_datums_controleren
    BEFORE UPDATE OF startdatum, einddatum
    ON schooljaren
    FOR EACH ROW
EXECUTE FUNCTION controleer_schooljaar_datums();


-- =========================================================
-- INGERICHTE TALENTEN
-- =========================================================

CREATE TABLE ingerichte_talenten
(
    ingericht_talent_id BIGINT GENERATED ALWAYS AS IDENTITY,
    naam                VARCHAR(100) NOT NULL,
    omschrijving        TEXT         NOT NULL,
    maximum_capaciteit  INTEGER      NOT NULL,
    doelgroep           VARCHAR(50)  NOT NULL,
    actief              BOOLEAN      NOT NULL DEFAULT TRUE,
    talent_id           BIGINT       NOT NULL,
    talenten_periode_id BIGINT       NOT NULL,

    CONSTRAINT pk_ingerichte_talenten
        PRIMARY KEY (ingericht_talent_id),

    CONSTRAINT fk_ingerichte_talenten_talent
        FOREIGN KEY (talent_id)
            REFERENCES talenten (talent_id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_ingerichte_talenten_periode
        FOREIGN KEY (talenten_periode_id)
            REFERENCES talenten_periodes (talenten_periode_id)
            ON DELETE CASCADE,

    CONSTRAINT chk_ingerichte_talenten_naam
        CHECK (btrim(naam) <> ''),

    CONSTRAINT chk_ingerichte_talenten_omschrijving
        CHECK (btrim(omschrijving) <> ''),

    CONSTRAINT chk_ingerichte_talenten_capaciteit
        CHECK (maximum_capaciteit > 0),

    CONSTRAINT chk_ingerichte_talenten_doelgroep
        CHECK (
            doelgroep IN (
                          'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',
                          'KWALIFICATIEFASE_TWEEDEGRAAD_AB'
                )
            ),

    -- Zelfde naam mag niet tweemaal voorkomen binnen dezelfde periode.
    CONSTRAINT uq_ingerichte_talenten_naam_periode
        UNIQUE (talenten_periode_id, naam),

    -- Nodig voor de samengestelde foreign keys in voorkeuren/toewijzingen.
    CONSTRAINT uq_ingerichte_talenten_id_periode
        UNIQUE (ingericht_talent_id, talenten_periode_id)
);


-- =========================================================
-- LEERKRACHTEN PER INGERICHT TALENT
-- =========================================================

CREATE TABLE ingericht_talent_leerkrachten
(
    ingericht_talent_id BIGINT NOT NULL,
    leerkracht_id       BIGINT NOT NULL,

    CONSTRAINT pk_ingericht_talent_leerkrachten
        PRIMARY KEY (ingericht_talent_id, leerkracht_id),

    CONSTRAINT fk_itl_ingericht_talent
        FOREIGN KEY (ingericht_talent_id)
            REFERENCES ingerichte_talenten (ingericht_talent_id)
            ON DELETE CASCADE,

    CONSTRAINT fk_itl_leerkracht
        FOREIGN KEY (leerkracht_id)
            REFERENCES leerkrachten (leerkracht_id)
            ON DELETE RESTRICT
);


-- =========================================================
-- VOORKEUREN
-- =========================================================

CREATE TABLE voorkeuren
(
    voorkeur_id         BIGINT GENERATED ALWAYS AS IDENTITY,
    voorkeur_nummer     SMALLINT NOT NULL,
    leerling_id         BIGINT   NOT NULL,
    talenten_periode_id BIGINT   NOT NULL,
    ingericht_talent_id BIGINT   NOT NULL,

    CONSTRAINT pk_voorkeuren
        PRIMARY KEY (voorkeur_id),

    CONSTRAINT fk_voorkeuren_leerling
        FOREIGN KEY (leerling_id)
            REFERENCES leerlingen (leerling_id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_voorkeuren_ingericht_talent_periode
        FOREIGN KEY (ingericht_talent_id, talenten_periode_id)
            REFERENCES ingerichte_talenten (
                                            ingericht_talent_id,
                                            talenten_periode_id
                )
            ON DELETE RESTRICT,

    CONSTRAINT chk_voorkeuren_nummer
        CHECK (voorkeur_nummer BETWEEN 1 AND 3),

    CONSTRAINT uq_voorkeuren_leerling_periode_nummer
        UNIQUE (
                leerling_id,
                talenten_periode_id,
                voorkeur_nummer
            ),

    CONSTRAINT uq_voorkeuren_leerling_periode_talent
        UNIQUE (
                leerling_id,
                talenten_periode_id,
                ingericht_talent_id
            )
);


-- =========================================================
-- TOEWIJZINGEN
-- =========================================================

CREATE TABLE toewijzingen
(
    toewijzing_id       BIGINT GENERATED ALWAYS AS IDENTITY,
    toewijzings_type    VARCHAR(20) NOT NULL,
    voorkeur_nummer     SMALLINT,
    leerling_id         BIGINT      NOT NULL,
    talenten_periode_id BIGINT      NOT NULL,
    ingericht_talent_id BIGINT      NOT NULL,
    toegewezen_op       TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,
    gewijzigd_op        TIMESTAMP,

    CONSTRAINT pk_toewijzingen
        PRIMARY KEY (toewijzing_id),

    CONSTRAINT fk_toewijzingen_leerling
        FOREIGN KEY (leerling_id)
            REFERENCES leerlingen (leerling_id)
            ON DELETE RESTRICT,

    CONSTRAINT fk_toewijzingen_ingericht_talent_periode
        FOREIGN KEY (ingericht_talent_id, talenten_periode_id)
            REFERENCES ingerichte_talenten (
                                            ingericht_talent_id,
                                            talenten_periode_id
                )
            ON DELETE RESTRICT,

    CONSTRAINT chk_toewijzingen_type
        CHECK (
            toewijzings_type IN (
                                 'AUTOMATISCH',
                                 'MANUEEL'
                )
            ),

    CONSTRAINT chk_toewijzingen_type_voorkeur
        CHECK (
            (
                toewijzings_type = 'AUTOMATISCH'
                    AND voorkeur_nummer BETWEEN 1 AND 3
                )
                OR
            (
                toewijzings_type = 'MANUEEL'
                    AND (
                    voorkeur_nummer IS NULL
                        OR voorkeur_nummer BETWEEN 1 AND 3
                    )
                )
            ),

    CONSTRAINT uq_toewijzingen_leerling_periode
        UNIQUE (leerling_id, talenten_periode_id)
);

-- =========================================================
-- VOORKEUR_IMPORT_PROBLEMEN
-- =========================================================

CREATE TABLE voorkeur_import_problemen
(
    voorkeur_import_probleem_id BIGSERIAL PRIMARY KEY,
    leerling_id                 BIGINT NOT NULL,
    talenten_periode_id         BIGINT NOT NULL,
    reden                       TEXT   NOT NULL,

    CONSTRAINT fk_voorkeur_import_probleem_leerling
        FOREIGN KEY (leerling_id)
            REFERENCES leerlingen (leerling_id),

    CONSTRAINT fk_voorkeur_import_probleem_periode
        FOREIGN KEY (talenten_periode_id)
            REFERENCES talenten_periodes (talenten_periode_id)
);


-- =========================================================
-- INDEXEN
-- =========================================================

CREATE INDEX idx_leerlingen_klas
    ON leerlingen (klas_id);

CREATE INDEX idx_talenten_periodes_schooljaar
    ON talenten_periodes (schooljaar_id);

CREATE INDEX idx_ingerichte_talenten_periode
    ON ingerichte_talenten (talenten_periode_id);

CREATE INDEX idx_ingerichte_talenten_talent
    ON ingerichte_talenten (talent_id);

-- Handig voor voorkeurenformulieren:
-- actieve ingerichte talenten per periode en doelgroep.
CREATE INDEX idx_ingerichte_talenten_periode_doelgroep_actief
    ON ingerichte_talenten (
                            talenten_periode_id,
                            doelgroep,
                            actief
        );

CREATE INDEX idx_itl_leerkracht
    ON ingericht_talent_leerkrachten (leerkracht_id);

CREATE INDEX idx_voorkeuren_periode
    ON voorkeuren (talenten_periode_id);

CREATE INDEX idx_voorkeuren_ingericht_talent
    ON voorkeuren (ingericht_talent_id);

CREATE INDEX idx_toewijzingen_periode
    ON toewijzingen (talenten_periode_id);

CREATE INDEX idx_toewijzingen_ingericht_talent
    ON toewijzingen (ingericht_talent_id);

-- =========================================================
-- INITIËLE DATA
-- =========================================================

INSERT INTO schooljaren (naam, startdatum, einddatum, actief)
VALUES ('2026-2027', '2026-09-01', '2027-06-30', TRUE);

COMMIT;
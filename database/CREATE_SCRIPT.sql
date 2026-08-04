BEGIN;

DROP TABLE IF EXISTS
    ingericht_talent_leerkrachten,
    voorkeuren,
    toewijzingen,
    leerlingen,
    ingerichte_talenten,
    leerkrachten,
    talenten,
    talenten_periodes,
    klassen
    CASCADE;


-- =========================================================
-- 1. KLASSEN
-- =========================================================

CREATE TABLE klassen (
                         klas_id BIGINT GENERATED ALWAYS AS IDENTITY,
                         klas_naam VARCHAR(50) NOT NULL,
                         schooljaar VARCHAR(9) NOT NULL,
                         leerjaar SMALLINT NOT NULL,
                         doelgroep VARCHAR(50) NOT NULL,

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
-- 2. LEERLINGEN
-- =========================================================

CREATE TABLE leerlingen (
                            leerling_id BIGINT GENERATED ALWAYS AS IDENTITY,
                            voornaam VARCHAR(100) NOT NULL,
                            achternaam VARCHAR(100) NOT NULL,
                            klas_id BIGINT NOT NULL,

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
-- 3. LEERKRACHTEN
-- =========================================================

CREATE TABLE leerkrachten (
                              leerkracht_id BIGINT GENERATED ALWAYS AS IDENTITY,
                              voornaam VARCHAR(100) NOT NULL,
                              achternaam VARCHAR(100) NOT NULL,

                              CONSTRAINT pk_leerkrachten
                                  PRIMARY KEY (leerkracht_id),

                              CONSTRAINT chk_leerkrachten_voornaam
                                  CHECK (btrim(voornaam) <> ''),

                              CONSTRAINT chk_leerkrachten_achternaam
                                  CHECK (btrim(achternaam) <> '')
);


-- =========================================================
-- 4. TALENTEN
-- =========================================================

CREATE TABLE talenten (
                          talent_id BIGINT GENERATED ALWAYS AS IDENTITY,
                          naam VARCHAR(100) NOT NULL,
                          beschrijving TEXT NOT NULL,

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
-- 5. TALENTENPERIODES
-- =========================================================

CREATE TABLE talenten_periodes (
                                   talenten_periode_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                   naam VARCHAR(100) NOT NULL,
                                   startdatum DATE NOT NULL,
                                   einddatum DATE NOT NULL,

                                   CONSTRAINT pk_talenten_periodes
                                       PRIMARY KEY (talenten_periode_id),

                                   CONSTRAINT chk_talenten_periodes_naam
                                       CHECK (btrim(naam) <> ''),

                                   CONSTRAINT chk_talenten_periodes_datums
                                       CHECK (einddatum > startdatum),

                                   CONSTRAINT uq_talenten_periodes_datums
                                       UNIQUE (startdatum, einddatum)
);


-- =========================================================
-- 6. INGERICHTE TALENTEN
-- =========================================================

CREATE TABLE ingerichte_talenten (
                                     ingericht_talent_id BIGINT GENERATED ALWAYS AS IDENTITY,
                                     maximum_capaciteit INTEGER NOT NULL,
                                     doelgroep VARCHAR(50) NOT NULL,
                                     talent_id BIGINT NOT NULL,
                                     talenten_periode_id BIGINT NOT NULL,

                                     CONSTRAINT pk_ingerichte_talenten
                                         PRIMARY KEY (ingericht_talent_id),

                                     CONSTRAINT fk_ingerichte_talenten_talent
                                         FOREIGN KEY (talent_id)
                                             REFERENCES talenten (talent_id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT fk_ingerichte_talenten_periode
                                         FOREIGN KEY (talenten_periode_id)
                                             REFERENCES talenten_periodes (talenten_periode_id)
                                             ON DELETE RESTRICT,

                                     CONSTRAINT chk_ingerichte_talenten_capaciteit
                                         CHECK (maximum_capaciteit > 0),

                                     CONSTRAINT chk_ingerichte_talenten_doelgroep
                                         CHECK (
                                             doelgroep IN (
                                                           'OBSERVATIE_OPLEIDINGSFASE_EERSTEGRAAD_AB',
                                                           'KWALIFICATIEFASE_TWEEDEGRAAD_AB'
                                                 )
                                             ),

    /*
     * Hetzelfde talent mag in dezelfde periode eenmaal
     * per doelgroep ingericht worden.
     */
                                     CONSTRAINT uq_ingerichte_talenten_talent_periode_doelgroep
                                         UNIQUE (
                                                 talent_id,
                                                 talenten_periode_id,
                                                 doelgroep
                                             ),

    /*
     * Deze unieke combinatie is nodig voor de samengestelde
     * foreign keys in voorkeuren en toewijzingen.
     */
                                     CONSTRAINT uq_ingerichte_talenten_id_periode
                                         UNIQUE (
                                                 ingericht_talent_id,
                                                 talenten_periode_id
                                             )
);


-- =========================================================
-- 7. KOPPELTABEL INGERICHT TALENT - LEERKRACHT
-- =========================================================

CREATE TABLE ingericht_talent_leerkrachten (
                                               ingericht_talent_id BIGINT NOT NULL,
                                               leerkracht_id BIGINT NOT NULL,

                                               CONSTRAINT pk_ingericht_talent_leerkrachten
                                                   PRIMARY KEY (
                                                                ingericht_talent_id,
                                                                leerkracht_id
                                                       ),

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
-- 8. VOORKEUREN
-- =========================================================

CREATE TABLE voorkeuren (
                            voorkeur_id BIGINT GENERATED ALWAYS AS IDENTITY,
                            voorkeur_nummer SMALLINT NOT NULL,
                            leerling_id BIGINT NOT NULL,
                            talenten_periode_id BIGINT NOT NULL,
                            ingericht_talent_id BIGINT NOT NULL,

                            CONSTRAINT pk_voorkeuren
                                PRIMARY KEY (voorkeur_id),

                            CONSTRAINT fk_voorkeuren_leerling
                                FOREIGN KEY (leerling_id)
                                    REFERENCES leerlingen (leerling_id)
                                    ON DELETE RESTRICT,

    /*
     * De samengestelde foreign key garandeert dat het gekozen
     * ingericht talent werkelijk bij dezelfde periode hoort.
     */
                            CONSTRAINT fk_voorkeuren_ingericht_talent_periode
                                FOREIGN KEY (
                                             ingericht_talent_id,
                                             talenten_periode_id
                                    )
                                    REFERENCES ingerichte_talenten (
                                                                    ingericht_talent_id,
                                                                    talenten_periode_id
                                        )
                                    ON DELETE RESTRICT,

                            CONSTRAINT chk_voorkeuren_nummer
                                CHECK (voorkeur_nummer BETWEEN 1 AND 3),

    /*
     * Een leerling heeft per periode maximaal één eerste,
     * tweede en derde voorkeur.
     */
                            CONSTRAINT uq_voorkeuren_leerling_periode_nummer
                                UNIQUE (
                                        leerling_id,
                                        talenten_periode_id,
                                        voorkeur_nummer
                                    ),

    /*
     * Een leerling mag hetzelfde ingericht talent niet
     * meermaals kiezen binnen dezelfde periode.
     */
                            CONSTRAINT uq_voorkeuren_leerling_periode_talent
                                UNIQUE (
                                        leerling_id,
                                        talenten_periode_id,
                                        ingericht_talent_id
                                    )
);


-- =========================================================
-- 9. TOEWIJZINGEN
-- =========================================================

CREATE TABLE toewijzingen (
                              toewijzing_id BIGINT GENERATED ALWAYS AS IDENTITY,
                              toewijzings_type VARCHAR(20) NOT NULL,
                              voorkeur_nummer SMALLINT,
                              leerling_id BIGINT NOT NULL,
                              talenten_periode_id BIGINT NOT NULL,
                              ingericht_talent_id BIGINT NOT NULL,
                              toegewezen_op TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              gewijzigd_op TIMESTAMP,

                              CONSTRAINT pk_toewijzingen
                                  PRIMARY KEY (toewijzing_id),

                              CONSTRAINT fk_toewijzingen_leerling
                                  FOREIGN KEY (leerling_id)
                                      REFERENCES leerlingen (leerling_id)
                                      ON DELETE RESTRICT,

    /*
     * Hierdoor kan een toewijzing nooit verwijzen naar
     * een ingericht talent uit een andere periode.
     */
                              CONSTRAINT fk_toewijzingen_ingericht_talent_periode
                                  FOREIGN KEY (
                                               ingericht_talent_id,
                                               talenten_periode_id
                                      )
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

    /*
     * Automatische toewijzingen bewaren voorkeur 1, 2 of 3.
     * Manuele toewijzingen hebben geen voorkeurNummer.
     */
                              CONSTRAINT chk_toewijzingen_type_voorkeur
                                  CHECK (
                                      (
                                          toewijzings_type = 'AUTOMATISCH'
                                              AND voorkeur_nummer BETWEEN 1 AND 3
                                          )
                                          OR
                                      (
                                          toewijzings_type = 'MANUEEL'
                                              AND voorkeur_nummer IS NULL
                                          )
                                      ),

    /*
     * Een leerling kan per talentenperiode maximaal
     * één toewijzing hebben.
     */
                              CONSTRAINT uq_toewijzingen_leerling_periode
                                  UNIQUE (
                                          leerling_id,
                                          talenten_periode_id
                                      )
);


-- =========================================================
-- INDEXEN VOOR FOREIGN KEYS EN VEELGEBRUIKTE ZOEKACTIES
-- =========================================================

CREATE INDEX idx_leerlingen_klas
    ON leerlingen (klas_id);

CREATE INDEX idx_ingerichte_talenten_periode
    ON ingerichte_talenten (talenten_periode_id);

CREATE INDEX idx_ingerichte_talenten_talent
    ON ingerichte_talenten (talent_id);

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


COMMIT;
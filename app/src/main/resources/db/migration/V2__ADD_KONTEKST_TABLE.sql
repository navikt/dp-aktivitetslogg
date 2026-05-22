-- Kobling-tabell for kontekster fra aktivitetslogger
CREATE TABLE aktivitetslogg_kontekst
(
    id                BIGSERIAL PRIMARY KEY,
    aktivitetslogg_id BIGINT      NOT NULL REFERENCES aktivitetslogg (id) ON DELETE CASCADE,
    kontekst_type     TEXT        NOT NULL,
    kontekst_verdi    TEXT        NOT NULL
);

CREATE INDEX idx_kontekst_type_verdi ON aktivitetslogg_kontekst (kontekst_type, kontekst_verdi);
CREATE INDEX idx_kontekst_aktivitetslogg_id ON aktivitetslogg_kontekst (aktivitetslogg_id);

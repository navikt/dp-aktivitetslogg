-- Legg til unique constraint for idempotent backfill (ON CONFLICT DO NOTHING)
ALTER TABLE aktivitetslogg_kontekst
    ADD CONSTRAINT aktivitetslogg_kontekst_unique
        UNIQUE (aktivitetslogg_id, kontekst_type, kontekst_verdi);

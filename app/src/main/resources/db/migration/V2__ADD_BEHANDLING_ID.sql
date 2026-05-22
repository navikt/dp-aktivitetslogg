-- Legg til behandling_id-kolonne som populeres ved insert
ALTER TABLE aktivitetslogg ADD COLUMN behandling_id uuid;

-- Backfill eksisterende rader
UPDATE aktivitetslogg SET behandling_id = (
    SELECT (kontekst -> 'kontekstMap' ->> 'behandlingId')::uuid
    FROM jsonb_array_elements(json -> 'aktiviteter') AS aktivitet,
         jsonb_array_elements(aktivitet -> 'kontekster') AS kontekst
    WHERE kontekst ->> 'kontekstType' = 'Behandling'
      AND kontekst -> 'kontekstMap' ? 'behandlingId'
    LIMIT 1
) WHERE behandling_id IS NULL;

CREATE INDEX idx_aktivitetslogg_behandling_id ON aktivitetslogg (behandling_id) WHERE behandling_id IS NOT NULL;

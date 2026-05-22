-- Legg til behandling_id-kolonne som populeres ved insert
ALTER TABLE aktivitetslogg ADD COLUMN behandling_id uuid;

CREATE INDEX idx_aktivitetslogg_behandling_id ON aktivitetslogg (behandling_id) WHERE behandling_id IS NOT NULL;

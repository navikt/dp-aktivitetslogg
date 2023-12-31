CREATE TABLE aktivitetslogg
(
    id         BIGSERIAL PRIMARY KEY,
    melding_id uuid                                                              NOT NULL UNIQUE,
    mottatt    TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    ident      VARCHAR(11)                                                       NOT NULL,
    json       jsonb                                                             NOT NULL
);

CREATE INDEX ON aktivitetslogg (ident);


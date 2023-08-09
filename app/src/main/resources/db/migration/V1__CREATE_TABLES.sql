CREATE TABLE aktivitetslogg
(
    id      uuid PRIMARY KEY,
    mottatt TIMESTAMP WITH TIME ZONE DEFAULT (NOW() AT TIME ZONE 'utc'::TEXT) NOT NULL,
    ident   VARCHAR(11)                                                       NOT NULL,
    json    jsonb                                                             NOT NULL
);

CREATE INDEX ON aktivitetslogg (ident);


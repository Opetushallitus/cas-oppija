CREATE TABLE postgres_jpa_ticket_entity (
    id text CONSTRAINT postgres_jpa_ticket_entity_pkey PRIMARY KEY,
    body text NOT NULL,
    creation_time timestamp NOT NULL,
    parent_id text,
    principal_id text,
    type text NOT NULL,
    attributes json
);
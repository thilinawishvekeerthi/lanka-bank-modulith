-- Owned by the framework, not by any single module, so it lives in the __root folder
-- that Spring Modulith reserves for shared migrations.
create table event_publication (
    id                     uuid                     not null,
    listener_id            varchar(512)             not null,
    event_type             varchar(512)             not null,
    serialized_event       text                     not null,
    publication_date       timestamp with time zone not null,
    completion_date        timestamp with time zone,
    last_resubmission_date timestamp with time zone,
    completion_attempts    integer                  not null default 0,
    status                 varchar(20),
    primary key (id)
);

-- The registry scans for incomplete publications on restart and on resubmission.
create index idx_event_publication_incomplete
    on event_publication (completion_date, publication_date);

-- completion-mode: ARCHIVE moves completed rows here, which keeps the hot table small.
create table event_publication_archive (
    id                     uuid                     not null,
    listener_id            varchar(512)             not null,
    event_type             varchar(512)             not null,
    serialized_event       text                     not null,
    publication_date       timestamp with time zone not null,
    completion_date        timestamp with time zone,
    last_resubmission_date timestamp with time zone,
    completion_attempts    integer                  not null default 0,
    status                 varchar(20),
    primary key (id)
);

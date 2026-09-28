create table payment (
    id           uuid                     not null,
    customer_id  varchar(64)              not null,
    amount       numeric(19, 2)           not null,
    currency     varchar(3)               not null,
    reference    varchar(128)             not null,
    completed_at timestamp with time zone not null,
    primary key (id),
    -- The idempotency guarantee is this index, not the application code
    constraint uk_payment_reference unique (reference)
);

-- No foreign key to customer: crossing a module boundary in the schema is how you
-- lose the ability to split the module out later.
create index idx_payment_customer_id on payment (customer_id);

create table sent_notification (
    id          uuid                     not null,
    payment_id  uuid                     not null,
    customer_id varchar(64)              not null,
    sent_at     timestamp with time zone not null,
    primary key (id)
);

create index idx_sent_notification_payment_id on sent_notification (payment_id);

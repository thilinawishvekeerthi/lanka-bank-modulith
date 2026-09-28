create table customer (
    id          uuid         not null,
    customer_id varchar(64)  not null,
    full_name   varchar(255) not null,
    active      boolean      not null,
    primary key (id),
    constraint uk_customer_customer_id unique (customer_id)
);

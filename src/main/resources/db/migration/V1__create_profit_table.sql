create table if not exists PRODUCT_PROFIT
(
    ID VARCHAR(128) not null,
    PRODUCT_ID VARCHAR(128) not null,
    PRODUCT_NAME VARCHAR(128) not null,
    AMOUNT_SOLD INT not null,
    PROFIT DECIMAL not null,
    TIME_FROM BIGINT not null,
    TIME_TO BIGINT not null,

    primary key (ID)
);
# --- Created by Ebean DDL
# To stop Ebean DDL generation, remove this comment and start using Evolutions

# --- !Ups

create table bank (
  id                            bigint auto_increment not null,
  name                          varchar(255),
  constraint uq_bank_name unique (name),
  constraint pk_bank primary key (id)
);

create table finance (
  id                            bigint auto_increment not null,
  year                          integer,
  month                         integer,
  amount                        bigint,
  bank_id                       bigint,
  constraint pk_finance primary key (id)
);

create table user (
  id                            bigint auto_increment not null,
  username                      varchar(255),
  password                      varchar(255),
  constraint uq_user_username unique (username),
  constraint pk_user primary key (id)
);

create index ix_finance_bank_id on finance (bank_id);
alter table finance add constraint fk_finance_bank_id foreign key (bank_id) references bank (id) on delete restrict on update restrict;


# --- !Downs

alter table finance drop constraint if exists fk_finance_bank_id;
drop index if exists ix_finance_bank_id;

drop table if exists bank;

drop table if exists finance;

drop table if exists user;


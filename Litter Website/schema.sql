--posts table
CREATE TABLE posts (
    id int not null auto_increment,
    posttext varchar(250) not null,
    user text not null,
    posttime timestamp not null default CURRENT_TIMESTAMP,
    edittime timestamp default null,
    likes int not null default 0,
    primary key(id)
);

--logins table
CREATE TABLE logins(
    id int not null auto_increment,
    user varchar(32) not null,
    password text not null,
    primary key(id),
    UNIQUE(user)
);
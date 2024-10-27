CREATE DATABASE NODERED;
USE NODERED;
CREATE TABLE Temperature (
	id int NOT NULL AUTO_INCREMENT,
    value int,
	sensor varchar(255),
	Primary key(id)
);

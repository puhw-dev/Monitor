
/* Drop Tables */

DROP TABLE METRIC;
DROP TABLE SENSOR;
DROP TABLE USERS;




/* Create Tables */

CREATE TABLE USERS
(
	login text NOT NULL UNIQUE,
	password text,
	PRIMARY KEY (login)
);


CREATE TABLE SENSOR
(
	id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	login text NOT NULL,
	hostname text,
	hostip text,
	sensorname text,
	sensortype text,
	rpm integer,
	PRIMARY KEY (id),
	FOREIGN KEY (login)
	REFERENCES USERS (login)
);


CREATE TABLE METRIC
(
	id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	sensorid integer NOT NULL,
	metricname text,
	time text,
	value text,
	PRIMARY KEY (id),
	FOREIGN KEY (sensorid)
	REFERENCES SENSOR (id)
);





/* Drop Tables */

DROP TABLE METRIC;
DROP TABLE SENSOR;
DROP TABLE USERS;




/* Create Tables */

CREATE TABLE USERS
(
	id integer NOT NULL,
	login text NOT NULL UNIQUE,
	password text UNIQUE,
	PRIMARY KEY (id)
);


CREATE TABLE SENSOR
(
	id integer NOT NULL PRIMARY KEY AUTOINCREMENT,
	userid integer NOT NULL,
	hostname text,
	hostip text,
	sensorname text,
	sensortype text,
	rpm integer,
	PRIMARY KEY (id),
	FOREIGN KEY (userid)
	REFERENCES USERS (id)
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




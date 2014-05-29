
/* Drop Tables */

DROP TABLE METRIC;
DROP TABLE SENSOR;
DROP TABLE USERR;




/* Create Tables */

CREATE TABLE USERR
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
	REFERENCES USERR (id)
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




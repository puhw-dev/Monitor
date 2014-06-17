FLAGS=-classpath
JC=javac
PYTHON=/usr/local/advanced_C/python3/bin/python3.4
LIB=".:sqlite-jdbc-3.7.2.jar:json-simple-1.1.1.jar"
.SUFFIXES: .java .class
.java.class:
	$(JC) $(FLAGS) $(LIB) $*.java
CLASSES = \
	 monitor/RestServer.java\
	 monitor/Monitor.java\
	 monitor/DataBase.java

default:
	mkdir -p logs;\
	${MAKE} classes

classes: $(CLASSES:.java=.class)

run_java:
	java $(FLAGS) $(LIB) monitor.Monitor

run_python:
	cd src;\
	$(PYTHON) server.py

run_all: 
	${MAKE} -j2 run_java run_python

clean:
	$(RM) monitor/*.class

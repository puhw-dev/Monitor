FLAGS=-classpath
JC=javac
LIB=".:sqlite-jdbc-3.7.2.jar:json-simple-1.1.1.jar"
.SUFFIXES: .java .class
.java.class:
	$(JC) $(FLAGS) $(LIB) $*.java
CLASSES = \
	 monitor/RestServer.java\
	 monitor/Monitor.java\
	 monitor/DataBase.java

default: classes

classes: $(CLASSES:.java=.class)

run:
	java $(FLAGS) $(LIB) monitor.Monitor

clean:
	$(RM) monitor/*.class
# Makefile for the online bookstore

CLASSPATH=/usr/share/java/xmlrpc-client.jar:/usr/share/java/xmlrpc-server.jar:/usr/share/java/xmlrpc-common.jar:/usr/share/java/ws-commons-util.jar:/usr/share/java/apache-commons-logging.jar:.

default: DatabaseServer.class FrontEndServer.class Client.class

DatabaseServer.class: CatalogServer.java
	javac -cp $(CLASSPATH) CatalogServer.java

FrontEndServer.class: FrontEndServer.java
	javac -cp $(CLASSPATH) FrontEndServer.java

Client.class: Client.java
	javac -cp $(CLASSPATH) Client.java

clean:
	-rm -f *.class

all: clean DatabaseServer.class FrontEndServer.class Client.class

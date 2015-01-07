JFLAGS = -g
JC = javac
.SUFFIXES: .java .class
.java.class:
	$(JC) $(JFLAGS) $*.java

CLASSES = \
	ClientThread.java \
        Client.java \
        ServerThread.java \
        Server.java 

default: classes

classes: $(CLASSES:.java=.class)

clean:
	$(RM) *.class

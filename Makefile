SHELL = /bin/sh

LIBS = lib/jna.jar
# How does one substitute spaces with colons?
# I certainly have no idea...

.PHONY: build run clean

build:	$(LIBS)
	mkdir -p bin
	javac -classpath lib/jna.jar \
		-sourcepath src \
		-d bin/ \
		src/filius/Main.java

run:	build
	java -cp lib/jna.jar:bin:src filius.Main

clean:
	$(RM) -r bin/

lib/jna.jar:
	mkdir -p lib
	wget -O lib/jna.jar 'https://github.com/downloads/twall/jna/jna.jar'


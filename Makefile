SHELL = /bin/sh
.SUFFIXES:
.SUFFIXES: .java .class

empty := 
space := $(empty) $(empty)

SRCS = $(shell find src/ -type f -name '*.java')
OBJS = $(SRCS:.java=.class)

LIBS = lib/jna.jar lib/htmlparser.jar
CLASSPATH = $(subst $(space),:,$(LIBS))

RUNARGS = -v

.PHONY: build run clean
.PRECIOUS: $(LIBS) lib/htmlparser.zip

test:	build
	java -cp $(CLASSPATH):src filius.Main $(RUNARGS)

build:	Makefile.depends $(LIBS) $(OBJS)

.java.class:
	javac -classpath $(CLASSPATH):src $<

clean:
	$(RM) Makefile.depends
	find src/ -type f -name '*.class' -delete

lib/jna.jar:
	mkdir -p lib
	wget -O lib/jna.jar 'https://maven.java.net/content/repositories/releases/net/java/dev/jna/jna/3.5.1/jna-3.5.1.jar'

lib/htmlparser.zip:
	mkdir -p lib
	wget -O lib/htmlparser.zip "http://sourceforge.net/projects/\
	htmlparser/files/htmlparser/1.6/htmlparser1_6_20060610.zip"

lib/htmlparser.jar: lib/htmlparser.zip
	unzip -p lib/htmlparser.zip \
		htmlparser1_6/lib/htmlparser.jar > lib/htmlparser.jar

Makefile.depends:
	for file in $(SRCS); do \
		echo -n "$$file" | sed 's/^src/bin/; s/.java$$/.class: /' ;\
		grep '^import filius' "$$file" | \
			sed 's!^import !src/!; s!\.!/!g; s!;$$!.class!' | \
			tr '\n' ' ' ;\
		echo ; \
	done | grep -v ': $$' > Makefile.depends


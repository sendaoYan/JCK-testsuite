# JCK-testsuite top-level Makefile — OpenJDK-style entry points.
#
#   make native          build every .c/.cpp under JDK-{8,27}/API into build/native/
#   make native-force    force full rebuild of native libraries
#   make test-8 DIR=...  run jtreg against JDK-8 (headless, with -nativepath)
#   make test-27 DIR=... run jtreg against JDK-27 (headless, with -nativepath)
#
# DIR defaults to the full API tree of the given side (use e.g. DIR=JDK-27/API/java/lang).

JTREG     ?= /data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg
JDK8      ?= /data/yansendao/JDK/binary/temurin/jdk8u372-b07
JDK27     ?= /data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1
JTREG_JDK ?= /data/yansendao/JDK/binary/temurin/jdk-17.0.7+7   # JVM that runs jtreg itself

NATIVE_ROOT := build/native

.PHONY: native native-force test-8 test-27

native:
	tools/build-native.sh

native-force:
	tools/build-native.sh -f

test-8: native
	env -u DISPLAY JTREG_JAVA=$(JTREG_JDK) $(JTREG) -jdk:$(JDK8) \
	    -nativepath:$(NATIVE_ROOT)/jdk8 -w build/jtreg/w8 -r build/jtreg/r8 \
	    $(DIR)

test-27: native
	env -u DISPLAY $(JTREG) -jdk:$(JDK27) \
	    -nativepath:$(NATIVE_ROOT)/jdk27 -w build/jtreg/w27 -r build/jtreg/r27 \
	    $(DIR)

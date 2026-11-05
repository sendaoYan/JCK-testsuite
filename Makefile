# JCK-testsuite top-level Makefile — OpenJDK-style entry points.
#
#   make native                     build every .c/.cpp into build/native/<side>-<ARCH>/
#   make native-force               force full rebuild of native libraries
#   make test-8 DIR=...             run jtreg against JDK-8 (headless, with -nativepath)
#   make test-27 DIR=...            run jtreg against JDK-27 (headless, with -nativepath)
#
# Variables (all overridable on the command line):
#   ARCH=riscv64        target CPU arch (default: host). Cross builds auto-pick
#                       <arch>-unknown-linux-gnu-gcc / <arch>-linux-gnu-gcc;
#                       override with CC=/CXX= for other toolchains.
#   SIDES="jdk8"        restrict `native` to one side (default "jdk8 jdk27")
#   JDK8_HOME=...       JDK home whose include/ is used for the jdk8 side
#   JTREG_E="-e:NAME=v" extra jtreg -e: env injections, e.g. for qemu runs:
#                         make test-8 ARCH=riscv64 SIDES=jdk8 \
#                           JDK8_HOME=~/git/riscv-port-jdk8u-ysd/build/.../j2sdk-image \
#                           JTREG_E="-e:QEMU_LD_PREFIX=/data/yansendao/software/riscv64/sysroot"
#   JTREG_ARGS="..."    any other raw jtreg arguments
#
# DIR defaults to the full API tree of the given side (use e.g. DIR=JDK-27/API/java/lang).

JTREG     ?= /data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg
JDK8      ?= /data/yansendao/JDK/binary/temurin/jdk8u372-b07
JDK27     ?= /data/yansendao/JDK/binary/temurin/jdk-27.0.1.0.2+1
JTREG_JDK ?= /data/yansendao/JDK/binary/temurin/jdk-17.0.7+7   # JVM that runs jtreg itself

HOST_ARCH := $(shell uname -m | sed -e 's/^x86_64$$/amd64/' -e 's/^i.86$$/x86/' -e 's/^arm64$$/aarch64/')
ARCH      ?= $(HOST_ARCH)
SIDES     ?= jdk8 jdk27
NATIVE_ROOT := build/native
JTREG_E   ?=

.PHONY: native native-force test-8 test-27

native:
	tools/build-native.sh -a $(ARCH) -s '$(SIDES)'

native-force:
	tools/build-native.sh -f -a $(ARCH) -s '$(SIDES)'

test-8: native
	env -u DISPLAY JTREG_JAVA=$(JTREG_JDK) $(JTREG) -jdk:$(JDK8) \
	    -nativepath:$(NATIVE_ROOT)/jdk8-$(ARCH) $(JTREG_E) \
	    -w build/jtreg/w8 -r build/jtreg/r8 $(JTREG_ARGS) $(DIR)

test-27: native
	env -u DISPLAY $(JTREG) -jdk:$(JDK27) \
	    -nativepath:$(NATIVE_ROOT)/jdk27-$(ARCH) $(JTREG_E) \
	    -w build/jtreg/w27 -r build/jtreg/r27 $(JTREG_ARGS) $(DIR)

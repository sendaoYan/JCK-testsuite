# JCK-testsuite / JDK-8

Conformance tests derived from the Java SE 8 editions of:

- **JLS** — *The Java® Language Specification*, Java SE 8 Edition
- **JVMS** — *The Java® Virtual Machine Specification*, Java SE 8 Edition

The goal is to produce one jtreg test per normative item of each
specification, asserting the exact behaviour (including exact exception
classes, error codes, and expected/negative compile results) that the
specification mandates.

## Layout

Test paths mirror the specification table-of-contents. Spaces in headings
are replaced with `-`; section numbers keep their dots.

- `JLS/Chapter-3-Lexical-Structure/3.10-Literals/3.10.1-Integer-Literals/…`
- `JVMS/Chapter-6-The-Java-Virtual-Machine-Instruction-Set/6.5-Instructions/aaload/…`

Each leaf directory holds one or more `*.java` test files (plus `*.jasm` /
`*.jcod` and `.out` reference files where needed). The `@summary` line cites
the exact specification clause the test maps to.

## Running

```
export JAVA_HOME=~/JDK/binary/temurin/jdk8u372-b07
/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg \
    -jdk:$JAVA_HOME -dir:$PWD <test-or-dir>
```

The helper `~/run-jck8-test.sh` wraps the same invocation.

## Java 8 specifics

- Class file version 52.0 (`jasm` files use `version 52:0`).
- No modules, no `var`, no records/sealed classes, no switch expressions,
  no pattern matching, no text blocks, no `jdk.classfile` API. Chapter 4
  tests parse class files by hand instead.
- The tests target JDK 8 (Temurin 1.8.0_372).

## Conventions

- Every test must contain assertions (or use `@compile/fail` for negative
  compile cases). Expected results map directly to the spec clause named in
  `@summary`.
- Exception assertions check the *exact* exception class (not `Exception` /
  `Throwable`) and, where the spec fixes one, the exact message/error code.
- Tests that open a port use `new ServerSocket(0)` rather than a hard-coded
  port.
- Tests avoid timing- and scheduling-sensitive logic; where unavoidable they
  use generous timeouts and `CountDownLatch`/`Phaser` rather than sleeps.

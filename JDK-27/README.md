# JCK-testsuite / JDK-27

Conformance tests derived from the Java SE 27 editions of:

- **JLS** — *The Java® Language Specification*, Java SE 27 Edition
- **JVMS** — *The Java® Virtual Machine Specification*, Java SE 27 Edition

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

Tests that currently fail because of a suspected JDK implementation bug are
archived under `fails/` together with a Chinese-language README describing the
test intent and the suspected bug. They are otherwise formatted as normal
jtreg tests.

## Running

```
export JAVA_HOME=~/JDK/binary/temurin/jdk-27.0.1.0.2+1
/data/yansendao/JDK/tools/jtreg/jtreg-8.3/bin/jtreg \
    -jdk:$JAVA_HOME -dir:$PWD <test-or-dir>
```

The helper `~/run-jck-test.sh` wraps the same invocation.

## Conventions

- Every test must contain assertions (or use `@compile/fail` for negative
  compile cases). Expected results map directly to the spec clause named in
  `@summary`.
- Exception assertions check the *exact* exception class (not `Exception` /
  `Throwable`) and, where the spec fixes one, the exact message/error code.
- Tests that open a port use `new ServerSocket(0)` (or the jtreg
  `Utils.getFreePort()`) rather than a hard-coded port.
- Tests avoid timing- and scheduling-sensitive logic; where unavoidable they
  use generous timeouts and `CountDownLatch`/`Phaser` rather than sleeps.

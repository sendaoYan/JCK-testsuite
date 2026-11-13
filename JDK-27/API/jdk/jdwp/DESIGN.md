# JDWP / JDI contract — design (JDK-27, mirrored in JDK-8)

This directory adds behavioural coverage for the **Java Debug Wire Protocol
(JDWP)** debugging path using the JDK's own high-level client, **JDI
(`com.sun.jdi`, module `jdk.jdi`)**.  It lives under `API/jdk/` because JDWP is
a wire protocol rather than a single exported Java package, and the JDK-8 tree
mirrors it byte-for-byte.

## 1. Spec / functionality under test

* **attach connection**: a debuggee started with
  `-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=localhost:0`
  reports its ephemeral listen port, and a JDI client connects with
  `com.sun.jdi.connect.AttachingConnector` `com.sun.jdi.SocketAttach`.
* **VM start event**: after attach, the initial `EventSet` contains a
  `VMStartEvent` (delivered to an attaching client while the target is
  `suspend=y`).
* **class preparation**: a `ClassPrepareRequest` filtered to the debuggee class
  yields the prepared `ReferenceType` on resume.
* **breakpoint hit**: a `BreakpointRequest` on the first line location of a
  known method suspends the target; the suspending thread is the `main` thread.
* **stack-frame reading**: `ThreadReference.frames()` returns the call stack
  (`compute` above `main`), and `StackFrame.visibleVariables()` +
  `StackFrame.getValue(LocalVariable)` read the method's parameter locals.

## 2. Test shape

The test embeds a small `Debuggee` class (`compute(int a, int b)` returns
`a + b`), compiles it with the JDK under test's own `javac` **with `-g`** (line
and local-variable tables are required for `visibleVariables()`), then:

1. launches the debuggee with the raw JDWP agent options above and reads the
   "Listening for transport dt_socket at address: <port>" line from its output;
2. attaches via `SocketAttach` (hostname `localhost`, port from step 1);
3. drains the initial `EventSet` and asserts a `VMStartEvent` is present;
4. requests `ClassPrepare` for `Debuggee`, resumes, and waits for it;
5. installs a breakpoint at the first line location of `compute`, resumes, and
   waits for the `BreakpointEvent`;
6. reads the suspended thread's frames and parameter locals `a`/`b`.

All assertions are on the **client side** (the test); the debuggee is a passive
target.  Cleanup (resume/dispose/wait/destroy) is wrapped so a
`VMDisconnectedException` racing the target's normal exit cannot fail the test.

### JDK-8 vs JDK-27 differences handled here

* **JDI location**: on JDK 27 `com.sun.jdi` is in the `jdk.jdi` root module
  (available by default); on JDK 8 it is in `$JAVA_HOME/lib/tools.jar`.  jtreg
  automatically places `tools.jar` on the compile and run classpath for JDK 8
  (verified with jtreg 8.3), so the `@run` header is **identical in both
  trees** — no explicit `-cp` or `--add-modules` is needed.
* **Child JVM binary**: `System.getProperty("java.home") + "/bin/java"` is
  `jre/bin/java` on JDK 8 and `<root>/bin/java` on JDK 27, correct on both; the
  debuggee is compiled by `jdkHome()/bin/javac` (the `jdkHome()` helper lifts
  JDK 8's `jre` to the real JDK root).
* **No `--enable-native-access`**: the test is pure Java (JDI over a socket);
  the debuggee's `-agentlib:jdwp` is a VM option, not gated by native access.
* The target's `vm.version()` string differs by release; the test does **not**
  assert it, keeping both trees' assertion counts identical.

### Capability probe / skipping

If JDI is unavailable (e.g. `Bootstrap.virtualMachineManager()` cannot be
loaded because `com.sun.jdi` is absent), the test throws
`jtreg.SkippedException` rather than silently passing.  There is no C compiler
dependency here.

## 3. Files

| file | covers |
|---|---|
| `JdwpContractTest.java` | embedded debuggee, JDWP launch + JDI attach/event/breakpoint/frame flow, 15 assertions |

Reference: JPDA / JDWP / JDI specifications and `com.sun.jdi` javadoc.
Behaviours were **empirically verified on the two JDKs under test** (temurin
8u372-b07 and 27.0.1) before being asserted; the tests are self-written, no
external test code was copied.

## 4. Error-prone details (for maintainers)

* The port line is
  `Listening for transport dt_socket at address: <port> ` (trailing space) on
  stdout; the test parses from the colon and strips the trailing whitespace.
* `Debuggee` **must** be compiled with `-g`, otherwise `visibleVariables()`
  throws `AbsentInformationException`.
* At the first line location of `compute` (the `int sum = a + b;` line), the
  local `sum` is not yet assigned/visible — only the parameter locals `a`/`b`
  are readable, so the test asserts those (not `sum`).
* `vm.dispose()` can throw `VMDisconnectedException` when the target already
  exited after `resume()`; cleanup must swallow it.

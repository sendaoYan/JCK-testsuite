# JVMTI native agent loading — design (JDK-27, mirrored in JDK-8)

This directory adds behavioural coverage for the **JVM Tool Interface (JVMTI)**
agent lifecycle and its core, phase-independent query functions, against a
real native agent compiled at run time.  JVMTI has no exported Java package
(`jvmti.h` is a native header under `<jdk>/include`), so it lives under the
`API/jdk/` tooling subtree rather than under `com/sun/...`; the JDK-8 tree
mirrors this directory byte-for-byte.

## 1. Spec / functionality under test

* JVMTI agent load contract: a shared library exporting `Agent_OnLoad` /
  `Agent_OnUnload` is discovered through the `-agentpath:<path>=<options>` VM
  option; `Agent_OnLoad` returns `JNI_OK` (0) and `Agent_OnUnload` runs at VM
  shutdown (JVMTI spec, "Agent Startup" / "Agent Shutdown").
* `GetVersionNumber`: returns a non-zero `jint` whose top byte is the JVMTI
  kind marker `0x30` (the version number is encoded `0x30MMmmrr`).
* `GetClassSignature` / `GetClassStatus`: at a `ClassPrepare` event for a known
  class, the signature is the JNI descriptor `L<name>;` and the status carries
  `JVMTI_CLASS_STATUS_PREPARED` (0x2) but not yet `INITIALIZED` (0x4).
* `GetThreadInfo` on the current thread (via `GetCurrentThread`) in the live
  phase: the main thread is named `"main"` with `NORM_PRIORITY` (5).

## 2. How the test builds and loads the agent

The agent's C source is **embedded as a Java string constant** and also kept as
a standalone `jvmtiagent.c` (canonical copy, built by `make native` into
`build/native/<side>-<arch>/libjvmtiagent.so` for cross-arch runs).  The two
must stay identical; the run-time path is:

```
gcc -shared -fPIC -I<jdkHome>/include -I<jdkHome>/include/linux \
    -o libjvmtiagent.so jvmtiagent.c
```

No `-ljvm` / extra link library is needed: `Agent_OnLoad`/`Agent_OnUnload` are
exported symbols and the JVMTI function table is a `jvmtiEnv*` of function
pointers resolved by the VM at load time.

The Java test compiles a tiny `AgentProbe` class (embedded source) with the
JDK under test's own `javac`, then launches a **child JVM** with
`-agentpath:<so>=<outfile> -cp <dir> AgentProbe`.  The agent writes its
observations to `<outfile>`; the test parses the key=value lines and asserts on
them.  Assertions therefore live on the **Java side** (the test), while the
agent only records raw observations — the agent contains no assertions of its
own, which keeps the native code tiny and the failure messages precise.

### JDK-8 vs JDK-27 differences handled here

* `java.home` on JDK 8 points at `.../jdk8u372-b07/jre` while `include/` sits in
  the parent; `jdkHome()` (`p.endsWith("jre") ? p.getParent() : p`) locates the
  real JDK root on both sides, so the same agent source builds against the JDK
  under test's own `jvmti.h`.
* The child JVM binary is `System.getProperty("java.home") + "/bin/java"`, which
  is `jre/bin/java` on JDK 8 and `<root>/bin/java` on JDK 27 — correct on both.
* **No `--enable-native-access` flag is needed on either side**: the *parent*
  test JVM only spawns a subprocess (no `System.load`/FFM), and the *child*
  loads the agent through the `-agentpath` VM option, which is not gated by the
  native-access warning.  The header is therefore identical in both trees.
* `GetVersionNumber` differs by release (JDK 8 reports JVMTI 1.2.3 =
  `0x30010203`; JDK 27 reports its release-versioned `0x301B0000`).  The test
  asserts only the stable contract — non-zero and the `0x30` kind byte — not the
  exact release, so both trees assert identically.

### Capability probes / skipping

`gccAvailable()` runs `gcc --version`; if the host has no C compiler and no
`test.nativepath` prebuilt library is available, the test throws
`jtreg.SkippedException` rather than silently passing.  With
`-nativepath` set (the `make test-8`/`test-27` flow) the test loads the prebuilt
`libjvmtiagent.so` and never invokes gcc.

## 3. Files

| file | covers |
|---|---|
| `JvmtiAgentTest.java` | embedded agent + probe source, gcc/nativepath build, child launch, 12 assertions |
| `jvmtiagent.c` | canonical agent source (identical to the embedded copy; `make native` input) |

Reference: JVMTI specification (JDK docs "JVM Tool Interface"),
`<jdk>/include/jvmti.h`.  Behaviours were **empirically verified on the two
JDKs under test** (temurin 8u372-b07 and 27.0.1) before being asserted; the
tests are self-written, no external test code was copied.

## 4. Error-prone details (for maintainers)

* `GetCurrentThread` / `GetThreadInfo` are **not** callable in the `Agent_OnLoad`
  (on-load) phase — `GetCurrentThread` returns `JVMTI_ERROR_WRONG_PHASE`.  The
  thread probe runs from the `ClassPrepare` callback (live phase) instead.
* The `options` string of `-agentpath:<so>=<options>` is exactly the argument to
  `Agent_OnLoad`; pass the absolute output-file path there, not through the
  environment.
* `Agent_OnUnload` may run after the parent has closed some file descriptors;
  the agent only appends to a plain file it opened itself, which remains valid.

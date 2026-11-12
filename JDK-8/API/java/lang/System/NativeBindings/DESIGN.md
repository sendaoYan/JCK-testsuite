# Native / JNI loading — design (JDK-8, mirrored in JDK-27)

This directory (plus the two sibling lifecycle tests) upgrades JNI coverage
from pure-Java negative paths to behavioural coverage against **real native
libraries compiled at run time**.  Nothing here touches the pre-existing
`SystemTest.java` / `RuntimeTest.java`.

## 1. API / spec functionality under test

* `System.load(String)`, `System.loadLibrary(String)`,
  `System.mapLibraryName(String)` and their `Runtime` counterparts
  (Java SE javadoc; JVMS 5.6 "Binding Native Method Implementations").
* The JNIEnv function table mainline as specified by the JNI specification
  (primitives, modified UTF-8 strings, arrays, fields, methods, exceptions,
  references, registration management, VM access).
* JVMS-level structure rules for native methods: ACC_NATIVE (0x0100) with and
  without a Code attribute, the abstract+native flag conflict (§4.6), native
  method stacks (§2.5.6), and lazy binding at first invocation (§5.6).

## 2. How the tests build a library (C→so→load pipeline)

The C source of every library is **embedded as a Java string constant** and
written to the jtreg scratch directory (`System.getProperty("user.dir")`) at
run time; the repository never stores `.c` or `.so` files.  The library is
compiled with

```
gcc -shared -fPIC -I<jdkHome>/include -I<jdkHome>/include/linux -o lib<jck>.so <src>.c
```

and loaded with `System.load(<absolute path>)`.

**Locating the JDK include directory — both layouts.**  On JDK 8
`java.home` points at `.../jdk8u372-b07/jre`, while `include/` sits in the
parent directory; on JDK 9+ `java.home` is the JDK root.  Every test uses:

```java
static Path jdkHome() {
    Path p = Paths.get(System.getProperty("java.home"));
    return p.endsWith("jre") ? p.getParent() : p;
}
```

so the same source builds against the JDK under test's own `jni.h` on both
sides.  gcc is invoked through ProcessBuilder with a bounded 60 s wait and
`destroyForcibly()` on timeout; gcc output is captured and reported on
failure.  C code is C89/C99-portable (declarations at block start, `/* */`
comments, no compiler-specific constructs).

**JDK-27 header form (JEP 472).**  On JDK 27 native access from the unnamed
module only *warns* by default; the warning is silenced by passing the VM
flag to jtreg inline, verified with jtreg 8.3:

```
@run main/othervm --enable-native-access=ALL-UNNAMED -Djava.library.path=. Test full
```

JDK 8 rejects that flag, so its header is plain
`@run main/othervm -Djava.library.path=. Test full`.  The `System`/`Runtime`
tests additionally run a second action without the flag (`bare`) which
documents that native loading still works when the warning is *not* silenced.

## 3. Test files and reference sources

| file | covers |
|---|---|
| `JniSemanticsTest.java` (this dir) | JNIEnv mainline semantics, ~45 JNIEnv functions, 97 assertions |
| `JniMonitorAndBufferTest.java` (this dir) | JNI monitor ops + NIO direct buffers + object arrays, 41 assertions |
| `../SystemLoadNativeTest.java` | System.load/loadLibrary lifecycle: success, idempotency, non-ELF rejection, missing-symbol ULE with signature, name mapping, null/empty/relative arguments |
| `../../Runtime/RuntimeLoadNativeTest.java` | Runtime.load/loadLibrary same semantics + shared registry with System.load |

References: JNI specification (JDK docs "Java Native Interface Specification"),
`java.lang.System` / `java.lang.Runtime` javadoc, JVMS §2.5.6, §4.6, §4.7.3,
§5.6.  Behaviours below were **empirically verified on the two JDKs under
test** (temurin 8u372-b07 and 27.0.1) before being asserted; the tests are
self-written, no external test code was copied.

## 4. Test case design and verified behaviours

### JniSemanticsTest — JNIEnv functions exercised

* primitives: `jint/jlong/jdouble/jboolean/jbyte/jshort/jchar/jfloat`
  parameters and returns, including MIN/MAX, `Long.MIN_VALUE` identity,
  IEEE-754 double overflow to +Infinity, NaN propagation, `primBool`
  negation.
* strings: `GetStringUTFLength`, `GetStringLength`, `GetStringUTFChars`,
  `ReleaseStringUTFChars`, `NewStringUTF`.  Modified-UTF-8 semantics: CJK
  BMP chars encode as 3 bytes each; a supplementary character ("😀")
  encodes as its **surrogate pair, 3+3 bytes**, not a 4-byte standard UTF-8
  sequence; embedded `\u0000` survives the round trip (0xC0 0x80).
* arrays: `GetArrayLength`, `NewIntArray`, `GetIntArrayRegion`,
  `SetIntArrayRegion` (in-range, `start==length` no-op, and out-of-bounds
  start/length which the JVM turns into `ArrayIndexOutOfBoundsException`
  propagating out of the native frame), `GetIntArrayElements` +
  `ReleaseIntArrayElements(mode 0)` copy-back.
* fields: `GetFieldID`/`Get{Int,Long,Boolean,Double,Object}Field` and the
  `Set…` counterparts, static `GetStaticFieldID` +
  `Get{StaticInt,StaticObject}Field`/`SetStaticIntField`; a missing field
  makes `GetFieldID` return NULL with a pending error observed through
  `ExceptionCheck` and removed with `ExceptionClear`.
* methods: `GetMethodID` + `CallIntMethod`/`CallObjectMethod` (virtual),
  `GetStaticMethodID` + `CallStaticIntMethod`, `CallNonvirtualIntMethod`
  (selects the declaring-class implementation on a subclass instance),
  `GetSuperclass` (direct superclass, `java.lang.Object` → NULL, interface →
  NULL), `IsAssignableFrom` (note: JNI argument order is reversed w.r.t.
  `Class.isAssignableFrom` — `IsAssignableFrom(c1, c2)` asks whether c1
  objects fit into c2).
* exceptions: `FindClass` + `ThrowNew` delivering the **exact** exception
  class and message to the Java caller; `ExceptionCheck`/`ExceptionClear`
  state machine (checked true while pending, false after clear, allocation
  possible again).
* references: `NewGlobalRef`/`DeleteGlobalRef` (identity survives across
  native calls), `NewWeakGlobalRef` (same object while reachable; cleared
  after the strong reference is dropped — bounded 10× `System.gc()` retry),
  `PushLocalFrame`/`PopLocalFrame` including the pass-through result.
* registration & VM: `JNI_OnLoad` invoked by `System.load`, returning
  `JNI_VERSION_1_8` (0x10008), cached in C and read back;
  `RegisterNatives` in `JNI_OnLoad` binding a method without `Java_` naming;
  `UnregisterNatives` invalidating it (subsequent call →
  `UnsatisfiedLinkError`, while `Java_`-named natives re-bind lazily);
  `GetVersion`; `GetJavaVM` returning 0 with a non-null `JavaVM*`.

### JniMonitorAndBufferTest — monitor / NIO direct-buffer / object-array functions

* monitors: `MonitorEnter`/`MonitorExit` act on the **same monitor as Java
  `synchronized`** — re-entrant inside a `synchronized` block, and `MonitorExit`
  only decrements the recursion count (Java's hold survives a native enter/exit);
  enter-twice/exit-once leaves `Thread.holdsLock` true, a final exit releases; a
  native thread holding the monitor excludes a second thread until it exits;
  `MonitorEnter(NULL)` throws NPE (rc=-1, pending exception).
* direct buffers: `GetDirectBufferCapacity` returns the buffer `capacity()` in
  **elements** for typed view buffers — a 32-byte direct buffer viewed as shorts
  reports 16, not 32 (verified identical on JDK 8 and 27), so the JNI spec's
  "capacity in bytes" is only literally true for `ByteBuffer`; heap and
  non-buffer objects report -1 with a NULL address; `GetDirectBufferCapacity(NULL)`
  returns -1 with **no** exception (contrast `MonitorEnter(NULL)`);
  `GetDirectBufferAddress(NULL)` is **not** probed because it dereferences the
  handle and crashes the VM.  `NewDirectByteBuffer` wraps malloc'd memory into a
  direct buffer with a known byte pattern and round-trips through
  `GetDirectBufferCapacity`/`GetDirectBufferAddress`.
* object arrays: `GetArrayLength`, `GetObjectArrayElement`/`SetObjectArrayElement`
  (read/write, plus out-of-bounds `GetObjectArrayElement(-1)` leaving a pending
  `ArrayIndexOutOfBoundsException`), `NewObjectArray` (length + fill identity),
  `IsSameObject` (reference identity, not `equals`).

### Load lifecycle (verified behaviours)

| scenario | JDK 8 | JDK 27 |
|---|---|---|
| `System.load` absolute path, then call | binds | binds (with flag; warning-only without) |
| second `load` of the same path, same loader | no-op | no-op |
| `loadLibrary("name")` after `load` of the same file | recognised, no-op | recognised, no-op |
| non-ELF file | ULE, message contains file name ("file too short") | same |
| library without the symbol | loads; call → ULE `Cls.m()V` (descriptor form) | loads; call → ULE `'void Cls.m()'` (type-name form) |
| `loadLibrary` unknown name | ULE `no <name> in java.library.path` | ULE, message also lists the search path |
| `load(null)` / `loadLibrary(null)` | NPE | NPE |
| `load("")` / relative path | ULE "Expecting an absolute path of the library:" | ULE "Expecting an absolute path of the library:" (empty string does **not** throw NPE) |
| two defining loaders, same path | second → ULE `Native Library <path> already loaded in another classloader` | same message |
| `JNIEnv->GetVersion()` | 0x00010008 | 0x00180000 (modern scheme) |

JVMS-side companions (in `JVMS/…`): `4.6-Methods/NativeMethodWithCode`
(native + Code → ClassFormatError), `4.6-Methods/AbstractNativeMethod`
(abstract + native → ClassFormatError), `4.6-Methods/NativeMethodNoCode`
(ACC_NATIVE positive: loads, `Modifier.isNative`, unbound call → ULE),
`2.5-Run-Time-Data-Areas/NativeMethodStackTest` (§2.5.6 native stacks,
mutual native/Java recursion, catchable StackOverflowError, VM usable
afterwards), `5.6-Binding-Native-Method-Implementations/BindingNativeMethodTest`
(§5.6 ULE-before/bound-after incl. a jasm-generated class),
`5.6-…/NativeLibraryNamespaceTest` (library name mapping lib<name>.so,
per-defining-loader namespaces).  Note: the task brief referenced "§5.3.4"
for native library loading; in JVMS, §5.3.4 is "Loading Constraints" and the
native material lives in §5.6, so the tests are anchored there.

### Error-prone details (documented for future maintainers)

* `SetIntArrayRegion` reads `len` elements from the source buffer — always
  pass a real array, not a pointer to a single value.
* JDK 8 `java.home` ends in `jre`; include/ is one level up.
* `loadLibrary("")`/`load("")` throw **UnsatisfiedLinkError**, not NPE; only
  `null` gives NPE.
* A class loaded by a null-parent custom class loader cannot call
  `System.load` (the VM resolves the caller chain and fails with
  NoClassDefFoundError); the namespace test therefore compiles a tiny helper
  into its own off-classpath directory and loads it with parented
  URLClassLoaders.
* jasm class versions: JDK 8 files use `version 52:0`, JDK 27 files
  `version 71:0`; a bodiless `public static native Method m:"(I)I";`
  compiles to a method with ACC_NATIVE and no Code attribute.

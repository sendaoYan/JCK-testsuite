# JNI function matrix — design (JDK-27)

This directory fills part of the JNI function-matrix gap (76 official function
directories listed in `docs/jvmti-jdwp-jni-matrix.md`) with five behavioural
tests against a **real native library compiled at run time**.  It lives under
`API/jdk/jni/` for the same reason as `API/jdk/jvmti/` and `API/jdk/jdwp/`: JNI
is a native interface (`jni.h`) rather than a single exported Java package.

The earlier `API/java/lang/System/NativeBindings/` tests cover the JNIEnv
mainline (primitives, modified-UTF-8 lengths, int-array regions/elements,
fields, virtual/static/non-virtual calls, references, registration) and
monitors / NIO direct buffers / object arrays.  This directory adds the
**representative subset of the 76 functions that were still missing**, one test
file per category.

## 1. Functions under test

| test | JNI functions |
|---|---|
| `JniLookupTest.java` | `FindClass` (binary name, array descriptor, missing -> NULL + pending exception), `GetMethodID` (missing -> NULL + pending exception), `IsInstanceOf`, `FromReflectedMethod`/`ToReflectedMethod`, `FromReflectedField`/`ToReflectedField` |
| `JniStringTest.java` | `NewString` (from UTF-16), `GetStringChars`/`ReleaseStringChars`, `GetStringRegion`, `GetStringUTFRegion` (modified UTF-8 bytes), `GetStringCritical`/`ReleaseStringCritical` |
| `JniArrayTest.java` | `NewLongArray`/`NewDoubleArray`/`NewByteArray`, `Get/SetLongArrayRegion`, `Get/SetDoubleArrayRegion`, `GetLongArrayElements`/`ReleaseLongArrayElements`, `GetByteArrayElements`/`ReleaseByteArrayElements`, `GetPrimitiveArrayCritical`/`ReleasePrimitiveArrayCritical`, `GetArrayLength` |
| `JniObjectTest.java` | `NewObject` (constructor), `AllocObject` (uninitialized), `NewLocalRef`/`DeleteLocalRef`, `EnsureLocalCapacity`, `GetObjectRefType` (local/global/weak-global/invalid) |
| `JniExceptionTest.java` | `ExceptionOccurred`, `Throw`, `ThrowNew`, `ExceptionDescribe` (+ `ExceptionCheck`/`ExceptionClear` state machine) |

## 2. How the tests build a library (C -> so -> load)

Each test embeds the C source as a Java string constant **and** keeps a
canonical `*.c` file next to it.  The two are byte-identical (the embedded copy
is generated from the canonical file).  The run-time path is:

```
gcc -shared -fPIC -I<jdkHome>/include -I<jdkHome>/include/linux -o lib<name>.so <name>.c
```

with `jdkHome()` lifting JDK 8's `jre` to the real JDK root (the JDK-27
`java.home` is already the root).  `System.load(absolute path)` binds the
library; JDK 27 native access from the unnamed module only warns, silenced by
the inline flag:

```
@run main/othervm --enable-native-access=ALL-UNNAMED <Test>
```

### Cross-arch support

`make native` (tools/build-native.sh) discovers every `*.c` under
`JDK-27/API` and builds `build/native/jdk27-<arch>/lib<name>.so`.  When jtreg
is run with `-nativepath:build/native/jdk27-<arch>`, each test loads the
prebuilt library directly and never invokes gcc.  Without `-nativepath`, the
test falls back to self-compiling the embedded C.  Both paths are verified on
amd64 (5/5 tests pass each way).

## 3. Files

| file | covers |
|---|---|
| `JniLookupTest.java` + `jnilookup.c` | class lookup & reflection, 19 assertions |
| `JniStringTest.java` + `jnistring.c` | strings, 16 assertions |
| `JniArrayTest.java` + `jniarray.c` | primitive arrays, 15 assertions |
| `JniObjectTest.java` + `jniobject.c` | objects & references, 13 assertions |
| `JniExceptionTest.java` + `jniexception.c` | exceptions, 6 assertions |

Reference: JNI specification (JDK docs "Java Native Interface Specification"),
`<jdk>/include/jni.h`.  Behaviours were **empirically verified on temurin
27.0.1 (amd64)** before being asserted; the tests are self-written, no external
test code was copied.

## 4. Verified behaviours / error-prone details

* **FindClass takes slash form.** `FindClass("java.lang.String")` fails with
  `NoClassDefFoundError: java.lang.String` (the dot is treated as part of the
  name); `FindClass("java/lang/String")` succeeds.  Array descriptors such as
  `"[I"` and `"[[Ljava/lang/String;"` also resolve.
* **IsInstanceOf(NULL, clazz) is JNI_TRUE** — a null reference can be cast to
  any class.  This is the opposite of the naive expectation and is asserted as
  such.
* **ExceptionDescribe clears the pending exception** on the reference
  implementation (it prints the throwable and backtrace to stderr and clears the
  pending status as a side effect); the test asserts pending-before + cleared-
  after rather than "does not clear".
* **GetStringUTFRegion emits modified UTF-8**, not standard UTF-8: a BMP CJK
  char is 3 bytes, a supplementary character is encoded as its surrogate pair
  in 3+3 bytes (`U+1F600` -> `ED A0 BD ED B8 80`), not a 4-byte standard
  sequence.  The buffer is sized `GetStringUTFLength + 1` so a possible null
  terminator cannot overflow; only the reported byte count is handed back.
* **AllocObject** runs no constructor, so a freshly allocated object exposes
  default field values (`int` -> 0); **NewObject** does invoke the constructor.
* **GetPrimitiveArrayCritical / GetStringCritical** must not make JNI calls
  inside the critical region; `GetArrayLength`/`GetStringLength` are read
  beforehand.
* `GetObjectRefType` returns `JNIInvalidRefType=0` for NULL,
  `JNILocalRefType=1`, `JNIGlobalRefType=2`, `JNIWeakGlobalRefType=3`.

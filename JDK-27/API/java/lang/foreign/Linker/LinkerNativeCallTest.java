/*
 * @test
 * @summary java.lang.foreign.Linker real downcall behavior: libc functions invoked
 *          through nativeLinker().downcallHandle() with exact return-value
 *          assertions, carrier matrix via a self-compiled C library, variadic
 *          linkage, errno capture and negative cases.
 * @run main/othervm --enable-native-access=ALL-UNNAMED LinkerNativeCallTest
 */
import java.lang.foreign.*;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodType;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class LinkerNativeCallTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean gccAvailable() {
        try {
            Process p = new ProcessBuilder("gcc", "--version").start();
            try { p.getInputStream().close(); } catch (Exception ignore) { }
            try { p.getOutputStream().close(); } catch (Exception ignore) { }
            try { p.getErrorStream().close(); } catch (Exception ignore) { }
            return p.waitFor() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    // Minimal self-written C library (no JNI headers needed): one function per
    // carrier plus helpers used by the Linker behavior tests. Written to the
    // jtreg scratch dir and compiled at runtime; the .c/.so never enter the repo.
    static final String C_SRC = """
            #include <stdint.h>
            #include <errno.h>

            // Note: `char` is unsigned by default on aarch64 (signed on x86-64),
            // so the byte carrier is declared `signed char` to make the sum
            // platform-independent (an unsigned char would read -1 as 255).
            double jck_carrier_sum(signed char a, signed char b, short c, int d, long e, float f,
                                   double g, int8_t h, int16_t i, int32_t j, int64_t k,
                                   _Bool l, uint16_t m) {
                double s = 0.0;
                s += (double)a; s += (double)b; s += (double)c; s += (double)d; s += (double)e;
                s += (double)f; s += g;
                s += (double)h; s += (double)i; s += (double)j; s += (double)k;
                s += l ? 1.5 : -1.5;
                s += (double)m;
                return s;
            }
            int jck_is_null(const void *p) { return p == 0; }
            void jck_set_errno(int v) { errno = v; }
            long jck_read_long(const long *p) { return *p; }
            void jck_write_long(long *p, long v) { *p = v; }
            int jck_sum2(int a, int b) { return a + 2 * b; }
            """;

    // Platform-typical libc names for the libraryLookup fallback; the primary
    // resolution path is defaultLookup(), which the Linker javadoc documents as
    // exposing the C libraries loaded with the runtime (strlen et al.).
    static final String[] LIBC_NAMES = {"libc.so.6", "libc.so", "libc.dylib"};

    static Path compileLib() throws Exception {
        Path dir = Paths.get(System.getProperty("user.dir"));
        Path src = dir.resolve("LinkerNativeCallTest_jckffm.c");
        Path so = dir.resolve("libLinkerNativeCallTest_jckffm.so");
        Files.writeString(src, C_SRC);
        ProcessBuilder pb = new ProcessBuilder("gcc", "-shared", "-fPIC",
                "-o", so.toString(), src.toString());
        pb.inheritIO();
        Process p = pb.start();
        if (!p.waitFor(60, TimeUnit.SECONDS)) {
            p.destroyForcibly();
            throw new IllegalStateException("gcc did not finish within 60s");
        }
        if (p.exitValue() != 0)
            throw new IllegalStateException("gcc exited " + p.exitValue());
        return so;
    }

    static Linker LINKER;
    static SymbolLookup OWN;      // symbols from the self-compiled library

    static MemorySegment libc(Arena arena, String name) {
        Optional<MemorySegment> m = LINKER.defaultLookup().find(name);
        if (m.isPresent()) return m.get();
        for (String lib : LIBC_NAMES) {
            try {
                return SymbolLookup.libraryLookup(lib, arena).findOrThrow(name);
            } catch (IllegalArgumentException | NoSuchElementException notHere) {
                // try next platform name
            }
        }
        throw new AssertionError("cannot resolve libc symbol " + name
                + " via defaultLookup or libraryLookup fallback");
    }

    // ---- strlen: exact size_t return ----
    static void testStrlen() throws Throwable {
        MethodHandle strlen = LINKER.downcallHandle(
                FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            check((long) strlen.invoke(libc(a, "strlen"), a.allocateFrom("abc")) == 3L,
                    "strlen(\"abc\") == 3");
            check((long) strlen.invoke(libc(a, "strlen"), a.allocateFrom("")) == 0L,
                    "strlen(\"\") == 0");
            check((long) strlen.invoke(libc(a, "strlen"), a.allocateFrom("Hello, FFM!")) == 11L,
                    "strlen(\"Hello, FFM!\") == 11");
            // C must see the segment's dynamic base: slicing shifts the pointer
            MemorySegment s = a.allocateFrom("alpha");
            check((long) strlen.invoke(libc(a, "strlen"), s.asSlice(2)) == 3L,
                    "strlen sees sliced segment base (\"pha\" from \"alpha\") == 3");
        }
    }

    // ---- strcmp: only the sign of the result is specified ----
    static void testStrcmpSign() throws Throwable {
        MethodHandle strcmp = LINKER.downcallHandle(
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            int lt = (int) strcmp.invoke(libc(a, "strcmp"), a.allocateFrom("abc"), a.allocateFrom("abd"));
            check(lt < 0, "strcmp(\"abc\",\"abd\") < 0");
            int gt = (int) strcmp.invoke(libc(a, "strcmp"), a.allocateFrom("abd"), a.allocateFrom("abc"));
            check(gt > 0, "strcmp(\"abd\",\"abc\") > 0");
            int eq = (int) strcmp.invoke(libc(a, "strcmp"), a.allocateFrom("same"), a.allocateFrom("same"));
            check(eq == 0, "strcmp(\"same\",\"same\") == 0");
            int pfx = (int) strcmp.invoke(libc(a, "strcmp"), a.allocateFrom("ab"), a.allocateFrom("abc"));
            check(pfx < 0, "strcmp prefix shorter < 0");
        }
    }

    // ---- abs/labs/llabs: exact integer returns ----
    static void testAbsFamily() throws Throwable {
        try (Arena a = Arena.ofConfined()) {
            MethodHandle abs = LINKER.downcallHandle(libc(a, "abs"),
                    FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.JAVA_INT));
            check((int) abs.invoke(-42) == 42, "abs(-42) == 42");
            check((int) abs.invoke(42) == 42, "abs(42) == 42");
            check((int) abs.invoke(0) == 0, "abs(0) == 0");
            MethodHandle labs = LINKER.downcallHandle(libc(a, "labs"),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
            check((long) labs.invoke(-123456789012L) == 123456789012L, "labs(-123456789012) == 123456789012");
            check((long) labs.invoke(0L) == 0L, "labs(0) == 0");
            MethodHandle llabs = LINKER.downcallHandle(libc(a, "llabs"),
                    FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.JAVA_LONG));
            check((long) llabs.invoke(-99L) == 99L, "llabs(-99) == 99");
        }
    }

    // ---- strtod: exact double parse plus endptr output written by C ----
    static void testStrtod() throws Throwable {
        MethodHandle strtod = LINKER.downcallHandle(libc(Arena.global(), "strtod"),
                FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE, ValueLayout.ADDRESS, ValueLayout.ADDRESS));
        try (Arena a = Arena.ofConfined()) {
            check((double) strtod.invoke(a.allocateFrom("3.25"), MemorySegment.NULL) == 3.25d,
                    "strtod(\"3.25\") == 3.25");
            check((double) strtod.invoke(a.allocateFrom("-2.5"), MemorySegment.NULL) == -2.5d,
                    "strtod(\"-2.5\") == -2.5");
            check((double) strtod.invoke(a.allocateFrom("0.5e3"), MemorySegment.NULL) == 500.0d,
                    "strtod(\"0.5e3\") == 500.0");
            // endptr: C writes back the address of the first unparsed char
            MemorySegment in = a.allocateFrom("3.25xyz");
            MemorySegment endPtr = a.allocate(ValueLayout.ADDRESS);
            double v = (double) strtod.invoke(in, endPtr);
            check(v == 3.25d, "strtod(\"3.25xyz\") value == 3.25");
            MemorySegment end = endPtr.getAtIndex(ValueLayout.ADDRESS, 0);
            check(!end.equals(MemorySegment.NULL), "endptr written by C is non-null");
            check(end.address() - in.address() == 4L,
                    "endptr points 4 bytes in (first unparsed char 'x')");
        }
    }

    // ---- strchr: ADDRESS return, null vs non-null semantics ----
    static void testStrchrAddressReturn() throws Throwable {
        MethodHandle strchr = LINKER.downcallHandle(libc(Arena.global(), "strchr"),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS, ValueLayout.JAVA_INT));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment s = a.allocateFrom("hello");
            MemorySegment hit = (MemorySegment) strchr.invoke(s, (int) 'l');
            check(!hit.equals(MemorySegment.NULL), "strchr found char returns non-null");
            check(hit.address() - s.address() == 2L, "strchr('l') points at offset 2");
            MemorySegment miss = (MemorySegment) strchr.invoke(s, (int) 'z');
            check(miss.equals(MemorySegment.NULL), "strchr missing char returns NULL segment");
            MemorySegment first = (MemorySegment) strchr.invoke(s, (int) 'l');
            check(first.address() == hit.address(), "strchr is deterministic across calls");
        }
    }

    // ---- memset: mixed (pointer,int,long) argument order, address return identity ----
    static void testMemsetMixedCarriers() throws Throwable {
        MethodHandle memset = LINKER.downcallHandle(libc(Arena.global(), "memset"),
                FunctionDescriptor.of(ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment buf = a.allocate(100, 1);
            MemorySegment r = (MemorySegment) memset.invoke(buf, (int) 'A', 100L);
            check(r.equals(buf), "memset returns its first argument segment");
            check(buf.get(ValueLayout.JAVA_BYTE, 0) == (byte) 'A', "memset filled byte 0");
            check(buf.get(ValueLayout.JAVA_BYTE, 99) == (byte) 'A', "memset filled last byte");
            memset.invoke(buf, (int) 'B', 1L);
            check(buf.get(ValueLayout.JAVA_BYTE, 0) == (byte) 'B'
               && buf.get(ValueLayout.JAVA_BYTE, 1) == (byte) 'A',
                    "memset with n=1 only writes first byte");
        }
    }

    // ---- sprintf through firstVariadicArg: real variadic linkage ----
    static void testVariadicSprintf() throws Throwable {
        MethodHandle sprintf = LINKER.downcallHandle(libc(Arena.global(), "sprintf"),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS, ValueLayout.ADDRESS,
                        ValueLayout.JAVA_INT, ValueLayout.ADDRESS),
                Linker.Option.firstVariadicArg(2));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment buf = a.allocate(64, 1);
            int n = (int) sprintf.invoke(buf, a.allocateFrom("%d-%s"), 42, a.allocateFrom("ok"));
            check(n == 5, "sprintf returns number of chars written (5)");
            check(buf.getString(0).equals("42-ok"), "sprintf formatted output read back as \"42-ok\"");
        }
    }

    // ---- unbound downcallHandle(FunctionDescriptor): leading address parameter ----
    static void testUnboundForm() throws Throwable {
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
        MethodHandle mh = LINKER.downcallHandle(fd);
        check(mh.type().parameterCount() == 2, "unbound handle takes address + function args");
        check(mh.type().parameterType(0) == MemorySegment.class, "unbound handle's first param is the target address");
        check(mh.type().returnType() == long.class, "unbound handle return type from descriptor");
        try (Arena a = Arena.ofConfined()) {
            check((long) mh.invoke(libc(a, "strlen"), a.allocateFrom("abcd")) == 4L,
                    "unbound strlen(\"abcd\") == 4");
        }
    }

    // ---- negative: NULL target address ----
    static void testNullTargetRejected() throws Throwable {
        MethodHandle mh = LINKER.downcallHandle(
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        try {
            mh.invoke(MemorySegment.NULL, MemorySegment.NULL);
            check(false, "downcall to NULL address must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "downcall to NULL address throws IllegalArgumentException");
        }
    }

    // ---- negative: heap segments cannot cross into native calls ----
    static void testHeapSegmentRejected() throws Throwable {
        MethodHandle isNull = LINKER.downcallHandle(OWN.findOrThrow("jck_is_null"),
                FunctionDescriptor.of(ValueLayout.JAVA_INT, ValueLayout.ADDRESS));
        try {
            isNull.invoke(MemorySegment.ofArray(new byte[8]));
            check(false, "heap segment as ADDRESS argument must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "heap segment as ADDRESS argument throws IllegalArgumentException");
        }
        check((int) isNull.invoke(MemorySegment.NULL) == 1, "same function accepts MemorySegment.NULL");
    }

    // ---- carrier matrix: every primitive carrier marshalled exactly once ----
    static void testCarrierMatrix() throws Throwable {
        MethodHandle carriers = LINKER.downcallHandle(OWN.findOrThrow("jck_carrier_sum"),
                FunctionDescriptor.of(ValueLayout.JAVA_DOUBLE,
                        ValueLayout.JAVA_BYTE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_FLOAT,
                        ValueLayout.JAVA_DOUBLE, ValueLayout.JAVA_BYTE, ValueLayout.JAVA_SHORT,
                        ValueLayout.JAVA_INT, ValueLayout.JAVA_LONG, ValueLayout.JAVA_BOOLEAN,
                        ValueLayout.JAVA_CHAR));
        check(carriers.type().parameterCount() == 13, "carrier handle has 13 parameters (one per layout)");
        // 1+2+3+4+5 + 6.25+7.5 + 8+9+10+11 + 1.5 + 65
        double r1 = (double) carriers.invoke((byte) 1, (byte) 2, (short) 3, 4, 5L, 6.25f, 7.5d,
                (byte) 8, (short) 9, 10, 11L, true, 'A');
        check(r1 == 133.25d, "carrier sum combination 1 == 133.25, got " + r1);
        // negatives, false and 'Z'(90): -66.75 - 1.5 + 90
        double r2 = (double) carriers.invoke((byte) -1, (byte) -2, (short) -3, -4, -5L, -6.25f, -7.5d,
                (byte) -8, (short) -9, -10, -11L, false, 'Z');
        check(r2 == 21.75d, "carrier sum combination 2 == 21.75, got " + r2);
        // carrier extrema exactly representable in double
        double r3 = (double) carriers.invoke(Byte.MIN_VALUE, Byte.MAX_VALUE, Short.MIN_VALUE,
                (int) Short.MAX_VALUE, 0L, 0.0f, 0.0d, (byte) 0, (short) 0, 0, 0L, true,
                Character.MAX_VALUE);
        double expect3 = (double) Byte.MIN_VALUE + Byte.MAX_VALUE + (double) Short.MIN_VALUE
                + Short.MAX_VALUE + 1.5d + (double) Character.MAX_VALUE;
        check(r3 == expect3, "carrier sum extrema combination exact, got " + r3 + " expected " + expect3);
    }

    // ---- errno capture: capture-state segment parameter directly follows the target address ----
    static void testErrnoCapture() throws Throwable {
        MethodHandle setErrno = LINKER.downcallHandle(OWN.findOrThrow("jck_set_errno"),
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT),
                Linker.Option.captureCallState("errno"));
        // bound form has no address parameter, so the state segment comes first
        check(setErrno.type().parameterCount() == 2, "capturing bound handle has state + function params");
        check(setErrno.type().parameterType(0) == MemorySegment.class,
                "capture state segment is the first parameter of a bound handle");
        StructLayout layout = Linker.Option.captureStateLayout();
        long errnoOff = layout.byteOffset(MemoryLayout.PathElement.groupElement("errno"));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment state = a.allocate(layout);
            setErrno.invoke(state, 7777);
            check(state.get(ValueLayout.JAVA_INT, errnoOff) == 7777,
                    "errno written by C read back from capture state segment");
            setErrno.invoke(state, 0);
            check(state.get(ValueLayout.JAVA_INT, errnoOff) == 0, "errno capture updated on second call");
        }
        // unbound form: state segment directly follows the address parameter
        MethodHandle unbound = LINKER.downcallHandle(
                FunctionDescriptor.ofVoid(ValueLayout.JAVA_INT),
                Linker.Option.captureCallState("errno"));
        check(unbound.type().parameterCount() == 3, "capturing unbound handle: address + state + function params");
        check(unbound.type().parameterType(1) == MemorySegment.class,
                "capture state segment follows the target address in unbound form");
        // unknown capture names are rejected eagerly when the option is built
        try {
            Linker.Option.captureCallState("jck_nonsense_state");
            check(false, "captureCallState with unknown name must throw");
        } catch (IllegalArgumentException expected) {
            check(true, "captureCallState with unknown name throws IllegalArgumentException");
        }
    }

    // ---- firstVariadicArg validation happens at linkage time ----
    static void testFirstVariadicValidation() throws Throwable {
        FunctionDescriptor fd = FunctionDescriptor.ofVoid(
                ValueLayout.JAVA_INT, ValueLayout.JAVA_INT, ValueLayout.JAVA_INT);
        for (int bad : new int[]{-1, 4, 100}) {
            try {
                LINKER.downcallHandle(fd, Linker.Option.firstVariadicArg(bad));
                check(false, "firstVariadicArg(" + bad + ") must be rejected at link time");
            } catch (IllegalArgumentException expected) {
                check(true, "firstVariadicArg(" + bad + ") throws IllegalArgumentException at link time");
            }
        }
        // index == N (none variadic) and 0 <= index < N are both linkable
        LINKER.downcallHandle(fd, Linker.Option.firstVariadicArg(3));
        check(true, "firstVariadicArg(N) linkable (no variadic args)");
        LINKER.downcallHandle(fd, Linker.Option.firstVariadicArg(0));
        check(true, "firstVariadicArg(0) linkable (all args variadic)");
    }

    // ---- critical vs plain linkage return identical results ----
    static void testCriticalOption() throws Throwable {
        FunctionDescriptor fd = FunctionDescriptor.of(ValueLayout.JAVA_LONG, ValueLayout.ADDRESS);
        MethodHandle plain = LINKER.downcallHandle(OWN.findOrThrow("jck_read_long"), fd);
        MethodHandle crit = LINKER.downcallHandle(OWN.findOrThrow("jck_read_long"), fd,
                Linker.Option.critical(true));
        MethodHandle nonCrit = LINKER.downcallHandle(OWN.findOrThrow("jck_read_long"), fd,
                Linker.Option.critical(false));
        try (Arena a = Arena.ofConfined()) {
            MemorySegment p = a.allocate(8, 8);
            LINKER.downcallHandle(OWN.findOrThrow("jck_write_long"),
                    FunctionDescriptor.ofVoid(ValueLayout.ADDRESS, ValueLayout.JAVA_LONG)).invoke(p, 4242L);
            check((long) plain.invoke(p) == 4242L, "plain downcall reads 4242");
            check((long) crit.invoke(p) == 4242L, "critical(true) downcall reads 4242");
            check((long) nonCrit.invoke(p) == 4242L, "critical(false) downcall reads 4242");
        }
    }

    public static void main(String[] args) throws Throwable {
        if (!gccAvailable()) {
            System.out.println("SKIP: no C compiler (gcc) available; native library cannot be built");
            return;
        }
        LINKER = Linker.nativeLinker();
        System.load(compileLib().toString());
        OWN = SymbolLookup.loaderLookup();
        check(OWN.find("jck_carrier_sum").isPresent(), "self-compiled library symbols visible via loaderLookup");

        testStrlen();
        testStrcmpSign();
        testAbsFamily();
        testStrtod();
        testStrchrAddressReturn();
        testMemsetMixedCarriers();
        testVariadicSprintf();
        testUnboundForm();
        testNullTargetRejected();
        testHeapSegmentRejected();
        testCarrierMatrix();
        testErrnoCapture();
        testFirstVariadicValidation();
        testCriticalOption();

        System.out.println("PASS (" + passed + " assertions)");
    }
}

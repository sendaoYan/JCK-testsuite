/*
 * @test
 * @summary java.lang.Runtime conformance: white-box structure + black-box behavior.
 * @run main RuntimeTest
 */

import java.lang.reflect.Modifier;

public class RuntimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Runtime");
        check(Modifier.isFinal(c.getModifiers()), "Runtime is final");
        check(!c.isInterface(), "Runtime is a class");
        check(c.getConstructors().length == 0, "Runtime has no public constructors");
        check(Modifier.isStatic(c.getMethod("getRuntime").getModifiers()), "getRuntime static");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testSingleton() {
        Runtime r1 = Runtime.getRuntime();
        Runtime r2 = Runtime.getRuntime();
        check(r1 != null, "getRuntime non-null");
        check(r1 == r2, "getRuntime returns the same instance");
    }

    static void testProcessors() {
        check(Runtime.getRuntime().availableProcessors() >= 1, "availableProcessors >= 1");
    }

    static void testMemory() {
        Runtime r = Runtime.getRuntime();
        long max = r.maxMemory();
        long total = r.totalMemory();
        long free = r.freeMemory();
        check(max >= total, "maxMemory >= totalMemory");
        check(total >= free, "totalMemory >= freeMemory");
        check(free >= 0, "freeMemory >= 0");
    }

    static void testVersion() {
        Runtime.Version v = Runtime.version();
        check(v != null, "version() non-null");
        check(v.toString() != null && !v.toString().isEmpty(), "version string non-empty");
    }

    static void testGC() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.runFinalization();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleton();
        testProcessors();
        testMemory();
        testVersion();
        testGC();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

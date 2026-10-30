/*
 * @test
 * @summary java.lang.Runtime API conformance: getRuntime singleton, availableProcessors,
 *          memory invariants, gc/runFinalization no-op, version() (Java 9+ via reflection),
 *          and add/removeShutdownHook roundtrip.
 * @run main RuntimeTest
 */

public class RuntimeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSingleton() {
        Runtime r1 = Runtime.getRuntime();
        Runtime r2 = Runtime.getRuntime();
        check(r1 == r2, "getRuntime returns the same instance");
        check(r1 != null, "non-null");
    }

    static void testProcessors() {
        check(Runtime.getRuntime().availableProcessors() >= 1, "availableProcessors >= 1");
    }

    static void testMemory() {
        Runtime r = Runtime.getRuntime();
        long free = r.freeMemory();
        long total = r.totalMemory();
        long max = r.maxMemory();
        check(free >= 0 && total >= 0 && max >= 0, "memory values non-negative");
        check(free <= total, "freeMemory <= totalMemory");
        check(total <= max || max == Long.MAX_VALUE, "totalMemory <= maxMemory");
    }

    static void testGc() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.runFinalization();
        check(r.totalMemory() >= 0, "gc/runFinalization complete without throwing");
    }

    static void testVersion() {
        try {
            java.lang.reflect.Method m = Runtime.class.getMethod("version");
            Object v = m.invoke(Runtime.getRuntime());
            check(v != null, "Runtime.version() returns non-null [Java 9+]");
            check(v.toString().length() > 0, "version().toString() non-empty");
        } catch (NoSuchMethodException e) {
            check(System.getProperty("java.version") != null, "java.version property present [JDK8]");
        } catch (ReflectiveOperationException e) {
            throw new AssertionError(e);
        }
    }

    static void testShutdownHook() {
        Runtime r = Runtime.getRuntime();
        Thread hook = new Thread(() -> {});
        r.addShutdownHook(hook);
        check(r.removeShutdownHook(hook), "removeShutdownHook returns true");
    }

    public static void main(String[] args) {
        testSingleton();
        testProcessors();
        testMemory();
        testGc();
        testVersion();
        testShutdownHook();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.lang.Runtime conformance: white-box structure (final, no public
 *          constructor, static getRuntime) + black-box behavior (singleton,
 *          availableProcessors, memory invariants, gc/runFinalization no-op,
 *          version via reflection, shutdown hook add/remove/duplicate/null,
 *          exec missing command -> IOException, load/loadLibrary missing -> ULE).
 * @run main RuntimeTest
 */

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;

public class RuntimeTest {
    static int passed;

    interface IOAction { void run() throws IOException; }

    static void expectIOException(IOAction r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IOException");
        } catch (IOException expected) {
            passed++;
        }
    }

    static void expectUnsatisfiedLinkError(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError expected) {
            passed++;
        }
    }

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Runtime");
        check(!Modifier.isFinal(c.getModifiers()), "Runtime is not final in JDK-8");
        check(!c.isInterface(), "Runtime is a class");
        check(c.getConstructors().length == 0, "Runtime has no public constructors");
        check(Modifier.isStatic(c.getMethod("getRuntime").getModifiers()), "getRuntime static");
    }

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

    static void testGc() {
        Runtime r = Runtime.getRuntime();
        r.gc();
        r.runFinalization();
        passed++;
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
        try {
            r.addShutdownHook(hook);
            throw new AssertionError("addShutdownHook twice: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        check(r.removeShutdownHook(hook), "removeShutdownHook returns true after add");
        check(!r.removeShutdownHook(hook), "removeShutdownHook returns false when not registered");
        try {
            r.addShutdownHook(null);
            throw new AssertionError("addShutdownHook(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testExecAndLoad() {
        Runtime r = Runtime.getRuntime();
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz"), "exec(String)");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0]), "exec(String,String[])");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0], new File(".")),
                          "exec(String,String[],File)");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}), "exec(String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0]),
                          "exec(String[],String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0], new File(".")),
                          "exec(String[],String[],File)");

        expectUnsatisfiedLinkError(() -> r.load("/jck/no/such/lib.so"), "load(missing path)");
        expectUnsatisfiedLinkError(() -> r.loadLibrary("jck_no_such_lib_xyz"), "loadLibrary(missing)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleton();
        testProcessors();
        testMemory();
        testGc();
        testVersion();
        testShutdownHook();
        testExecAndLoad();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

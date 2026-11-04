/*
 * @test
 * @summary java.lang.Runtime conformance: white-box structure + black-box behavior.
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

    // ---- Part 2b: black-box for shutdown hooks, exec, load/loadLibrary ----
    static void testBlackBox() {
        Runtime r = Runtime.getRuntime();

        // addShutdownHook / removeShutdownHook roundtrip (no leftover hooks).
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

        // exec overloads: a missing command yields IOException before any process is spawned.
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz"), "exec(String)");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0]), "exec(String,String[])");
        expectIOException(() -> r.exec("jck-no-such-cmd-xyz", new String[0], new File(".")),
                          "exec(String,String[],File)");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}), "exec(String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0]),
                          "exec(String[],String[])");
        expectIOException(() -> r.exec(new String[]{"jck-no-such-cmd-xyz"}, new String[0], new File(".")),
                          "exec(String[],String[],File)");

        // load / loadLibrary with a missing library -> UnsatisfiedLinkError.
        expectUnsatisfiedLinkError(() -> r.load("/jck/no/such/lib.so"), "load(missing path)");
        expectUnsatisfiedLinkError(() -> r.loadLibrary("jck_no_such_lib_xyz"), "loadLibrary(missing)");

        // exit(int) and halt(int) terminate the JVM, so they are intentionally not invoked.
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testSingleton();
        testProcessors();
        testMemory();
        testVersion();
        testGC();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

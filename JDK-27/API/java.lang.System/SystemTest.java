/*
 * @test
 * @summary java.lang.System conformance: white-box structure + black-box behavior.
 * @run main SystemTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;

public class SystemTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expectNPE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void expectIOOBE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected IndexOutOfBoundsException");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }
    }

    static void expectASE(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected ArrayStoreException");
        } catch (ArrayStoreException expected) {
            passed++;
        }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.System");
        check(Modifier.isFinal(c.getModifiers()), "System is final");
        check(!c.isInterface(), "System is a class");
        check(c.getConstructors().length == 0, "System has no public constructors");
        check(c.getField("in") != null && c.getField("out") != null && c.getField("err") != null,
              "in/out/err fields present");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testStreams() {
        check(System.in != null, "System.in != null");
        check(System.out != null, "System.out != null");
        check(System.err != null, "System.err != null");
    }

    static void testArraycopy() {
        // Basic full copy.
        int[] src = {1, 2, 3, 4, 5};
        int[] dst = new int[5];
        System.arraycopy(src, 0, dst, 0, 5);
        check(Arrays.equals(src, dst), "arraycopy full");

        // Partial copy.
        int[] part = new int[3];
        System.arraycopy(src, 1, part, 0, 3);
        check(Arrays.equals(part, new int[]{2, 3, 4}), "arraycopy partial");

        // Overlapping same array: shift right (must copy from the tail).
        int[] a = {1, 2, 3, 4, 5};
        System.arraycopy(a, 0, a, 1, 4);
        check(Arrays.equals(a, new int[]{1, 1, 2, 3, 4}), "arraycopy overlap shift right");

        // Overlapping same array: shift left.
        int[] b = {1, 2, 3, 4, 5};
        System.arraycopy(b, 1, b, 0, 4);
        check(Arrays.equals(b, new int[]{2, 3, 4, 5, 5}), "arraycopy overlap shift left");

        // length 0 does nothing.
        int[] c = {1, 2, 3};
        System.arraycopy(c, 0, c, 2, 0);
        check(Arrays.equals(c, new int[]{1, 2, 3}), "arraycopy length 0 no-op");

        // Reference copy where the source element type is assignable.
        Object[] osrc = {"a", "b"};
        String[] sdst = new String[2];
        System.arraycopy(osrc, 0, sdst, 0, 2);
        check(sdst[0].equals("a") && sdst[1].equals("b"), "arraycopy Object[] -> String[]");

        expectNPE(() -> System.arraycopy(null, 0, new int[1], 0, 1), "arraycopy null src");
        expectNPE(() -> System.arraycopy(new int[1], 0, null, 0, 1), "arraycopy null dst");
        expectIOOBE(() -> System.arraycopy(src, -1, dst, 0, 1), "arraycopy negative srcPos");
        expectIOOBE(() -> System.arraycopy(src, 0, dst, 0, 6), "arraycopy length overflow");

        // Element type mismatch.
        expectASE(() -> System.arraycopy(new Object[]{"a", Integer.valueOf(1)}, 0,
                                         new String[2], 0, 2), "arraycopy element type mismatch");
        expectASE(() -> System.arraycopy(new int[]{1}, 0, new long[1], 0, 1),
                  "arraycopy int[] -> long[]");
    }

    static void testProperties() {
        java.util.Properties props = System.getProperties();
        check(props != null, "getProperties != null");
        check(System.getProperty("java.version") != null
              && !System.getProperty("java.version").isEmpty(), "java.version set");
        check(System.getProperty("os.name") != null && !System.getProperty("os.name").isEmpty(),
              "os.name set");
        check(System.getProperty("jck.sys.absent", "dflt").equals("dflt"),
              "getProperty with default");
        // Isolated set/get/clear on a unique key, restored via clearProperty.
        try {
            check(System.setProperty("jck.sys.test", "v1") == null, "setProperty returns null initially");
            check(System.getProperty("jck.sys.test").equals("v1"), "getProperty returns set value");
            check(System.setProperty("jck.sys.test", "v2").equals("v1"), "setProperty returns old value");
            check(System.clearProperty("jck.sys.test").equals("v2"), "clearProperty returns old value");
            check(System.getProperty("jck.sys.test") == null, "cleared property is null");
        } finally {
            System.clearProperty("jck.sys.test");
        }
    }

    static void testEnv() {
        Map<String, String> env = System.getenv();
        check(env != null, "getenv() != null");
        check(System.getenv("__jck_definitely_not_set__") == null, "getenv(missing) == null");
    }

    static void testIdentityHashCode() {
        Object o = new Object();
        check(System.identityHashCode(o) == System.identityHashCode(o), "identityHashCode stable");
        check(System.identityHashCode(o) == o.hashCode(), "identityHashCode == default hashCode");
        check(System.identityHashCode(null) == 0, "identityHashCode(null) == 0");
    }

    static void testLineSeparator() {
        String ls = System.lineSeparator();
        check(ls.equals("\n") || ls.equals("\r\n") || ls.equals("\r"), "lineSeparator valid");
        check(ls.equals(System.getProperty("line.separator")),
              "lineSeparator == line.separator property");
    }

    static void testTime() {
        long now = System.currentTimeMillis();
        check(now > 1_000_000_000_000L, "currentTimeMillis > year 2001");
        check(now < 10_000_000_000_000L, "currentTimeMillis < year 2286");
        long t1 = System.nanoTime();
        long t2 = System.nanoTime();
        check(t1 >= 0 && t2 >= 0, "nanoTime non-negative");
        check(t2 >= t1, "nanoTime monotonic");
    }

    static void testLoggerAndMapLibraryName() {
        System.Logger log = System.getLogger("jck-system-logger");
        check(log != null, "getLogger non-null");
        check("jck-system-logger".equals(log.getName()), "logger name");
        check(System.mapLibraryName("foo") != null && !System.mapLibraryName("foo").isEmpty(),
              "mapLibraryName non-empty");
    }

    static void testGC() {
        // These must be callable without throwing; their effect is not asserted.
        System.gc();
        System.runFinalization();
        passed++;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testStreams();
        testArraycopy();
        testProperties();
        testEnv();
        testIdentityHashCode();
        testLineSeparator();
        testTime();
        testLoggerAndMapLibraryName();
        testGC();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

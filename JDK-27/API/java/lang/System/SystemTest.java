/*
 * @test
 * @summary java.lang.System conformance: white-box structure + black-box behavior.
 * @run main SystemTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Map;
import java.util.Properties;

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

    // ================= 契约深化（2026-11）追加 =================
    // Overlap expectation helper: reads always come from a pristine snapshot,
    // which is exactly the "temporary array" semantics required by the spec.
    static int[] expectedOverlap(int[] src, int srcPos, int destPos, int len) {
        int[] pristine = src.clone();
        int[] exp = src.clone();
        for (int i = 0; i < len; i++) exp[destPos + i] = pristine[srcPos + i];
        return exp;
    }

    static String[] expectedOverlap(String[] src, int srcPos, int destPos, int len) {
        String[] pristine = src.clone();
        String[] exp = src.clone();
        for (int i = 0; i < len; i++) exp[destPos + i] = pristine[srcPos + i];
        return exp;
    }

    static void testArraycopyMatrix() {
        // All 8 primitive array kinds: full copy round-trip.
        byte[] by1 = {1, 2, 3}; byte[] by2 = new byte[3];
        System.arraycopy(by1, 0, by2, 0, 3);
        check(Arrays.equals(by1, by2), "arraycopy byte[]");
        short[] sh1 = {1, 2, 3}; short[] sh2 = new short[3];
        System.arraycopy(sh1, 0, sh2, 0, 3);
        check(Arrays.equals(sh1, sh2), "arraycopy short[]");
        long[] lo1 = {1L, 2L, 3L}; long[] lo2 = new long[3];
        System.arraycopy(lo1, 0, lo2, 0, 3);
        check(Arrays.equals(lo1, lo2), "arraycopy long[]");
        float[] fl1 = {1.5f, 2.5f}; float[] fl2 = new float[2];
        System.arraycopy(fl1, 0, fl2, 0, 2);
        check(Arrays.equals(fl1, fl2), "arraycopy float[]");
        double[] do1 = {1.5, 2.5}; double[] do2 = new double[2];
        System.arraycopy(do1, 0, do2, 0, 2);
        check(Arrays.equals(do1, do2), "arraycopy double[]");
        char[] ch1 = {'a', 'b', 'c'}; char[] ch2 = new char[3];
        System.arraycopy(ch1, 0, ch2, 0, 3);
        check(Arrays.equals(ch1, ch2), "arraycopy char[]");
        boolean[] bo1 = {true, false}; boolean[] bo2 = new boolean[2];
        System.arraycopy(bo1, 0, bo2, 0, 2);
        check(Arrays.equals(bo1, bo2), "arraycopy boolean[]");

        // Source untouched by the copy.
        int[] srcT = {1, 2, 3, 4, 5};
        System.arraycopy(srcT, 0, new int[5], 0, 5);
        check(Arrays.equals(srcT, new int[]{1, 2, 3, 4, 5}), "arraycopy leaves src intact");

        // Cross-array partial copies in both offset directions.
        int[] csrc = {10, 11, 12, 13, 14, 15};
        int[] cdst = new int[4];
        System.arraycopy(csrc, 1, cdst, 0, 4);
        check(Arrays.equals(cdst, new int[]{11, 12, 13, 14}), "arraycopy offset head->head");
        int[] cdst2 = new int[5];
        System.arraycopy(csrc, 0, cdst2, 2, 3);
        check(Arrays.equals(cdst2, new int[]{0, 0, 10, 11, 12}), "arraycopy offset into tail");
        int[] cdst3 = {9, 9, 9, 9};
        System.arraycopy(csrc, 2, cdst3, 1, 2);
        check(Arrays.equals(cdst3, new int[]{9, 12, 13, 9}), "arraycopy middle window");

        // Same-array overlapping shift, parameterized over several windows.
        int[][] windows = {{0, 3, 5}, {2, 0, 5}, {1, 4, 4}, {3, 3, 3}, {0, 1, 6}, {5, 0, 5}, {1, 3, 4}, {2, 6, 4}};
        for (int[] w : windows) {
            int[] arr = {0, 1, 2, 3, 4, 5, 6, 7, 8, 9};
            System.arraycopy(arr, w[0], arr, w[1], w[2]);
            check(Arrays.equals(arr, expectedOverlap(
                      new int[]{0, 1, 2, 3, 4, 5, 6, 7, 8, 9}, w[0], w[1], w[2])),
                  "arraycopy overlap " + w[0] + "->" + w[1] + " len " + w[2]);
        }

        // Same-array non-overlapping region move.
        int[] mv = {1, 2, 3, 0, 0, 0};
        System.arraycopy(mv, 0, mv, 3, 3);
        check(Arrays.equals(mv, new int[]{1, 2, 3, 1, 2, 3}), "arraycopy disjoint move");

        // srcPos == destPos is an unobservable no-op even when it overlaps.
        int[] same = {5, 6, 7, 8};
        System.arraycopy(same, 1, same, 1, 2);
        check(Arrays.equals(same, new int[]{5, 6, 7, 8}), "arraycopy srcPos==destPos no-op");

        // Exact boundary windows are legal.
        int[] bsrc = {1, 2, 3};
        int[] bdst = new int[3];
        System.arraycopy(bsrc, 0, bdst, 0, 3);
        check(bdst[2] == 3, "arraycopy full-length boundary");
        check(bdst[0] == 1 && bdst[1] == 2, "arraycopy full-length boundary head");
        System.arraycopy(bsrc, 1, bdst, 1, 2);
        check(bdst[0] == 1 && bdst[1] == 2 && bdst[2] == 3, "arraycopy tail boundary");
        System.arraycopy(bsrc, 3, bdst, 0, 0);
        System.arraycopy(bsrc, 0, bdst, 3, 0);
        check(bdst[0] == 1, "arraycopy length 0 at array end");

        // Reference-array overlap with the same snapshot semantics.
        String[] rs = {"a", "b", "c", "d", "e"};
        System.arraycopy(rs, 0, rs, 2, 3);
        check(Arrays.equals(rs, expectedOverlap(
                  new String[]{"a", "b", "c", "d", "e"}, 0, 2, 3)),
              "arraycopy String[] overlap forward");
        String[] rs2 = {"a", "b", "c", "d", "e"};
        System.arraycopy(rs2, 2, rs2, 0, 3);
        check(Arrays.equals(rs2, expectedOverlap(
                  new String[]{"a", "b", "c", "d", "e"}, 2, 0, 3)),
              "arraycopy String[] overlap backward");

        // Element identity: reference copies alias the same objects.
        StringBuilder sb = new StringBuilder("x");
        Object[] osrc = {sb};
        Object[] odst = new Object[1];
        System.arraycopy(osrc, 0, odst, 0, 1);
        check(odst[0] == sb, "arraycopy copies references");
        sb.append("y");
        check(((StringBuilder) odst[0]).toString().equals("xy"), "arraycopy elements are aliased");

        // 2D arrays are object arrays: row references are copied, rows shared.
        int[][] m1 = {{1, 2}, {3, 4}};
        int[][] m2 = new int[2][2];
        System.arraycopy(m1, 0, m2, 0, 2);
        check(m2[0] == m1[0] && m2[1] == m1[1], "arraycopy int[][] copies row references");
        m1[0][0] = 99;
        check(m2[0][0] == 99, "arraycopy 2D rows are aliased");

        // Covariant source: Integer[] -> Number[] (assignable component).
        Integer[] isrc = {Integer.valueOf(7)};
        Number[] ndst = new Number[1];
        System.arraycopy(isrc, 0, ndst, 0, 1);
        check(ndst[0].intValue() == 7, "arraycopy Integer[] -> Number[]");
        String[] svals = {"p", "q"};
        Object[] ovals = new Object[2];
        System.arraycopy(svals, 0, ovals, 0, 2);
        check("p".equals(ovals[0]) && "q".equals(ovals[1]), "arraycopy String[] -> Object[]");
        byte[] bva = {1, 2, 3, 4};
        System.arraycopy(bva, 1, bva, 0, 3);
        check(Arrays.equals(bva, new byte[]{2, 3, 4, 4}), "arraycopy byte[] overlap backward");
        float[] fsrc = {0.5f, 1.5f, 2.5f};
        float[] fdst = new float[2];
        System.arraycopy(fsrc, 1, fdst, 0, 2);
        check(fdst[0] == 1.5f && fdst[1] == 2.5f, "arraycopy float[] partial");
    }

    static void testArraycopyExceptions() {
        int[] src = {1, 2, 3};
        // JDK-27 messages are detailed; each violation position has its own form.
        try {
            System.arraycopy(src, 0, new int[3], 0, -1);
            throw new AssertionError("negative length: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: length -1 is negative".equals(e.getMessage()),
                  "arraycopy negative length message [JDK27]");
        }
        try {
            System.arraycopy(src, -1, new int[3], 0, 1);
            throw new AssertionError("negative srcPos: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: source index -1 out of bounds for int[3]".equals(e.getMessage()),
                  "arraycopy negative srcPos message [JDK27]");
        }
        try {
            System.arraycopy(src, 2, new int[3], 0, 2);
            throw new AssertionError("srcPos+len overflow: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: last source index 4 out of bounds for int[3]".equals(e.getMessage()),
                  "arraycopy srcPos+len>srcLen message [JDK27]");
        }
        try {
            System.arraycopy(src, 0, new int[3], -1, 1);
            throw new AssertionError("negative destPos: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: destination index -1 out of bounds for int[3]".equals(e.getMessage()),
                  "arraycopy negative destPos message [JDK27]");
        }
        try {
            System.arraycopy(src, 0, new int[3], 2, 2);
            throw new AssertionError("destPos+len overflow: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: last destination index 4 out of bounds for int[3]".equals(e.getMessage()),
                  "arraycopy destPos+len>dstLen message [JDK27]");
        }
        expectNPE(() -> System.arraycopy(null, 0, null, 0, 1), "arraycopy both null");

        // ArrayStoreException timing (locked by experiment): element-wise copy,
        // first-element mismatch leaves the destination completely untouched.
        Object[] first = {Integer.valueOf(1), "b", "c"};
        String[] d1 = {"x", "y", "z"};
        try {
            System.arraycopy(first, 0, d1, 0, 3);
            throw new AssertionError("first-element mismatch: expected ASE");
        } catch (ArrayStoreException e) {
            check(e.getMessage() != null && e.getMessage().startsWith("arraycopy: element type mismatch"),
                  "arraycopy ASE message [JDK27]");
            check(("arraycopy: element type mismatch: can not cast one of the elements of "
                  + "java.lang.Object[] to the type of the destination array, java.lang.String")
                  .equals(e.getMessage()), "arraycopy ASE exact message [JDK27]");
        }
        check(d1[0].equals("x") && d1[1].equals("y") && d1[2].equals("z"),
              "arraycopy ASE at index 0 leaves dest unchanged");

        // When a LATER element is incompatible the prefix has already been copied.
        Object[] second = {"a", Integer.valueOf(2), "c"};
        String[] d2 = {"x", "y", "z"};
        try {
            System.arraycopy(second, 0, d2, 0, 3);
            throw new AssertionError("second-element mismatch: expected ASE");
        } catch (ArrayStoreException e) {
            check(e.getMessage().startsWith("arraycopy: element type mismatch"),
                  "arraycopy ASE at index 1 message [JDK27]");
        }
        check(d2[0].equals("a"), "arraycopy ASE prefix element copied");
        check(d2[1].equals("y") && d2[2].equals("z"), "arraycopy ASE suffix untouched");

        // A null element is storable in any reference array.
        Integer[] withNull = new Integer[1];
        String[] d3 = new String[1];
        System.arraycopy(withNull, 0, d3, 0, 1);
        check(d3[0] == null, "arraycopy null element crosses Integer[] -> String[]");

        // Static component-type mismatch messages (primitive vs object arrays).
        try {
            System.arraycopy(new int[1], 0, new Integer[1], 0, 1);
            throw new AssertionError("int[] -> Integer[]: expected ASE");
        } catch (ArrayStoreException e) {
            check("arraycopy: type mismatch: can not copy int[] into object array[]".equals(e.getMessage()),
                  "arraycopy int[] -> object[] message [JDK27]");
        }
        try {
            System.arraycopy(new Integer[1], 0, new int[1], 0, 1);
            throw new AssertionError("Integer[] -> int[]: expected ASE");
        } catch (ArrayStoreException e) {
            check("arraycopy: type mismatch: can not copy object array[] into int[]".equals(e.getMessage()),
                  "arraycopy object[] -> int[] message [JDK27]");
        }
        expectASE(() -> System.arraycopy(new Integer[]{Integer.valueOf(1)}, 0, new String[1], 0, 1),
                  "arraycopy Integer[] -> String[] non-null element");
    }

    static void testArraycopyEdges() {
        int[] src = {1, 2, 3};
        // A position beyond the array is rejected even for length 0.
        try {
            System.arraycopy(src, 4, new int[2], 0, 0);
            throw new AssertionError("srcPos > length with len 0: expected AIOOBE");
        } catch (ArrayIndexOutOfBoundsException e) {
            check("arraycopy: last source index 4 out of bounds for int[3]".equals(e.getMessage()),
                  "arraycopy srcPos>length len 0 message [JDK27]");
        }
        expectIOOBE(() -> System.arraycopy(src, 0, new int[2], 4, 0),
                    "destPos > length with len 0");
        expectIOOBE(() -> System.arraycopy(src, Integer.MIN_VALUE, new int[2], 0, 1),
                    "Integer.MIN_VALUE srcPos");
        expectIOOBE(() -> System.arraycopy(src, 0, new int[2], 0, Integer.MAX_VALUE),
                    "Integer.MAX_VALUE length");
        // char[] and boolean[] overlapping shifts with exact content.
        char[] ca = {'a', 'b', 'c', 'd'};
        System.arraycopy(ca, 0, ca, 1, 3);
        check(Arrays.equals(ca, new char[]{'a', 'a', 'b', 'c'}), "arraycopy char[] overlap forward");
        char[] cb = {'a', 'b', 'c', 'd'};
        System.arraycopy(cb, 1, cb, 0, 3);
        check(Arrays.equals(cb, new char[]{'b', 'c', 'd', 'd'}), "arraycopy char[] overlap backward");
        boolean[] ba = {true, true, false, true, false};
        System.arraycopy(ba, 0, ba, 2, 3);
        check(Arrays.equals(ba, new boolean[]{true, true, true, true, false}),
              "arraycopy boolean[] overlap forward");
        // Larger int content overlap.
        int[] big = new int[32];
        for (int i = 0; i < 32; i++) big[i] = i;
        System.arraycopy(big, 0, big, 8, 24);
        int[] bigExp = new int[32];
        for (int i = 0; i < 32; i++) bigExp[i] = i;
        int[] pristine = new int[32];
        for (int i = 0; i < 32; i++) pristine[i] = i;
        for (int i = 0; i < 24; i++) bigExp[8 + i] = pristine[i];
        check(Arrays.equals(big, bigExp), "arraycopy 32-element overlap forward");
        long[] la = {10L, 20L, 30L, 40L};
        System.arraycopy(la, 0, la, 1, 3);
        check(Arrays.equals(la, new long[]{10L, 10L, 20L, 30L}), "arraycopy long[] overlap forward");
        double[] da = {1.5, 2.5, 3.5};
        System.arraycopy(da, 2, da, 0, 1);
        check(da[0] == 3.5 && da[1] == 2.5 && da[2] == 3.5, "arraycopy double[] overlap backward");
        short[] sa = {9, 8, 7, 6};
        System.arraycopy(sa, 1, sa, 0, 3);
        check(Arrays.equals(sa, new short[]{8, 7, 6, 6}), "arraycopy short[] overlap backward");
    }

    static void testTimeWeak() {
        long t1 = System.currentTimeMillis();
        long t2 = System.currentTimeMillis();
        check(t2 >= t1, "currentTimeMillis non-decreasing (weak)");
        check(t1 > 0, "currentTimeMillis positive");
        long n1 = System.nanoTime();
        check(n1 > 0, "nanoTime positive [JDK27 Linux]");
        long n2 = System.nanoTime();
        check(n2 >= n1, "nanoTime non-decreasing (weak)");
        long n3 = System.nanoTime();
        check(n3 >= n2, "nanoTime non-decreasing 3rd read");
        long t3 = System.currentTimeMillis();
        check(t3 >= t2, "currentTimeMillis non-decreasing 3rd read");
    }

    static void testPropertiesContract() {
        String[] stdKeys = {"java.version", "java.vendor", "java.vendor.url", "java.home",
                            "java.class.path", "java.class.version", "os.name", "os.arch",
                            "os.version", "file.separator", "path.separator", "line.separator",
                            "user.dir", "user.home", "user.name",
                            "java.specification.version", "java.specification.vendor",
                            "java.specification.name", "java.vm.specification.version",
                            "java.vm.specification.vendor", "java.vm.specification.name",
                            "java.vm.version", "java.vm.vendor", "java.vm.name",
                            "java.io.tmpdir"};
        for (String k : stdKeys) {
            check(System.getProperty(k) != null, "std property " + k + " non-null");
        }
        check(System.getProperty("line.separator") != null, "line.separator non-null");
        check(System.getProperty("file.separator").length() == 1, "file.separator single char");
        check(System.getProperty("path.separator").length() == 1, "path.separator single char");
        check(System.getProperty("lcF.definitely.absent.key") == null, "getProperty missing -> null");
        check(System.getProperty("lcF.definitely.absent.key", "dflt").equals("dflt"),
              "getProperty default when absent");
        check(System.getProperty("lcF.definitely.absent.key", null) == null,
              "getProperty null default when absent");
        check(System.getProperty("java.version", "notThis").equals(System.getProperty("java.version")),
              "getProperty default ignored when present");
        check(System.clearProperty("lcF.never.set") == null, "clearProperty absent -> null");

        String KEY = "lcF.props.roundtrip";
        try {
            check(System.setProperty(KEY, "v1") == null, "setProperty new key returns null");
            check(System.getProperty(KEY).equals("v1"), "getProperty after set");
            check(System.setProperty(KEY, "v2").equals("v1"), "setProperty returns previous");
            check(System.clearProperty(KEY).equals("v2"), "clearProperty returns previous");
            check(System.getProperty(KEY) == null, "getProperty after clear");
        } finally {
            System.clearProperty(KEY);
        }

        Properties props = System.getProperties();
        check(props instanceof Properties, "getProperties type");
        check(props.containsKey("java.version"), "getProperties contains java.version");
        check(props.getProperty("java.version") != null, "Properties.getProperty sees java.version");
        check(props.size() > 10, "getProperties has a populated table");
        try {
            props.put("lcF.live.props", "viaProps");
            check(System.getProperty("lcF.live.props").equals("viaProps"),
                  "getProperties() is the live table (put visible)");
            props.setProperty("lcF.live.props", "viaSetProperty");
            check(System.getProperty("lcF.live.props").equals("viaSetProperty"),
                  "Properties.setProperty visible to getProperty");
        } finally {
            props.remove("lcF.live.props");
        }
        check(System.getProperty("lcF.live.props") == null, "live table restored");
    }

    static void expectUoe(Runnable r, String msg) {
        try {
            r.run();
            throw new AssertionError(msg + ": expected UnsupportedOperationException");
        } catch (UnsupportedOperationException expected) {
            passed++;
        }
    }

    static void testEnvContract() {
        check(System.getenv("PATH") != null, "getenv(PATH) non-null");
        check(System.getenv("lcF.definitely.not.set") == null, "getenv missing -> null");
        Map<String, String> v1 = System.getenv();
        Map<String, String> v2 = System.getenv();
        check(v1.containsKey("PATH"), "env map contains PATH");
        check(v1.equals(v2), "env views are equal");
        check(v1 == v2, "env views are the same object [locked by experiment]");
        check(!v1.containsKey("lcF.definitely.not.set"), "env map lacks missing key");
        check(v1.size() > 0, "env map non-empty");
        check(!v1.isEmpty(), "env map not empty");
        check(v1.get("PATH").equals(System.getenv("PATH")), "env map PATH == getenv(PATH)");
        check(v1.keySet().contains("PATH"), "env keySet contains PATH");
        check(v1.values().contains(System.getenv("PATH")), "env values contains PATH value");
        check(v1.entrySet().size() == v1.size(), "env entrySet size matches");
        check(v1.getOrDefault("PATH", "fallback").equals(System.getenv("PATH")),
              "env getOrDefault present key");
        check(v1.getOrDefault("lcF.definitely.not.set", "fallback").equals("fallback"),
              "env getOrDefault absent key");
        expectUoe(() -> v1.put("lcF", "x"), "env map put");
        expectUoe(() -> v1.putAll(java.util.Collections.singletonMap("lcF2", "x")), "env map putAll");
        expectUoe(() -> v1.remove("PATH"), "env map remove");
        check(v1.toString().contains("PATH"), "env map toString contains PATH");
        check(v2.get("PATH").equals(v1.get("PATH")), "env views PATH agree");
        expectUoe(() -> v1.entrySet().add(new java.util.Map.Entry<String, String>() {
            public String getKey() { return "k"; }
            public String getValue() { return "v"; }
            public String setValue(String v) { return "old"; }
        }), "env map entrySet add");
    }

    static class FixedHash {
        int value = 1;
        public int hashCode() { return 42; }
    }

    static void testIdentityHashCodeContract() {
        Object o = new Object();
        int h1 = System.identityHashCode(o);
        int h2 = System.identityHashCode(o);
        check(h1 == h2, "identityHashCode stable across calls");
        check(System.identityHashCode(o) == o.hashCode(), "identityHashCode == Object.hashCode");
        check(System.identityHashCode(null) == 0, "identityHashCode(null) == 0");
        Object p = new Object();
        check(System.identityHashCode(p) == System.identityHashCode(p), "identityHashCode stable for 2nd object");
        String s = "lcF-identity-probe";
        check(System.identityHashCode(s) == System.identityHashCode(s), "identityHashCode stable for String");
        int[] arr = {1};
        check(System.identityHashCode(arr) == System.identityHashCode(arr), "identityHashCode stable for array");

        Integer i1 = Integer.valueOf(101);
        Integer i2 = Integer.valueOf(101);
        check(i1 == i2, "Integer cache identity");
        check(System.identityHashCode(i1) == System.identityHashCode(i2), "identityHashCode equal for identical cached boxes");

        FixedHash fh = new FixedHash();
        check(fh.hashCode() == 42, "FixedHash.hashCode overridden");
        check(System.identityHashCode(fh) == System.identityHashCode(fh),
              "identityHashCode stable for hashCode-overriding class");
        FixedHash fh2 = new FixedHash();
        check(fh2.hashCode() == fh.hashCode(), "FixedHash equal content hashes");
        check(System.identityHashCode(fh2) == System.identityHashCode(fh2),
              "identityHashCode stable for 2nd FixedHash");
        check(System.identityHashCode(o) >= 0, "identityHashCode non-negative");

        String a = "lcF-divergence";
        String b = new String("lcF-divergence");
        check(a.equals(b) && a.hashCode() == b.hashCode(), "equal Strings, equal hashCode");
        check(a != b, "equal Strings are distinct objects");
        check(System.identityHashCode(a) != b.hashCode(),
              "identityHashCode differs from content hashCode [practically collision-free]");
    }

    static void testSecurityManagerRemoved() {
        // JEP 486: the Security Manager is permanently disabled; the accessor
        // still exists but always answers null. It is never installed here.
        check(System.getSecurityManager() == null, "getSecurityManager() == null [JEP 486]");
        check(System.getSecurityManager() == null, "getSecurityManager() stable null");
        check(System.getProperty("java.version").equals(System.getProperty("java.version")),
              "getProperty repeatable");
        check(System.getenv("PATH").equals(System.getenv("PATH")), "getenv repeatable");
    }

    static void testLoadNegatives() {
        // Absolute path to a library that does not exist: message names the path.
        // The path must be absolute on every platform (a leading slash alone is
        // drive-relative on Windows), otherwise the "absolute path" guard fires
        // before the library is looked up.
        String missing = new java.io.File(System.getProperty("java.io.tmpdir"),
                                          "lcF-definitely-missing-lib").getAbsolutePath();
        try {
            System.load(missing);
            throw new AssertionError("System.load(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(("Can't load library: " + missing).equals(e.getMessage()),
                  "System.load(missing) exact message [JDK27]: " + e.getMessage());
        }
        try {
            System.loadLibrary("lcFNoSuchLib");
            throw new AssertionError("System.loadLibrary(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            // JDK-27 appends the searched java.library.path list, so only the
            // stable prefix is locked.
            check(e.getMessage() != null && e.getMessage().startsWith("no lcFNoSuchLib in java.library.path"),
                  "System.loadLibrary(missing) message prefix [JDK27]");
        }
        try {
            System.load("lcF/relative.so");
            throw new AssertionError("System.load(relative): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check("Expecting an absolute path of the library: lcF/relative.so".equals(e.getMessage()),
                  "System.load(relative) exact message [JDK27]");
        }
        // The guard tests the platform's own separator, so the probe must use it.
        String separated = "a" + java.io.File.separatorChar + "b";
        try {
            System.loadLibrary(separated);
            throw new AssertionError("System.loadLibrary(separator): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            // 不锁具体措辞，只要求消息回显该库名（分隔符/文案随平台实现变化）。
            check(e.getMessage() != null && e.getMessage().contains(separated),
                  "System.loadLibrary(separator) message names the library [JDK27]: " + e.getMessage());
        }
        try {
            System.loadLibrary("");
            throw new AssertionError("System.loadLibrary(\"\"): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError e) {
            check(e.getMessage() != null && e.getMessage().startsWith("no  in java.library.path"),
                  "System.loadLibrary(empty) message prefix [JDK27]");
        }
        expectNPE(() -> System.load(null), "System.load(null)");
        expectNPE(() -> System.loadLibrary(null), "System.loadLibrary(null)");
    }

    static void testMiscSurface() {
        // The mapping is the platform's native naming convention: an optional
        // lib prefix plus the platform suffix (macOS uses .dylib).
        boolean win = java.io.File.separatorChar == '\\';
        String prefix = win ? "" : "lib";
        String suffix = win ? ".dll"
                : System.getProperty("os.name").toLowerCase().contains("mac") ? ".dylib" : ".so";
        check((prefix + "foo" + suffix).equals(System.mapLibraryName("foo")),
              "mapLibraryName platform form: " + System.mapLibraryName("foo"));
        check((prefix + "libfoo" + suffix).equals(System.mapLibraryName("libfoo")),
              "mapLibraryName applies the platform prefix once: " + System.mapLibraryName("libfoo"));
        check(System.mapLibraryName("lcF-bar") != null, "mapLibraryName non-null");
        check(System.out instanceof PrintStream, "System.out is a PrintStream");
        check(System.err instanceof PrintStream, "System.err is a PrintStream");
        check(System.in instanceof java.io.InputStream, "System.in is an InputStream");
        boolean flushed = false;
        System.out.flush();
        System.err.flush();
        flushed = true;
        check(flushed, "System.out/err flush callable");
        check(System.lineSeparator().length() >= 1, "lineSeparator non-empty");
        check(System.lineSeparator().equals(System.getProperty("line.separator")),
              "lineSeparator identity with property");
        check(System.out.toString() != null, "System.out toString non-null");
    }

    // ---- Part 2b: black-box for setIn/setOut/setProperties/load/loadLibrary/inheritedChannel ----
    static void testBlackBox() throws Exception {
        // setIn / setOut / setProperties roundtrips, always restored (finally-guarded).
        java.io.InputStream origIn = System.in;
        PrintStream origOut = System.out;
        Properties origProps = System.getProperties();
        try {
            java.io.InputStream newIn = new ByteArrayInputStream(new byte[0]);
            System.setIn(newIn);
            check(System.in == newIn, "setIn sets System.in");
            System.setIn(origIn);
            check(System.in == origIn, "setIn restored");

            PrintStream newOut = new PrintStream(new ByteArrayOutputStream());
            System.setOut(newOut);
            check(System.out == newOut, "setOut sets System.out");
            System.setOut(origOut);
            check(System.out == origOut, "setOut restored");

            Properties newProps = new Properties();
            newProps.setProperty("jck.sys.props", "hello");
            System.setProperties(newProps);
            check(System.getProperties() == newProps, "setProperties sets the properties object");
            System.setProperties(origProps);
            check(System.getProperties() == origProps, "setProperties restored");
        } finally {
            System.setIn(origIn);
            System.setOut(origOut);
            System.setProperties(origProps);
        }

        // inheritedChannel(): null or a Channel in a normal run; must not throw.
        java.nio.channels.Channel ch = System.inheritedChannel();
        check(ch == null || ch instanceof java.nio.channels.Channel,
              "inheritedChannel returns null or a Channel");

        // load / loadLibrary with a missing library -> UnsatisfiedLinkError.
        try {
            System.load("/jck/no/such/lib.so");
            throw new AssertionError("System.load(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError expected) {
            passed++;
        }
        try {
            System.loadLibrary("jck_no_such_lib_xyz");
            throw new AssertionError("System.loadLibrary(missing): expected UnsatisfiedLinkError");
        } catch (UnsatisfiedLinkError expected) {
            passed++;
        }

        // System.exit(int) terminates the JVM, so it is intentionally not invoked.
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
        testBlackBox();
        testArraycopyMatrix();
        testArraycopyExceptions();
        testArraycopyEdges();
        testTimeWeak();
        testPropertiesContract();
        testEnvContract();
        testIdentityHashCodeContract();
        testSecurityManagerRemoved();
        testLoadNegatives();
        testMiscSurface();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

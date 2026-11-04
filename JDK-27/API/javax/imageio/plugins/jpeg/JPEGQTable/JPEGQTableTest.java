/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGQTable conformance: white-box structure + black-box behavior.
 * @run main JPEGQTableTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.imageio.plugins.jpeg.JPEGQTable;

public class JPEGQTableTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGQTable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 1, "one public constructor");
        check(c.getField("K1Luminance") != null, "has K1Luminance");
        check(c.getField("K1Div2Luminance") != null, "has K1Div2Luminance");
        check(c.getField("K2Chrominance") != null, "has K2Chrominance");
        check(c.getField("K2Div2Chrominance") != null, "has K2Div2Chrominance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        JPEGQTable[] std = {
            JPEGQTable.K1Luminance,
            JPEGQTable.K1Div2Luminance,
            JPEGQTable.K2Chrominance,
            JPEGQTable.K2Div2Chrominance
        };
        for (int i = 0; i < std.length; i++) {
            JPEGQTable t = std[i];
            check(t != null, "std qtable " + i + " non-null");
            int[] table = t.getTable();
            check(table != null && table.length == 64, "qtable " + i + " has 64 elements");
            check(t.toString().startsWith("JPEGQTable"), "qtable " + i + " toString prefix");
        }

        // constructor round-trip (copies input)
        int[] arr = new int[64];
        for (int i = 0; i < 64; i++) arr[i] = i + 1;
        JPEGQTable q = new JPEGQTable(arr);
        check(Arrays.equals(arr, q.getTable()), "constructed table round-trip");
        // getTable returns a copy
        int[] got = q.getTable();
        got[0] = 999;
        check(q.getTable()[0] == 1, "getTable returns a copy");
        arr[0] = -1;
        check(q.getTable()[0] == 1, "constructor copies input array");

        // getScaledInstance scaleFactor 1.0 round-trips exactly
        JPEGQTable same = JPEGQTable.K1Luminance.getScaledInstance(1.0f, false);
        check(Arrays.equals(JPEGQTable.K1Luminance.getTable(), same.getTable()),
              "scale 1.0 preserves table");

        // scaleFactor 2.0 doubles each element (all K1 values stay under clamp 32767)
        JPEGQTable doubled = JPEGQTable.K1Luminance.getScaledInstance(2.0f, false);
        int[] base = JPEGQTable.K1Luminance.getTable();
        int[] d = doubled.getTable();
        boolean okDouble = true;
        for (int i = 0; i < 64; i++) {
            if (d[i] != base[i] * 2) okDouble = false;
        }
        check(okDouble, "scale 2.0 doubles every element");

        // forceBaseline clamps to <=255 and >=1
        JPEGQTable baseline = JPEGQTable.K1Luminance.getScaledInstance(100.0f, true);
        int[] b = baseline.getTable();
        boolean okClamp = true;
        for (int i = 0; i < 64; i++) {
            if (b[i] < 1 || b[i] > 255) okClamp = false;
        }
        check(okClamp, "forceBaseline clamps into [1,255]");

        // non-baseline clamps into [1,32767]
        JPEGQTable huge = JPEGQTable.K1Luminance.getScaledInstance(1000.0f, false);
        int[] h = huge.getTable();
        boolean okHuge = true;
        for (int i = 0; i < 64; i++) {
            if (h[i] < 1 || h[i] > 32767) okHuge = false;
        }
        check(okHuge, "non-baseline clamps into [1,32767]");

        // invalid construction
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(null), "null table");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[63]), "63-length table");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[65]), "65-length table");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

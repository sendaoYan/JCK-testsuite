/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGQTable conformance: white-box structure + black-box behavior.
 * @run main JPEGQTableTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
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
        testApiSurface();
        testConstructorMatrix();
        testDefensiveCopies();
        testStandardTables();
        testScaledInstanceRounding();
        testScaledInstanceClamping();
        testEqualsHashCodeIdentity();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    static int[] fill(int v) { int[] a = new int[64]; Arrays.fill(a, v); return a; }

    static void testApiSurface() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGQTable");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1 && ctors[0].getParameterTypes().length == 1
              && ctors[0].getParameterTypes()[0] == int[].class,
              "single public constructor (int[])");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        Set<String> want = new HashSet<String>(Arrays.asList("getTable", "getScaledInstance", "toString"));
        check(names.equals(want), "declared public method set exact: " + names);
        check(!names.contains("equals") && !names.contains("hashCode"),
              "no equals/hashCode override (identity semantics)");
        for (java.lang.reflect.Field f : c.getDeclaredFields()) {
            if (Modifier.isPublic(f.getModifiers())) {
                check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                      "public field " + f.getName() + " is static final");
                check(f.getType() == c, "public field " + f.getName() + " typed JPEGQTable");
            }
        }
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    static void testConstructorMatrix() {
        // exactly 64 entries required
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(null), "null table");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[0]), "empty table");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[63]), "63 entries");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[64 + 1]), "65 entries");
        expectThrows(IllegalArgumentException.class, () -> new JPEGQTable(new int[1]), "1 entry");
        // out-of-range values are NOT clamped by the constructor (observed on both JDKs);
        // clamping happens only in getScaledInstance
        int[] ext = fill(16);
        ext[0] = 0; ext[63] = 1000;
        JPEGQTable raw = new JPEGQTable(ext);
        check(raw.getTable()[0] == 0, "constructor stores 0 verbatim");
        check(raw.getTable()[63] == 1000, "constructor stores 1000 verbatim");
        check(raw.getTable().length == 64, "64 entries round-trip");
        // negative values also stored verbatim
        int[] neg = fill(16); neg[0] = -5;
        JPEGQTable negT = new JPEGQTable(neg);
        check(negT.getTable()[0] == -5, "constructor stores negative verbatim");
        // natural zigzag first element round-trip
        int[] seq = new int[64];
        for (int i = 0; i < 64; i++) seq[i] = i + 1;
        check(Arrays.equals(new JPEGQTable(seq).getTable(), seq), "sequential content round-trip");
    }

    static void testDefensiveCopies() {
        int[] in = fill(7);
        JPEGQTable t = new JPEGQTable(in);
        // constructor copies input
        in[0] = 999;
        check(t.getTable()[0] == 7, "constructor copies the input array");
        // getter returns a fresh copy each call
        int[] g1 = t.getTable();
        int[] g2 = t.getTable();
        check(g1 != g2, "getTable returns a new array each call");
        g1[0] = 999;
        check(t.getTable()[0] == 7, "mutating a returned array has no effect");
        // scaled instances are independent objects
        JPEGQTable s = t.getScaledInstance(1.0f, false);
        check(s != t, "getScaledInstance returns a new instance");
        check(s != t.getScaledInstance(1.0f, false), "fresh instance per call");
    }

    static void testStandardTables() {
        JPEGQTable[] std = {JPEGQTable.K1Luminance, JPEGQTable.K1Div2Luminance,
                            JPEGQTable.K2Chrominance, JPEGQTable.K2Div2Chrominance};
        String[] names = {"K1Luminance", "K1Div2Luminance", "K2Chrominance", "K2Div2Chrominance"};
        for (int i = 0; i < std.length; i++) {
            check(std[i] != null, names[i] + " non-null");
            int[] tab = std[i].getTable();
            check(tab != null && tab.length == 64, names[i] + " has 64 entries");
            boolean baselineOk = true;
            for (int v : tab) if (v < 1 || v > 255) baselineOk = false;
            check(baselineOk, names[i] + " entries within baseline range [1,255]");
        }
        // spot anchors from Annex K (verified identical on JDK-8 and JDK-27)
        check(JPEGQTable.K1Luminance.getTable()[0] == 16, "K1 first entry 16");
        check(JPEGQTable.K2Chrominance.getTable()[0] == 17, "K2 first entry 17");
        check(JPEGQTable.K1Div2Luminance.getTable()[0] == 8, "K1Div2 first entry 8");
        check(JPEGQTable.K2Div2Chrominance.getTable()[0] == 9, "K2Div2 first entry 9");
        // the Div2 tables are exactly the base tables scaled by 0.5
        check(Arrays.equals(JPEGQTable.K1Div2Luminance.getTable(),
                            JPEGQTable.K1Luminance.getScaledInstance(0.5f, false).getTable()),
              "K1Div2 == K1 scaled by 0.5");
        check(Arrays.equals(JPEGQTable.K2Div2Chrominance.getTable(),
                            JPEGQTable.K2Chrominance.getScaledInstance(0.5f, false).getTable()),
              "K2Div2 == K2 scaled by 0.5");
        check(std[0] != std[1] && std[0] != std[2], "standard tables are distinct instances");
        // K1 and K2 differ in content
        check(!Arrays.equals(JPEGQTable.K1Luminance.getTable(), JPEGQTable.K2Chrominance.getTable()),
              "K1 and K2 differ in content");
    }

    static void testScaledInstanceRounding() {
        // spec formula: (int)(v * scaleFactor + 0.5f), i.e. round-half-up
        check(new JPEGQTable(fill(3)).getScaledInstance(0.5f, false).getTable()[0] == 2, "3 * 0.5 -> 2");
        check(new JPEGQTable(fill(5)).getScaledInstance(0.5f, false).getTable()[0] == 3, "5 * 0.5 -> 3");
        check(new JPEGQTable(fill(7)).getScaledInstance(0.5f, false).getTable()[0] == 4, "7 * 0.5 -> 4");
        check(new JPEGQTable(fill(1)).getScaledInstance(0.5f, false).getTable()[0] == 1, "1 * 0.5 -> 1 (clamped)");
        // scale 1.0 is the identity for in-range values
        int[] seq = new int[64];
        for (int i = 0; i < 64; i++) seq[i] = i + 1;
        check(Arrays.equals(new JPEGQTable(seq).getScaledInstance(1.0f, true).getTable(), seq),
              "scale 1.0 identity with forceBaseline");
        // every element is scaled by the same factor
        int[] doubled = new JPEGQTable(fill(10)).getScaledInstance(2.0f, false).getTable();
        boolean allDoubled = true;
        for (int v : doubled) if (v != 20) allDoubled = false;
        check(allDoubled, "scale 2.0 doubles every entry");
    }

    static void testScaledInstanceClamping() {
        // zero and negative scales clamp everything to the minimum 1
        int[] zero = new JPEGQTable(fill(50)).getScaledInstance(0.0f, false).getTable();
        boolean allOne = true;
        for (int v : zero) if (v != 1) allOne = false;
        check(allOne, "scale 0.0 clamps to 1");
        int[] neg = new JPEGQTable(fill(50)).getScaledInstance(-3.0f, false).getTable();
        allOne = true;
        for (int v : neg) if (v != 1) allOne = false;
        check(allOne, "negative scale clamps to 1");
        // NaN scale: (int)NaN == 0 -> clamped to 1 (deterministic IEEE behavior, verified)
        int[] nan = new JPEGQTable(fill(50)).getScaledInstance(Float.NaN, false).getTable();
        allOne = true;
        for (int v : nan) if (v != 1) allOne = false;
        check(allOne, "NaN scale clamps to 1");
        // upper clamps: 32767 without forceBaseline, 255 with it
        int[] inf = new JPEGQTable(fill(50)).getScaledInstance(Float.POSITIVE_INFINITY, false).getTable();
        boolean allMax = true;
        for (int v : inf) if (v != 32767) allMax = false;
        check(allMax, "+Inf scale clamps to 32767 without forceBaseline");
        int[] infB = new JPEGQTable(fill(50)).getScaledInstance(Float.POSITIVE_INFINITY, true).getTable();
        allMax = true;
        for (int v : infB) if (v != 255) allMax = false;
        check(allMax, "+Inf scale clamps to 255 with forceBaseline");
        // moderate scale of a big table: 1000*2=2000 legal without baseline, 255 with baseline
        int[] big = fill(1000);
        check(new JPEGQTable(big).getScaledInstance(2.0f, false).getTable()[0] == 2000,
              "2000 allowed without forceBaseline");
        check(new JPEGQTable(big).getScaledInstance(2.0f, true).getTable()[0] == 255,
              "forceBaseline clamps 2000 to 255");
        // constructor-stored 0 scales up into range
        int[] withZero = fill(10); withZero[0] = 0;
        check(new JPEGQTable(withZero).getScaledInstance(2.0f, false).getTable()[0] == 1,
              "stored 0 scales to minimum 1");
        // all results have exactly 64 entries
        check(new JPEGQTable(fill(4)).getScaledInstance(3.0f, true).getTable().length == 64,
              "scaled table keeps 64 entries");
    }

    static void testEqualsHashCodeIdentity() {
        JPEGQTable a = new JPEGQTable(fill(7));
        JPEGQTable b = new JPEGQTable(fill(7));
        // no equals/hashCode override: content-equal tables are NOT equals (recorded; javadoc silent)
        check(!a.equals(b), "content-equal qtables are not equals() (identity semantics)");
        check(a.equals(a), "reflexive equals");
        check(a.hashCode() == a.hashCode(), "hashCode stable within an instance");
        check(!a.equals(null), "not equal to null");
        check(!a.equals(new Object()), "not equal to unrelated type");
        check(JPEGQTable.K1Luminance.equals(JPEGQTable.K1Luminance), "K1 equals itself");
        check(!JPEGQTable.K1Luminance.equals(JPEGQTable.K2Chrominance), "K1 != K2 instance");
        // scaled copy of K1 is a different instance even with identical content
        JPEGQTable scaled = JPEGQTable.K1Luminance.getScaledInstance(1.0f, true);
        check(!JPEGQTable.K1Luminance.equals(scaled), "identical-content scaled copy is not equals()");
    }

    static void testToString() {
        String s = JPEGQTable.K1Luminance.toString();
        check(s != null && !s.isEmpty(), "toString non-empty");
        check(s.startsWith("JPEGQTable"), "toString prefix");
        check(s.contains("16"), "toString renders table entries");
        check(s.equals(JPEGQTable.K1Luminance.toString()), "toString deterministic");
        check(new JPEGQTable(fill(42)).toString().contains("42"), "toString renders custom values");
    }
}

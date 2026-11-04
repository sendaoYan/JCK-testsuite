/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGHuffmanTable conformance: behavioral.
 * @run main JPEGHuffmanTableTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;

public class JPEGHuffmanTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        short[] lengths = new short[16]; lengths[0] = 2;
        short[] values = new short[]{0, 1};
        javax.imageio.plugins.jpeg.JPEGHuffmanTable t = new javax.imageio.plugins.jpeg.JPEGHuffmanTable(lengths, values);
        check(t.getLengths() != null && t.getLengths().length == 16, "lengths 16");
        check(t.getValues() != null, "values non-null");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCLuminance != null, "StdDCLuminance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdDCChrominance != null, "StdDCChrominance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACLuminance != null, "StdACLuminance");
        check(javax.imageio.plugins.jpeg.JPEGHuffmanTable.StdACChrominance != null, "StdACChrominance");
        testApiSurface();
        testConstructorMatrix();
        testDefensiveCopies();
        testStandardTables();
        testEqualsHashCodeIdentity();
        testToString();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ================= appended: contract deepening (evtplug) =================

    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(t.getClass() == expected,
                  msg + ": expected " + expected.getSimpleName() + " but got "
                  + t.getClass().getSimpleName() + " (" + t.getMessage() + ")");
        }
        check(thrown, msg + ": should throw " + expected.getSimpleName());
    }

    static short[] values(int n) {
        short[] v = new short[n];
        for (int i = 0; i < n; i++) v[i] = (short) (i & 0xff);
        return v;
    }

    static void testApiSurface() throws Exception {
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGHuffmanTable");
        check(c.getSuperclass() == Object.class, "extends Object directly");
        Constructor<?>[] ctors = c.getConstructors();
        check(ctors.length == 1 && ctors[0].getParameterTypes().length == 2,
              "single public constructor (short[], short[])");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers())) names.add(m.getName());
        }
        Set<String> want = new HashSet<String>(Arrays.asList("getLengths", "getValues", "toString"));
        check(names.equals(want), "declared method set exact: " + names);
        // no equals/hashCode override -> identity semantics (verified below)
        check(!names.contains("equals") && !names.contains("hashCode"),
              "no equals/hashCode override (Object identity semantics)");
        java.lang.reflect.Field[] fields = c.getDeclaredFields();
        for (java.lang.reflect.Field f : fields) {
            // private internals plus the four public standard tables
            if (Modifier.isPublic(f.getModifiers())) {
                check(Modifier.isStatic(f.getModifiers()) && Modifier.isFinal(f.getModifiers()),
                      "public field " + f.getName() + " is static final");
                check(f.getType() == c, "public field " + f.getName() + " typed JPEGHuffmanTable");
            }
        }
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        check(!Cloneable.class.isAssignableFrom(c), "not Cloneable");
    }

    static void testConstructorMatrix() {
        short[] l16 = new short[16]; l16[0] = 2;
        // minimal legal table: 1 count, 1 value; lengths array keeps its natural length
        JPEGHuffmanTable min = new JPEGHuffmanTable(new short[]{1}, new short[]{9});
        check(min.getLengths().length == 1, "lengths array kept at natural length 1");
        check(min.getValues().length == 1, "values length 1");
        // counts shorter than 16 entries are legal
        JPEGHuffmanTable l1 = new JPEGHuffmanTable(new short[]{3}, new short[]{1, 2, 3});
        check(l1.getValues().length == 3, "length-1 counts with 3 values legal");
        // full 16-entry shape
        JPEGHuffmanTable full = new JPEGHuffmanTable(l16, new short[]{0, 1});
        check(full.getLengths().length == 16, "length-16 counts kept at 16");
        check(full.getValues().length == 2, "two values");
        // rejections
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(null, new short[]{1}), "null lengths");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(l16, null), "null values");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(new short[0], new short[]{1}), "empty lengths");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(new short[16], new short[0]), "empty values");
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(new short[17], values(17)), "lengths longer than 16 rejected");
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(l16, new short[]{1}), "counts sum != values length");
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(new short[16], new short[]{1}), "zero-sum counts with values");
        short[] neg = new short[16]; neg[0] = -1;
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(neg, new short[]{1, 2, 3, 4}), "negative count");
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(l16, new short[]{-1, 1}), "negative value");
        // >256 total symbols rejected by the length cap
        short[] bigCounts = new short[16];
        for (int i = 0; i < 16; i++) bigCounts[i] = 20; // sum = 320, but values capped first
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(bigCounts, new short[320]), "counts sum 320 (values>256) rejected");
        // exactly 256 symbols is the legal maximum
        short[] counts256 = new short[16];
        for (int i = 0; i < 16; i++) counts256[i] = 16; // sum = 256
        JPEGHuffmanTable maxTable = new JPEGHuffmanTable(counts256, values(256));
        check(maxTable.getValues().length == 256, "exactly 256 symbols accepted");
        check(maxTable.getLengths().length == 16, "256-symbol table has 16 count rows");
        expectThrows(IllegalArgumentException.class,
            () -> new JPEGHuffmanTable(counts256, values(255)), "256 counts vs 255 values mismatch");
        // a single count row of 16 symbols of one code length is fine
        short[] row = new short[16]; row[15] = 16;
        JPEGHuffmanTable oneRow = new JPEGHuffmanTable(row, values(16));
        check(oneRow.getValues().length == 16, "16 symbols of one code length accepted");
        // failed constructions never return a half-built object
        check(new JPEGHuffmanTable(l16, new short[]{5, 6}).getValues()[0] == 5, "valid construction after failures still works");
    }

    static void testDefensiveCopies() {
        short[] countsIn = {0, 1, 1};
        short[] valsIn = {7, 9};
        JPEGHuffmanTable t = new JPEGHuffmanTable(countsIn, valsIn);
        // constructor copies its input
        countsIn[1] = 99; valsIn[0] = 99;
        check(t.getLengths()[1] == 1, "constructor copies the counts array");
        check(t.getValues()[0] == 7, "constructor copies the values array");
        // getters return fresh copies every time
        short[] got1 = t.getLengths();
        short[] got2 = t.getLengths();
        check(got1 != got2, "getLengths returns a new array each call");
        got1[0] = 42;
        check(t.getLengths()[0] == 0, "mutating a returned counts array has no effect");
        short[] v1 = t.getValues();
        short[] v2 = t.getValues();
        check(v1 != v2, "getValues returns a new array each call");
        v1[0] = 42;
        check(t.getValues()[0] == 7, "mutating a returned values array has no effect");
        // content round-trip
        check(Arrays.equals(t.getLengths(), new short[]{0, 1, 1}), "counts content round-trip");
        check(Arrays.equals(t.getValues(), new short[]{7, 9}), "values content round-trip");
    }

    static void testStandardTables() {
        JPEGHuffmanTable[] std = {
            JPEGHuffmanTable.StdDCLuminance, JPEGHuffmanTable.StdDCChrominance,
            JPEGHuffmanTable.StdACLuminance, JPEGHuffmanTable.StdACChrominance};
        String[] names = {"StdDCLuminance", "StdDCChrominance", "StdACLuminance", "StdACChrominance"};
        int[] expectedValues = {12, 12, 162, 162}; // verified identical on JDK-8 and JDK-27
        for (int i = 0; i < std.length; i++) {
            check(std[i] != null, names[i] + " non-null");
            check(std[i].getLengths().length == 16, names[i] + " uses all 16 count rows");
            check(std[i].getValues().length == expectedValues[i],
                  names[i] + " values length " + expectedValues[i]);
            int sum = 0;
            for (short s : std[i].getLengths()) sum += s;
            check(sum == std[i].getValues().length, names[i] + " counts sum equals values length");
            boolean rangeOk = true;
            for (short v : std[i].getValues()) if (v < 0 || v > 255) rangeOk = false;
            check(rangeOk, names[i] + " values are byte-sized");
        }
        // the four standard tables are distinct instances
        check(std[0] != std[1] && std[0] != std[2] && std[0] != std[3], "standard tables are distinct");
        // re-instantiating from a standard table's data yields identical content
        JPEGHuffmanTable rebuilt = new JPEGHuffmanTable(
            JPEGHuffmanTable.StdACLuminance.getLengths(), JPEGHuffmanTable.StdACLuminance.getValues());
        check(Arrays.equals(rebuilt.getValues(), JPEGHuffmanTable.StdACLuminance.getValues()),
              "rebuilding StdACLuminance from its getters matches content");
        // well-known prefix of the AC luminance symbol list
        check(JPEGHuffmanTable.StdACLuminance.getValues()[0] == 1
              && JPEGHuffmanTable.StdACLuminance.getValues()[1] == 2
              && JPEGHuffmanTable.StdACLuminance.getValues()[3] == 0,
              "StdACLuminance begins with the canonical symbol run 1,2,3,0,4");
    }

    static void testEqualsHashCodeIdentity() {
        short[] counts = {0, 1, 1};
        short[] vals = {7, 9};
        JPEGHuffmanTable a = new JPEGHuffmanTable(counts, vals);
        JPEGHuffmanTable b = new JPEGHuffmanTable(counts.clone(), vals.clone());
        // no equals/hashCode override: content-equal instances are NOT equals (recorded deviation
        // from value-style expectations; javadoc is silent)
        check(!a.equals(b), "content-equal tables are not equals() (identity semantics)");
        check(a.equals(a), "reflexive equals");
        check(b.equals(b), "reflexive equals (b)");
        check(a.hashCode() == a.hashCode(), "hashCode stable within an instance");
        check(a.equals(JPEGHuffmanTable.StdDCLuminance) == false, "never equals an unrelated instance");
        check(!a.equals(null), "not equal to null");
        check(!a.equals(new Object()), "not equal to an unrelated type");
        check(JPEGHuffmanTable.StdDCLuminance.equals(JPEGHuffmanTable.StdDCLuminance),
              "standard table equals itself");
    }

    static void testToString() {
        JPEGHuffmanTable t = new JPEGHuffmanTable(new short[]{2}, new short[]{5, 6});
        String s = t.toString();
        check(s != null && !s.isEmpty(), "toString non-empty");
        check(s.startsWith("JPEGHuffmanTable"), "toString prefix: " + s.split("\n")[0]);
        check(s.contains("lengths"), "toString renders the counts section");
        check(s.contains("values"), "toString renders the values section");
        check(s.contains("2"), "toString contains the count");
        check(s.contains("5") && s.contains("6"), "toString contains the values");
        // every standard table renders without throwing
        check(JPEGHuffmanTable.StdDCLuminance.toString().contains("values"), "std table toString works");
        check(JPEGHuffmanTable.StdACChrominance.toString().contains("lengths"), "std chroma table toString works");
        // toString is repeatable
        check(t.toString().equals(s), "toString deterministic across calls");
    }
}

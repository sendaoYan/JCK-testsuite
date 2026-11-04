/*
 * @test
 * @summary javax.imageio.plugins.jpeg.JPEGHuffmanTable conformance: white-box structure + black-box behavior.
 * @run main JPEGHuffmanTableTest
 */

import java.lang.reflect.Modifier;
import java.util.Arrays;
import javax.imageio.plugins.jpeg.JPEGHuffmanTable;

public class JPEGHuffmanTableTest {
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
        Class<?> c = Class.forName("javax.imageio.plugins.jpeg.JPEGHuffmanTable");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface() == false, "is interface");
        check(c.isAnnotation() == false, "is annotation");
        check(c.isEnum() == false, "is enum");
        check(Modifier.isPublic(mods), "public modifier");
        check(Modifier.isAbstract(mods) == false, "not abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getConstructors().length == 1, "one public constructor");
        check(c.getField("StdDCLuminance") != null, "has StdDCLuminance");
        check(c.getField("StdDCChrominance") != null, "has StdDCChrominance");
        check(c.getField("StdACLuminance") != null, "has StdACLuminance");
        check(c.getField("StdACChrominance") != null, "has StdACChrominance");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // built-in standard tables are non-null and self-consistent
        JPEGHuffmanTable[] std = {
            JPEGHuffmanTable.StdDCLuminance,
            JPEGHuffmanTable.StdDCChrominance,
            JPEGHuffmanTable.StdACLuminance,
            JPEGHuffmanTable.StdACChrominance
        };
        for (int i = 0; i < std.length; i++) {
            JPEGHuffmanTable t = std[i];
            check(t != null, "std table " + i + " non-null");
            short[] lengths = t.getLengths();
            short[] values = t.getValues();
            check(lengths != null && lengths.length <= 16, "lengths <=16 for table " + i);
            check(values != null && values.length <= 256, "values <=256 for table " + i);
            int sum = 0;
            for (short s : lengths) sum += s;
            check(sum == values.length, "sum(lengths)==values.length for table " + i);
            check(t.toString().startsWith("JPEGHuffmanTable"), "toString prefix for table " + i);
        }

        // DC luminance / AC luminance specific sizes
        check(JPEGHuffmanTable.StdDCLuminance.getLengths().length == 16, "DC luma lengths 16");
        check(JPEGHuffmanTable.StdDCLuminance.getValues().length == 12, "DC luma values 12");
        check(JPEGHuffmanTable.StdACLuminance.getLengths().length == 16, "AC luma lengths 16");
        check(JPEGHuffmanTable.StdACLuminance.getValues().length == 162, "AC luma values 162");

        // constructor round-trip copies input
        short[] lengths = JPEGHuffmanTable.StdDCLuminance.getLengths();
        short[] values = JPEGHuffmanTable.StdDCLuminance.getValues();
        JPEGHuffmanTable t = new JPEGHuffmanTable(lengths, values);
        check(Arrays.equals(lengths, t.getLengths()), "constructed lengths round-trip");
        check(Arrays.equals(values, t.getValues()), "constructed values round-trip");
        // getters return copies (mutating caller array does not affect table)
        short[] gotLengths = t.getLengths();
        gotLengths[0] = 99;
        check(t.getLengths()[0] != 99, "getLengths returns a copy");
        short[] gotValues = t.getValues();
        gotValues[0] = 99;
        check(t.getValues()[0] != 99, "getValues returns a copy");

        // toString contains both sections
        String s = t.toString();
        check(s.contains("lengths:") && s.contains("values:"), "toString contains lengths and values");

        // invalid constructions
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(null, new short[]{1}), "null lengths");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(new short[]{1}, null), "null values");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(new short[0], new short[]{1}), "empty lengths");
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(new short[]{2}, new short[]{1}), "sum mismatch");
        short[] negLengths = new short[16];
        negLengths[0] = -1;
        expectThrows(IllegalArgumentException.class, () -> new JPEGHuffmanTable(negLengths, new short[]{1}), "negative length");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.image.LookupTable conformance: white-box structure plus black-box behavior.
 * @run main LookupTableTest
 */

import java.awt.image.LookupTable;
import java.lang.reflect.*;

public class LookupTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.LookupTable");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "public abstract class");
        check(hasMethod(c, "getNumComponents"), "getNumComponents()");
        check(hasMethod(c, "getOffset"), "getOffset()");
        check(hasMethod(c, "lookupPixel", int[].class, int[].class), "lookupPixel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // identity lookup over 3 components, offset 0
        LookupTable table = new LookupTable(0, 3) {
            public int[] lookupPixel(int[] src, int[] dst) {
                if (dst == null) dst = new int[src.length];
                System.arraycopy(src, 0, dst, 0, src.length);
                return dst;
            }
        };
        check(table.getNumComponents() == 3, "getNumComponents 3");
        check(table.getOffset() == 0, "getOffset 0");
        int[] out = table.lookupPixel(new int[]{1, 2, 3}, null);
        check(out[0] == 1 && out[2] == 3, "identity lookupPixel");

        LookupTable offsetTable = new LookupTable(5, 2) {
            public int[] lookupPixel(int[] src, int[] dst) {
                if (dst == null) dst = new int[src.length];
                for (int i = 0; i < src.length; i++) dst[i] = src[i] + 5;
                return dst;
            }
        };
        check(offsetTable.getOffset() == 5 && offsetTable.getNumComponents() == 2, "offset/numComponents");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

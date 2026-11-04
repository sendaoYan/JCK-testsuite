/*
 * @test
 * @summary java.awt.image.LookupTable conformance: white-box structure plus black-box behavior.
 * @run main LookupTableTest
 */

import java.awt.image.LookupTable;
import java.lang.reflect.*;
import java.awt.image.ByteLookupTable;
import java.awt.image.ShortLookupTable;

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
        passed += LookupTableDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class LookupTableDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.LookupTable");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getNumComponents"), "method getNumComponents");
        check(hasMethod(c, "getOffset"), "method getOffset");
        check(hasMethod(c, "lookupPixel"), "method lookupPixel");
        // Concrete subclasses in the platform.
        check(ByteLookupTable.class.getSuperclass() == LookupTable.class, "ByteLookupTable extends LookupTable");
        check(ShortLookupTable.class.getSuperclass() == LookupTable.class, "ShortLookupTable extends LookupTable");
    }

    static void testAccessorsThroughSubclasses() {
        // numComponents/offset are set by the concrete constructors.
        ByteLookupTable b1 = new ByteLookupTable(0, new byte[256]);
        check(b1.getNumComponents() == 1, "byte single array -> 1 component");
        check(b1.getOffset() == 0, "offset 0 recorded");
        ByteLookupTable b2 = new ByteLookupTable(10, new byte[][] { new byte[246], new byte[246] });
        check(b2.getNumComponents() == 2, "byte 2d array -> 2 components");
        check(b2.getOffset() == 10, "offset 10 recorded");
        ShortLookupTable s1 = new ShortLookupTable(0, new short[256]);
        check(s1.getNumComponents() == 1, "short single array -> 1 component");
        ShortLookupTable s2 = new ShortLookupTable(5, new short[][] { new short[4], new short[4], new short[4] });
        check(s2.getNumComponents() == 3, "short 3-row array -> 3 components");
        check(s2.getOffset() == 5, "short offset recorded");
        // Non-positive offsets are rejected (javadoc: offset must be > 0 in the message, 0 allowed).
        boolean threw = false;
        try { new ByteLookupTable(-1, new byte[256]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "negative offset -> IAE");
        threw = false;
        try { new ShortLookupTable(-1, new short[256]); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "short negative offset -> IAE");
    }

    static void testAbstractLookupPixel() {
        // The abstract lookupPixel(int[],int[]) is implemented by both subclasses.
        ByteLookupTable b = new ByteLookupTable(0, new byte[] { 1, 2, 3, 4 });
        int[] out = b.lookupPixel(new int[] { 3 }, null);
        check(out[0] == 4, "byte lookupPixel(int[]) routes to table");
        ShortLookupTable s = new ShortLookupTable(0, new short[] { 10, 20, 30 });
        int[] out2 = s.lookupPixel(new int[] { 2 }, null);
        check(out2[0] == 30, "short lookupPixel(int[]) routes to table");
    }



    static int run() throws Exception {
        testWhiteBox();
        testAccessorsThroughSubclasses();
        testAbstractLookupPixel();
        return passed;
    }
}

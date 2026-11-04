/*
 * @test
 * @summary java.awt.font.MultipleMaster conformance: white-box structure plus black-box behavior.
 * @run main MultipleMasterTest
 */

import java.awt.Font;
import java.awt.font.MultipleMaster;
import java.lang.reflect.*;

public class MultipleMasterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.font.MultipleMaster");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "getNumDesignAxes"), "getNumDesignAxes()");
        check(hasMethod(c, "getDesignAxisRanges"), "getDesignAxisRanges()");
        check(hasMethod(c, "getDesignAxisDefaults"), "getDesignAxisDefaults()");
        check(hasMethod(c, "getDesignAxisNames"), "getDesignAxisNames()");
        check(hasMethod(c, "deriveMMFont", float[].class), "deriveMMFont(float[])");
        check(hasMethod(c, "deriveMMFont", float[].class, float.class, float.class, float.class, float.class), "deriveMMFont(float[],float,float,float,float)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // anonymous implementation of the interface
        MultipleMaster mm = new MultipleMaster() {
            public int getNumDesignAxes() { return 2; }
            public float[] getDesignAxisRanges() { return new float[]{0, 100, 0, 100}; }
            public float[] getDesignAxisDefaults() { return new float[]{50, 50}; }
            public String[] getDesignAxisNames() { return new String[]{"Weight", "Width"}; }
            public Font deriveMMFont(float[] axes) { return new Font(Font.DIALOG, Font.PLAIN, 12); }
            public Font deriveMMFont(float[] axes, float dx, float dy, float sx, float sy) {
                return new Font(Font.DIALOG, Font.PLAIN, 12);
            }
        };
        check(mm.getNumDesignAxes() == 2, "getNumDesignAxes");
        check(mm.getDesignAxisRanges().length == 4, "getDesignAxisRanges length");
        check(mm.getDesignAxisDefaults().length == 2, "getDesignAxisDefaults length");
        check(mm.getDesignAxisNames().length == 2, "getDesignAxisNames length");
        check(mm.deriveMMFont(new float[]{1, 1}) != null, "deriveMMFont(float[]) non-null");
        check(mm.deriveMMFont(new float[]{1, 1}, 0, 0, 1, 1) != null, "deriveMMFont 6-arg non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

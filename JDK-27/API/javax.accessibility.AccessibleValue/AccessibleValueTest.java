/*
 * @test
 * @summary javax.accessibility.AccessibleValue conformance: white-box structure + black-box behavior.
 * @run main AccessibleValueTest
 */

import java.lang.reflect.*;

public class AccessibleValueTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleValue");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getCurrentAccessibleValue"), "declares getCurrentAccessibleValue");
        check(hasMethod(c, "setCurrentAccessibleValue"), "declares setCurrentAccessibleValue");
        check(hasMethod(c, "getMinimumAccessibleValue"), "declares getMinimumAccessibleValue");
        check(hasMethod(c, "getMaximumAccessibleValue"), "declares getMaximumAccessibleValue");
    }

    static void testBlackBox() throws Exception {
        javax.accessibility.AccessibleValue v = new javax.accessibility.AccessibleValue() {
            Number cur = Integer.valueOf(0);
            public Number getCurrentAccessibleValue() { return cur; }
            public boolean setCurrentAccessibleValue(Number n) { cur = n; return true; }
            public Number getMinimumAccessibleValue() { return Integer.valueOf(0); }
            public Number getMaximumAccessibleValue() { return Integer.valueOf(100); }
        };
        check(Integer.valueOf(0).equals(v.getCurrentAccessibleValue()), "initial current 0");
        check(Integer.valueOf(0).equals(v.getMinimumAccessibleValue()), "minimum 0");
        check(Integer.valueOf(100).equals(v.getMaximumAccessibleValue()), "maximum 100");
        check(v.setCurrentAccessibleValue(Integer.valueOf(42)), "setCurrent returns true");
        check(Integer.valueOf(42).equals(v.getCurrentAccessibleValue()), "current after set 42");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.accessibility.AccessibleKeyBinding conformance: white-box structure + black-box behavior.
 * @run main AccessibleKeyBindingTest
 */

import java.lang.reflect.*;

public class AccessibleKeyBindingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleKeyBinding");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleKeyBindingCount"), "declares getAccessibleKeyBindingCount");
        check(hasMethod(c, "getAccessibleKeyBinding"), "declares getAccessibleKeyBinding");
    }

    static void testBlackBox() throws Exception {
        final Object[] bindings = new Object[]{"F1", "Ctrl+C"};
        javax.accessibility.AccessibleKeyBinding kb = new javax.accessibility.AccessibleKeyBinding() {
            public int getAccessibleKeyBindingCount() { return bindings.length; }
            public Object getAccessibleKeyBinding(int i) { return bindings[i]; }
        };
        check(kb.getAccessibleKeyBindingCount() == 2, "binding count");
        check("F1".equals(kb.getAccessibleKeyBinding(0)), "binding[0]");
        check("Ctrl+C".equals(kb.getAccessibleKeyBinding(1)), "binding[1]");

        javax.accessibility.AccessibleKeyBinding empty = new javax.accessibility.AccessibleKeyBinding() {
            public int getAccessibleKeyBindingCount() { return 0; }
            public Object getAccessibleKeyBinding(int i) { return null; }
        };
        check(empty.getAccessibleKeyBindingCount() == 0, "empty binding count");
        check(empty.getAccessibleKeyBinding(0) == null, "empty binding returns null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

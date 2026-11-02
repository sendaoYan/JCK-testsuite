/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedComponent conformance: interface.
 * @run main AccessibleExtendedComponentTest
 */

import java.lang.reflect.*;


public class AccessibleExtendedComponentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedComponent");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleComponent"), "extends javax.accessibility.AccessibleComponent");
        check(hasMethod(c, "getToolTipText"), "declares getToolTipText");
        check(hasMethod(c, "getTitledBorderText"), "declares getTitledBorderText");
        check(hasMethod(c, "getAccessibleKeyBinding"), "declares getAccessibleKeyBinding");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

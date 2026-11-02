/*
 * @test
 * @summary javax.accessibility.AccessibleExtendedTable conformance: interface.
 * @run main AccessibleExtendedTableTest
 */

import java.lang.reflect.*;


public class AccessibleExtendedTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleExtendedTable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleTable"), "extends javax.accessibility.AccessibleTable");
        check(hasMethod(c, "getAccessibleRow"), "declares getAccessibleRow");
        check(hasMethod(c, "getAccessibleColumn"), "declares getAccessibleColumn");
        check(hasMethod(c, "getAccessibleIndex"), "declares getAccessibleIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

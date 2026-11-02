/*
 * @test
 * @summary javax.accessibility.AccessibleTable conformance: interface.
 * @run main AccessibleTableTest
 */

import java.lang.reflect.*;


public class AccessibleTableTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleTable");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleCaption"), "declares getAccessibleCaption");
        check(hasMethod(c, "setAccessibleCaption"), "declares setAccessibleCaption");
        check(hasMethod(c, "getAccessibleSummary"), "declares getAccessibleSummary");
        check(hasMethod(c, "setAccessibleSummary"), "declares setAccessibleSummary");
        check(hasMethod(c, "getAccessibleRowCount"), "declares getAccessibleRowCount");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

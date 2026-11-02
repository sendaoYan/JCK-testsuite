/*
 * @test
 * @summary javax.accessibility.AccessibleSelection conformance: interface.
 * @run main AccessibleSelectionTest
 */

import java.lang.reflect.*;


public class AccessibleSelectionTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleSelection");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleSelectionCount"), "declares getAccessibleSelectionCount");
        check(hasMethod(c, "getAccessibleSelection"), "declares getAccessibleSelection");
        check(hasMethod(c, "isAccessibleChildSelected"), "declares isAccessibleChildSelected");
        check(hasMethod(c, "addAccessibleSelection"), "declares addAccessibleSelection");
        check(hasMethod(c, "removeAccessibleSelection"), "declares removeAccessibleSelection");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

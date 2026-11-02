/*
 * @test
 * @summary javax.accessibility.AccessibleAction conformance: interface.
 * @run main AccessibleActionTest
 */

import java.lang.reflect.*;


public class AccessibleActionTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleAction");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "getAccessibleActionCount"), "declares getAccessibleActionCount");
        check(hasMethod(c, "getAccessibleActionDescription"), "declares getAccessibleActionDescription");
        check(hasMethod(c, "doAccessibleAction"), "declares doAccessibleAction");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

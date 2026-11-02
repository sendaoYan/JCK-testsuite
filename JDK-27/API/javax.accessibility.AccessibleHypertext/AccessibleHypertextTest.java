/*
 * @test
 * @summary javax.accessibility.AccessibleHypertext conformance: interface.
 * @run main AccessibleHypertextTest
 */

import java.lang.reflect.*;


public class AccessibleHypertextTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleHypertext");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 1, "1 superinterface(s)");
        check(hasInterface(c, "javax.accessibility.AccessibleText"), "extends javax.accessibility.AccessibleText");
        check(hasMethod(c, "getLinkCount"), "declares getLinkCount");
        check(hasMethod(c, "getLink"), "declares getLink");
        check(hasMethod(c, "getLinkIndex"), "declares getLinkIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

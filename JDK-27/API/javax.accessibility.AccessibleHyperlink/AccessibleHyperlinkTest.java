/*
 * @test
 * @summary javax.accessibility.AccessibleHyperlink conformance: abstract class.
 * @run main AccessibleHyperlinkTest
 */

import java.lang.reflect.*;


public class AccessibleHyperlinkTest {
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
        Class<?> c = Class.forName("javax.accessibility.AccessibleHyperlink");
        check(Modifier.isAbstract(c.getModifiers()), "isAbstract");
        check(!c.isInterface(), "not interface");
        check(c.getSuperclass() == java.lang.Object.class, "extends Object");
        check(c.getInterfaces().length == 1, "1 interface(s)");
        check(hasMethod(c, "isValid"), "declares isValid");
        check(hasMethod(c, "getAccessibleActionCount"), "declares getAccessibleActionCount");
        check(hasMethod(c, "doAccessibleAction"), "declares doAccessibleAction");
        check(hasMethod(c, "getAccessibleActionDescription"), "declares getAccessibleActionDescription");
        check(hasMethod(c, "getAccessibleActionObject"), "declares getAccessibleActionObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

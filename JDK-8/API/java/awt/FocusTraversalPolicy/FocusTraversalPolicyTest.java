/*
 * @test
 * @summary java.awt.FocusTraversalPolicy conformance: abstract class reflection.
 * @run main FocusTraversalPolicyTest
 */

public class FocusTraversalPolicyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.FocusTraversalPolicy");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getComponentAfter"), "method getComponentAfter");
        check(hasMethod(c, "getComponentBefore"), "method getComponentBefore");
        check(hasMethod(c, "getFirstComponent"), "method getFirstComponent");
        check(hasMethod(c, "getLastComponent"), "method getLastComponent");
        check(hasMethod(c, "getDefaultComponent"), "method getDefaultComponent");
        check(hasMethod(c, "getInitialComponent"), "method getInitialComponent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

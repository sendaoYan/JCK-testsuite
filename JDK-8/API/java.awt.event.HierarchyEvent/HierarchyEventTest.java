/*
 * @test
 * @summary java.awt.event.HierarchyEvent conformance: class reflection.
 * @run main HierarchyEventTest
 */

public class HierarchyEventTest {
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
        Class<?> c = Class.forName("java.awt.event.HierarchyEvent");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.AWTEvent"), "superclass java.awt.AWTEvent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getComponent"), "method getComponent");
        check(hasMethod(c, "getChanged"), "method getChanged");
        check(hasMethod(c, "getChangedParent"), "method getChangedParent");
        check(hasMethod(c, "getChangeFlags"), "method getChangeFlags");
        check(hasMethod(c, "paramString"), "method paramString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

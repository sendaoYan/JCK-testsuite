/*
 * @test
 * @summary java.awt.Label.AccessibleAWTLabel conformance: class reflection.
 * @run main AccessibleAWTLabelTest
 */

public class AccessibleAWTLabelTest {
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
        Class<?> c = Class.forName("java.awt.Label$AccessibleAWTLabel");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleName"), "method getAccessibleName");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

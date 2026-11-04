/*
 * @test
 * @summary java.awt.Container.AccessibleAWTContainer conformance: class reflection.
 * @run main AccessibleAWTContainerTest
 */

public class AccessibleAWTContainerTest {
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
        Class<?> c = Class.forName("java.awt.Container$AccessibleAWTContainer");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Component$AccessibleAWTComponent"), "superclass java.awt.Component$AccessibleAWTComponent");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleChildrenCount"), "method getAccessibleChildrenCount");
        check(hasMethod(c, "getAccessibleChild"), "method getAccessibleChild");
        check(hasMethod(c, "getAccessibleAt"), "method getAccessibleAt");
        check(hasMethod(c, "addPropertyChangeListener"), "method addPropertyChangeListener");
        check(hasMethod(c, "removePropertyChangeListener"), "method removePropertyChangeListener");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

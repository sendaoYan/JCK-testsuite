/*
 * @test
 * @summary java.awt.MouseInfo conformance: class reflection.
 * @run main MouseInfoTest
 */

public class MouseInfoTest {
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
        Class<?> c = Class.forName("java.awt.MouseInfo");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getPointerInfo"), "method getPointerInfo");
        check(hasMethod(c, "getNumberOfButtons"), "method getNumberOfButtons");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

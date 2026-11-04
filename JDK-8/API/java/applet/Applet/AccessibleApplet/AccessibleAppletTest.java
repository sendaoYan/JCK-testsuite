/*
 * @test
 * @summary java.applet.Applet.AccessibleApplet conformance: class reflection.
 * @run main AccessibleAppletTest
 */

public class AccessibleAppletTest {
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
        Class<?> c = Class.forName("java.applet.Applet$AccessibleApplet");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Panel$AccessibleAWTPanel"), "superclass java.awt.Panel$AccessibleAWTPanel");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.Frame.AccessibleAWTFrame conformance: class reflection.
 * @run main AccessibleAWTFrameTest
 */

public class AccessibleAWTFrameTest {
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
        Class<?> c = Class.forName("java.awt.Frame$AccessibleAWTFrame");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.Window$AccessibleAWTWindow"), "superclass java.awt.Window$AccessibleAWTWindow");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAccessibleRole"), "method getAccessibleRole");
        check(hasMethod(c, "getAccessibleStateSet"), "method getAccessibleStateSet");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

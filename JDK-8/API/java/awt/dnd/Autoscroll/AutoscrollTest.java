/*
 * @test
 * @summary java.awt.dnd.Autoscroll conformance: interface reflection.
 * @run main AutoscrollTest
 */

public class AutoscrollTest {
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
        Class<?> c = Class.forName("java.awt.dnd.Autoscroll");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAutoscrollInsets"), "method getAutoscrollInsets");
        check(hasMethod(c, "autoscroll"), "method autoscroll");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

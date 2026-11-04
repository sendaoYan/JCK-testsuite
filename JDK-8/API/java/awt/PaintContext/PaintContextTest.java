/*
 * @test
 * @summary java.awt.PaintContext conformance: interface reflection.
 * @run main PaintContextTest
 */

public class PaintContextTest {
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
        Class<?> c = Class.forName("java.awt.PaintContext");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "dispose"), "method dispose");
        check(hasMethod(c, "getColorModel"), "method getColorModel");
        check(hasMethod(c, "getRaster"), "method getRaster");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

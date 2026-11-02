/*
 * @test
 * @summary java.awt.Shape conformance: interface reflection.
 * @run main ShapeTest
 */

public class ShapeTest {
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
        Class<?> c = Class.forName("java.awt.Shape");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

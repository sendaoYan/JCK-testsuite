/*
 * @test
 * @summary java.awt.geom.PathIterator conformance: interface reflection.
 * @run main PathIteratorTest
 */

public class PathIteratorTest {
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
        Class<?> c = Class.forName("java.awt.geom.PathIterator");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getWindingRule"), "method getWindingRule");
        check(hasMethod(c, "isDone"), "method isDone");
        check(hasMethod(c, "next"), "method next");
        check(hasMethod(c, "currentSegment"), "method currentSegment");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

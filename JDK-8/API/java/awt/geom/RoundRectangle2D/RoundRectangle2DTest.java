/*
 * @test
 * @summary java.awt.geom.RoundRectangle2D conformance: abstract class reflection.
 * @run main RoundRectangle2DTest
 */

public class RoundRectangle2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.RoundRectangle2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.geom.RectangularShape"), "superclass java.awt.geom.RectangularShape");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getArcWidth"), "method getArcWidth");
        check(hasMethod(c, "getArcHeight"), "method getArcHeight");
        check(hasMethod(c, "setRoundRect"), "method setRoundRect");
        check(hasMethod(c, "setFrame"), "method setFrame");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

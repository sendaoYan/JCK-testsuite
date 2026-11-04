/*
 * @test
 * @summary java.awt.geom.Rectangle2D conformance: abstract class reflection.
 * @run main Rectangle2DTest
 */

public class Rectangle2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.Rectangle2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.geom.RectangularShape"), "superclass java.awt.geom.RectangularShape");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setRect"), "method setRect");
        check(hasMethod(c, "intersectsLine"), "method intersectsLine");
        check(hasMethod(c, "outcode"), "method outcode");
        check(hasMethod(c, "setFrame"), "method setFrame");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "createIntersection"), "method createIntersection");
        check(hasMethod(c, "intersect"), "method intersect");
        check(hasMethod(c, "createUnion"), "method createUnion");
        check(hasMethod(c, "union"), "method union");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

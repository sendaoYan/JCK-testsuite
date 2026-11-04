/*
 * @test
 * @summary java.awt.geom.Arc2D conformance: abstract class reflection.
 * @run main Arc2DTest
 */

public class Arc2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.Arc2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.awt.geom.RectangularShape"), "superclass java.awt.geom.RectangularShape");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "getAngleStart"), "method getAngleStart");
        check(hasMethod(c, "getAngleExtent"), "method getAngleExtent");
        check(hasMethod(c, "getArcType"), "method getArcType");
        check(hasMethod(c, "getStartPoint"), "method getStartPoint");
        check(hasMethod(c, "getEndPoint"), "method getEndPoint");
        check(hasMethod(c, "setArc"), "method setArc");
        check(hasMethod(c, "setArcByCenter"), "method setArcByCenter");
        check(hasMethod(c, "setArcByTangent"), "method setArcByTangent");
        check(hasMethod(c, "setAngleStart"), "method setAngleStart");
        check(hasMethod(c, "setAngleExtent"), "method setAngleExtent");
        check(hasMethod(c, "setAngles"), "method setAngles");
        check(hasMethod(c, "setArcType"), "method setArcType");
        check(hasMethod(c, "setFrame"), "method setFrame");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "containsAngle"), "method containsAngle");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "equals"), "method equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

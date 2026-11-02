/*
 * @test
 * @summary java.awt.geom.QuadCurve2D conformance: abstract class reflection.
 * @run main QuadCurve2DTest
 */

public class QuadCurve2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.QuadCurve2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Shape", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getX1"), "method getX1");
        check(hasMethod(c, "getY1"), "method getY1");
        check(hasMethod(c, "getP1"), "method getP1");
        check(hasMethod(c, "getCtrlX"), "method getCtrlX");
        check(hasMethod(c, "getCtrlY"), "method getCtrlY");
        check(hasMethod(c, "getCtrlPt"), "method getCtrlPt");
        check(hasMethod(c, "getX2"), "method getX2");
        check(hasMethod(c, "getY2"), "method getY2");
        check(hasMethod(c, "getP2"), "method getP2");
        check(hasMethod(c, "setCurve"), "method setCurve");
        check(hasMethod(c, "getFlatnessSq"), "method getFlatnessSq");
        check(hasMethod(c, "getFlatness"), "method getFlatness");
        check(hasMethod(c, "subdivide"), "method subdivide");
        check(hasMethod(c, "solveQuadratic"), "method solveQuadratic");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

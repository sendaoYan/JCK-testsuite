/*
 * @test
 * @summary java.awt.geom.RectangularShape conformance: abstract class reflection.
 * @run main RectangularShapeTest
 */

public class RectangularShapeTest {
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
        Class<?> c = Class.forName("java.awt.geom.RectangularShape");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Shape", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getX"), "method getX");
        check(hasMethod(c, "getY"), "method getY");
        check(hasMethod(c, "getWidth"), "method getWidth");
        check(hasMethod(c, "getHeight"), "method getHeight");
        check(hasMethod(c, "getMinX"), "method getMinX");
        check(hasMethod(c, "getMinY"), "method getMinY");
        check(hasMethod(c, "getMaxX"), "method getMaxX");
        check(hasMethod(c, "getMaxY"), "method getMaxY");
        check(hasMethod(c, "getCenterX"), "method getCenterX");
        check(hasMethod(c, "getCenterY"), "method getCenterY");
        check(hasMethod(c, "getFrame"), "method getFrame");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "setFrame"), "method setFrame");
        check(hasMethod(c, "setFrameFromDiagonal"), "method setFrameFromDiagonal");
        check(hasMethod(c, "setFrameFromCenter"), "method setFrameFromCenter");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

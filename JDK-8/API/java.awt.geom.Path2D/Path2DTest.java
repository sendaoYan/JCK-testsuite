/*
 * @test
 * @summary java.awt.geom.Path2D conformance: abstract class reflection.
 * @run main Path2DTest
 */

public class Path2DTest {
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
        Class<?> c = Class.forName("java.awt.geom.Path2D");
        check(!c.isInterface() && !c.isEnum(), "not interface/enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.Shape", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "moveTo"), "method moveTo");
        check(hasMethod(c, "lineTo"), "method lineTo");
        check(hasMethod(c, "quadTo"), "method quadTo");
        check(hasMethod(c, "curveTo"), "method curveTo");
        check(hasMethod(c, "closePath"), "method closePath");
        check(hasMethod(c, "append"), "method append");
        check(hasMethod(c, "getWindingRule"), "method getWindingRule");
        check(hasMethod(c, "setWindingRule"), "method setWindingRule");
        check(hasMethod(c, "getCurrentPoint"), "method getCurrentPoint");
        check(hasMethod(c, "reset"), "method reset");
        check(hasMethod(c, "transform"), "method transform");
        check(hasMethod(c, "createTransformedShape"), "method createTransformedShape");
        check(hasMethod(c, "getBounds"), "method getBounds");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "intersects"), "method intersects");
        check(hasMethod(c, "getPathIterator"), "method getPathIterator");
        check(hasMethod(c, "clone"), "method clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

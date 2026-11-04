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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() throws Exception {
        // Shape interface implemented by the concrete awt/geom types
        Class<?> c = java.awt.Shape.class;
        check(c.isInterface(), "Shape is an interface");
        String[] names = {"getBounds", "getBounds2D", "contains", "intersects", "getPathIterator"};
        for (String n : names) {
            boolean found = false;
            for (java.lang.reflect.Method m : c.getMethods()) if (m.getName().equals(n)) found = true;
            check(found, "interface method: " + n);
        }
        // getPathIterator has the 2-arg (AffineTransform, double flatness) variant
        check(c.getMethod("getPathIterator", java.awt.geom.AffineTransform.class, double.class) != null,
              "getPathIterator(AffineTransform,double)");
        // concrete implementors agree on contains/intersects cross-implementation
        java.awt.Shape rect = new java.awt.Rectangle(0, 0, 10, 10);
        java.awt.Shape rect2d = new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10);
        check(rect.contains(5, 5) && rect2d.contains(5, 5), "both contain interior point");
        check(!rect.contains(15, 5) && !rect2d.contains(15, 5), "both exclude exterior");
        check(rect.intersects(5, 5, 10, 10) && rect2d.intersects(5, 5, 10, 10), "both intersect overlap");
        // polygon as Shape
        java.awt.Polygon poly = new java.awt.Polygon();
        poly.addPoint(0, 0); poly.addPoint(10, 0); poly.addPoint(0, 10);
        check(poly.contains(2, 2), "polygon Shape contains");
        // path iterator iterates the same number of segments for equal shapes
        int seg1 = countSegs(rect.getPathIterator(null));
        int seg2 = countSegs(rect2d.getPathIterator(null));
        check(seg1 == seg2 && seg1 > 0, "Rectangle and Rectangle2D same segment count: " + seg1 + "/" + seg2);
    }
    static int countSegs(java.awt.geom.PathIterator it) {
        int n = 0; while (!it.isDone()) { it.next(); n++; } return n;
    }

}

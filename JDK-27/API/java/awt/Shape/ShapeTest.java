/*
 * @test
 * @summary java.awt.Shape conformance: white-box structure + black-box behavior.
 * @run main ShapeTest
 */

import java.lang.reflect.*;

public class ShapeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.Shape", false, ShapeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.Shape");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isInterface(m), "is interface");

        check(c.getMethod("getBounds").getReturnType() == java.awt.Rectangle.class,
              "getBounds returns Rectangle");
        check(c.getMethod("getBounds2D").getReturnType() == java.awt.geom.Rectangle2D.class,
              "getBounds2D returns Rectangle2D");
        check(c.getMethod("contains", double.class, double.class).getReturnType() == boolean.class,
              "contains(double,double) boolean");
        check(c.getMethod("contains", java.awt.geom.Point2D.class) != null, "contains(Point2D)");
        check(c.getMethod("intersects", double.class, double.class, double.class, double.class) != null,
              "intersects(double,double,double,double)");
        check(c.getMethod("intersects", java.awt.geom.Rectangle2D.class) != null, "intersects(Rectangle2D)");
        check(c.getMethod("contains", double.class, double.class, double.class, double.class) != null,
              "contains(double,double,double,double)");
        check(c.getMethod("contains", java.awt.geom.Rectangle2D.class) != null, "contains(Rectangle2D)");
        check(c.getMethod("getPathIterator", java.awt.geom.AffineTransform.class) != null,
              "getPathIterator(AffineTransform)");
        check(c.getMethod("getPathIterator", java.awt.geom.AffineTransform.class, double.class) != null,
              "getPathIterator(AffineTransform,double)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // Shape contract exercised through java.awt.Rectangle
        java.awt.Shape rect = new java.awt.Rectangle(0, 0, 10, 10);
        check(rect.getBounds().equals(new java.awt.Rectangle(0, 0, 10, 10)), "Rectangle.getBounds via Shape");
        check(rect.getBounds2D().getX() == 0.0 && rect.getBounds2D().getWidth() == 10.0,
              "Rectangle.getBounds2D via Shape");
        check(rect.contains(5.0, 5.0), "Shape.contains(double,double)");
        check(rect.contains(new java.awt.geom.Point2D.Double(5.0, 5.0)), "Shape.contains(Point2D)");
        check(rect.intersects(5.0, 5.0, 3.0, 3.0), "Shape.intersects(double,double,double,double)");
        check(rect.intersects(new java.awt.geom.Rectangle2D.Double(5, 5, 3, 3)), "Shape.intersects(Rectangle2D)");
        check(rect.contains(2.0, 2.0, 3.0, 3.0), "Shape.contains(double,double,double,double)");
        check(rect.contains(new java.awt.geom.Rectangle2D.Double(2, 2, 3, 3)), "Shape.contains(Rectangle2D)");

        java.awt.geom.PathIterator it = rect.getPathIterator(null);
        check(it != null, "Rectangle path iterator non-null");
        check(it.getWindingRule() == java.awt.geom.PathIterator.WIND_NON_ZERO,
              "Rectangle winding rule non-zero");
        float[] coords = new float[2];
        check(it.currentSegment(coords) == java.awt.geom.PathIterator.SEG_MOVETO,
              "Rectangle first segment MOVETO");

        // Shape contract exercised through java.awt.Polygon (even-odd rule)
        java.awt.Shape poly = new java.awt.Polygon(new int[] {0, 10, 10, 0}, new int[] {0, 0, 10, 10}, 4);
        check(poly.getBounds().equals(new java.awt.Rectangle(0, 0, 10, 10)), "Polygon.getBounds via Shape");
        check(poly.contains(5.0, 5.0), "Polygon contains via Shape");
        java.awt.geom.PathIterator pit = poly.getPathIterator(null);
        check(pit.getWindingRule() == java.awt.geom.PathIterator.WIND_EVEN_ODD,
              "Polygon winding rule even-odd");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

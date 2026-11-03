/*
 * @test
 * @summary java.awt.geom.Area conformance: white-box structure plus black-box behavior.
 * @run main AreaTest
 */

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class AreaTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Area");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Area is public");
        check(!Modifier.isAbstract(m), "Area is concrete");
        check(Shape.class.isAssignableFrom(c), "Area implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "Area implements Cloneable");
        check(hasMethod(c, "add", Area.class), "add(Area)");
        check(hasMethod(c, "subtract", Area.class), "subtract(Area)");
        check(hasMethod(c, "intersect", Area.class), "intersect(Area)");
        check(hasMethod(c, "exclusiveOr", Area.class), "exclusiveOr(Area)");
        check(hasMethod(c, "reset"), "reset");
        check(hasMethod(c, "isEmpty"), "isEmpty");
        check(hasMethod(c, "isPolygonal") && hasMethod(c, "isRectangular") && hasMethod(c, "isSingular"), "shape classifiers");
        check(hasMethod(c, "transform", AffineTransform.class), "transform");
        check(hasMethod(c, "createTransformedArea", AffineTransform.class), "createTransformedArea");
        check(hasMethod(c, "equals", Area.class), "equals(Area)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Area empty = new Area();
        check(empty.isEmpty(), "default ctor empty");
        check(empty.getBounds2D().equals(new Rectangle2D.Double(0, 0, 0, 0)), "empty bounds zero");

        Area rect = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        check(!rect.isEmpty(), "Area(Shape) not empty");
        check(rect.isRectangular(), "rectangle Area isRectangular");
        check(rect.isSingular(), "rectangle Area isSingular");
        check(rect.isPolygonal(), "rectangle Area isPolygonal");

        Area ell = new Area(new Ellipse2D.Double(0, 0, 10, 10));
        check(!ell.isRectangular(), "ellipse Area not rectangular");
        check(ell.isSingular(), "ellipse Area isSingular");
        check(!ell.isPolygonal(), "ellipse Area not polygonal");

        // boolean combinations with exact bounds
        Area a1 = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        Area a2 = new Area(new Rectangle2D.Double(5, 0, 10, 10));

        Area s = new Area(a1); s.add(a2);
        check(s.getBounds2D().equals(new Rectangle2D.Double(0, 0, 15, 10)), "add union bounds");

        Area d = new Area(a1); d.subtract(a2);
        check(d.getBounds2D().equals(new Rectangle2D.Double(0, 0, 5, 10)), "subtract difference bounds");

        Area i = new Area(a1); i.intersect(a2);
        check(i.getBounds2D().equals(new Rectangle2D.Double(5, 0, 5, 10)), "intersect bounds");

        Area x = new Area(a1); x.exclusiveOr(a2);
        check(x.getBounds2D().equals(new Rectangle2D.Double(0, 0, 15, 10)), "exclusiveOr bounds");
        check(!x.isEmpty(), "exclusiveOr of overlapping not empty");

        // identical subtraction / xor empties the area
        Area sub = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        sub.subtract(new Area(new Rectangle2D.Double(0, 0, 10, 10)));
        check(sub.isEmpty(), "subtract identical -> empty");
        Area xor = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        xor.exclusiveOr(new Area(new Rectangle2D.Double(0, 0, 10, 10)));
        check(xor.isEmpty(), "exclusiveOr identical -> empty");

        // reset
        Area r = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        r.reset();
        check(r.isEmpty(), "reset -> empty");

        // bounds
        Area b = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        check(b.getBounds2D().equals(new Rectangle2D.Double(0, 0, 10, 10)), "getBounds2D");
        Rectangle rb = b.getBounds();
        check(rb.x == 0 && rb.y == 0 && rb.width == 10 && rb.height == 10, "getBounds integer");

        // contains / intersects
        check(b.contains(5, 5), "contains center");
        check(!b.contains(15, 5), "excludes outside");
        check(b.contains(new Point2D.Double(5, 5)), "contains(Point2D)");
        check(b.contains(1, 1, 2, 2), "contains inner rectangle");
        check(b.intersects(5, 5, 10, 10), "intersects overlapping");
        check(!b.intersects(20, 20, 5, 5), "excludes far rectangle");

        // transform and createTransformedArea
        Area tr = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        tr.transform(AffineTransform.getTranslateInstance(5, 5));
        check(tr.getBounds2D().equals(new Rectangle2D.Double(5, 5, 10, 10)), "transform translate");
        Area cta = tr.createTransformedArea(AffineTransform.getScaleInstance(2, 2));
        check(cta.getBounds2D().equals(new Rectangle2D.Double(10, 10, 20, 20)), "createTransformedArea scale");

        // clone
        Area cl = (Area) b.clone();
        check(cl != b && cl.equals(b), "clone distinct but equal");
        cl.subtract(new Area(new Rectangle2D.Double(0, 0, 5, 5)));
        check(!cl.equals(b), "mutating clone leaves original");

        // equals(Area)
        Area eq1 = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        Area eq2 = new Area(new Rectangle2D.Double(0, 0, 10, 10));
        Area eq3 = new Area(new Rectangle2D.Double(0, 0, 5, 10));
        check(eq1.equals(eq2), "equal areas");
        check(!eq1.equals(eq3), "different areas not equal");

        // path iterator produces geometry
        PathIterator it = b.getPathIterator(null);
        double[] c = new double[6];
        int segs = 0;
        while (!it.isDone()) { it.currentSegment(c); segs++; it.next(); }
        check(segs >= 5, "Area path iterator yields segments");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

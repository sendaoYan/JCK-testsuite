/*
 * @test
 * @summary java.awt.geom.RectangularShape conformance: white-box structure plus black-box behavior.
 * @run main RectangularShapeTest
 */

import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class RectangularShapeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.RectangularShape");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) && Modifier.isAbstract(m), "abstract public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(Shape.class.isAssignableFrom(c), "implements Shape");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getX"), "getX()");
        check(hasMethod(c, "getY"), "getY()");
        check(hasMethod(c, "getWidth"), "getWidth()");
        check(hasMethod(c, "getHeight"), "getHeight()");
        check(hasMethod(c, "isEmpty"), "isEmpty()");
        check(hasMethod(c, "setFrame", double.class, double.class, double.class, double.class), "setFrame");
        check(hasMethod(c, "getMinX"), "getMinX()");
        check(hasMethod(c, "getCenterX"), "getCenterX()");
        check(hasMethod(c, "setFrameFromDiagonal", double.class, double.class, double.class, double.class), "setFrameFromDiagonal");
        check(hasMethod(c, "setFrameFromCenter", double.class, double.class, double.class, double.class), "setFrameFromCenter");
        check(hasMethod(c, "getPathIterator", AffineTransform.class, double.class), "getPathIterator");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Rectangle2D.Double r = new Rectangle2D.Double(10, 20, 30, 40);
        // min/max/center
        check(close(r.getMinX(), 10) && close(r.getMinY(), 20), "getMinX/getMinY");
        check(close(r.getMaxX(), 40) && close(r.getMaxY(), 60), "getMaxX/getMaxY");
        check(close(r.getCenterX(), 25) && close(r.getCenterY(), 40), "getCenterX/getCenterY");

        // getFrame
        Rectangle2D frame = r.getFrame();
        check(close(frame.getX(), 10) && close(frame.getY(), 20)
              && close(frame.getWidth(), 30) && close(frame.getHeight(), 40), "getFrame");

        // setFrame(Point2D, Dimension2D)
        r.setFrame(new Point2D.Double(1, 2), new java.awt.Dimension(5, 6));
        check(close(r.getX(), 1) && close(r.getY(), 2) && close(r.getWidth(), 5) && close(r.getHeight(), 6),
              "setFrame(Point2D,Dimension2D)");

        // setFrame(Rectangle2D)
        r.setFrame(new Rectangle2D.Double(0, 0, 7, 8));
        check(close(r.getWidth(), 7) && close(r.getHeight(), 8), "setFrame(Rectangle2D)");

        // setFrameFromDiagonal normalizes
        r.setFrameFromDiagonal(50, 60, 10, 20);
        check(close(r.getX(), 10) && close(r.getY(), 20) && close(r.getWidth(), 40) && close(r.getHeight(), 40),
              "setFrameFromDiagonal normalizes corners");

        // setFrameFromCenter
        r.setFrameFromCenter(25, 40, 10, 20);
        check(close(r.getX(), 10) && close(r.getY(), 20) && close(r.getWidth(), 30) && close(r.getHeight(), 40),
              "setFrameFromCenter");

        // contains / intersects
        check(r.contains(new Point2D.Double(25, 40)), "contains(Point2D) center");
        check(!r.contains(new Point2D.Double(0, 0)), "contains(Point2D) outside");
        check(r.intersects(new Rectangle2D.Double(30, 30, 20, 20)), "intersects(Rectangle2D)");
        check(r.contains(new Rectangle2D.Double(15, 25, 5, 5)), "contains(Rectangle2D) inner");

        // getBounds (java.awt.Rectangle)
        Rectangle b = r.getBounds();
        check(b.x == 10 && b.y == 20 && b.width == 30 && b.height == 40, "getBounds int rect");

        // getPathIterator
        PathIterator it = r.getPathIterator(null, 0.1);
        int segs = 0;
        while (!it.isDone()) { segs++; it.next(); }
        check(segs == 6, "getPathIterator iterates 6 segments");

        // clone
        RectangularShape cl = (RectangularShape) r.clone();
        check(cl != r && cl.getFrame().equals(r.getFrame()), "clone independent equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

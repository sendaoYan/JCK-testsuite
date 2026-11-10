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

    // ===================== 契约深化：getBounds 取整、负宽不归一化、Point2D 设框重载、getFrame 独立 =====================
    static void testRectangularShapeContracts() {
        // getBounds 用 floor/ceil 取整并向外扩到覆盖分数边界（RectangularShape 基础实现）。
        java.awt.Rectangle b1 = new Rectangle2D.Double(0.5, 0.5, 1.5, 1.5).getBounds();
        check(b1.x == 0 && b1.y == 0 && b1.width == 2 && b1.height == 2, "getBounds rounds fractional extents outward");
        java.awt.Rectangle b2 = new Rectangle2D.Double(0.5, 0.5, 1.0, 1.0).getBounds();
        check(b2.x == 0 && b2.y == 0 && b2.width == 2 && b2.height == 2, "getBounds inflates fractional width");
        java.awt.Rectangle b3 = new Rectangle2D.Double(-0.5, -0.5, 1.5, 1.5).getBounds();
        check(b3.x == -1 && b3.y == -1 && b3.width == 2 && b3.height == 2, "getBounds floors negative origin");

        // 负宽/负高：getBounds 返回空矩形 (0,0,0,0)。
        java.awt.Rectangle b4 = new Rectangle2D.Double(1, 2, -3, 4).getBounds();
        check(b4.x == 0 && b4.y == 0 && b4.width == 0 && b4.height == 0 && b4.isEmpty(),
              "getBounds of negative width returns empty rectangle");

        // min/max/center 不归一化负宽（getMinX > getMaxX）。
        Rectangle2D.Double neg = new Rectangle2D.Double(0, 0, -10, 20);
        check(neg.getMinX() == 0.0 && neg.getMaxX() == -10.0, "negative width: getMinX/getMaxX not normalized");
        check(neg.getCenterX() == -5.0 && neg.getCenterY() == 10.0, "negative width: getCenterX reflects raw values");

        // setFrameFromDiagonal(Point2D, Point2D) 归一化角点。
        Rectangle2D.Double d = new Rectangle2D.Double();
        d.setFrameFromDiagonal(new Point2D.Double(50, 60), new Point2D.Double(10, 20));
        check(close(d.getX(), 10) && close(d.getY(), 20) && close(d.getWidth(), 40) && close(d.getHeight(), 40),
              "setFrameFromDiagonal(Point2D,Point2D) normalizes corners");

        // setFrameFromCenter(Point2D, Point2D) 与负角点（abs 半宽半高恒正）。
        Rectangle2D.Double c = new Rectangle2D.Double();
        c.setFrameFromCenter(new Point2D.Double(25, 40), new Point2D.Double(10, 20));
        check(close(c.getX(), 10) && close(c.getY(), 20) && close(c.getWidth(), 30) && close(c.getHeight(), 40),
              "setFrameFromCenter(Point2D,Point2D)");
        Rectangle2D.Double cn = new Rectangle2D.Double();
        cn.setFrameFromCenter(10, 20, -5, -5); // corner opposite side -> positive frame
        check(close(cn.getX(), -5) && close(cn.getY(), -5) && close(cn.getWidth(), 30) && close(cn.getHeight(), 50),
              "setFrameFromCenter uses absolute half-extents");

        // getFrame 返回独立新对象。
        Rectangle2D.Double h = new Rectangle2D.Double(0, 0, 10, 20);
        Rectangle2D frame = h.getFrame();
        frame.setRect(0, 0, 1, 1);
        check(close(h.getWidth(), 10) && close(h.getHeight(), 20), "getFrame returns independent rectangle");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRectangularShapeContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

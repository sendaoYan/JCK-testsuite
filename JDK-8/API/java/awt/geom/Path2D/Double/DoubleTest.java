/*
 * @test
 * @summary java.awt.geom.Path2D.Double conformance: white-box structure plus black-box behavior.
 * @run main DoubleTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class DoubleTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Path2D$Double");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Path2D.Double is public");
        check(!Modifier.isAbstract(m), "Path2D.Double is concrete");
        check(c.getSuperclass() == Path2D.class, "Path2D.Double extends Path2D");
        check(Serializable.class.isAssignableFrom(c), "Path2D.Double implements Serializable");
        check(c.getConstructors().length == 5, "five public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path2D.Double p = new Path2D.Double();
        check(p.getWindingRule() == Path2D.WIND_NON_ZERO, "default winding NON_ZERO");

        p.moveTo(1, 2);
        p.lineTo(3, 4);
        p.quadTo(5, 6, 7, 8);
        p.curveTo(9, 10, 11, 12, 13, 14);
        p.closePath();

        PathIterator it = p.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO && close(c[0], 1) && close(c[1], 2), "MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_LINETO && close(c[0], 3) && close(c[1], 4), "LINETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_QUADTO && close(c[2], 7) && close(c[3], 8), "QUADTO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_CUBICTO && close(c[4], 13) && close(c[5], 14), "CUBICTO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_CLOSE, "CLOSE");
        it.next();
        check(it.isDone(), "iterator done");

        // bounds
        Rectangle2D b = p.getBounds2D();
        check(close(b.getX(), 1) && close(b.getY(), 2), "getBounds2D x/y");
        check(close(b.getWidth(), 12) && close(b.getHeight(), 12), "getBounds2D width/height");

        // winding rules
        Path2D.Double w = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        check(w.getWindingRule() == Path2D.WIND_EVEN_ODD, "constructor winding rule");

        // Shape constructors
        Rectangle2D r = new Rectangle2D.Double(2, 3, 10, 20);
        Path2D.Double s = new Path2D.Double(r);
        check(close(s.getBounds2D().getX(), 2) && close(s.getBounds2D().getY(), 3), "Shape ctor x/y");
        check(close(s.getBounds2D().getWidth(), 10) && close(s.getBounds2D().getHeight(), 20), "Shape ctor w/h");
        Path2D.Double st = new Path2D.Double(r, AffineTransform.getScaleInstance(2, 2));
        check(close(st.getBounds2D().getWidth(), 20) && close(st.getBounds2D().getHeight(), 40), "Shape+AT ctor scaled");

        // transformed iterator
        Path2D.Double line = new Path2D.Double();
        line.moveTo(0, 0); line.lineTo(10, 0);
        PathIterator tit = line.getPathIterator(AffineTransform.getTranslateInstance(0, 5));
        double[] tc = new double[6];
        tit.currentSegment(tc);
        check(close(tc[1], 5), "transformed iterator MOVETO y");
        tit.next();
        tit.currentSegment(tc);
        check(close(tc[0], 10) && close(tc[1], 5), "transformed iterator LINETO");

        // flatten a cubic
        Path2D.Double cu = new Path2D.Double();
        cu.moveTo(0, 0); cu.curveTo(10, 20, 30, -20, 40, 0);
        int n = 0;
        PathIterator fit = cu.getPathIterator(null, 1.0);
        while (!fit.isDone()) { fit.currentSegment(new double[6]); n++; fit.next(); }
        check(n > 5, "cubic flatten subdivides (" + n + ")");

        // append(PathIterator)
        Path2D.Double a = new Path2D.Double();
        a.moveTo(0, 0);
        a.append(line.getPathIterator(null), false);
        check(close(a.getBounds2D().getWidth(), 10), "append(PathIterator) geometry");

        // clone independence
        Path2D.Double cl = (Path2D.Double) p.clone();
        check(cl != p && cl.getBounds2D().equals(p.getBounds2D()), "clone equal and distinct");
        cl.lineTo(100, 100);
        check(!cl.getBounds2D().equals(p.getBounds2D()), "clone mutation independent");

        // getCurrentPoint
        Path2D.Double cp = new Path2D.Double();
        cp.moveTo(3, 4);
        check(cp.getCurrentPoint().equals(new Point2D.Double(3, 4)), "getCurrentPoint");
        cp.closePath();
        check(cp.getCurrentPoint().equals(new Point2D.Double(3, 4)), "getCurrentPoint after closePath");

        // serialization round-trip preserves geometry and winding rule
        Path2D.Double ser = new Path2D.Double(Path2D.WIND_EVEN_ODD);
        ser.moveTo(1, 2); ser.lineTo(3, 4); ser.closePath();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(ser);
        Path2D back = (Path2D) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof Path2D.Double, "Path2D.Double round-trip class");
        check(back.getWindingRule() == Path2D.WIND_EVEN_ODD, "round-trip winding rule preserved");
        check(back.getBounds2D().equals(new Rectangle2D.Double(1, 2, 2, 2)), "round-trip geometry preserved");
        check(back.getCurrentPoint() != null && back.getCurrentPoint().equals(new Point2D.Double(1, 2)),
              "round-trip current point restored");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

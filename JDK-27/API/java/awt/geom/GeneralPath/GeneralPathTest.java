/*
 * @test
 * @summary java.awt.geom.GeneralPath conformance: white-box structure plus black-box behavior.
 * @run main GeneralPathTest
 */

import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class GeneralPathTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.GeneralPath");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "GeneralPath is public");
        check(Modifier.isFinal(m), "GeneralPath is final");
        check(c.getSuperclass() == Path2D.Float.class, "GeneralPath extends Path2D.Float");
        check(Path2D.class.isAssignableFrom(c), "GeneralPath is a Path2D");
        check(Serializable.class.isAssignableFrom(c), "GeneralPath is Serializable");
        check(c.getConstructors().length == 4, "four public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        GeneralPath gp = new GeneralPath();
        check(gp.getWindingRule() == Path2D.WIND_NON_ZERO, "default winding NON_ZERO");

        GeneralPath eo = new GeneralPath(Path2D.WIND_EVEN_ODD);
        check(eo.getWindingRule() == Path2D.WIND_EVEN_ODD, "winding rule constructor");

        GeneralPath cap = new GeneralPath(Path2D.WIND_NON_ZERO, 16);
        cap.moveTo(0, 0); cap.lineTo(5, 5);
        check(close(cap.getBounds2D().getWidth(), 5), "rule+capacity constructor usable");

        Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 20);
        GeneralPath fromShape = new GeneralPath(r);
        check(close(fromShape.getBounds2D().getWidth(), 10) && close(fromShape.getBounds2D().getHeight(), 20),
              "Shape constructor copies geometry");

        // full drawing chain
        GeneralPath p = new GeneralPath();
        p.moveTo(1, 2);
        p.lineTo(3, 4);
        p.quadTo(5, 6, 7, 8);
        p.curveTo(9, 10, 11, 12, 13, 14);
        p.closePath();

        PathIterator it = p.getPathIterator(null);
        double[] c = new double[6];
        check(it.currentSegment(c) == PathIterator.SEG_MOVETO && close(c[0], 1) && close(c[1], 2), "MOVETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_LINETO && close(c[0], 3), "LINETO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_QUADTO, "QUADTO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_CUBICTO, "CUBICTO");
        it.next();
        check(it.currentSegment(c) == PathIterator.SEG_CLOSE, "CLOSE");

        // contains on a square
        GeneralPath sq = new GeneralPath();
        sq.moveTo(0, 0); sq.lineTo(10, 0); sq.lineTo(10, 10); sq.lineTo(0, 10); sq.closePath();
        check(sq.contains(5, 5), "square contains center");
        check(!sq.contains(15, 5), "square excludes outside point");
        check(sq.intersects(9, 9, 5, 5), "square intersects overlapping rect");

        // bounds
        Rectangle2D b = sq.getBounds2D();
        check(close(b.getX(), 0) && close(b.getY(), 0) && close(b.getWidth(), 10) && close(b.getHeight(), 10),
              "square bounds");

        // getCurrentPoint lifecycle
        GeneralPath cp = new GeneralPath();
        cp.moveTo(2, 3);
        check(cp.getCurrentPoint().equals(new Point2D.Double(2, 3)), "getCurrentPoint after moveTo");
        cp.lineTo(4, 5);
        check(cp.getCurrentPoint().equals(new Point2D.Double(4, 5)), "getCurrentPoint after lineTo");

        // append
        GeneralPath ap = new GeneralPath();
        ap.moveTo(0, 0);
        ap.append(new Rectangle2D.Double(0, 0, 7, 7), false);
        check(close(ap.getBounds2D().getWidth(), 7), "append(Shape) geometry");

        // transform
        GeneralPath tr = new GeneralPath();
        tr.moveTo(0, 0); tr.lineTo(10, 0);
        tr.transform(AffineTransform.getTranslateInstance(5, 0));
        check(close(tr.getBounds2D().getX(), 5) && close(tr.getBounds2D().getWidth(), 10), "transform in place");

        // clone independence (inherited from Path2D.Float)
        GeneralPath cl = (GeneralPath) sq.clone();
        check(cl != sq && cl.getBounds2D().equals(sq.getBounds2D()), "clone equal and distinct");
        cl.lineTo(100, 100);
        check(!cl.getBounds2D().equals(sq.getBounds2D()), "clone mutation independent");

        // trimToSize
        GeneralPath trim = new GeneralPath();
        trim.moveTo(0, 0); trim.lineTo(4, 4);
        trim.trimToSize();
        check(close(trim.getBounds2D().getWidth(), 4), "trimToSize preserves geometry");

        // reset
        trim.reset();
        check(trim.getCurrentPoint() == null, "reset clears current point");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

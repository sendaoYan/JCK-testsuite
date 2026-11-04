/*
 * @test
 * @summary java.awt.geom.Path2D.Float conformance: white-box structure plus black-box behavior.
 * @run main FloatTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.io.Serializable;
import java.lang.reflect.*;

public class FloatTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Path2D$Float");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Path2D.Float is public");
        check(!Modifier.isAbstract(m), "Path2D.Float is concrete");
        check(c.getSuperclass() == Path2D.class, "Path2D.Float extends Path2D");
        check(Serializable.class.isAssignableFrom(c), "Path2D.Float implements Serializable");
        String[] ctorNames = {"", "int", "int,int", "Shape", "Shape,AffineTransform"};
        int i = 0;
        for (Constructor<?> ct : c.getConstructors()) { check(Modifier.isPublic(ct.getModifiers()), "public ctor"); i++; }
        check(c.getConstructors().length == 5, "five public constructors");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Path2D.Float f = new Path2D.Float();
        check(f.getWindingRule() == Path2D.WIND_NON_ZERO, "default winding NON_ZERO");

        Path2D.Float w = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        check(w.getWindingRule() == Path2D.WIND_EVEN_ODD, "constructor sets winding rule");

        // full segment types via iterator
        Path2D.Float p = new Path2D.Float();
        p.moveTo(1f, 2f);
        p.lineTo(3f, 4f);
        p.quadTo(5f, 6f, 7f, 8f);
        p.curveTo(9f, 10f, 11f, 12f, 13f, 14f);
        p.closePath();
        PathIterator it = p.getPathIterator(null);
        double[] c = new double[6];
        check(!it.isDone() && it.currentSegment(c) == PathIterator.SEG_MOVETO, "segment 1 MOVETO");
        check(close(c[0], 1) && close(c[1], 2), "MOVETO coords");
        it.next();
        check(!it.isDone() && it.currentSegment(c) == PathIterator.SEG_LINETO, "segment 2 LINETO");
        check(close(c[0], 3) && close(c[1], 4), "LINETO coords");
        it.next();
        check(!it.isDone() && it.currentSegment(c) == PathIterator.SEG_QUADTO, "segment 3 QUADTO");
        check(close(c[0], 5) && close(c[1], 6) && close(c[2], 7) && close(c[3], 8), "QUADTO coords");
        it.next();
        check(!it.isDone() && it.currentSegment(c) == PathIterator.SEG_CUBICTO, "segment 4 CUBICTO");
        check(close(c[4], 13) && close(c[5], 14), "CUBICTO end coords");
        it.next();
        check(!it.isDone() && it.currentSegment(c) == PathIterator.SEG_CLOSE, "segment 5 CLOSE");
        it.next();
        check(it.isDone(), "iterator ends after CLOSE");

        // double overloads accepted by Float
        Path2D.Float d = new Path2D.Float();
        d.moveTo(0.5, 1.5);
        d.lineTo(2.5, 3.5);
        check(close(d.getBounds2D().getX(), 0.5) && close(d.getBounds2D().getWidth(), 2.0), "double overloads");

        // constructors from Shape and Shape+AT
        Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 20);
        Path2D.Float fs = new Path2D.Float(r);
        check(close(fs.getBounds2D().getX(), 0) && close(fs.getBounds2D().getY(), 0), "Shape ctor x/y");
        check(close(fs.getBounds2D().getWidth(), 10) && close(fs.getBounds2D().getHeight(), 20), "Shape ctor width/height");
        Path2D.Float fst = new Path2D.Float(r, AffineTransform.getTranslateInstance(5, 5));
        check(close(fst.getBounds2D().getX(), 5) && close(fst.getBounds2D().getY(), 5), "Shape+AT ctor translated");

        // getPathIterator with transform
        Path2D.Float line = new Path2D.Float();
        line.moveTo(0, 0); line.lineTo(10, 0);
        PathIterator tit = line.getPathIterator(AffineTransform.getTranslateInstance(5, 0));
        double[] tc = new double[6];
        tit.currentSegment(tc);
        check(close(tc[0], 5) && close(tc[1], 0), "transformed iterator MOVETO");
        tit.next();
        tit.currentSegment(tc);
        check(close(tc[0], 15) && close(tc[1], 0), "transformed iterator LINETO");

        // flatten iterator
        Path2D.Float q = new Path2D.Float();
        q.moveTo(0, 0); q.quadTo(10, 10, 20, 0);
        int flatSegs = 0;
        PathIterator fit = q.getPathIterator(null, 0.5);
        while (!fit.isDone()) { fit.currentSegment(new double[6]); flatSegs++; fit.next(); }
        check(flatSegs > 3, "flatness iterator subdivides curve (" + flatSegs + " segments)");

        // append(PathIterator, connect)
        Path2D.Float a = new Path2D.Float();
        a.moveTo(0, 0); a.lineTo(1, 0);
        a.append(line.getPathIterator(null), false);
        check(close(a.getBounds2D().getWidth(), 10), "append(PathIterator) adds geometry");

        // clone independence
        Path2D.Float cl = (Path2D.Float) p.clone();
        check(cl != p, "clone distinct object");
        check(cl.getBounds2D().equals(p.getBounds2D()), "clone equal bounds");
        cl.lineTo(100, 100);
        check(!cl.getBounds2D().equals(p.getBounds2D()), "mutating clone leaves original intact");

        // trimToSize keeps geometry
        Path2D.Float tr = new Path2D.Float();
        tr.moveTo(0, 0); tr.lineTo(5, 5);
        tr.trimToSize();
        check(close(tr.getBounds2D().getWidth(), 5), "trimToSize preserves geometry");

        // getCurrentPoint after moveTo
        Path2D.Float cp = new Path2D.Float();
        cp.moveTo(3, 4);
        check(cp.getCurrentPoint().equals(new Point2D.Double(3, 4)), "getCurrentPoint");

        // serialization round-trip preserves geometry and winding rule
        Path2D.Float ser = new Path2D.Float(Path2D.WIND_EVEN_ODD);
        ser.moveTo(1, 2); ser.lineTo(3, 4); ser.closePath();
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        new java.io.ObjectOutputStream(bos).writeObject(ser);
        Path2D back = (Path2D) new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray())).readObject();
        check(back instanceof Path2D.Float, "Path2D.Float round-trip class");
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

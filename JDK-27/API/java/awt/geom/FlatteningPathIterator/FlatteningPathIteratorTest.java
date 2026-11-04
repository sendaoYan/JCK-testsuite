/*
 * @test
 * @summary java.awt.geom.FlatteningPathIterator conformance: white-box structure plus black-box behavior.
 * @run main FlatteningPathIteratorTest
 */

import java.awt.geom.*;
import java.lang.reflect.*;

public class FlatteningPathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.FlatteningPathIterator");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!c.isInterface() && !c.isEnum(), "class, not interface/enum");
        check(PathIterator.class.isAssignableFrom(c), "implements PathIterator");
        check(hasMethod(c, "getFlatness"), "getFlatness()");
        check(hasMethod(c, "getRecursionLimit"), "getRecursionLimit()");
        check(hasMethod(c, "getWindingRule"), "getWindingRule()");
        check(hasMethod(c, "isDone"), "isDone()");
        check(hasMethod(c, "next"), "next()");
        check(hasMethod(c, "currentSegment", float[].class), "currentSegment(float[])");
        check(hasMethod(c, "currentSegment", double[].class), "currentSegment(double[])");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // curved path: moveTo, quadTo, lineTo, close
        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 0);
        p.quadTo(50, 100, 100, 0);
        p.lineTo(100, 50);
        p.closePath();

        FlatteningPathIterator it = new FlatteningPathIterator(p.getPathIterator(null), 1.0);
        check(close(it.getFlatness(), 1.0), "getFlatness == 1.0");
        check(it.getRecursionLimit() == 10, "default recursion limit == 10");
        check(it.getWindingRule() == PathIterator.WIND_NON_ZERO, "getWindingRule WIND_NON_ZERO");

        int segs = 0; boolean sawCurve = false;
        double[] coords = new double[6];
        while (!it.isDone()) {
            int type = it.currentSegment(coords);
            if (type == PathIterator.SEG_QUADTO || type == PathIterator.SEG_CUBICTO) sawCurve = true;
            segs++;
            it.next();
        }
        check(segs >= 5, "flattened curve path emits multiple segments");
        check(!sawCurve, "flattened output has no QUAD/CUBIC segments");

        // explicit recursion-limit constructor
        FlatteningPathIterator it2 = new FlatteningPathIterator(p.getPathIterator(null), 0.5, 3);
        check(close(it2.getFlatness(), 0.5), "getFlatness == 0.5");
        check(it2.getRecursionLimit() == 3, "getRecursionLimit == 3");

        // already-flat rectangle passes through as moveTo/lineTo/close
        Rectangle2D r = new Rectangle2D.Double(0, 0, 10, 10);
        FlatteningPathIterator it3 = new FlatteningPathIterator(r.getPathIterator(null), 0.5);
        int move = 0, line = 0, closeSeg = 0;
        while (!it3.isDone()) {
            int t = it3.currentSegment(coords);
            if (t == PathIterator.SEG_MOVETO) move++;
            else if (t == PathIterator.SEG_LINETO) line++;
            else if (t == PathIterator.SEG_CLOSE) closeSeg++;
            it3.next();
        }
        check(move == 1 && closeSeg == 1 && line == 4, "rectangle: 1 moveTo, 4 lineTo, 1 close");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

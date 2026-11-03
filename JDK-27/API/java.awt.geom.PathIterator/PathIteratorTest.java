/*
 * @test
 * @summary java.awt.geom.PathIterator conformance: white-box structure plus black-box behavior.
 * @run main PathIteratorTest
 */

import java.awt.geom.*;
import java.lang.reflect.*;

public class PathIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.PathIterator");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        Field[] fields = {
            c.getField("WIND_EVEN_ODD"), c.getField("WIND_NON_ZERO"),
            c.getField("SEG_MOVETO"), c.getField("SEG_LINETO"),
            c.getField("SEG_QUADTO"), c.getField("SEG_CUBICTO"), c.getField("SEG_CLOSE")
        };
        for (Field f : fields) {
            int fm = f.getModifiers();
            check(Modifier.isPublic(fm) && Modifier.isStatic(fm) && Modifier.isFinal(fm)
                  && f.getType() == int.class, "constant " + f.getName() + " public static final int");
        }
        check(hasMethod(c, "getWindingRule"), "getWindingRule()");
        check(hasMethod(c, "isDone"), "isDone()");
        check(hasMethod(c, "next"), "next()");
        check(hasMethod(c, "currentSegment", float[].class), "currentSegment(float[])");
        check(hasMethod(c, "currentSegment", double[].class), "currentSegment(double[])");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(PathIterator.WIND_EVEN_ODD == 0, "WIND_EVEN_ODD == 0");
        check(PathIterator.WIND_NON_ZERO == 1, "WIND_NON_ZERO == 1");
        check(PathIterator.SEG_MOVETO == 0, "SEG_MOVETO == 0");
        check(PathIterator.SEG_LINETO == 1, "SEG_LINETO == 1");
        check(PathIterator.SEG_QUADTO == 2, "SEG_QUADTO == 2");
        check(PathIterator.SEG_CUBICTO == 3, "SEG_CUBICTO == 3");
        check(PathIterator.SEG_CLOSE == 4, "SEG_CLOSE == 4");

        Path2D.Double p = new Path2D.Double();
        p.moveTo(0, 0);
        p.lineTo(10, 0);
        p.lineTo(0, 10);
        p.closePath();
        PathIterator it = p.getPathIterator(null);
        check(it.getWindingRule() == PathIterator.WIND_NON_ZERO, "Path2D default WIND_NON_ZERO");

        double[] coords = new double[6];
        double[] moveCoords = new double[2];
        int[] types = new int[4];
        int n = 0;
        while (!it.isDone() && n < types.length) {
            types[n] = it.currentSegment(coords);
            if (types[n] == PathIterator.SEG_MOVETO) {
                moveCoords[0] = coords[0];
                moveCoords[1] = coords[1];
            }
            it.next();
            n++;
        }
        check(n == 4, "triangle iterates 4 segments");
        check(types[0] == PathIterator.SEG_MOVETO && types[1] == PathIterator.SEG_LINETO
              && types[2] == PathIterator.SEG_LINETO && types[3] == PathIterator.SEG_CLOSE,
              "segment sequence MOVETO,LINETO,LINETO,CLOSE");
        check(close(moveCoords[0], 0) && close(moveCoords[1], 0), "moveto coords");

        // currentSegment(float[]) fills float coords
        float[] fcoords = new float[6];
        PathIterator it2 = p.getPathIterator(null);
        int t = it2.currentSegment(fcoords);
        check(t == PathIterator.SEG_MOVETO && fcoords[0] == 0.0f && fcoords[1] == 0.0f,
              "currentSegment(float[]) fills float coords");

        // anonymous implementation of the interface behaves predictably
        PathIterator anon = new PathIterator() {
            int i = 0;
            public int getWindingRule() { return WIND_EVEN_ODD; }
            public boolean isDone() { return i >= 1; }
            public void next() { i++; }
            public int currentSegment(float[] c) { return SEG_MOVETO; }
            public int currentSegment(double[] c) { return SEG_MOVETO; }
        };
        check(anon.getWindingRule() == PathIterator.WIND_EVEN_ODD, "anon getWindingRule");
        check(!anon.isDone() && anon.currentSegment(fcoords) == PathIterator.SEG_MOVETO, "anon first segment");
        anon.next();
        check(anon.isDone(), "anon done after next");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

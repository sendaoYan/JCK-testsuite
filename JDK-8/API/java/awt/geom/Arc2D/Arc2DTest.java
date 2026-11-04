/*
 * @test
 * @summary java.awt.geom.Arc2D conformance: white-box structure plus black-box behavior.
 * @run main Arc2DTest
 */

import java.awt.Shape;
import java.awt.geom.*;
import java.lang.reflect.*;

public class Arc2DTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean close(double a, double b) { return Math.abs(a - b) < 1e-9; }
    static boolean closeLoose(double a, double b) { return Math.abs(a - b) < 1e-6; }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.geom.Arc2D");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "Arc2D is public");
        check(Modifier.isAbstract(m), "Arc2D is abstract");
        check(c.getSuperclass() == RectangularShape.class, "Arc2D extends RectangularShape");
        check(Shape.class.isAssignableFrom(c), "Arc2D implements Shape");
        check(Arc2D.OPEN == 0 && Arc2D.CHORD == 1 && Arc2D.PIE == 2, "arc type constants OPEN/CHORD/PIE");
        check(hasMethod(c, "getAngleStart") && hasMethod(c, "getAngleExtent"), "abstract angle getters");
        check(hasMethod(c, "getArcType"), "getArcType");
        check(hasMethod(c, "getStartPoint") && hasMethod(c, "getEndPoint"), "start/end points");
        check(hasMethod(c, "setArcByCenter", double.class, double.class, double.class, double.class, double.class, int.class), "setArcByCenter");
        check(hasMethod(c, "setArcByTangent", Point2D.class, Point2D.class, Point2D.class, double.class), "setArcByTangent");
        check(hasMethod(c, "setAngles", double.class, double.class, double.class, double.class), "setAngles");
        check(hasMethod(c, "containsAngle", double.class), "containsAngle");
        check(hasMethod(c, "setArcType", int.class), "setArcType");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // setArcByCenter angle semantics
        Arc2D.Double c = new Arc2D.Double();
        c.setArcByCenter(0, 0, 10, 0, 90, Arc2D.OPEN);
        check(close(c.getX(), -10) && close(c.getY(), -10) && close(c.getWidth(), 20) && close(c.getHeight(), 20), "setArcByCenter frame");
        check(close(c.getAngleStart(), 0) && close(c.getAngleExtent(), 90), "setArcByCenter angles");
        check(close(c.getStartPoint().getX(), 10) && close(c.getStartPoint().getY(), 0), "start point at angle 0");
        check(close(c.getEndPoint().getX(), 0) && close(c.getEndPoint().getY(), -10), "end point at angle 90");

        // negative extent goes the other way
        Arc2D.Double n = new Arc2D.Double();
        n.setArcByCenter(0, 0, 10, 0, -90, Arc2D.OPEN);
        check(close(n.getAngleExtent(), -90), "negative extent preserved");
        check(close(n.getEndPoint().getX(), 0) && close(n.getEndPoint().getY(), 10), "negative extent end point");

        // arc type / contains
        Arc2D.Double pie = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.PIE);
        check(pie.getArcType() == Arc2D.PIE, "getArcType PIE");
        check(pie.contains(10, 10), "PIE contains center");
        Arc2D.Double chord = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.CHORD);
        check(!chord.contains(10, 10), "CHORD excludes center");
        Arc2D.Double open = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        check(!open.contains(10, 10), "OPEN excludes center");

        // containsAngle
        check(pie.containsAngle(45), "containsAngle inside");
        check(!pie.containsAngle(120), "containsAngle outside");

        // setArcType
        Arc2D.Double t = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        t.setArcType(Arc2D.PIE);
        check(t.getArcType() == Arc2D.PIE, "setArcType");
        check(t.contains(10, 10), "setArcType changes containment");

        // setAngles
        Arc2D.Double sa = new Arc2D.Double(0, 0, 20, 20, 0, 0, Arc2D.OPEN);
        sa.setAngles(20, 10, 10, 0);
        check(close(sa.getAngleStart(), 0) && close(sa.getAngleExtent(), 90), "setAngles(x1,y1,x2,y2)");
        Arc2D.Double sa2 = new Arc2D.Double(0, 0, 20, 20, 0, 0, Arc2D.OPEN);
        sa2.setAngles(new Point2D.Double(20, 10), new Point2D.Double(10, 0));
        check(close(sa2.getAngleStart(), 0) && close(sa2.getAngleExtent(), 90), "setAngles(Point2D,Point2D)");

        // setAngleStart(Point2D)
        Arc2D.Double sp = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        sp.setAngleStart(new Point2D.Double(20, 10)); // right of center -> angle 0
        check(close(sp.getAngleStart(), 0), "setAngleStart(Point2D)");

        // setArc overloads
        Arc2D.Double viaDim = new Arc2D.Double();
        viaDim.setArc(new Point2D.Double(0, 0), new java.awt.Dimension(10, 20), 0, 90, Arc2D.OPEN);
        check(close(viaDim.getWidth(), 10) && close(viaDim.getHeight(), 20) && close(viaDim.getAngleExtent(), 90), "setArc(Point2D,Dimension2D)");
        Arc2D.Double viaRect = new Arc2D.Double();
        viaRect.setArc(new Rectangle2D.Double(1, 2, 30, 40), 10, 80, Arc2D.CHORD);
        check(close(viaRect.getX(), 1) && close(viaRect.getWidth(), 30) && close(viaRect.getAngleStart(), 10) && close(viaRect.getAngleExtent(), 80), "setArc(Rectangle2D)");
        Arc2D.Double viaArc = new Arc2D.Double();
        viaArc.setArc(viaRect);
        check(viaArc.equals(viaRect), "setArc(Arc2D) copies");

        // bounds
        Rectangle2D b = open.getBounds2D();
        check(closeLoose(b.getX(), 10) && closeLoose(b.getY(), 0), "open arc bounds origin");
        check(closeLoose(b.getWidth(), 10) && closeLoose(b.getHeight(), 10), "open arc bounds size");

        // path iterator for PIE
        PathIterator it = pie.getPathIterator(null);
        double[] cc = new double[6];
        check(it.currentSegment(cc) == PathIterator.SEG_MOVETO, "PIE iterator starts MOVETO");
        boolean sawCubic = false, sawLine = false, sawClose = false;
        while (!it.isDone()) {
            int seg = it.currentSegment(cc);
            if (seg == PathIterator.SEG_CUBICTO) sawCubic = true;
            if (seg == PathIterator.SEG_LINETO) sawLine = true;
            if (seg == PathIterator.SEG_CLOSE) sawClose = true;
            it.next();
        }
        check(sawCubic && sawLine && sawClose, "PIE iterator has CUBICTO + LINETO + CLOSE");

        // equals / hashCode
        Arc2D.Double same = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.PIE);
        check(pie.equals(same), "equal arcs");
        check(pie.hashCode() == same.hashCode(), "equal arcs same hashCode");
        check(!pie.equals(open), "different arc type not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

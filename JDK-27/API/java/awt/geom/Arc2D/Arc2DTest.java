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

    static void expectThrows(Class<? extends RuntimeException> ex, Runnable r, String msg) {
        try {
            r.run();
            check(false, msg + " (no exception)");
        } catch (RuntimeException e) {
            check(ex.isAssignableFrom(e.getClass()), msg + " (got " + e.getClass().getSimpleName() + ")");
        }
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

    // ===================== 契约深化：setArcByTangent/setArcByCenter 退化、类型非法、containsAngle 边界 =====================
    static void testArcContracts() {
        // setArcByCenter 负半径：不抛异常，得到负宽高（isEmpty）。
        Arc2D.Double nc = new Arc2D.Double();
        nc.setArcByCenter(0, 0, -10, 0, 90, Arc2D.OPEN);
        check(nc.isEmpty(), "setArcByCenter negative radius -> empty");
        check(nc.getX() == 10.0 && nc.getWidth() == -20.0, "setArcByCenter negative radius frame (x+10, width -20)");

        // setArcByTangent 直角相切：起始角 -270、extent -90，起/终点恰为 p1/p3。
        Arc2D.Double tan = new Arc2D.Double();
        tan.setArcByTangent(new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(10, 10), 10);
        check(tan.getArcType() == Arc2D.OPEN, "setArcByTangent keeps arc type");
        check(close(tan.getAngleStart(), -270) && close(tan.getAngleExtent(), -90), "setArcByTangent angles (-270, -90)");
        check(close(tan.getX(), -10) && close(tan.getY(), 0) && close(tan.getWidth(), 20) && close(tan.getHeight(), 20),
              "setArcByTangent frame centered at (10,10)");
        check(close(tan.getStartPoint().getX(), 0) && close(tan.getStartPoint().getY(), 0),
              "setArcByTangent start point == p1");
        check(close(tan.getEndPoint().getX(), 10) && close(tan.getEndPoint().getY(), 10),
              "setArcByTangent end point == p3");

        // setArcByTangent 共线三点（平行直线）：不抛异常，保持非空。
        Arc2D.Double par = new Arc2D.Double();
        par.setArcByTangent(new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(20, 0), 10);
        check(!par.isEmpty(), "setArcByTangent collinear points do not throw");

        // setArcByTangent 负半径：不抛异常，退化为空。
        Arc2D.Double nr = new Arc2D.Double();
        nr.setArcByTangent(new Point2D.Double(0, 0), new Point2D.Double(10, 0), new Point2D.Double(10, 10), -10);
        check(nr.isEmpty(), "setArcByTangent negative radius -> empty (no exception)");

        // setArcType / 构造器非法类型抛 IllegalArgumentException。
        expectThrows(IllegalArgumentException.class, new Runnable() { public void run() { new Arc2D.Double().setArcType(3); } },
                     "setArcType(3) throws IllegalArgumentException");
        expectThrows(IllegalArgumentException.class, new Runnable() { public void run() { new Arc2D.Double().setArcType(-1); } },
                     "setArcType(-1) throws IllegalArgumentException");
        expectThrows(IllegalArgumentException.class, new Runnable() { public void run() { new Arc2D.Double(0, 0, 10, 10, 0, 90, 5); } },
                     "Arc2D ctor invalid type throws IllegalArgumentException");

        // containsAngle：起始角含、终止角不含；360/-270 归一化；NaN false。
        Arc2D.Double ca = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        check(ca.containsAngle(0), "containsAngle(start) inclusive");
        check(!ca.containsAngle(90), "containsAngle(end) exclusive");
        check(ca.containsAngle(360), "containsAngle(360) wraps to 0");
        check(!ca.containsAngle(-270), "containsAngle(-270) wraps to 90 -> false");
        check(!ca.containsAngle(Double.NaN), "containsAngle(NaN) false");

        // 负 extent 反向：起点含、终点不含、对侧不含。
        Arc2D.Double cn = new Arc2D.Double(0, 0, 20, 20, 0, -90, Arc2D.OPEN);
        check(cn.containsAngle(0), "negative extent containsAngle(start)");
        check(cn.containsAngle(-45), "negative extent containsAngle(mid)");
        check(!cn.containsAngle(-90), "negative extent containsAngle(end) exclusive");
        check(!cn.containsAngle(45), "negative extent containsAngle(opposite) false");

        // 整圆 extent >= 360：任何角（含 NaN）都含。
        Arc2D.Double cf = new Arc2D.Double(0, 0, 20, 20, 0, 360, Arc2D.OPEN);
        check(cf.containsAngle(123.456), "full circle contains any angle");
        check(cf.containsAngle(Double.NaN), "full circle contains NaN angle");

        // setAngleStart 不归一化。
        Arc2D.Double sn = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        sn.setAngleStart(370);
        check(sn.getAngleStart() == 370.0, "setAngleStart does not normalize");

        // 起/终点恒返回 Point2D.Double。
        Arc2D.Double sp = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        check(sp.getStartPoint() instanceof Point2D.Double, "getStartPoint returns Point2D.Double");
        check(sp.getEndPoint() instanceof Point2D.Double, "getEndPoint returns Point2D.Double");

        // 负宽弧：isEmpty，getBounds2D 保留负宽（Arc2D 不归一化）。
        Arc2D.Double neg = new Arc2D.Double(0, 0, -10, 20, 0, 90, Arc2D.OPEN);
        check(neg.isEmpty(), "negative width arc isEmpty");
        check(neg.getBounds2D().getWidth() == -10.0 && neg.getBounds2D().getHeight() == 20.0,
              "negative width arc getBounds2D preserves negative width");

        // PIE/CHORD/OPEN 包含区域：PIE 含起始半径内侧点，CHORD 排除该侧但含弦外区域。
        Arc2D.Double pie2 = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.PIE);
        Arc2D.Double chord2 = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.CHORD);
        Arc2D.Double open2 = new Arc2D.Double(0, 0, 20, 20, 0, 90, Arc2D.OPEN);
        check(pie2.contains(18, 10), "PIE contains start-radius interior point");
        check(!pie2.contains(20, 10), "PIE excludes boundary point");
        check(!chord2.contains(18, 10), "CHORD excludes pie-triangle side");
        check(chord2.contains(18, 6), "CHORD contains cap region beyond chord");
        check(open2.contains(18, 6), "OPEN contains cap region (same as CHORD)");

        // setFrame 保留角度与类型。
        Arc2D.Double sf = new Arc2D.Double(0, 0, 20, 20, 30, 60, Arc2D.CHORD);
        sf.setFrame(5, 6, 7, 8);
        check(sf.getX() == 5 && sf.getY() == 6 && sf.getWidth() == 7 && sf.getHeight() == 8, "setFrame updates frame");
        check(sf.getAngleStart() == 30 && sf.getAngleExtent() == 60 && sf.getArcType() == Arc2D.CHORD,
              "setFrame preserves angles and type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testArcContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

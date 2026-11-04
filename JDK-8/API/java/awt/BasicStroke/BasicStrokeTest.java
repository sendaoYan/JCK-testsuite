/*
 * @test
 * @summary java.awt.BasicStroke conformance: width/join/cap.
 * @run main BasicStrokeTest
 */


import java.awt.BasicStroke;
public class BasicStrokeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        BasicStroke s = new BasicStroke(2.5f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_BEVEL);
        check(s.getLineWidth() == 2.5f, "line width");
        check(s.getEndCap() == BasicStroke.CAP_ROUND, "end cap");
        check(s.getLineJoin() == BasicStroke.JOIN_BEVEL, "line join");
        check(s.getMiterLimit() == 10.0f, "default miter limit");
        check(s.getDashArray() == null, "no dash array default");
        BasicStroke def = new BasicStroke();
        check(def.getLineWidth() == 1.0f && def.getEndCap() == BasicStroke.CAP_SQUARE, "defaults");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // defaults
        java.awt.BasicStroke dflt = new java.awt.BasicStroke();
        check(dflt.getLineWidth() == 1.0f, "default width 1.0");
        check(dflt.getEndCap() == java.awt.BasicStroke.CAP_SQUARE, "default cap SQUARE (per javadoc)");
        check(dflt.getLineJoin() == java.awt.BasicStroke.JOIN_MITER, "default join MITER");
        check(dflt.getMiterLimit() == 10.0f, "default miterlimit 10");
        check(dflt.getDashArray() == null, "default no dash");
        check(dflt.getDashPhase() == 0.0f, "default dash phase 0");
        // full ctor reads back
        float[] dash = {5f, 3f};
        java.awt.BasicStroke bs = new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_BEVEL, 4.5f, dash, 1.5f);
        check(bs.getLineWidth() == 2.5f, "width readback");
        check(bs.getEndCap() == java.awt.BasicStroke.CAP_ROUND, "cap readback");
        check(bs.getLineJoin() == java.awt.BasicStroke.JOIN_BEVEL, "join readback");
        check(bs.getMiterLimit() == 4.5f, "miterlimit readback");
        check(bs.getDashPhase() == 1.5f, "dash phase readback");
        float[] got = bs.getDashArray();
        check(got.length == 2 && got[0] == 5f && got[1] == 3f, "dash array readback");
        // getDashArray returns defensive copy
        got[0] = 99f;
        check(bs.getDashArray()[0] == 5f, "getDashArray defensive copy");
        dash[0] = 77f;
        check(bs.getDashArray()[0] == 5f, "ctor copies dash array");
        // constants
        check(java.awt.BasicStroke.JOIN_MITER == 0 && java.awt.BasicStroke.JOIN_ROUND == 1
              && java.awt.BasicStroke.JOIN_BEVEL == 2, "join constants");
        check(java.awt.BasicStroke.CAP_BUTT == 0 && java.awt.BasicStroke.CAP_ROUND == 1
              && java.awt.BasicStroke.CAP_SQUARE == 2, "cap constants");
        // exceptions with messages
        RuntimeException ex = null;
        try { new java.awt.BasicStroke(-1f); } catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "negative width".equals(ex.getMessage()), "negative width IAE message: " + ex);
        ex = null;
        try { new java.awt.BasicStroke(1f, 7, 0); } catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "illegal end cap value".equals(ex.getMessage()), "bad cap IAE message");
        ex = null;
        try { new java.awt.BasicStroke(1f, 0, 7); } catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "illegal line join value".equals(ex.getMessage()), "bad join IAE message");
        ex = null;
        try { new java.awt.BasicStroke(1f, 0, 0, 0.5f); } catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "miter limit < 1".equals(ex.getMessage()), "miterlimit <1 IAE message");
        ex = null;
        try { new java.awt.BasicStroke(1f, 0, 0, 10f, new float[]{0f, 0f}, 0f); }
        catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "dash lengths all zero".equals(ex.getMessage()), "all-zero dash IAE message");
        ex = null;
        try { new java.awt.BasicStroke(1f, 0, 0, 10f, new float[]{5f, 3f}, -0.1f); }
        catch (java.lang.IllegalArgumentException e) { ex = e; }
        check(ex != null && "negative dash phase".equals(ex.getMessage()), "negative dash phase IAE: " + (ex==null?"":ex.getMessage()));
        // observed: phase >= dash cycle sum is NOT rejected by the constructor
        // (javadoc states phase must be < sum, implementation accepts it - locked by observation)
        new java.awt.BasicStroke(1f, 0, 0, 10f, new float[]{5f, 3f}, 9f);
        new java.awt.BasicStroke(1f, 0, 0, 10f, new float[]{5f, 3f}, 100f);
        check(true, "dash phase beyond cycle accepted (observed)");
        new java.awt.BasicStroke(1f, 0, 0, 10f, new float[]{5f, 3f}, 0f);
        check(true, "dash phase 0 legal");
        // width 0 is legal (thinnest)
        check(new java.awt.BasicStroke(0f).getLineWidth() == 0f, "width 0 legal");
        // createStrokedShape: stroked rect grows by width/2 on all sides, exact bounds
        java.awt.BasicStroke w2 = new java.awt.BasicStroke(2f);
        java.awt.Shape s = w2.createStrokedShape(new java.awt.geom.Rectangle2D.Double(10, 10, 50, 50));
        java.awt.geom.Rectangle2D b = s.getBounds2D();
        check(b.getX() == 9.0 && b.getY() == 9.0 && b.getWidth() == 52.0 && b.getHeight() == 52.0,
              "stroked bounds exact 9,9,52,52: " + b);
        check(s.getBounds().x == 9 && s.getBounds().width == 52, "stroked int bounds");
        // wider stroke grows proportionally
        java.awt.Shape s4 = new java.awt.BasicStroke(4f).createStrokedShape(new java.awt.geom.Rectangle2D.Double(10, 10, 50, 50));
        check(s4.getBounds2D().getX() == 8.0 && s4.getBounds2D().getWidth() == 54.0, "width-4 stroke bounds 8,54");
        // stroking a degenerate point (line of zero length) still yields a shape
        java.awt.Shape point = new java.awt.BasicStroke(2f).createStrokedShape(new java.awt.geom.Line2D.Double(5, 5, 5, 5));
        check(point.getBounds2D().getWidth() >= 2.0 || point.getBounds2D().getHeight() >= 2.0,
              "zero-length line stroked to non-empty: " + point.getBounds2D());
        // BasicStroke is not Serializable (state kept in fields but no interface)
        check(!(java.io.Serializable.class.isAssignableFrom(java.awt.BasicStroke.class)),
              "BasicStroke not Serializable");
        check(!(java.awt.Stroke.class.isAssignableFrom(java.awt.BasicStroke.class) == false),
              "implements Stroke");
    }

}

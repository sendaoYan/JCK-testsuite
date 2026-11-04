/*
 * @test
 * @summary java.awt.BasicStroke conformance: white-box structure + black-box behavior.
 * @run main BasicStrokeTest
 */

import java.lang.reflect.*;

public class BasicStrokeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.BasicStroke", false, BasicStrokeTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.BasicStroke");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.awt.Stroke.class.isAssignableFrom(c), "implements java.awt.Stroke");

        check(c.getField("JOIN_MITER").getType() == int.class, "static int JOIN_MITER");
        check(c.getField("CAP_BUTT").getType() == int.class, "static int CAP_BUTT");

        check(c.getConstructor() != null, "ctor ()");
        check(c.getConstructor(float.class) != null, "ctor (float)");
        check(c.getConstructor(float.class, int.class, int.class) != null, "ctor (float,int,int)");
        check(c.getConstructor(float.class, int.class, int.class, float.class) != null,
              "ctor (float,int,int,float)");
        check(c.getConstructor(float.class, int.class, int.class, float.class, float[].class, float.class) != null,
              "ctor (float,int,int,float,float[],float)");

        for (String name : new String[] {"createStrokedShape", "getLineWidth", "getEndCap",
                "getLineJoin", "getMiterLimit", "getDashArray", "getDashPhase", "equals", "hashCode"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // constants
        check(java.awt.BasicStroke.JOIN_MITER == 0, "JOIN_MITER == 0");
        check(java.awt.BasicStroke.JOIN_ROUND == 1, "JOIN_ROUND == 1");
        check(java.awt.BasicStroke.JOIN_BEVEL == 2, "JOIN_BEVEL == 2");
        check(java.awt.BasicStroke.CAP_BUTT == 0, "CAP_BUTT == 0");
        check(java.awt.BasicStroke.CAP_ROUND == 1, "CAP_ROUND == 1");
        check(java.awt.BasicStroke.CAP_SQUARE == 2, "CAP_SQUARE == 2");

        // default + convenience constructors
        java.awt.BasicStroke def = new java.awt.BasicStroke();
        check(def.getLineWidth() == 1.0f, "default width 1.0");
        check(def.getEndCap() == java.awt.BasicStroke.CAP_SQUARE, "default cap SQUARE");
        check(def.getLineJoin() == java.awt.BasicStroke.JOIN_MITER, "default join MITER");
        check(def.getMiterLimit() == 10.0f, "default miterlimit 10.0");
        check(def.getDashArray() == null, "default no dash");
        check(def.getDashPhase() == 0.0f, "default dash phase 0");

        java.awt.BasicStroke w = new java.awt.BasicStroke(2.5f);
        check(w.getLineWidth() == 2.5f && w.getEndCap() == java.awt.BasicStroke.CAP_SQUARE
              && w.getLineJoin() == java.awt.BasicStroke.JOIN_MITER, "(float) defaults cap/join");

        java.awt.BasicStroke cj = new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND,
                                                           java.awt.BasicStroke.JOIN_ROUND);
        check(cj.getEndCap() == java.awt.BasicStroke.CAP_ROUND, "(float,int,int) cap");
        check(cj.getLineJoin() == java.awt.BasicStroke.JOIN_ROUND, "(float,int,int) join");
        check(cj.getMiterLimit() == 10.0f, "(float,int,int) default miter");

        java.awt.BasicStroke ml = new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND,
                                                           java.awt.BasicStroke.JOIN_ROUND, 3.0f);
        check(ml.getMiterLimit() == 3.0f, "(float,int,int,float) miterlimit");

        // dashed stroke
        java.awt.BasicStroke dashed = new java.awt.BasicStroke(2.5f, java.awt.BasicStroke.CAP_ROUND,
                java.awt.BasicStroke.JOIN_ROUND, 3.0f, new float[] {2f, 3f, 4f}, 1.5f);
        float[] da = dashed.getDashArray();
        check(da.length == 3 && da[0] == 2f && da[1] == 3f && da[2] == 4f, "getDashArray values");
        check(dashed.getDashPhase() == 1.5f, "getDashPhase");

        // getDashArray returns clone (mutation not shared)
        da[0] = 99f;
        check(dashed.getDashArray()[0] == 2f, "getDashArray returns independent copy");

        // validation
        check(throwsIllegalArgument(new float[] { }), "empty dash array invalid");
        boolean iaeNegWidth = false;
        try { new java.awt.BasicStroke(-1.0f); } catch (IllegalArgumentException e) { iaeNegWidth = true; }
        check(iaeNegWidth, "negative width throws");
        boolean iaeBadCap = false;
        try { new java.awt.BasicStroke(1f, 999, java.awt.BasicStroke.JOIN_MITER); }
        catch (IllegalArgumentException e) { iaeBadCap = true; }
        check(iaeBadCap, "bad cap throws");
        boolean iaeBadJoin = false;
        try { new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, 999); }
        catch (IllegalArgumentException e) { iaeBadJoin = true; }
        check(iaeBadJoin, "bad join throws");
        boolean iaeMiter = false;
        try { new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER, 0.5f); }
        catch (IllegalArgumentException e) { iaeMiter = true; }
        check(iaeMiter, "miterlimit < 1 with MITER throws");
        boolean iaeNegDash = false;
        try { new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER,
                                       10f, new float[] {1f, -2f}, 0f); }
        catch (IllegalArgumentException e) { iaeNegDash = true; }
        check(iaeNegDash, "negative dash length throws");
        boolean iaePhase = false;
        try { new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT, java.awt.BasicStroke.JOIN_MITER,
                                       10f, new float[] {1f}, -1f); }
        catch (IllegalArgumentException e) { iaePhase = true; }
        check(iaePhase, "negative dash phase throws");

        // equals / hashCode
        java.awt.BasicStroke s1 = new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND,
                                                           java.awt.BasicStroke.JOIN_BEVEL, 5f,
                                                           new float[] {2f, 1f}, 0.5f);
        java.awt.BasicStroke s2 = new java.awt.BasicStroke(2f, java.awt.BasicStroke.CAP_ROUND,
                                                           java.awt.BasicStroke.JOIN_BEVEL, 5f,
                                                           new float[] {2f, 1f}, 0.5f);
        check(s1.equals(s2), "equal strokes");
        check(s1.hashCode() == s2.hashCode(), "equal hashCodes");
        check(!s1.equals(new java.awt.BasicStroke(3f)), "different width not equal");
        check(!s1.equals(new java.awt.BasicStroke(2f)), "different dash not equal");
        check(!s1.equals(null), "not equal null");
        check(!s1.equals("stroke"), "not equal unrelated type");

        // createStrokedShape
        java.awt.geom.Rectangle2D rect = new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10);
        java.awt.Shape stroked = s1.createStrokedShape(rect);
        check(stroked != null, "createStrokedShape non-null");
        check(stroked.getBounds2D().getWidth() > 10.0, "stroked rectangle wider than source");
        java.awt.Shape line = s1.createStrokedShape(new java.awt.geom.Line2D.Double(0, 0, 10, 0));
        check(line != null && !line.getBounds2D().isEmpty(), "stroked line non-empty");
    }

    static boolean throwsIllegalArgument(float[] dash) {
        try { new java.awt.BasicStroke(1f, java.awt.BasicStroke.CAP_BUTT,
                                        java.awt.BasicStroke.JOIN_MITER, 10f, dash, 0f); }
        catch (IllegalArgumentException e) { return true; }
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

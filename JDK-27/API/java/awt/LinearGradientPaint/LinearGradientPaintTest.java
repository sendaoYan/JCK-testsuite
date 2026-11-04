/*
 * @test
 * @summary java.awt.LinearGradientPaint conformance: white-box structure + black-box behavior.
 * @run main LinearGradientPaintTest
 */

import java.lang.reflect.*;

public class LinearGradientPaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.LinearGradientPaint", false, LinearGradientPaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.LinearGradientPaint");
        int m = c.getModifiers();
        check(Modifier.isPublic(m) || Modifier.isProtected(m) || c.isMemberClass(), "public/protected/member");
        if (c.isEnum()) check(c.getEnumConstants() != null, "enum has constants");
        else check(c.getEnumConstants() == null, "non-enum returns null constants");
        check(c.getDeclaredConstructors() != null, "constructors array non-null");
        check(c.getDeclaredMethods() != null, "methods array non-null");
        check(c.getDeclaredFields() != null, "fields array non-null");
        if (c.isInterface()) check(Modifier.isInterface(m), "interface modifier consistent");
        else if (c != Object.class) check(c.getSuperclass() != null, "class superclass non-null");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getSuperclass().getName().equals("java.awt.MultipleGradientPaint"), "extends MultipleGradientPaint");
        check(java.awt.Paint.class.isAssignableFrom(c) && java.awt.Transparency.class.isAssignableFrom(c), "is Paint + Transparency");
    }

    static void testBlackBox() throws Exception {
        float[] fr = {0f, 1f};
        java.awt.Color[] cols = {java.awt.Color.RED, java.awt.Color.BLUE};
        java.awt.LinearGradientPaint p = new java.awt.LinearGradientPaint(0f, 0f, 10f, 10f, fr, cols);
        check(p.getStartPoint().getX() == 0.0 && p.getStartPoint().getY() == 0.0, "start point");
        check(p.getEndPoint().getX() == 10.0 && p.getEndPoint().getY() == 10.0, "end point");
        check(p.getFractions() != null && p.getFractions().length == 2, "fractions length");
        check(p.getColors() != null && p.getColors().length == 2, "colors length");
        check(java.awt.Color.RED.equals(p.getColors()[0]), "colors[0]");
        check(p.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "default cycle NO_CYCLE");
        check(p.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "default sRGB");
        check(p.getTransform() != null, "transform non-null");
        check(p.getTransparency() == java.awt.Transparency.OPAQUE, "opaque transparency");
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
        java.awt.Rectangle dev = new java.awt.Rectangle(0, 0, 10, 10);
        java.awt.geom.Rectangle2D user = new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10);
        java.awt.geom.AffineTransform xf = new java.awt.geom.AffineTransform();
        check(p.createContext(cm, dev, user, xf, new java.awt.RenderingHints(null)) != null, "createContext non-null");
        java.awt.LinearGradientPaint p2 = new java.awt.LinearGradientPaint(0f, 0f, 10f, 10f, fr, cols, java.awt.MultipleGradientPaint.CycleMethod.REFLECT);
        check(p2.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.REFLECT, "cycle REFLECT");
        java.awt.LinearGradientPaint p3 = new java.awt.LinearGradientPaint(new java.awt.geom.Point2D.Double(1,2), new java.awt.geom.Point2D.Double(3,4), fr, cols);
        check(p3.getStartPoint().getX() == 1.0 && p3.getEndPoint().getY() == 4.0, "Point2D ctor");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        java.awt.Color[] cols = {java.awt.Color.red, java.awt.Color.green, java.awt.Color.blue};
        float[] fr = {0f, 0.5f, 1f};
        java.awt.LinearGradientPaint ok = new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, fr, cols);
        check(ok.getStartPoint().getX() == 0.0 && ok.getEndPoint().getX() == 10.0, "start/end points");
        check(ok.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "default NO_CYCLE");
        check(ok.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "default SRGB");
        check(ok.getTransform().isIdentity(), "default identity transform");
        java.awt.Color[] back = ok.getColors();
        check(back.length == 3 && back[0].equals(java.awt.Color.red), "getColors");
        back[0] = java.awt.Color.yellow;
        check(ok.getColors()[0].equals(java.awt.Color.red), "getColors defensive copy");
        float[] backF = ok.getFractions();
        check(backF.length == 3 && backF[0] == 0f && backF[2] == 1f, "getFractions");
        backF[1] = 0.9f;
        check(ok.getFractions()[1] == 0.5f, "getFractions defensive copy");
        // fractions must be strictly increasing
        boolean iae = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 0.5f, 0.4f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.green, java.awt.Color.blue, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "decreasing fractions -> IAE");
        iae = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 0.5f, 0.5f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.green, java.awt.Color.blue, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "duplicate fractions -> IAE");
        // null colors / fractions
        boolean npe = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, fr, null); }
        catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "null colors -> NPE");
        npe = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, null, cols); }
        catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "null fractions -> NPE");
        // length mismatch
        iae = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 1f},
                new java.awt.Color[]{java.awt.Color.red}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "colors.length != fractions.length -> IAE");
        // fractions outside [0,1]
        iae = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{-0.1f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "fraction < 0 -> IAE");
        iae = false;
        try { new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 1.1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "fraction > 1 -> IAE");
        // first not 0 / last not 1 is permitted (synthetic keyframes added) - observed
        java.awt.LinearGradientPaint loose = new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f,
                new float[]{0.1f, 0.5f, 0.9f}, cols);
        check(loose.getFractions()[0] == 0f && loose.getFractions()[loose.getFractions().length - 1] == 1f,
              "implicit 0/1 keyframes materialized: " + java.util.Arrays.toString(loose.getFractions()));
        check(loose.getColors().length == 5, "implicit keyframes duplicate endpoint colors");
        check(loose.getColors()[0].equals(java.awt.Color.red) && loose.getColors()[4].equals(java.awt.Color.blue),
              "implicit keyframes reuse endpoint colors");
        // cycle methods + color spaces
        java.awt.LinearGradientPaint refl = new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue},
                java.awt.MultipleGradientPaint.CycleMethod.REFLECT);
        check(refl.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.REFLECT, "REFLECT cycle");
        java.awt.LinearGradientPaint lin = new java.awt.LinearGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f),
                new java.awt.geom.Point2D.Float(10f, 0f), new float[]{0f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue},
                java.awt.MultipleGradientPaint.CycleMethod.REPEAT,
                java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB,
                new java.awt.geom.AffineTransform());
        check(lin.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.REPEAT, "REPEAT cycle");
        check(lin.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB, "LINEAR_RGB space");
        // transparency derived from colors
        check(ok.getTransparency() == java.awt.Transparency.OPAQUE, "opaque colors -> OPAQUE");
        java.awt.LinearGradientPaint t2 = new java.awt.LinearGradientPaint(0f, 0f, 10f, 0f, new float[]{0f, 1f},
                new java.awt.Color[]{new java.awt.Color(1, 2, 3, 200), java.awt.Color.blue});
        check(t2.getTransparency() == java.awt.Transparency.TRANSLUCENT, "any translucent -> TRANSLUCENT");
        // degenerate zero-length gradient (p1 == p2) -> IAE
        boolean degThrown = false; String degMsg = null;
        try { new java.awt.LinearGradientPaint(0f, 0f, 0f, 0f, new float[]{0f, 1f},
                new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { degThrown = true; degMsg = e.getMessage(); }
        check(degThrown, "zero-length gradient -> IAE");
        check("Start point cannot equalendpoint".equals(degMsg), "zero-length gradient IAE message: " + degMsg);
        // createContext headless
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        check(ok.createContext(bi.getColorModel(), new java.awt.Rectangle(0, 0, 2, 2),
                new java.awt.geom.Rectangle2D.Double(0, 0, 2, 2), new java.awt.geom.AffineTransform(),
                new java.awt.RenderingHints(null)) != null, "createContext headless");
    }

}

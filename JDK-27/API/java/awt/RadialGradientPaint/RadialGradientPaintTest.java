/*
 * @test
 * @summary java.awt.RadialGradientPaint conformance: white-box structure + black-box behavior.
 * @run main RadialGradientPaintTest
 */

import java.lang.reflect.*;

public class RadialGradientPaintTest {
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
        Class<?> c = Class.forName("java.awt.RadialGradientPaint", false, RadialGradientPaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.RadialGradientPaint");
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
    }

    static void testBlackBox() throws Exception {
        float[] fr = {0f, 1f};
        java.awt.Color[] cols = {java.awt.Color.RED, java.awt.Color.BLUE};
        java.awt.geom.Point2D center = new java.awt.geom.Point2D.Float(5f, 5f);
        java.awt.RadialGradientPaint p = new java.awt.RadialGradientPaint(center, 10f, fr, cols);
        check(p.getCenterPoint().getX() == 5.0 && p.getCenterPoint().getY() == 5.0, "center point");
        check(p.getRadius() == 10.0f, "radius");
        check(p.getFractions().length == 2 && p.getColors().length == 2, "fractions/colors");
        check(p.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "default cycle");
        check(p.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "default sRGB");
        check(p.getFocusPoint() != null, "focus point non-null");
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
        java.awt.Rectangle dev = new java.awt.Rectangle(0, 0, 10, 10);
        java.awt.geom.Rectangle2D user = new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10);
        java.awt.geom.AffineTransform xf = new java.awt.geom.AffineTransform();
        check(p.createContext(cm, dev, user, xf, new java.awt.RenderingHints(null)) != null, "createContext non-null");
        java.awt.RadialGradientPaint p2 = new java.awt.RadialGradientPaint(5f, 5f, 10f, fr, cols);
        check(p2.getRadius() == 10.0f, "float ctor radius");
        check(p2.getCenterPoint().getX() == 5.0, "float ctor center");
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
        java.awt.RadialGradientPaint rgp = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(5f, 5f), 10f, fr, cols);
        check(rgp.getCenterPoint().getX() == 5.0 && rgp.getCenterPoint().getY() == 5.0, "center exact");
        check(rgp.getFocusPoint().equals(rgp.getCenterPoint()), "focus defaults to center");
        check(rgp.getRadius() == 10f, "radius readback");
        check(rgp.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE, "default NO_CYCLE");
        check(rgp.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.SRGB, "default SRGB");
        check(rgp.getColors().length == 3 && rgp.getFractions()[1] == 0.5f, "colors/fractions readback");
        // radius 0 -> IAE
        boolean iae = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), 0f,
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "radius 0 -> IAE");
        iae = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), -1f,
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "negative radius -> IAE");
        // bad fractions
        iae = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), 10f,
                new float[]{0f, 0.5f, 0.4f}, cols); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "unsorted fractions -> IAE");
        boolean npe = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), 10f, fr, null); }
        catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "null colors -> NPE");
        npe = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), 10f, null, cols); }
        catch (java.lang.NullPointerException e) { npe = true; }
        check(npe, "null fractions -> NPE");
        // fraction extremes
        iae = false;
        try { new java.awt.RadialGradientPaint(new java.awt.geom.Point2D.Float(0f, 0f), 10f,
                new float[]{0.2f, 1.2f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue}); }
        catch (java.lang.IllegalArgumentException e) { iae = true; }
        check(iae, "fraction > 1 -> IAE");
        // focus ctor: distinct focus readable; focus distance beyond radius accepted (observed both JDKs)
        java.awt.RadialGradientPaint foc = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(0f, 0f), 10f, new java.awt.geom.Point2D.Float(4f, 0f),
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue},
                java.awt.MultipleGradientPaint.CycleMethod.REPEAT);
        check(foc.getFocusPoint().getX() == 4.0, "explicit focus readback");
        check(foc.getCycleMethod() == java.awt.MultipleGradientPaint.CycleMethod.REPEAT, "cycle method readback");
        java.awt.RadialGradientPaint far = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(0f, 0f), 10f, new java.awt.geom.Point2D.Float(50f, 0f),
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue},
                java.awt.MultipleGradientPaint.CycleMethod.REPEAT);
        check(far.getFocusPoint().getX() == 50.0, "distant focus accepted (observed JDK-8 & JDK-27)");
        // x/y/radius float ctor
        java.awt.RadialGradientPaint f3 = new java.awt.RadialGradientPaint(1f, 2f, 30f, fr, cols);
        check(f3.getCenterPoint().getX() == 1.0 && f3.getRadius() == 30f, "float ctor center/radius");
        // full ctor with ColorSpace + transform
        java.awt.geom.AffineTransform xform = java.awt.geom.AffineTransform.getTranslateInstance(2, 3);
        java.awt.RadialGradientPaint full = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(0f, 0f), 10f, new java.awt.geom.Point2D.Float(0f, 0f),
                new float[]{0f, 1f}, new java.awt.Color[]{java.awt.Color.red, java.awt.Color.blue},
                java.awt.MultipleGradientPaint.CycleMethod.NO_CYCLE,
                java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB, xform);
        check(full.getColorSpace() == java.awt.MultipleGradientPaint.ColorSpaceType.LINEAR_RGB, "full ctor space");
        check(full.getTransform().equals(xform), "transform readback");
        // transparency follows colors
        check(rgp.getTransparency() == java.awt.Transparency.OPAQUE, "opaque -> OPAQUE");
        java.awt.RadialGradientPaint tr = new java.awt.RadialGradientPaint(
                new java.awt.geom.Point2D.Float(5f, 5f), 10f, new float[]{0f, 1f},
                new java.awt.Color[]{new java.awt.Color(9, 9, 9, 100), java.awt.Color.blue});
        check(tr.getTransparency() == java.awt.Transparency.TRANSLUCENT, "translucent -> TRANSLUCENT");
        // createContext headless
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        check(rgp.createContext(bi.getColorModel(), new java.awt.Rectangle(0, 0, 2, 2),
                new java.awt.geom.Rectangle2D.Double(0, 0, 2, 2), new java.awt.geom.AffineTransform(),
                new java.awt.RenderingHints(null)) != null, "createContext headless");
    }

}

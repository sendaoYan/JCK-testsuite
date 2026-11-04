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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

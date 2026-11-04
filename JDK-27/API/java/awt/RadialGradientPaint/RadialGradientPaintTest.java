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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

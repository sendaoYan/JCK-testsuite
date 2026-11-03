/*
 * @test
 * @summary java.awt.GradientPaint conformance: white-box structure + black-box behavior.
 * @run main GradientPaintTest
 */

import java.lang.reflect.*;

public class GradientPaintTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.GradientPaint", false, GradientPaintTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.GradientPaint");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(!Modifier.isAbstract(m), "concrete class");
        check(!c.isInterface(), "not interface");
        check(c.getEnumConstants() == null, "non-enum");
        check(java.awt.Paint.class.isAssignableFrom(c), "implements java.awt.Paint");

        check(c.getConstructor(float.class, float.class, java.awt.Color.class,
                               float.class, float.class, java.awt.Color.class) != null,
              "ctor (float,float,Color,float,float,Color)");
        check(c.getConstructor(java.awt.geom.Point2D.class, java.awt.Color.class,
                               java.awt.geom.Point2D.class, java.awt.Color.class) != null,
              "ctor (Point2D,Color,Point2D,Color)");
        check(c.getConstructor(float.class, float.class, java.awt.Color.class,
                               float.class, float.class, java.awt.Color.class, boolean.class) != null,
              "ctor with cyclic");
        check(c.getConstructor(java.awt.geom.Point2D.class, java.awt.Color.class,
                               java.awt.geom.Point2D.class, java.awt.Color.class, boolean.class) != null,
              "ctor (Point2D,Color,Point2D,Color,cyclic)");

        for (String name : new String[] {"getPoint1", "getColor1", "getPoint2", "getColor2",
                "isCyclic", "createContext", "getTransparency"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(mm -> mm.getName().equals(name)), "method present: " + name);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.Color red = java.awt.Color.RED;
        java.awt.Color blue = java.awt.Color.BLUE;

        // float constructor (acyclic)
        java.awt.GradientPaint gp = new java.awt.GradientPaint(0f, 0f, red, 10f, 20f, blue);
        check(!gp.isCyclic(), "default acyclic");
        java.awt.geom.Point2D p1 = gp.getPoint1();
        check(p1.getX() == 0.0 && p1.getY() == 0.0, "getPoint1 coordinates");
        java.awt.geom.Point2D p2 = gp.getPoint2();
        check(p2.getX() == 10.0 && p2.getY() == 20.0, "getPoint2 coordinates");
        check(gp.getColor1().equals(red), "getColor1 == red");
        check(gp.getColor2().equals(blue), "getColor2 == blue");

        // getPoint1/getPoint2 return copies (mutation not shared)
        p1.setLocation(99, 99);
        check(gp.getPoint1().getX() == 0.0, "getPoint1 returns independent copy");

        // Point2D constructor
        java.awt.geom.Point2D a = new java.awt.geom.Point2D.Double(1.5, 2.5);
        java.awt.geom.Point2D b = new java.awt.geom.Point2D.Double(3.5, 4.5);
        java.awt.GradientPaint gp2 = new java.awt.GradientPaint(a, red, b, blue);
        check(gp2.getPoint1().getX() == 1.5 && gp2.getPoint1().getY() == 2.5, "Point2D ctor point1");
        check(gp2.getPoint2().getX() == 3.5 && gp2.getPoint2().getY() == 4.5, "Point2D ctor point2");

        // cyclic variants
        java.awt.GradientPaint cyc = new java.awt.GradientPaint(0f, 0f, red, 1f, 1f, blue, true);
        check(cyc.isCyclic(), "cyclic ctor true");
        java.awt.GradientPaint cyc2 = new java.awt.GradientPaint(a, red, b, blue, true);
        check(cyc2.isCyclic(), "Point2D cyclic ctor true");

        // null validation
        boolean npe1 = false;
        try { new java.awt.GradientPaint(0f, 0f, null, 1f, 1f, blue); }
        catch (NullPointerException e) { npe1 = true; }
        check(npe1, "null color1 throws NPE");
        boolean npe2 = false;
        try { new java.awt.GradientPaint(a, red, null, blue); }
        catch (NullPointerException e) { npe2 = true; }
        check(npe2, "null point2 throws NPE");

        // getTransparency
        check(gp.getTransparency() == java.awt.Transparency.OPAQUE, "opaque colors -> OPAQUE");
        java.awt.Color translucent = new java.awt.Color(255, 0, 0, 128);
        java.awt.GradientPaint tg = new java.awt.GradientPaint(0f, 0f, translucent, 1f, 1f, blue);
        check(tg.getTransparency() == java.awt.Transparency.TRANSLUCENT,
              "translucent color -> TRANSLUCENT");

        // createContext
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();
        java.awt.PaintContext ctx = gp.createContext(cm,
                new java.awt.Rectangle(0, 0, 10, 10),
                new java.awt.geom.Rectangle2D.Double(0, 0, 10, 10),
                new java.awt.geom.AffineTransform(), null);
        check(ctx != null, "createContext non-null");
        java.awt.image.Raster rast = ctx.getRaster(0, 0, 4, 4);
        check(rast != null && rast.getWidth() == 4 && rast.getHeight() == 4,
              "paint context raster dimensions");
        ctx.dispose();
        check(true, "paint context dispose callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

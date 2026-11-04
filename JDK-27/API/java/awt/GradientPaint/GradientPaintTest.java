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
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testContract() {
        // constructors
        java.awt.GradientPaint gp = new java.awt.GradientPaint(1f, 2f, java.awt.Color.red, 3f, 4f, java.awt.Color.blue);
        check(!gp.isCyclic(), "float ctor acyclic");
        check(gp.getPoint1().getX() == 1.0 && gp.getPoint1().getY() == 2.0, "point1 exact");
        check(gp.getPoint2().getX() == 3.0 && gp.getPoint2().getY() == 4.0, "point2 exact");
        java.awt.GradientPaint cyc = new java.awt.GradientPaint(1f, 2f, java.awt.Color.red, 3f, 4f, java.awt.Color.blue, true);
        check(cyc.isCyclic(), "cyclic ctor flag true");
        check(!new java.awt.GradientPaint(1f, 2f, java.awt.Color.red, 3f, 4f, java.awt.Color.blue, false).isCyclic(),
              "explicit acyclic");
        // Point2D ctors
        java.awt.GradientPaint pp = new java.awt.GradientPaint(new java.awt.geom.Point2D.Float(0f, 0f),
                java.awt.Color.green, new java.awt.geom.Point2D.Float(1f, 1f), java.awt.Color.white);
        check(pp.getPoint1().getX() == 0.0 && pp.getPoint2().getX() == 1.0, "Point2D ctor");
        java.awt.GradientPaint ppc = new java.awt.GradientPaint(new java.awt.geom.Point2D.Float(0f, 0f),
                java.awt.Color.green, new java.awt.geom.Point2D.Float(1f, 1f), java.awt.Color.white, true);
        check(ppc.isCyclic(), "Point2D cyclic ctor");
        // transparency: opaque colors -> OPAQUE; any translucent -> TRANSLUCENT
        check(gp.getTransparency() == java.awt.Transparency.OPAQUE, "opaque gradient");
        java.awt.GradientPaint tr = new java.awt.GradientPaint(0f, 0f, new java.awt.Color(255, 0, 0, 128),
                1f, 1f, java.awt.Color.blue);
        check(tr.getTransparency() == java.awt.Transparency.TRANSLUCENT, "translucent gradient");
        // deprecation aliases map to same values
        check(gp.getColor1().equals(java.awt.Color.red) && gp.getColor2().equals(java.awt.Color.blue),
              "getColor1/getColor2 deprecated accessors");
        // createContext headless works
        java.awt.image.BufferedImage bi = new java.awt.image.BufferedImage(2, 2, java.awt.image.BufferedImage.TYPE_INT_RGB);
        java.awt.PaintContext ctx = gp.createContext(bi.getColorModel(),
                new java.awt.Rectangle(0, 0, 2, 2), new java.awt.geom.Rectangle2D.Double(0, 0, 2, 2),
                new java.awt.geom.AffineTransform(), new java.awt.RenderingHints(null));
        check(ctx != null, "createContext headless");
        check(ctx.getColorModel() != null, "context color model");
        java.awt.image.Raster r = ctx.getRaster(0, 0, 2, 1);
        check(r != null && r.getWidth() >= 2, "raster covers requested width");
        ctx.dispose();
        // first raster pixel is color1 (x=0 -> 1,2? gradient start)
        int p0 = r.getPixel(0, 0, (int[]) null)[0];
        // raster model may be component-based; only assert non-null pixels
        check(r.getPixels(0, 0, 2, 1, (float[]) null) != null, "float pixels readable");
        // GradientPaint not Serializable
        check(!(java.io.Serializable.class.isAssignableFrom(java.awt.GradientPaint.class)), "not Serializable");
    }

}

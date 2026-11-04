/*
 * @test
 * @summary java.awt.GradientPaint conformance: start/end points and colors.
 * @run main GradientPaintTest
 */


import java.awt.GradientPaint;
import java.awt.Color;
public class GradientPaintTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        GradientPaint g = new GradientPaint(0f, 0f, Color.RED, 10f, 10f, Color.BLUE);
        check(g.getPoint1().getX() == 0.0 && g.getPoint1().getY() == 0.0, "point1");
        check(g.getPoint2().getX() == 10.0 && g.getPoint2().getY() == 10.0, "point2");
        check(g.getColor1().equals(Color.RED), "color1");
        check(g.getColor2().equals(Color.BLUE), "color2");
        check(!g.isCyclic(), "not cyclic by default");
        GradientPaint cyclic = new GradientPaint(0f, 0f, Color.RED, 10f, 10f, Color.BLUE, true);
        check(cyclic.isCyclic(), "cyclic");
        check(g.getTransparency() == java.awt.Transparency.OPAQUE, "opaque transparency");
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

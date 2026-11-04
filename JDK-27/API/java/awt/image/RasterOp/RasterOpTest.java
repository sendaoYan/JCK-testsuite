/*
 * @test
 * @summary java.awt.image.RasterOp conformance: white-box structure plus black-box behavior.
 * @run main RasterOpTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.image.AffineTransformOp;
import java.awt.image.BandCombineOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.LookupOp;
import java.awt.image.Raster;
import java.awt.image.RasterOp;
import java.awt.image.RescaleOp;
import java.awt.image.WritableRaster;

public class RasterOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RasterOp");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "filter", Raster.class, WritableRaster.class), "filter");
        check(hasMethod(c, "getBounds2D", Raster.class), "getBounds2D");
        check(hasMethod(c, "createCompatibleDestRaster", Raster.class), "createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D", Point2D.class, Point2D.class), "getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "getRenderingHints");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RasterOp op = new RasterOp() {
            public WritableRaster filter(Raster src, WritableRaster dest) { return dest; }
            public Rectangle2D getBounds2D(Raster src) { return src.getBounds(); }
            public WritableRaster createCompatibleDestRaster(Raster src) { return src.createCompatibleWritableRaster(); }
            public Point2D getPoint2D(Point2D srcPt, Point2D dstPt) { return srcPt; }
            public RenderingHints getRenderingHints() { return null; }
        };
        BufferedImage img = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        Raster src = img.getRaster();
        check(op.getBounds2D(src).equals(src.getBounds()), "getBounds2D");
        check(op.createCompatibleDestRaster(src) != null, "createCompatibleDestRaster");
        check(op.getPoint2D(new Point2D.Double(1, 2), null) != null, "getPoint2D");
        check(op.filter(src, null) == null, "identity filter returns dest (null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += RasterOpDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class RasterOpDeepContract {
    // Deepened contract coverage ported from the JDK-8 mirror (JDK-27 divergences adjusted).
    static int passed;



    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }



    static int run() throws Exception {

                Class<?> c = Class.forName("java.awt.image.RasterOp");
                check(c.isInterface(), "is interface");
                check(c.getSuperclass() == null, "no superclass");
                check(c.getInterfaces().length == 0, "no direct interfaces");
                check(hasMethod(c, "filter"), "method filter");
                check(hasMethod(c, "getBounds2D"), "method getBounds2D");
                check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
                check(hasMethod(c, "getPoint2D"), "method getPoint2D");
                check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
                // Known platform implementors.
                Class<?>[] implementors = { ConvolveOp.class, AffineTransformOp.class, LookupOp.class,
                        RescaleOp.class, ColorConvertOp.class, BandCombineOp.class };
                for (Class<?> im : implementors)
                    check(RasterOp.class.isAssignableFrom(im), im.getSimpleName() + " implements RasterOp");
                // The interface methods are abstract.
                check(java.lang.reflect.Modifier.isAbstract(
                        c.getMethod("filter", Raster.class, WritableRaster.class).getModifiers()),
                      "filter(Raster, WritableRaster) abstract");

        return passed;
    }
}

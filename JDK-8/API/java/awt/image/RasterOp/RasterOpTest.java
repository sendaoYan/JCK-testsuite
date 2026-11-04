/*
 * @test
 * @summary java.awt.image.RasterOp conformance: interface reflection + implementor contract.
 * @run main RasterOpTest
 */

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

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

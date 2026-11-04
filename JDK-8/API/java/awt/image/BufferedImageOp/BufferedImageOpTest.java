/*
 * @test
 * @summary java.awt.image.BufferedImageOp conformance: interface reflection + implementor contract.
 * @run main BufferedImageOpTest
 */

import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.ConvolveOp;
import java.awt.image.LookupOp;
import java.awt.image.RescaleOp;

public class BufferedImageOpTest {
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
        Class<?> c = Class.forName("java.awt.image.BufferedImageOp");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
        // Known platform implementors.
        Class<?>[] implementors = { ConvolveOp.class, AffineTransformOp.class, LookupOp.class,
                RescaleOp.class, ColorConvertOp.class };
        for (Class<?> im : implementors)
            check(BufferedImageOp.class.isAssignableFrom(im), im.getSimpleName() + " implements BufferedImageOp");
        // BandCombineOp deliberately implements only RasterOp.
        check(!BufferedImageOp.class.isAssignableFrom(java.awt.image.BandCombineOp.class),
              "BandCombineOp is not a BufferedImageOp");
        // The interface methods are abstract.
        check(java.lang.reflect.Modifier.isAbstract(
                c.getMethod("filter", BufferedImage.class, BufferedImage.class).getModifiers()),
              "filter(BufferedImage, BufferedImage) abstract");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

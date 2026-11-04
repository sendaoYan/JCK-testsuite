/*
 * @test
 * @summary java.awt.image.ConvolveOp conformance: white-box structure + black-box behavior.
 * @run main ConvolveOpTest
 */

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RasterOp;
import java.lang.reflect.*;

public class ConvolveOpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }
    static boolean hasField(Class<?> c, String name) {
        try { c.getField(name); return true; } catch (NoSuchFieldException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ConvolveOp", false, ConvolveOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(BufferedImageOp.class.isAssignableFrom(c), "implements BufferedImageOp");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasField(c, "EDGE_ZERO_FILL"), "EDGE_ZERO_FILL");
        check(hasField(c, "EDGE_NO_OP"), "EDGE_NO_OP");
        check(hasMethod(c, "getKernel"), "getKernel()");
        check(hasMethod(c, "getEdgeCondition"), "getEdgeCondition()");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D");
    }

    static void testBlackBox() {
        // identity kernel: EDGE_NO_OP preserves pixels exactly
        float[] id = new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 };
        ConvolveOp idOp = new ConvolveOp(new Kernel(3, 3, id), ConvolveOp.EDGE_NO_OP, null);
        check(idOp.getEdgeCondition() == ConvolveOp.EDGE_NO_OP, "edge condition NO_OP");
        check(idOp.getKernel().getWidth() == 3 && idOp.getKernel().getHeight() == 3, "kernel dims");
        check(idOp.getRenderingHints() == null, "null hints");

        BufferedImage src = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        src.setRGB(2, 2, 0xFF112233);
        src.setRGB(1, 1, 0xFF445566);
        src.setRGB(3, 3, 0xFF778899);
        BufferedImage dst = idOp.filter(src, null);
        check(dst.getWidth() == 5 && dst.getHeight() == 5, "identity dest dims");
        check(dst.getRGB(2, 2) == 0xFF112233, "identity preserves center");
        check(dst.getRGB(1, 1) == 0xFF445566, "identity preserves pixel");

        Rectangle2D b = idOp.getBounds2D(src);
        check(b.getX() == 0 && b.getY() == 0 && b.getWidth() == 5 && b.getHeight() == 5, "getBounds2D");
        Point2D p = idOp.getPoint2D(new Point2D.Float(3, 4), null);
        check(p.getX() == 3.0 && p.getY() == 4.0, "getPoint2D identity");

        // Single-tap kernel at the top-left element shifts the image down-right:
        // dest(x,y) = src(x+1,y+1) in the interior (convolution flips the kernel).
        float[] shift = new float[] { 1, 0, 0, 0, 0, 0, 0, 0, 0 };
        ConvolveOp shiftOp = new ConvolveOp(new Kernel(3, 3, shift), ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage shifted = shiftOp.filter(src, null);
        check(shifted.getRGB(1, 1) == src.getRGB(2, 2), "shift maps (1,1)<-(2,2)");
        check(shifted.getRGB(2, 2) == src.getRGB(3, 3), "shift maps (2,2)<-(3,3)");
        check(shifted.getRGB(0, 0) == 0xFF000000, "shift left/top edge zero-filled");

        // default constructor uses EDGE_ZERO_FILL
        ConvolveOp defOp = new ConvolveOp(new Kernel(3, 3, id));
        check(defOp.getEdgeCondition() == ConvolveOp.EDGE_ZERO_FILL, "default edge ZERO_FILL");

        // createCompatibleDestImage
        BufferedImage compat = idOp.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 5 && compat.getHeight() == 5, "compatible dest dims");
        check(compat.getType() == BufferedImage.TYPE_INT_RGB, "compatible dest type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

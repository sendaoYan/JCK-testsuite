/*
 * @test
 * @summary java.awt.image.RescaleOp conformance: white-box structure + black-box behavior.
 * @run main RescaleOpTest
 */

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RasterOp;
import java.awt.image.RescaleOp;
import java.lang.reflect.*;

public class RescaleOpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.RescaleOp", false, RescaleOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(BufferedImageOp.class.isAssignableFrom(c), "implements BufferedImageOp");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasMethod(c, "getScaleFactors", float[].class), "getScaleFactors");
        check(hasMethod(c, "getOffsets", float[].class), "getOffsets");
        check(hasMethod(c, "getNumFactors"), "getNumFactors()");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D");
    }

    static void testBlackBox() {
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        src.setRGB(0, 0, 0xFF6432C8); // (100, 50, 200)

        RescaleOp op = new RescaleOp(2.0f, 10.0f, null);
        check(op.getNumFactors() == 1, "scalar numFactors 1");
        float[] sf = op.getScaleFactors(null);
        float[] off = op.getOffsets(null);
        check(sf.length == 1 && sf[0] == 2.0f, "scale factors {2}");
        check(off.length == 1 && off[0] == 10.0f, "offsets {10}");

        BufferedImage dst = op.filter(src, null);
        int p = dst.getRGB(0, 0);
        check(((p >> 16) & 0xFF) == 210, "100*2+10 -> 210 red");
        check(((p >> 8) & 0xFF) == 110, "50*2+10 -> 110 green");
        check((p & 0xFF) == 255, "200*2+10 clamped 255 blue");

        // half scale, zero offset: exact integer results
        RescaleOp half = new RescaleOp(0.5f, 0.0f, null);
        BufferedImage dh = half.filter(src, null);
        int hp = dh.getRGB(0, 0);
        check(((hp >> 16) & 0xFF) == 50, "100*0.5 -> 50");
        check(((hp >> 8) & 0xFF) == 25, "50*0.5 -> 25");
        check((hp & 0xFF) == 100, "200*0.5 -> 100");

        // per-band array factors
        RescaleOp arr = new RescaleOp(new float[] { 1f, 2f, 3f }, new float[] { 0f, 0f, 0f }, null);
        check(arr.getNumFactors() == 3, "array numFactors 3");
        BufferedImage da = arr.filter(src, null);
        int ap = da.getRGB(0, 0);
        check(((ap >> 16) & 0xFF) == 100, "band0 *1 -> 100");
        check(((ap >> 8) & 0xFF) == 100, "band1 *2 -> 100");
        check((ap & 0xFF) == 255, "band2 *3 -> 255 clamped");

        Rectangle2D b = op.getBounds2D(src);
        check(b.getWidth() == 2 && b.getHeight() == 2, "getBounds2D dims");
        Point2D pt = op.getPoint2D(new Point2D.Float(1, 1), null);
        check(pt.getX() == 1.0 && pt.getY() == 1.0, "getPoint2D identity");
        BufferedImage compat = op.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 2 && compat.getHeight() == 2, "compatible dest dims");
        check(op.getRenderingHints() == null, "null hints");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

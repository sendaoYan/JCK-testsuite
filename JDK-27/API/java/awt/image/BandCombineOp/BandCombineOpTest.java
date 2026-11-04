/*
 * @test
 * @summary java.awt.image.BandCombineOp conformance: white-box structure plus black-box behavior.
 * @run main BandCombineOpTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.lang.reflect.*;

public class BandCombineOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BandCombineOp");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasMethod(c, "getMatrix"), "getMatrix()");
        check(hasMethod(c, "filter", Raster.class, WritableRaster.class), "filter");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImage img = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        WritableRaster src = img.getRaster();
        src.setPixel(0, 0, new int[]{100, 150, 200});
        float[][] matrix = {{1, 0, 0, 0}, {0, 1, 0, 0}, {0, 0, 1, 0}};
        BandCombineOp op = new BandCombineOp(matrix, null);

        // identity matrix -> unchanged pixels
        WritableRaster dst = op.filter(src, null);
        int[] out = dst.getPixel(0, 0, (int[]) null);
        check(out[0] == 100 && out[1] == 150 && out[2] == 200, "identity matrix leaves pixels unchanged");

        // offset matrix adds constant
        float[][] add = {{1, 0, 0, 10}, {0, 1, 0, 20}, {0, 0, 1, 30}};
        BandCombineOp addOp = new BandCombineOp(add, null);
        int[] out2 = addOp.filter(src, null).getPixel(0, 0, (int[]) null);
        check(out2[0] == 110 && out2[1] == 170 && out2[2] == 230, "offset matrix adds constants");

        check(op.getMatrix() != matrix && op.getMatrix()[0][0] == 1, "getMatrix returns copy");
        check(op.getBounds2D(src).equals(src.getBounds()), "getBounds2D == raster bounds");
        check(op.createCompatibleDestRaster(src) != null, "createCompatibleDestRaster non-null");
        check(op.getPoint2D(new Point2D.Double(1, 2), null) != null, "getPoint2D non-null");
        check(op.getRenderingHints() == null, "null rendering hints");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

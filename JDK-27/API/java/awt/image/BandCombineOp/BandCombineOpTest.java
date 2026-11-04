/*
 * @test
 * @summary java.awt.image.BandCombineOp conformance: white-box structure plus black-box behavior.
 * @run main BandCombineOpTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.lang.reflect.*;
import java.awt.geom.Point2D;
import java.awt.image.BandCombineOp;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;

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
        passed += BandCombineOpDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class BandCombineOpDeepContract {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BandCombineOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getMatrix"), "method getMatrix");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static float[][] identity3() {
        return new float[][] { { 1f, 0f, 0f }, { 0f, 1f, 0f }, { 0f, 0f, 1f } };
    }

    static void testMatrix() {
        float[][] m = identity3();
        BandCombineOp op = new BandCombineOp(m, null);
        float[][] got = op.getMatrix();
        check(got.length == 3 && got[0].length == 3, "matrix shape");
        check(got[0][0] == 1f && got[0][1] == 0f && got[1][1] == 1f && got[2][2] == 1f,
              "getMatrix equal content");
        check(op.getRenderingHints() == null, "no hints -> null");
    }

    static void testFilter() {
        BandCombineOp id = new BandCombineOp(identity3(), null);
        WritableRaster src = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).getRaster();
        src.setPixel(0, 0, new int[] { 10, 20, 30 });
        WritableRaster out = id.filter(src, null);
        check(out.getNumBands() == 3, "identity keeps band count");
        check(java.util.Arrays.equals(out.getPixel(0, 0, (int[]) null), new int[] { 10, 20, 30 }),
              "identity matrix preserves pixel");
        // Row weights scale individual bands.
        BandCombineOp scale = new BandCombineOp(
                new float[][] { { 2f, 0f, 0f }, { 0f, 1f, 0f }, { 0f, 0f, 1f } }, null);
        WritableRaster o2 = scale.filter(src, null);
        check(java.util.Arrays.equals(o2.getPixel(0, 0, (int[]) null), new int[] { 20, 20, 30 }),
              "weight scales first band");
        // Mixed weights sum bands; a band-reducing matrix writes into a provided destination.
        BandCombineOp mix = new BandCombineOp(new float[][] { { 1f, 1f, 0f } }, null);
        WritableRaster d1 = java.awt.image.Raster.createInterleavedRaster(
                java.awt.image.DataBuffer.TYPE_BYTE, 2, 2, 1, new java.awt.Point());
        check(mix.filter(src, d1) == d1, "band-reducing filter returns provided dst");
        check(d1.getSample(0, 0, 0) == 30, "row sums selected bands");
        // Auto-created destination for a 1-row matrix is unsupported -> IAE.
        boolean threw = false;
        try { mix.filter(src, null); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "auto dst with fewer bands than src -> IAE");
        // Provided destination is used and returned.
        WritableRaster provided = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB).getRaster();
        check(id.filter(src, provided) == provided, "filter returns provided dst");
    }

    static void testGeometryAndErrors() {
        BandCombineOp id = new BandCombineOp(identity3(), null);
        WritableRaster src = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB).getRaster();
        check(id.getBounds2D(src).equals(new java.awt.Rectangle(0, 0, 3, 2)), "getBounds2D same rect");
        WritableRaster cd = id.createCompatibleDestRaster(src);
        check(cd.getWidth() == 3 && cd.getHeight() == 2, "compatible raster same dims");
        check(cd.getNumBands() == 3, "compatible raster bands equal matrix rows");
        Point2D p = id.getPoint2D(new Point2D.Double(2, 3), null);
        check(p.getX() == 2 && p.getY() == 3, "getPoint2D identity");
        // Column count must match source band count.
        BandCombineOp bad = new BandCombineOp(new float[][] { { 1f, 0f } }, null);
        boolean threw = false;
        try { bad.filter(src, null); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "matrix columns != src bands -> IAE");
    }



    static int run() throws Exception {
        testWhiteBox();
        testMatrix();
        testFilter();
        testGeometryAndErrors();
        return passed;
    }
}

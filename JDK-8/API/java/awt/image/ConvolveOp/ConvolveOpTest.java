/*
 * @test
 * @summary java.awt.image.ConvolveOp conformance: white-box structure + black-box contract.
 * @run main ConvolveOpTest
 */

import java.awt.RenderingHints;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;

public class ConvolveOpTest {
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
        Class<?> c = Class.forName("java.awt.image.ConvolveOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getEdgeCondition"), "method getEdgeCondition");
        check(hasMethod(c, "getKernel"), "method getKernel");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static Kernel identity() {
        return new Kernel(3, 3, new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 });
    }

    static void testConstruction() {
        check(ConvolveOp.EDGE_ZERO_FILL == 0, "EDGE_ZERO_FILL == 0");
        check(ConvolveOp.EDGE_NO_OP == 1, "EDGE_NO_OP == 1");
        Kernel k = identity();
        ConvolveOp zero = new ConvolveOp(k, ConvolveOp.EDGE_ZERO_FILL, null);
        check(zero.getEdgeCondition() == ConvolveOp.EDGE_ZERO_FILL, "zero fill condition");
        Kernel gk = zero.getKernel();
        check(gk.getWidth() == 3 && gk.getHeight() == 3 && gk.getKernelData(null)[4] == 1f,
              "getKernel equal content");
        check(zero.getRenderingHints() == null, "no hints -> null");
        RenderingHints h = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        ConvolveOp noop = new ConvolveOp(k, ConvolveOp.EDGE_NO_OP, h);
        check(noop.getEdgeCondition() == ConvolveOp.EDGE_NO_OP, "no-op condition");
        check(noop.getRenderingHints() == h, "hints kept by reference");
        ConvolveOp def = new ConvolveOp(k);
        check(def.getEdgeCondition() == ConvolveOp.EDGE_ZERO_FILL, "default edge condition zero fill");
    }

    static void testFilterIdentity() {
        // With an identity kernel the interior pixel is preserved exactly.
        ConvolveOp co = new ConvolveOp(identity(), ConvolveOp.EDGE_ZERO_FILL, null);
        BufferedImage src = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        src.setRGB(1, 1, 0xFF112233);
        BufferedImage dst = co.filter(src, null);
        check(dst.getWidth() == 3 && dst.getHeight() == 3, "identity keeps dims");
        check(dst.getRGB(1, 1) == 0xFF112233, "identity preserves interior pixel");

        // EDGE_NO_OP keeps the corner pixel, EDGE_ZERO_FILL zeroes it.
        BufferedImage src2 = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        src2.setRGB(0, 0, 0xFF112233);
        BufferedImage rNoOp = new ConvolveOp(identity(), ConvolveOp.EDGE_NO_OP, null).filter(src2, null);
        check(rNoOp.getRGB(0, 0) == 0xFF112233, "EDGE_NO_OP preserves corner");
        BufferedImage rZero = co.filter(src2, null);
        check(rZero.getRGB(0, 0) == 0xFF000000, "EDGE_ZERO_FILL zeroes corner");

        // Filtering into a caller-provided destination returns that destination.
        BufferedImage provided = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        check(co.filter(src, provided) == provided, "filter returns provided dst");

        // A weighted kernel sums neighbors: 2x center with a 3x3 of all-ones/9.
        Kernel box = new Kernel(3, 3, new float[] { 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f, 1f / 9f,
                1f / 9f, 1f / 9f, 1f / 9f });
        BufferedImage gray = new BufferedImage(3, 3, BufferedImage.TYPE_BYTE_GRAY);
        for (int x = 0; x < 3; x++)
            for (int y = 0; y < 3; y++)
                gray.getRaster().setSample(x, y, 0, 90);
        BufferedImage sm = new ConvolveOp(box, ConvolveOp.EDGE_NO_OP, null).filter(gray, null);
        check(sm.getRaster().getSample(1, 1, 0) == 90, "box blur of uniform image is uniform");
    }

    static void testGeometryAndErrors() {
        ConvolveOp co = new ConvolveOp(identity(), ConvolveOp.EDGE_NO_OP, null);
        BufferedImage src = new BufferedImage(5, 5, BufferedImage.TYPE_INT_RGB);
        check(co.getBounds2D(src).equals(new java.awt.Rectangle(0, 0, 5, 5)), "getBounds2D same rect");
        BufferedImage cd = co.createCompatibleDestImage(src, null);
        check(cd.getWidth() == 5 && cd.getHeight() == 5, "compatible dest same dims");
        check(cd.getType() == src.getType(), "compatible dest same type");
        Point2D p = co.getPoint2D(new Point2D.Double(2, 3), null);
        check(p.getX() == 2 && p.getY() == 3, "getPoint2D identity");
        Point2D reused = co.getPoint2D(new Point2D.Double(1, 1), new Point2D.Double());
        check(reused.getX() == 1 && reused.getY() == 1, "getPoint2D reuses dst");

        // src == dst is rejected.
        boolean threw = false;
        try { co.filter(src, src); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "src == dst -> IAE");
        // A smaller caller-provided destination is accepted and returned.
        BufferedImage small = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        check(co.filter(src, small) == small, "smaller dst accepted and returned");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstruction();
        testFilterIdentity();
        testGeometryAndErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

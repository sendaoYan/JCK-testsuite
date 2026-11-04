/*
 * @test
 * @summary java.awt.image.RescaleOp conformance: white-box structure + black-box contract.
 * @run main RescaleOpTest
 */

import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RescaleOp;

public class RescaleOpTest {
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
        Class<?> c = Class.forName("java.awt.image.RescaleOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getScaleFactors"), "method getScaleFactors");
        check(hasMethod(c, "getOffsets"), "method getOffsets");
        check(hasMethod(c, "getNumFactors"), "method getNumFactors");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static void testConstruction() {
        RescaleOp multi = new RescaleOp(new float[] { 1.5f, 2f, 3f, 4f }, new float[] { 10f, 20f, 30f, 40f }, null);
        check(multi.getNumFactors() == 4, "array ctor num factors");
        float[] sf = multi.getScaleFactors(null);
        check(sf.length == 4 && sf[0] == 1.5f && sf[3] == 4f, "scale factors exact");
        float[] off = multi.getOffsets(null);
        check(off.length == 4 && off[1] == 20f && off[3] == 40f, "offsets exact");
        float[] provided = new float[4];
        check(multi.getScaleFactors(provided) == provided, "getScaleFactors reuses array");
        check(multi.getOffsets(provided) == provided, "getOffsets reuses array");
        RescaleOp single = new RescaleOp(2.0f, 10.0f, null);
        check(single.getNumFactors() == 1, "scalar ctor num factors");
        check(single.getScaleFactors(null)[0] == 2.0f, "scalar ctor scale");
        check(single.getOffsets(null)[0] == 10.0f, "scalar ctor offset");
        check(single.getRenderingHints() == null, "no hints -> null");
        // Negative and zero scale factors are legal inputs.
        check(new RescaleOp(-1f, 0f, null).getScaleFactors(null)[0] == -1f, "negative scale kept");
        check(new RescaleOp(0f, 5f, null).getScaleFactors(null)[0] == 0f, "zero scale kept");
        // A one-element scale/offset pair applied to a multi-band image rescales every band.
        RescaleOp gray1 = new RescaleOp(3.0f, 1.0f, null);
        BufferedImage g1 = new BufferedImage(1, 1, BufferedImage.TYPE_BYTE_GRAY);
        g1.getRaster().setSample(0, 0, 0, 10);
        check(gray1.filter(g1, null).getRaster().getSample(0, 0, 0) == 31, "3*10+1 = 31");
    }

    static void testFilter() {
        // gray' = clamp(scale * gray + offset).
        RescaleOp op = new RescaleOp(2.0f, 10.0f, null);
        BufferedImage gray = new BufferedImage(3, 1, BufferedImage.TYPE_BYTE_GRAY);
        gray.getRaster().setSample(0, 0, 0, 30);   // 2*30+10 = 70
        gray.getRaster().setSample(1, 0, 0, 130);  // 2*130+10 = 270 -> clamp 255
        gray.getRaster().setSample(2, 0, 0, 0);    // 2*0+10 = 10
        BufferedImage out = op.filter(gray, null);
        check(out.getRaster().getSample(0, 0, 0) == 70, "rescale 30 -> 70");
        check(out.getRaster().getSample(1, 0, 0) == 255, "rescale clamps to 255");
        check(out.getRaster().getSample(2, 0, 0) == 10, "rescale 0 -> 10");

        // Scalar ctor applies to every band of an ARGB image.
        RescaleOp s2 = new RescaleOp(2.0f, 0f, null);
        BufferedImage argb = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);
        argb.setRGB(0, 0, 0x80102030);
        BufferedImage o2 = s2.filter(argb, null);
        check(o2.getRGB(0, 0) == 0x80204060, "scalar rescale on ARGB leaves alpha untouched");
        RescaleOp s3 = new RescaleOp(2.0f, 10f, null);
        BufferedImage o4 = s3.filter(argb, null);
        check(o4.getRGB(0, 0) == 0x802a4a6a, "rescale with offset keeps alpha");
        // Identity rescale leaves the image unchanged.
        RescaleOp id = new RescaleOp(1.0f, 0.0f, null);
        BufferedImage o3 = id.filter(argb, null);
        check(o3.getRGB(0, 0) == 0x80102030, "identity rescale exact");
        // Provided destination is used and returned.
        BufferedImage provided = new BufferedImage(3, 1, BufferedImage.TYPE_BYTE_GRAY);
        check(op.filter(gray, provided) == provided, "filter returns provided dst");
        check(provided.getRaster().getSample(0, 0, 0) == 70, "provided dst filled");
    }

    static void testGeometryAndErrors() {
        RescaleOp op = new RescaleOp(1f, 0f, null);
        BufferedImage src = new BufferedImage(4, 2, BufferedImage.TYPE_INT_RGB);
        check(op.getBounds2D(src).equals(new java.awt.Rectangle(0, 0, 4, 2)), "getBounds2D same rect");
        BufferedImage cd = op.createCompatibleDestImage(src, null);
        check(cd.getWidth() == 4 && cd.getHeight() == 2 && cd.getType() == src.getType(),
              "compatible dest same dims/type");
        Point2D p = op.getPoint2D(new Point2D.Double(3, 1), null);
        check(p.getX() == 3 && p.getY() == 1, "getPoint2D identity");
        // src == dst is accepted: RescaleOp filters in place and returns the same image.
        check(op.filter(src, src) == src, "src == dst filtered in place");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstruction();
        testFilter();
        testGeometryAndErrors();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

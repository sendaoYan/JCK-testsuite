/*
 * @test
 * @summary java.awt.image.ColorConvertOp conformance: class reflection + conversion contract.
 * @run main ColorConvertOpTest
 */

import java.awt.color.ColorSpace;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.ColorConvertOp;
import java.awt.image.WritableRaster;

public class ColorConvertOpTest {
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
        Class<?> c = Class.forName("java.awt.image.ColorConvertOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static ColorConvertOp srgbToGray() {
        return new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_sRGB),
                ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
    }

    static void testGrayConversion() {
        ColorConvertOp op = srgbToGray();
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF0000);
        int red = (op.filter(bi, null).getRGB(0, 0) >> 16) & 0xFF;
        bi.setRGB(0, 0, 0x00FF00);
        int green = (op.filter(bi, null).getRGB(0, 0) >> 16) & 0xFF;
        bi.setRGB(0, 0, 0xFFFFFF);
        int white = (op.filter(bi, null).getRGB(0, 0) >> 16) & 0xFF;
        check(red == 130, "red converts to ICC gray 130");
        check(green == 220, "green converts to ICC gray 220");
        check(white == 255, "white converts to gray 255");
        check(red < green && green < white, "gray ordering red < green < white");
        // The destination is a byte-gray image.
        BufferedImage out = op.filter(bi, null);
        check(out.getType() == BufferedImage.TYPE_BYTE_GRAY, "destination is TYPE_BYTE_GRAY");
        check(out.getColorModel().getNumComponents() == 1, "gray color model single component");
    }

    static void testIdentity() {
        ColorSpace srgb = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorConvertOp id = new ColorConvertOp(srgb, srgb, null);
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0x123456);
        BufferedImage out = id.filter(bi, null);
        check(out.getRGB(0, 0) == 0xFF123456, "sRGB -> sRGB preserves pixel");
        check(out.getWidth() == 1 && out.getHeight() == 1, "identity keeps dims");
    }

    static void testRasterAndGeometry() {
        ColorConvertOp op = srgbToGray();
        BufferedImage bi = new BufferedImage(3, 2, BufferedImage.TYPE_INT_RGB);
        check(op.getBounds2D(bi).equals(new java.awt.Rectangle(0, 0, 3, 2)), "bounds preserved");
        BufferedImage cd = op.createCompatibleDestImage(bi, null);
        check(cd.getWidth() == 3 && cd.getHeight() == 2, "compatible dest dims");
        check(cd.getType() == BufferedImage.TYPE_BYTE_GRAY, "compatible dest is gray");
        Point2D p = op.getPoint2D(new Point2D.Double(1.5, 0.5), null);
        check(p.getX() == 1.5 && p.getY() == 0.5, "getPoint2D identity");
        check(op.getRenderingHints() == null, "no hints -> null");
        // Raster filtering yields a single-band destination.
        WritableRaster r = op.filter(bi.getRaster(), null);
        check(r.getNumBands() == 1, "raster output single band");
        check(r.getWidth() == 3 && r.getHeight() == 2, "raster output dims");
        // Provided destination is used and returned.
        WritableRaster into = java.awt.image.Raster.createWritableRaster(
                r.getSampleModel(), new java.awt.Point());
        check(op.filter(bi.getRaster(), into) == into, "raster filter returns provided dst");
    }

    static void testSingleColorSpaceCtor() {
        // The single-space constructor converts to that space.
        ColorConvertOp toGray = new ColorConvertOp(ColorSpace.getInstance(ColorSpace.CS_GRAY), null);
        BufferedImage bi = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFFFFFF);
        check((toGray.filter(bi, null).getRGB(0, 0) >> 16 & 0xFF) == 255,
              "white stays 255 through single-space gray op");
        // Null color space -> NPE.
        boolean npe = false;
        try { new ColorConvertOp((ColorSpace) null, null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null ColorSpace -> NPE");
    }


    static void testOriginalSurface() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ColorConvertOp");
        check(hasMethod(c, "getICC_Profiles"), "method getICC_Profiles");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testOriginalSurface();
        testGrayConversion();
        testIdentity();
        testRasterAndGeometry();
        testSingleColorSpaceCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

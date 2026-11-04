/*
 * @test
 * @summary java.awt.image.ColorConvertOp conformance: white-box structure + black-box behavior.
 * @run main ColorConvertOpTest
 */

import java.awt.color.ColorSpace;
import java.awt.color.ICC_Profile;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.ColorConvertOp;
import java.awt.image.RasterOp;
import java.lang.reflect.*;

public class ColorConvertOpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ColorConvertOp", false, ColorConvertOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(BufferedImageOp.class.isAssignableFrom(c), "implements BufferedImageOp");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasMethod(c, "getICC_Profiles"), "getICC_Profiles()");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage", BufferedImage.class, java.awt.image.ColorModel.class), "createCompatibleDestImage");
    }

    static int ch(int rgb, int shift) {
        return (rgb >> shift) & 0xFF;
    }
    static boolean near(int a, int b) {
        return Math.abs(a - b) <= 2;
    }

    static void testBlackBox() {
        ColorSpace sRGB = ColorSpace.getInstance(ColorSpace.CS_sRGB);
        ColorSpace linRGB = ColorSpace.getInstance(ColorSpace.CS_LINEAR_RGB);

        BufferedImage src = new BufferedImage(5, 1, BufferedImage.TYPE_INT_RGB);
        int[] colors = new int[] { 0xFF0000, 0x00FF00, 0x0000FF, 0x808080, 0xFFFFFF };
        for (int i = 0; i < 5; i++) src.setRGB(i, 0, colors[i]);

        ColorConvertOp toLin = new ColorConvertOp(sRGB, linRGB, null);
        ColorConvertOp toSrgb = new ColorConvertOp(linRGB, sRGB, null);
        BufferedImage mid = toLin.filter(src, null);
        check(mid.getWidth() == 5 && mid.getHeight() == 1, "linear dest dims");
        BufferedImage back = toSrgb.filter(mid, null);

        for (int i = 0; i < 5; i++) {
            int o = colors[i], r = back.getRGB(i, 0);
            check(near(ch(o, 16), ch(r, 16)), "round-trip red color " + i);
            check(near(ch(o, 8), ch(r, 8)), "round-trip green color " + i);
            check(near(ch(o, 0), ch(r, 0)), "round-trip blue color " + i);
        }

        Rectangle2D b = toLin.getBounds2D(src);
        check(b.getWidth() == 5 && b.getHeight() == 1, "getBounds2D");
        Point2D pt = toLin.getPoint2D(new Point2D.Float(3, 0), null);
        check(pt.getX() == 3.0 && pt.getY() == 0.0, "getPoint2D identity");
        check(toLin.getRenderingHints() == null, "null hints");

        BufferedImage compat = toLin.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 5 && compat.getHeight() == 1, "compatible dest dims");

        // ICC_Profile[] constructor exposes profiles
        ColorConvertOp profOp = new ColorConvertOp(new ICC_Profile[] { ICC_Profile.getInstance(ColorSpace.CS_sRGB) }, null);
        ICC_Profile[] profs = profOp.getICC_Profiles();
        check(profs != null && profs.length == 1, "ICC profiles length 1");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

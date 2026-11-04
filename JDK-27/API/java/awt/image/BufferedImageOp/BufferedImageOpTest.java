/*
 * @test
 * @summary java.awt.image.BufferedImageOp conformance: white-box structure + black-box contract.
 * @run main BufferedImageOpTest
 */

import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RescaleOp;
import java.lang.reflect.*;

public class BufferedImageOpTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferedImageOp", false, BufferedImageOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D(BufferedImage)");
        check(hasMethod(c, "createCompatibleDestImage", BufferedImage.class, java.awt.image.ColorModel.class), "createCompatibleDestImage");
        check(hasMethod(c, "getPoint2D", Point2D.class, Point2D.class), "getPoint2D(Point2D,Point2D)");
        check(hasMethod(c, "getRenderingHints"), "getRenderingHints()");
    }

    static void testBlackBox() {
        // Exercise the interface contract through a concrete implementation.
        BufferedImageOp op = new RescaleOp(1.0f, 0.0f, null);
        BufferedImage src = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        src.setRGB(1, 1, 0xFF112233);
        BufferedImage dst = op.filter(src, null);
        check(dst != null, "filter returns non-null");
        check(dst.getWidth() == 4 && dst.getHeight() == 3, "filter dims");
        check(dst.getRGB(1, 1) == 0xFF112233, "filter identity");

        Rectangle2D b = op.getBounds2D(src);
        check(b.getWidth() == 4 && b.getHeight() == 3, "getBounds2D dims");
        Point2D p = op.getPoint2D(new Point2D.Float(2, 2), null);
        check(p.getX() == 2.0 && p.getY() == 2.0, "getPoint2D");
        BufferedImage compat = op.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 4 && compat.getHeight() == 3, "createCompatibleDestImage dims");
        check(op.getRenderingHints() == null, "getRenderingHints null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.image.AffineTransformOp conformance: white-box structure + black-box behavior.
 * @run main AffineTransformOpTest
 */

import java.awt.geom.AffineTransform;
import java.awt.geom.Point2D;
import java.awt.geom.Rectangle2D;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.awt.image.BufferedImageOp;
import java.awt.image.RasterOp;
import java.lang.reflect.*;

public class AffineTransformOpTest {
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
        Class<?> c = Class.forName("java.awt.image.AffineTransformOp", false, AffineTransformOpTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(BufferedImageOp.class.isAssignableFrom(c), "implements BufferedImageOp");
        check(RasterOp.class.isAssignableFrom(c), "implements RasterOp");
        check(hasField(c, "TYPE_NEAREST_NEIGHBOR"), "TYPE_NEAREST_NEIGHBOR");
        check(hasField(c, "TYPE_BILINEAR"), "TYPE_BILINEAR");
        check(hasField(c, "TYPE_BICUBIC"), "TYPE_BICUBIC");
        check(hasMethod(c, "getInterpolationType"), "getInterpolationType()");
        check(hasMethod(c, "getTransform"), "getTransform()");
        check(hasMethod(c, "filter", BufferedImage.class, BufferedImage.class), "filter(BufferedImage,BufferedImage)");
        check(hasMethod(c, "getBounds2D", BufferedImage.class), "getBounds2D");
    }

    static void testBlackBox() {
        check(AffineTransformOp.TYPE_NEAREST_NEIGHBOR == 1, "TYPE_NEAREST_NEIGHBOR 1");
        check(AffineTransformOp.TYPE_BILINEAR == 2, "TYPE_BILINEAR 2");
        check(AffineTransformOp.TYPE_BICUBIC == 3, "TYPE_BICUBIC 3");

        BufferedImage src = new BufferedImage(4, 3, BufferedImage.TYPE_INT_RGB);
        src.setRGB(0, 0, 0xFF112233);
        src.setRGB(3, 2, 0xFF445566);
        src.setRGB(1, 1, 0xFF778899);

        AffineTransform id = new AffineTransform();
        AffineTransformOp idOp = new AffineTransformOp(id, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        check(idOp.getInterpolationType() == AffineTransformOp.TYPE_NEAREST_NEIGHBOR, "interpolation NEAREST");
        check(idOp.getTransform().equals(id), "getTransform equals");
        check(idOp.getRenderingHints() != null, "non-null hints");

        BufferedImage dst = idOp.filter(src, null);
        check(dst.getWidth() == 4 && dst.getHeight() == 3, "identity dest dims");
        check(dst.getRGB(0, 0) == 0xFF112233, "identity copies (0,0)");
        check(dst.getRGB(3, 2) == 0xFF445566, "identity copies (3,2)");
        check(dst.getRGB(1, 1) == 0xFF778899, "identity copies (1,1)");

        Rectangle2D b = idOp.getBounds2D(src);
        check(b.getWidth() == 4 && b.getHeight() == 3, "identity bounds");
        Point2D pt = idOp.getPoint2D(new Point2D.Float(2, 3), null);
        check(pt.getX() == 2.0 && pt.getY() == 3.0, "identity point");

        AffineTransform scale = AffineTransform.getScaleInstance(2.0, 2.0);
        AffineTransformOp scaleOp = new AffineTransformOp(scale, AffineTransformOp.TYPE_BILINEAR);
        check(scaleOp.getInterpolationType() == AffineTransformOp.TYPE_BILINEAR, "interpolation BILINEAR");
        Point2D sp = scaleOp.getPoint2D(new Point2D.Float(1, 2), null);
        check(sp.getX() == 2.0 && sp.getY() == 4.0, "scale point maps (1,2)->(2,4)");
        Rectangle2D sb = scaleOp.getBounds2D(src);
        check(sb.getWidth() == 8 && sb.getHeight() == 6, "scale bounds 8x6");

        BufferedImage compat = idOp.createCompatibleDestImage(src, src.getColorModel());
        check(compat.getWidth() == 4 && compat.getHeight() == 3, "compatible dest dims");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

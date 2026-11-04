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
import java.awt.RenderingHints;

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
        passed += AffineTransformOpDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class AffineTransformOpDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.AffineTransformOp");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.BufferedImageOp", "java.awt.image.RasterOp"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getInterpolationType"), "method getInterpolationType");
        check(hasMethod(c, "filter"), "method filter");
        check(hasMethod(c, "getBounds2D"), "method getBounds2D");
        check(hasMethod(c, "createCompatibleDestImage"), "method createCompatibleDestImage");
        check(hasMethod(c, "createCompatibleDestRaster"), "method createCompatibleDestRaster");
        check(hasMethod(c, "getPoint2D"), "method getPoint2D");
        check(hasMethod(c, "getTransform"), "method getTransform");
        check(hasMethod(c, "getRenderingHints"), "method getRenderingHints");
    }

    static void testConstruction() {
        check(AffineTransformOp.TYPE_NEAREST_NEIGHBOR == 1, "TYPE_NEAREST_NEIGHBOR == 1");
        check(AffineTransformOp.TYPE_BILINEAR == 2, "TYPE_BILINEAR == 2");
        check(AffineTransformOp.TYPE_BICUBIC == 3, "TYPE_BICUBIC == 3");
        AffineTransform at = AffineTransform.getTranslateInstance(3, 4);
        AffineTransformOp byType = new AffineTransformOp(at, AffineTransformOp.TYPE_BILINEAR);
        check(byType.getInterpolationType() == AffineTransformOp.TYPE_BILINEAR, "ctor by type");
        check(byType.getTransform().equals(at), "getTransform equal");
        RenderingHints h = new RenderingHints(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        AffineTransformOp byHints = new AffineTransformOp(at, h);
        check(byHints.getInterpolationType() == AffineTransformOp.TYPE_BILINEAR,
              "hints ctor derives interpolation type");
        check(byHints.getTransform() == at || byHints.getTransform().equals(at), "hints ctor keeps transform");
        boolean threw = false;
        try { new AffineTransformOp(at, 99); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "unknown interpolation type -> IAE");
    }

    static void testGeometry() {
        AffineTransform translate = AffineTransform.getTranslateInstance(3, 4);
        AffineTransformOp op = new AffineTransformOp(translate, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        // getPoint2D maps a source point through the transform.
        Point2D p = op.getPoint2D(new Point2D.Double(1, 1), null);
        check(p.getX() == 4 && p.getY() == 5, "getPoint2D applies transform");
        Point2D reused = op.getPoint2D(new Point2D.Double(0, 0), new Point2D.Double());
        check(reused.getX() == 3 && reused.getY() == 4, "getPoint2D reuses dst");
        // Identity transform maps points to themselves.
        AffineTransformOp id = new AffineTransformOp(new AffineTransform(),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        Point2D q = id.getPoint2D(new Point2D.Double(2.5, -1.5), null);
        check(q.getX() == 2.5 && q.getY() == -1.5, "identity getPoint2D");
        // getBounds2D grows by the translation.
        BufferedImage src = new BufferedImage(7, 9, BufferedImage.TYPE_INT_ARGB);
        Rectangle2D b = op.getBounds2D(src);
        check(b.getX() == 3 && b.getY() == 4 && b.getWidth() == 7 && b.getHeight() == 9, "getBounds2D translated");
        Rectangle2D ib = id.getBounds2D(src);
        check(ib.getWidth() == 7 && ib.getHeight() == 9, "identity bounds same");
        // createCompatibleDestImage matches the transformed size.
        BufferedImage cd = op.createCompatibleDestImage(src, null);
        check(cd.getWidth() == 10 && cd.getHeight() == 13, "compatible dest translated dims");
        BufferedImage cd2 = id.createCompatibleDestImage(src, null);
        check(cd2.getWidth() == 7 && cd2.getHeight() == 9, "identity compatible dest dims");
    }

    static void testFilter() {
        // 2x scale with nearest neighbor: each source pixel occupies a 2x2 block.
        AffineTransformOp op = new AffineTransformOp(AffineTransform.getScaleInstance(2, 2),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage src = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        src.setRGB(1, 1, 0xFF010203);
        BufferedImage dst = op.filter(src, null);
        check(dst.getWidth() == 4 && dst.getHeight() == 4, "2x scale doubles dims");
        check(dst.getRGB(1, 1) == 0xFF000000, "top-left quadrant black");
        check(dst.getRGB(3, 3) == 0xFF010203, "bottom-right quadrant copied");
        check(dst.getRGB(2, 2) == 0xFF010203, "replication covers block");
        // Identity filter preserves pixels.
        AffineTransformOp id = new AffineTransformOp(new AffineTransform(),
                AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
        BufferedImage same = id.filter(src, null);
        check(same.getRGB(1, 1) == 0xFF010203 && same.getWidth() == 2, "identity filter exact");
        // Provided destination is used and returned.
        BufferedImage provided = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        check(op.filter(src, provided) == provided, "filter returns provided dst");
        // src == dst rejected.
        boolean threw = false;
        try { id.filter(src, src); } catch (IllegalArgumentException e) { threw = true; }
        check(threw, "src == dst -> IAE");
    }



    static int run() throws Exception {
        testWhiteBox();
        testConstruction();
        testGeometry();
        testFilter();
        return passed;
    }
}

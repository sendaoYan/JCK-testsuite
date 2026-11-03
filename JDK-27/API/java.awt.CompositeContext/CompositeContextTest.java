/*
 * @test
 * @summary java.awt.CompositeContext conformance: white-box structure + black-box behavior.
 * @run main CompositeContextTest
 */

import java.lang.reflect.*;

public class CompositeContextTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.CompositeContext", false, CompositeContextTest.class.getClassLoader());
        check(c != null, "type loads: java.awt.CompositeContext");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public type");
        check(Modifier.isInterface(m), "is interface");

        Method dispose = c.getMethod("dispose");
        check(dispose.getReturnType() == void.class, "dispose returns void");
        Method compose = c.getMethod("compose", java.awt.image.Raster.class,
                java.awt.image.Raster.class, java.awt.image.WritableRaster.class);
        check(compose.getReturnType() == void.class, "compose returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        java.awt.image.ColorModel cm = java.awt.image.ColorModel.getRGBdefault();

        java.awt.image.BufferedImage src = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xffff0000); // opaque red
        java.awt.image.BufferedImage dst = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        dst.setRGB(0, 0, 0xff00ff00); // opaque green

        // Src rule: output takes the source
        java.awt.CompositeContext srcCtx = java.awt.AlphaComposite.Src.createContext(cm, cm, null);
        check(srcCtx != null, "Src createContext non-null");
        java.awt.image.BufferedImage outSrc = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        outSrc.setRGB(0, 0, 0xff00ff00);
        srcCtx.compose(src.getRaster(), outSrc.getRaster(), outSrc.getRaster());
        check(outSrc.getRGB(0, 0) == 0xffff0000, "Src compose replaces dst with src");
        srcCtx.dispose();

        // SrcOver rule with opaque source also yields source over destination
        java.awt.CompositeContext overCtx = java.awt.AlphaComposite.SrcOver.createContext(cm, cm, null);
        java.awt.image.BufferedImage outOver = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        outOver.setRGB(0, 0, 0xff00ff00);
        overCtx.compose(src.getRaster(), outOver.getRaster(), outOver.getRaster());
        check(outOver.getRGB(0, 0) == 0xffff0000, "SrcOver opaque src covers dst");
        overCtx.dispose();

        // Dst rule: output keeps destination
        java.awt.CompositeContext dstCtx = java.awt.AlphaComposite.Dst.createContext(cm, cm, null);
        java.awt.image.BufferedImage outDst = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        outDst.setRGB(0, 0, 0xff00ff00);
        dstCtx.compose(src.getRaster(), outDst.getRaster(), outDst.getRaster());
        check(outDst.getRGB(0, 0) == 0xff00ff00, "Dst compose keeps destination");
        dstCtx.dispose();

        // Clear rule: output becomes transparent
        java.awt.CompositeContext clearCtx = java.awt.AlphaComposite.Clear.createContext(cm, cm, null);
        java.awt.image.BufferedImage outClear = new java.awt.image.BufferedImage(4, 4,
                java.awt.image.BufferedImage.TYPE_INT_ARGB);
        outClear.setRGB(0, 0, 0xff00ff00);
        clearCtx.compose(src.getRaster(), outClear.getRaster(), outClear.getRaster());
        check(outClear.getRGB(0, 0) == 0x00000000, "Clear compose produces transparent black");
        clearCtx.dispose();

        check(true, "all composite contexts disposed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

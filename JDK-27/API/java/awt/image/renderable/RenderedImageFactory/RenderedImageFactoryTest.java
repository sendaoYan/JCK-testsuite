/*
 * @test
 * @summary java.awt.image.renderable.RenderedImageFactory conformance: white-box structure plus black-box behavior.
 * @run main RenderedImageFactoryTest
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.lang.reflect.*;

public class RenderedImageFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.RenderedImageFactory");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "create", ParameterBlock.class, RenderingHints.class), "create(ParameterBlock,RenderingHints)");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RenderedImageFactory rif = new RenderedImageFactory() {
            public RenderedImage create(ParameterBlock pb, RenderingHints hints) {
                return new BufferedImage(6, 6, BufferedImage.TYPE_INT_ARGB);
            }
        };
        RenderedImage img = rif.create(new ParameterBlock(), null);
        check(img != null && img.getWidth() == 6 && img.getHeight() == 6, "create returns 6x6 image");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

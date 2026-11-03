/*
 * @test
 * @summary java.awt.image.renderable.ContextualRenderedImageFactory conformance: white-box structure plus black-box behavior.
 * @run main ContextualRenderedImageFactoryTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.lang.reflect.*;

public class ContextualRenderedImageFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.ContextualRenderedImageFactory");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(RenderedImageFactory.class.isAssignableFrom(c), "extends RenderedImageFactory");
        check(hasMethod(c, "mapRenderContext", int.class, RenderContext.class, ParameterBlock.class, RenderableImage.class),
              "mapRenderContext");
        check(hasMethod(c, "create", RenderContext.class, ParameterBlock.class), "create(RenderContext,ParameterBlock)");
        check(hasMethod(c, "getBounds2D", ParameterBlock.class), "getBounds2D");
        check(hasMethod(c, "getProperty", ParameterBlock.class, String.class), "getProperty");
        check(hasMethod(c, "getPropertyNames"), "getPropertyNames");
        check(hasMethod(c, "isDynamic"), "isDynamic");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ContextualRenderedImageFactory crif = new ContextualRenderedImageFactory() {
            public RenderContext mapRenderContext(int i, RenderContext rc, ParameterBlock pb, RenderableImage ri) { return rc; }
            public RenderedImage create(RenderContext rc, ParameterBlock pb) { return new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB); }
            public RenderedImage create(ParameterBlock pb, RenderingHints hints) { return new BufferedImage(8, 8, BufferedImage.TYPE_INT_ARGB); }
            public Rectangle2D getBounds2D(ParameterBlock pb) { return new Rectangle2D.Double(0, 0, 8, 8); }
            public Object getProperty(ParameterBlock pb, String name) { return "v"; }
            public String[] getPropertyNames() { return new String[]{"p"}; }
            public boolean isDynamic() { return false; }
        };
        ParameterBlock pb = new ParameterBlock();
        RenderContext rc = new RenderContext(new AffineTransform());
        check(crif.mapRenderContext(0, rc, pb, null) == rc, "mapRenderContext");
        check(crif.create(rc, pb).getWidth() == 8, "create(RenderContext,ParameterBlock)");
        check(crif.create(pb, null).getWidth() == 8, "create(ParameterBlock,RenderingHints)");
        check(crif.getBounds2D(pb).getWidth() == 8, "getBounds2D");
        check(crif.getProperty(pb, "p").equals("v"), "getProperty");
        check(crif.getPropertyNames().length == 1, "getPropertyNames");
        check(!crif.isDynamic(), "isDynamic false");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.awt.image.renderable.RenderableImageOp conformance: white-box structure plus black-box behavior.
 * @run main RenderableImageOpTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.util.*;
import java.lang.reflect.*;

public class RenderableImageOpTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.RenderableImageOp");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(RenderableImage.class.isAssignableFrom(c), "implements RenderableImage");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ContextualRenderedImageFactory crif = new ContextualRenderedImageFactory() {
            public RenderContext mapRenderContext(int i, RenderContext rc, ParameterBlock pb, RenderableImage ri) { return rc; }
            public RenderedImage create(RenderContext rc, ParameterBlock pb) { return new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB); }
            public RenderedImage create(ParameterBlock pb, RenderingHints hints) { return new BufferedImage(100, 50, BufferedImage.TYPE_INT_ARGB); }
            public Rectangle2D getBounds2D(ParameterBlock pb) { return new Rectangle2D.Double(0, 0, 100, 50); }
            public Object getProperty(ParameterBlock pb, String name) { return "val"; }
            public String[] getPropertyNames() { return new String[]{"p"}; }
            public boolean isDynamic() { return false; }
        };
        ParameterBlock pb = new ParameterBlock();
        RenderableImageOp op = new RenderableImageOp(crif, pb);

        check(op.getWidth() == 100.0f, "getWidth from CRIF bounds");
        check(op.getHeight() == 50.0f, "getHeight from CRIF bounds");
        check(op.getMinX() == 0.0f && op.getMinY() == 0.0f, "getMinX/getMinY");
        check(!op.isDynamic(), "isDynamic false");
        check(op.getPropertyNames().length == 1, "getPropertyNames");
        check(op.getProperty("p").equals("val"), "getProperty");

        ParameterBlock stored = op.getParameterBlock();
        check(stored != pb, "constructor clones paramBlock");
        ParameterBlock old = op.setParameterBlock(new ParameterBlock());
        check(old == stored, "setParameterBlock returns old");
        ParameterBlock pb2 = new ParameterBlock();
        op.setParameterBlock(pb2);
        check(op.getParameterBlock() != pb2, "getParameterBlock returns stored clone");

        check(op.createDefaultRendering().getWidth() == 100, "createDefaultRendering");
        check(op.createScaledRendering(20, 10, null).getWidth() == 100, "createScaledRendering");
        check(op.createRendering(new RenderContext(new AffineTransform())).getWidth() == 100, "createRendering");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

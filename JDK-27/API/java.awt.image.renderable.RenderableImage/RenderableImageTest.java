/*
 * @test
 * @summary java.awt.image.renderable.RenderableImage conformance: white-box structure plus black-box behavior.
 * @run main RenderableImageTest
 */

import java.awt.*;
import java.awt.geom.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.util.*;
import java.lang.reflect.*;

public class RenderableImageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n, Class<?>... p) {
        try { c.getMethod(n, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.RenderableImage");
        int m = c.getModifiers();
        check(Modifier.isInterface(m), "interface");
        check(c.getField("HINTS_OBSERVED") != null, "HINTS_OBSERVED constant");
        check(hasMethod(c, "createScaledRendering", int.class, int.class, RenderingHints.class), "createScaledRendering");
        check(hasMethod(c, "createDefaultRendering"), "createDefaultRendering");
        check(hasMethod(c, "createRendering", RenderContext.class), "createRendering");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check("HINTS_OBSERVED".equals(RenderableImage.HINTS_OBSERVED), "HINTS_OBSERVED value");
        final RenderedImage rendered = new BufferedImage(5, 5, BufferedImage.TYPE_INT_ARGB);
        RenderableImage ri = new RenderableImage() {
            public Vector<RenderableImage> getSources() { return new Vector<RenderableImage>(); }
            public Object getProperty(String name) { return Image.UndefinedProperty; }
            public String[] getPropertyNames() { return null; }
            public boolean isDynamic() { return false; }
            public float getWidth() { return 5; }
            public float getHeight() { return 5; }
            public float getMinX() { return 0; }
            public float getMinY() { return 0; }
            public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) { return rendered; }
            public RenderedImage createDefaultRendering() { return rendered; }
            public RenderedImage createRendering(RenderContext rc) { return rendered; }
        };
        check(ri.getWidth() == 5 && ri.getHeight() == 5, "getWidth/getHeight");
        check(ri.getMinX() == 0 && ri.getMinY() == 0, "getMinX/getMinY");
        check(!ri.isDynamic(), "isDynamic false");
        check(ri.getSources().size() == 0, "getSources empty");
        check(ri.getProperty("x") == Image.UndefinedProperty, "getProperty UndefinedProperty");
        check(ri.createDefaultRendering().getWidth() == 5, "createDefaultRendering");
        check(ri.createScaledRendering(2, 2, null).getWidth() == 5, "createScaledRendering");
        check(ri.createRendering(new RenderContext(new AffineTransform())).getWidth() == 5, "createRendering");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

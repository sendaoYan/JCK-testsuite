/*
 * @test
 * @summary java.awt.image.renderable.RenderableImageProducer conformance: white-box structure plus black-box behavior.
 * @run main RenderableImageProducerTest
 */

import java.awt.*;
import java.awt.image.*;
import java.awt.image.renderable.*;
import java.util.*;
import java.lang.reflect.*;

public class RenderableImageProducerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.RenderableImageProducer");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(ImageProducer.class.isAssignableFrom(c), "implements ImageProducer");
        check(Runnable.class.isAssignableFrom(c), "implements Runnable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        RenderableImage ri = new RenderableImage() {
            public Vector<RenderableImage> getSources() { return new Vector<RenderableImage>(); }
            public Object getProperty(String name) { return Image.UndefinedProperty; }
            public String[] getPropertyNames() { return null; }
            public boolean isDynamic() { return false; }
            public float getWidth() { return 4; }
            public float getHeight() { return 4; }
            public float getMinX() { return 0; }
            public float getMinY() { return 0; }
            public RenderedImage createScaledRendering(int w, int h, RenderingHints hints) { return new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB); }
            public RenderedImage createDefaultRendering() { return new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB); }
            public RenderedImage createRendering(RenderContext rc) { return new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB); }
        };
        RenderContext rc = new RenderContext(new java.awt.geom.AffineTransform());
        RenderableImageProducer rip = new RenderableImageProducer(ri, rc);
        rip.setRenderContext(rc);

        ImageConsumer ic = new ImageConsumer() {
            public void setDimensions(int w, int h) { }
            public void setProperties(Hashtable<?, ?> props) { }
            public void setColorModel(ColorModel model) { }
            public void setHints(int hintflags) { }
            public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] p, int off, int scansize) { }
            public void setPixels(int x, int y, int w, int h, ColorModel model, int[] p, int off, int scansize) { }
            public void imageComplete(int status) { }
        };
        rip.addConsumer(ic);
        check(rip.isConsumer(ic), "isConsumer true");
        rip.removeConsumer(ic);
        check(!rip.isConsumer(ic), "isConsumer false after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

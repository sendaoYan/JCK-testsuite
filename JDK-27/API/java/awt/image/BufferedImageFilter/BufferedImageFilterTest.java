/*
 * @test
 * @summary java.awt.image.BufferedImageFilter conformance: white-box structure plus black-box behavior.
 * @run main BufferedImageFilterTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;

public class BufferedImageFilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.BufferedImageFilter");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public class");
        check(ImageFilter.class.isAssignableFrom(c), "extends ImageFilter");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImageOp op = new RescaleOp(1.0f, 0.0f, null);
        BufferedImageFilter bif = new BufferedImageFilter(op);
        check(bif.getBufferedImageOp() == op, "getBufferedImageOp returns op");

        // drive the filter end-to-end through FilteredImageSource; consumer must get imageComplete
        BufferedImage srcImg = new BufferedImage(2, 2, BufferedImage.TYPE_INT_ARGB);
        FilteredImageSource fis = new FilteredImageSource(srcImg.getSource(), bif);
        final boolean[] complete = {false};
        ImageConsumer ic = new ImageConsumer() {
            public void setDimensions(int w, int h) { }
            public void setProperties(Hashtable<?, ?> props) { }
            public void setColorModel(ColorModel model) { }
            public void setHints(int hintflags) { }
            public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] p, int off, int scansize) { }
            public void setPixels(int x, int y, int w, int h, ColorModel model, int[] p, int off, int scansize) { }
            public void imageComplete(int status) { complete[0] = true; }
        };
        fis.startProduction(ic);
        long deadline = System.currentTimeMillis() + 5000;
        while (!complete[0] && System.currentTimeMillis() < deadline) {
            Thread.sleep(10);
        }
        check(complete[0], "filter pipeline delivered imageComplete to consumer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

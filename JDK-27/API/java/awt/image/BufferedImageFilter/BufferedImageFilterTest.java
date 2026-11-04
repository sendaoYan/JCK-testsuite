/*
 * @test
 * @summary java.awt.image.BufferedImageFilter conformance: white-box structure plus black-box behavior.
 * @run main BufferedImageFilterTest
 */

import java.awt.*;
import java.awt.image.*;
import java.util.*;
import java.lang.reflect.*;
import java.awt.image.BufferedImageFilter;
import java.awt.image.BufferedImageOp;
import java.awt.image.ConvolveOp;
import java.awt.image.Kernel;
import java.awt.image.RescaleOp;

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
        passed += BufferedImageFilterDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class BufferedImageFilterDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.BufferedImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getBufferedImageOp"), "method getBufferedImageOp");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
    }

    static BufferedImageOp identityConvolve() {
        return new ConvolveOp(new Kernel(3, 3, new float[] { 0, 0, 0, 0, 1, 0, 0, 0, 0 }));
    }

    static void testOpWrapping() throws Exception {
        // The wrapped op is returned by reference and survives cloning.
        BufferedImageOp op = identityConvolve();
        BufferedImageFilter bif = new BufferedImageFilter(op);
        check(bif.getBufferedImageOp() == op, "getBufferedImageOp same reference");
        BufferedImageFilter cloned = (BufferedImageFilter) bif.clone();
        check(cloned != bif, "clone distinct object");
        check(cloned.getBufferedImageOp() == op, "clone wraps the same op");
        // A different op is also accepted.
        RescaleOp rs = new RescaleOp(1f, 0f, null);
        check(new BufferedImageFilter(rs).getBufferedImageOp() == rs, "RescaleOp wrappable");
        // getFilterInstance produces a per-consumer clone with the same op.
        Object inst = bif.getFilterInstance(null);
        check(inst != bif && inst instanceof BufferedImageFilter, "getFilterInstance returns clone");
        check(((BufferedImageFilter) inst).getBufferedImageOp() == op,
              "instance clone keeps the op");
    }

    static void testNullOp() {
        boolean npe = false;
        try { new BufferedImageFilter(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "null op -> NPE");
    }



    static int run() throws Exception {
        testWhiteBox();
        testOpWrapping();
        testNullOp();
        return passed;
    }
}

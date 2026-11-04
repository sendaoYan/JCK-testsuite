/*
 * @test
 * @summary java.awt.image.ImageFilter conformance: class reflection + passthrough contract.
 * @run main ImageFilterTest
 */

import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ImageFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.ImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.ImageConsumer", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "getFilterInstance"), "method getFilterInstance");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
        check(hasMethod(c, "resendTopDownLeftRight"), "method resendTopDownLeftRight");
        check(hasMethod(c, "clone"), "method clone");
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, pixels, completions;
        Hashtable<?,?> props;
        final List<String> order = new ArrayList<String>();
        public void setDimensions(int w, int h) { this.w = w; this.h = h; order.add("dim"); }
        public void setProperties(Hashtable<?,?> p) { props = p; order.add("props"); }
        public void setColorModel(java.awt.image.ColorModel m) { order.add("cm"); }
        public void setHints(int h) { order.add("hints"); }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) { pixels += w * h; order.add("pix"); }
        public void imageComplete(int status) { completions++; order.add("complete"); }
    }

    static void testPassthrough() throws Exception {
        // The base filter forwards everything unchanged through a FilteredImageSource.
        int[] pix = { 0xFF100000, 0xFF200000, 0xFF300000, 0xFF400000,
                      0xFF500000, 0xFF600000, 0xFF700000, 0xFF800000 };
        MemoryImageSource src = new MemoryImageSource(4, 2, pix, 0, 4);
        FilteredImageSource fis = new FilteredImageSource(src, new ImageFilter());
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 4 && rec.h == 2, "dimensions forwarded unchanged");
        check(rec.pixels == 8, "all 8 pixels forwarded");
        check(rec.completions >= 1, "completion forwarded");
        check(rec.order.indexOf("dim") < rec.order.indexOf("pix")
              && rec.order.indexOf("pix") < rec.order.indexOf("complete"),
              "delivery order dim -> pix -> complete");
        check(rec.props != null && rec.props.containsKey("filters"),
              "filter chain appends 'filters' property");
    }

    static void testCloneAndInstance() {
        ImageFilter f = new ImageFilter();
        // clone() yields a distinct filter of the same class.
        Object cl = f.clone();
        check(cl != f && cl instanceof ImageFilter, "clone distinct same class");
        // getFilterInstance hands each consumer its own filter clone.
        ImageFilter inst = f.getFilterInstance(new RecordingConsumer());
        check(inst != f && inst instanceof ImageFilter, "getFilterInstance returns a clone");
        ImageFilter inst2 = f.getFilterInstance(new RecordingConsumer());
        check(inst2 != inst, "each consumer gets its own instance");
    }

    static void testResendDefaults() {
        // resendTopDownLeftRight on the base filter asks the producer to resend.
        ImageFilter f = new ImageFilter();
        MemoryImageSource src = new MemoryImageSource(1, 1, new int[] { 0 }, 0, 1);
        f.resendTopDownLeftRight(src);
        check(true, "resendTopDownLeftRight callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testPassthrough();
        testCloneAndInstance();
        testResendDefaults();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

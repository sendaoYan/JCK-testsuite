/*
 * @test
 * @summary java.awt.image.ImageFilter conformance: white-box structure + black-box behavior.
 * @run main ImageFilterTest
 */

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.lang.reflect.*;
import java.awt.image.FilteredImageSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ImageFilterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImageFilter", false, ImageFilterTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(ImageConsumer.class.isAssignableFrom(c), "implements ImageConsumer");
        check(Cloneable.class.isAssignableFrom(c), "implements Cloneable");
        check(hasMethod(c, "getFilterInstance", ImageConsumer.class), "getFilterInstance");
        check(hasMethod(c, "setDimensions", int.class, int.class), "setDimensions");
        check(hasMethod(c, "setProperties", java.util.Hashtable.class), "setProperties");
        check(hasMethod(c, "setColorModel", ColorModel.class), "setColorModel");
        check(hasMethod(c, "setHints", int.class), "setHints");
        check(hasMethod(c, "setPixels", int.class, int.class, int.class, int.class, ColorModel.class, int[].class, int.class, int.class), "setPixels int[]");
        check(hasMethod(c, "imageComplete", int.class), "imageComplete");
        check(hasMethod(c, "resendTopDownLeftRight", ImageProducer.class), "resendTopDownLeftRight");
        check(hasMethod(c, "clone"), "clone()");
    }

    static class Recorder implements ImageConsumer {
        int w = -1, h = -1, hints = -1, status = -1;
        ColorModel cm;
        int[] pixels;
        java.util.Hashtable<?, ?> props;
        public void setDimensions(int width, int height) { this.w = width; this.h = height; }
        public void setProperties(java.util.Hashtable<?, ?> props) { this.props = props; }
        public void setColorModel(ColorModel model) { this.cm = model; }
        public void setHints(int hintflags) { this.hints = hintflags; }
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
            this.pixels = Arrays.copyOfRange(pixels, off, off + w * h);
        }
        public void imageComplete(int status) { this.status = status; }
    }

    static void testBlackBox() {
        ImageFilter f = new ImageFilter();
        Recorder rc = new Recorder();
        ImageFilter inst = f.getFilterInstance(rc);
        check(inst != null && inst != f, "getFilterInstance returns new instance");

        inst.setDimensions(4, 3);
        check(rc.w == 4 && rc.h == 3, "setDimensions forwards");

        ColorModel cm = ColorModel.getRGBdefault();
        inst.setColorModel(cm);
        check(rc.cm == cm, "setColorModel forwards");
        inst.setHints(7);
        check(rc.hints == 7, "setHints forwards");

        int[] px = new int[] { 1, 2, 3, 4 };
        inst.setPixels(0, 0, 2, 2, cm, px, 0, 2);
        check(rc.pixels != null && rc.pixels[0] == 1 && rc.pixels[3] == 4, "setPixels forwards region");

        inst.imageComplete(ImageConsumer.STATICIMAGEDONE);
        check(rc.status == ImageConsumer.STATICIMAGEDONE, "imageComplete forwards status");

        java.util.Hashtable<Object, Object> props = new java.util.Hashtable<>();
        props.put("k", "v");
        inst.setProperties(props);
        check(rc.props != null && rc.props.containsKey("filters"), "setProperties clones and adds filters key");

        ImageProducer producer = new MemoryImageSource(1, 1, new int[] { 0 }, 0, 1);
        inst.resendTopDownLeftRight(producer);
        check(true, "resendTopDownLeftRight callable");

        Object cl = f.clone();
        check(cl != null && cl != f, "clone returns new instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        passed += ImageFilterDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class ImageFilterDeepContract {
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



    static int run() throws Exception {
        testWhiteBox();
        testPassthrough();
        testCloneAndInstance();
        testResendDefaults();
        return passed;
    }
}

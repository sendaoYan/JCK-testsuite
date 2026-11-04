/*
 * @test
 * @summary java.awt.image.FilteredImageSource conformance: class reflection + chain contract.
 * @run main FilteredImageSourceTest
 */

import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.util.Hashtable;

public class FilteredImageSourceTest {
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
        Class<?> c = Class.forName("java.awt.image.FilteredImageSource");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.awt.image.ImageProducer"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "addConsumer"), "method addConsumer");
        check(hasMethod(c, "isConsumer"), "method isConsumer");
        check(hasMethod(c, "removeConsumer"), "method removeConsumer");
        check(hasMethod(c, "startProduction"), "method startProduction");
        check(hasMethod(c, "requestTopDownLeftRightResend"), "method requestTopDownLeftRightResend");
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, pixels, first = -1;
        public void setDimensions(int w, int h) { this.w = w; this.h = h; }
        public void setProperties(Hashtable<?,?> p) { }
        public void setColorModel(java.awt.image.ColorModel m) { }
        public void setHints(int h) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) {
            if (pixels == 0) first = pix[off];
            pixels += w * h;
        }
        public void imageComplete(int status) { }
    }

    static ImageProducer chain(ImageFilter filter) {
        int[] pix = { 0xFF100000, 0xFF200000, 0xFF300000, 0xFF400000,
                      0xFF500000, 0xFF600000, 0xFF700000, 0xFF800000 };
        return new FilteredImageSource(new MemoryImageSource(4, 2, pix, 0, 4), filter);
    }

    static void testConsumerManagement() {
        FilteredImageSource fis = (FilteredImageSource) chain(new ImageFilter());
        RecordingConsumer rec = new RecordingConsumer();
        check(!fis.isConsumer(rec), "unknown consumer not registered");
        fis.addConsumer(rec);
        check(fis.isConsumer(rec), "addConsumer registers");
        // Adding is idempotent: no duplicate delivery.
        int before = rec.pixels;
        fis.addConsumer(rec);
        check(rec.pixels == before, "duplicate addConsumer does not re-deliver");
        fis.removeConsumer(rec);
        check(!fis.isConsumer(rec), "removeConsumer unregisters");
        fis.removeConsumer(rec);
        check(true, "double removeConsumer tolerated");
        try { fis.removeConsumer(null); check(true, "removeConsumer(null) tolerated"); }
        catch (Throwable t) { check(false, "removeConsumer(null) should be tolerated"); }
    }

    static void testChainedDelivery() {
        // A chained crop delivers only the cropped pixels.
        FilteredImageSource fis = (FilteredImageSource) chain(new CropImageFilter(0, 0, 2, 2));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        check(rec.w == 2 && rec.h == 2, "cropped dims delivered");
        check(rec.pixels == 4, "4 cropped pixels delivered");
        check(rec.first == 0xFF100000, "first pixel exact");
        // startProduction registers the consumer as a side effect.
        check(fis.isConsumer(rec), "startProduction registers the consumer");
    }

    static void testResend() {
        FilteredImageSource fis = (FilteredImageSource) chain(new ImageFilter());
        RecordingConsumer rec = new RecordingConsumer();
        fis.addConsumer(rec);
        // requestTopDownLeftRightResend on an unfiltered-order source is a no-op here
        // but must not throw for the chained source.
        fis.requestTopDownLeftRightResend(rec);
        check(true, "requestTopDownLeftRightResend callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConsumerManagement();
        testChainedDelivery();
        testResend();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

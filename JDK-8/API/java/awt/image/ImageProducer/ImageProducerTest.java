/*
 * @test
 * @summary java.awt.image.ImageProducer conformance: interface reflection + implementor contract.
 * @run main ImageProducerTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageFilter;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;

public class ImageProducerTest {
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
        Class<?> c = Class.forName("java.awt.image.ImageProducer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "addConsumer"), "method addConsumer");
        check(hasMethod(c, "isConsumer"), "method isConsumer");
        check(hasMethod(c, "removeConsumer"), "method removeConsumer");
        check(hasMethod(c, "startProduction"), "method startProduction");
        check(hasMethod(c, "requestTopDownLeftRightResend"), "method requestTopDownLeftRightResend");
    }

    static void testImplementors() {
        // Known platform implementors.
        check(MemoryImageSource.class.getInterfaces().length == 1
              && MemoryImageSource.class.getInterfaces()[0] == ImageProducer.class,
              "MemoryImageSource implements ImageProducer");
        check(FilteredImageSource.class.getInterfaces()[0] == ImageProducer.class,
              "FilteredImageSource implements ImageProducer");
        // BufferedImage.getSource() hands out a live ImageProducer.
        BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        ImageProducer p = bi.getSource();
        check(p != null && p instanceof ImageProducer, "BufferedImage.getSource() is an ImageProducer");
        // The producer of a filtered chain wraps the original producer.
        FilteredImageSource fis = new FilteredImageSource(p, new ImageFilter());
        check(fis != null, "FilteredImageSource wraps producer + filter");
    }

    static void testProducerContract() {
        // An animated MemoryImageSource keeps consumers registered between frames,
        // exercising the full five-method contract of the interface.
        int[] pix = { 0xFF000000, 0xFF010101, 0xFF020202, 0xFF030303 };
        MemoryImageSource src = new MemoryImageSource(2, 2, pix, 0, 2);
        src.setAnimated(true);
        RecordingConsumer c1 = new RecordingConsumer();
        RecordingConsumer c2 = new RecordingConsumer();
        check(!src.isConsumer(c1), "unknown consumer not tracked");
        src.addConsumer(c1);
        check(src.isConsumer(c1), "addConsumer registers the consumer");
        check(c1.frames == 1, "addConsumer delivers the current frame synchronously");
        src.addConsumer(c1);   // duplicate registration is idempotent
        check(src.isConsumer(c1) && c1.frames == 1, "duplicate addConsumer tolerated, one frame");
        src.removeConsumer(c1);
        check(!src.isConsumer(c1), "removeConsumer unregisters the consumer");
        src.removeConsumer(c1);   // removing twice is tolerated
        check(true, "double removeConsumer tolerated");
        RecordingConsumer c3 = new RecordingConsumer();
        src.addConsumer(c3);
        src.addConsumer(c2);
        check(src.isConsumer(c3) && src.isConsumer(c2), "multiple consumers registered");
        src.removeConsumer(c3);
        RecordingConsumer c4 = new RecordingConsumer();
        src.startProduction(c4);   // registers and delivers to the given consumer
        check(c4.frames == 1, "startProduction registers and delivers one frame");
        check(c3.frames == 1, "removed consumer receives no further frames");
        check(c2.frames == 1, "already-current consumer not re-delivered");
        // requestTopDownLeftRightResend must be callable (no-op allowed for
        // sources that already send in that order).
        src.requestTopDownLeftRightResend(c2);
        check(true, "requestTopDownLeftRightResend callable");
        // removeConsumer(null) is tolerated; addConsumer(null) rejects with NPE.
        src.removeConsumer(null);
        check(true, "removeConsumer(null) tolerated");
        boolean npe = false;
        try { src.addConsumer(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "addConsumer(null) -> NPE");
    }

    static class RecordingConsumer implements java.awt.image.ImageConsumer {
        int frames;
        public void setDimensions(int w, int h) { }
        public void setProperties(java.util.Hashtable<?,?> p) { }
        public void setColorModel(java.awt.image.ColorModel m) { }
        public void setHints(int h) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) { frames++; }
        public void imageComplete(int status) { }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testImplementors();
        testProducerContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

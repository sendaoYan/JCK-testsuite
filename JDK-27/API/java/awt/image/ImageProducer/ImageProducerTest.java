/*
 * @test
 * @summary java.awt.image.ImageProducer conformance: white-box structure + black-box contract.
 * @run main ImageProducerTest
 */

import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.lang.reflect.*;

public class ImageProducerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.ImageProducer", false, ImageProducerTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(Modifier.isInterface(m), "interface");
        check(hasMethod(c, "addConsumer", ImageConsumer.class), "addConsumer(ImageConsumer)");
        check(hasMethod(c, "isConsumer", ImageConsumer.class), "isConsumer(ImageConsumer)");
        check(hasMethod(c, "removeConsumer", ImageConsumer.class), "removeConsumer(ImageConsumer)");
        check(hasMethod(c, "startProduction", ImageConsumer.class), "startProduction(ImageConsumer)");
        check(hasMethod(c, "requestTopDownLeftRightResend", ImageConsumer.class), "requestTopDownLeftRightResend(ImageConsumer)");
    }

    // Minimal recording consumer used to exercise the producer contract.
    static class Consumer implements ImageConsumer {
        int status = -1, w = -1, h = -1;
        public void setDimensions(int width, int height) { this.w = width; this.h = height; }
        public void setProperties(java.util.Hashtable<?, ?> props) { }
        public void setColorModel(java.awt.image.ColorModel model) { }
        public void setHints(int hintflags) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, byte[] pixels, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel model, int[] pixels, int off, int scansize) { }
        public void imageComplete(int status) { this.status = status; }
    }

    static void testBlackBox() {
        // MemoryImageSource is a concrete ImageProducer: drive its contract.
        // A non-animating MemoryImageSource delivers immediately and then
        // auto-removes the consumer, ending with IMAGEERROR after STATICIMAGEDONE.
        ImageProducer producer = new MemoryImageSource(4, 3, new int[12], 0, 4);
        Consumer c = new Consumer();
        check(!producer.isConsumer(c), "not yet consumer");
        producer.addConsumer(c);
        check(c.w == 4 && c.h == 3, "addConsumer delivers dimensions");
        check(c.status == ImageConsumer.IMAGEERROR, "addConsumer ends IMAGEERROR for non-animating source");
        check(!producer.isConsumer(c), "auto-removed after delivery");
        Consumer c2 = new Consumer();
        producer.startProduction(c2);
        check(c2.w == 4 && c2.h == 3, "startProduction delivers dimensions");
        check(c2.status == ImageConsumer.IMAGEERROR, "startProduction ends IMAGEERROR for non-animating source");
        producer.removeConsumer(c);
        check(!producer.isConsumer(c), "isConsumer false after remove");
        producer.requestTopDownLeftRightResend(c);
        check(true, "requestTopDownLeftRightResend callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

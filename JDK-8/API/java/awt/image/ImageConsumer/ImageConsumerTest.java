/*
 * @test
 * @summary java.awt.image.ImageConsumer conformance: interface reflection + constant contract + delivery.
 * @run main ImageConsumerTest
 */

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.MemoryImageSource;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

public class ImageConsumerTest {
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
        Class<?> c = Class.forName("java.awt.image.ImageConsumer");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setColorModel"), "method setColorModel");
        check(hasMethod(c, "setHints"), "method setHints");
        check(hasMethod(c, "setPixels"), "method setPixels");
        check(hasMethod(c, "imageComplete"), "method imageComplete");
    }

    static void testConstants() {
        // Delivery-ordering hints.
        check(ImageConsumer.RANDOMPIXELORDER == 1, "RANDOMPIXELORDER == 1");
        check(ImageConsumer.TOPDOWNLEFTRIGHT == 2, "TOPDOWNLEFTRIGHT == 2");
        check(ImageConsumer.COMPLETESCANLINES == 4, "COMPLETESCANLINES == 4");
        check(ImageConsumer.SINGLEPASS == 8, "SINGLEPASS == 8");
        check(ImageConsumer.SINGLEFRAME == 16, "SINGLEFRAME == 16");
        // Completion statuses.
        check(ImageConsumer.IMAGEERROR == 1, "IMAGEERROR == 1");
        check(ImageConsumer.SINGLEFRAMEDONE == 2, "SINGLEFRAMEDONE == 2");
        check(ImageConsumer.STATICIMAGEDONE == 3, "STATICIMAGEDONE == 3");
        check(ImageConsumer.IMAGEABORTED == 4, "IMAGEABORTED == 4");
        check((ImageConsumer.TOPDOWNLEFTRIGHT | ImageConsumer.COMPLETESCANLINES
                | ImageConsumer.SINGLEPASS | ImageConsumer.SINGLEFRAME) == 30,
              "hint flags are distinct powers of two");
    }

    /** Records the exact callback sequence delivered by an ImageProducer. */
    static class RecordingConsumer implements ImageConsumer {
        final List<String> log = new ArrayList<String>();
        int firstPixel = -1;
        int firstStatus = -1;
        public void setDimensions(int w, int h) { log.add("dim"); this.w = w; this.h = h; }
        public void setProperties(Hashtable<?,?> p) { log.add("props"); }
        public void setColorModel(ColorModel m) { log.add("cm"); cm = m; }
        public void setHints(int h) { log.add("hints"); hints = h; }
        public void setPixels(int x, int y, int w, int h, ColorModel m, byte[] pix, int off, int s) {
            log.add("pixB");
        }
        public void setPixels(int x, int y, int w, int h, ColorModel m, int[] pix, int off, int s) {
            log.add("pixI"); firstPixel = pix[off];
        }
        public void imageComplete(int status) {
            log.add("complete"); if (firstStatus < 0) firstStatus = status;
        }
        int w = -1, h = -1, hints = -1;
        ColorModel cm;
    }

    static void testDelivery() {
        // A MemoryImageSource delivers a complete frame to a registered consumer:
        // dimensions first, then properties/model/hints, then the pixel rect, then completion.
        int[] pix = { 0xFF112233, 0xFF445566, 0xFF778899, 0xFFAABBCC };
        MemoryImageSource src = new MemoryImageSource(2, 2, pix, 0, 2);
        RecordingConsumer rec = new RecordingConsumer();
        src.startProduction(rec);
        check(rec.w == 2 && rec.h == 2, "setDimensions reports 2x2");
        check(rec.log.get(0).equals("dim"), "dimensions delivered first");
        check(rec.log.contains("props") && rec.log.contains("cm") && rec.log.contains("hints"),
              "properties, model and hints delivered");
        check(rec.cm instanceof ColorModel, "color model delivered");
        check(rec.log.contains("pixI") && rec.firstPixel == 0xFF112233,
              "int pixel rect delivered with exact first pixel");
        check(rec.firstStatus == ImageConsumer.STATICIMAGEDONE,
              "first completion status is STATICIMAGEDONE");
        check(rec.log.indexOf("pixI") < rec.log.indexOf("complete"),
              "pixels arrive before completion");
        // Removing the consumer stops further deliveries.
        src.removeConsumer(rec);
        rec.log.clear();
        src.startProduction(rec);   // startProduction re-adds and re-delivers by contract
        check(rec.log.contains("pixI"), "startProduction re-delivers after removeConsumer");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstants();
        testDelivery();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

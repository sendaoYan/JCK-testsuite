/*
 * @test
 * @summary java.awt.image.FilteredImageSource conformance: white-box structure + black-box behavior.
 * @run main FilteredImageSourceTest
 */

import java.awt.image.ColorModel;
import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.lang.reflect.*;

public class FilteredImageSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.FilteredImageSource", false, FilteredImageSourceTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(ImageProducer.class.isAssignableFrom(c), "implements ImageProducer");
        check(hasMethod(c, "addConsumer", ImageConsumer.class), "addConsumer");
        check(hasMethod(c, "isConsumer", ImageConsumer.class), "isConsumer");
        check(hasMethod(c, "removeConsumer", ImageConsumer.class), "removeConsumer");
        check(hasMethod(c, "startProduction", ImageConsumer.class), "startProduction");
        check(hasMethod(c, "requestTopDownLeftRightResend", ImageConsumer.class), "requestTopDownLeftRightResend");
    }

    static class Recorder implements ImageConsumer {
        int w = -1, h = -1, status = -1;
        int[] pixels;
        public void setDimensions(int width, int height) { this.w = width; this.h = height; }
        public void setProperties(java.util.Hashtable<?, ?> props) { }
        public void setColorModel(ColorModel model) { }
        public void setHints(int hintflags) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
            this.pixels = Arrays.copyOfRange(pixels, off, off + w * h);
        }
        public void imageComplete(int status) { this.status = status; }
    }

    static void testBlackBox() {
        int[] pix = new int[] { 0xFF111111, 0xFF222222, 0xFF333333, 0xFF444444 };
        MemoryImageSource mis = new MemoryImageSource(2, 2, pix, 0, 2);

        // Full crop = identity passthrough of the underlying producer.
        FilteredImageSource fis = new FilteredImageSource(mis, new CropImageFilter(0, 0, 2, 2));
        Recorder rc = new Recorder();
        check(!fis.isConsumer(rc), "not consumer initially");
        fis.addConsumer(rc);
        check(fis.isConsumer(rc), "isConsumer after add");
        fis.startProduction(rc);
        check(rc.w == 2 && rc.h == 2, "filtered dimensions 2x2");
        check(rc.pixels != null && rc.pixels[0] == 0xFF111111 && rc.pixels[3] == 0xFF444444, "filtered pixels passthrough");
        check(rc.status == ImageConsumer.IMAGEERROR, "non-animating source ends IMAGEERROR after STATICIMAGEDONE");

        // Cropping filter narrows to 1x1 top-left pixel.
        FilteredImageSource fisCrop = new FilteredImageSource(mis, new CropImageFilter(0, 0, 1, 1));
        Recorder rc2 = new Recorder();
        fisCrop.startProduction(rc2);
        check(rc2.w == 1 && rc2.h == 1, "cropped dimensions 1x1");
        check(rc2.pixels != null && rc2.pixels[0] == 0xFF111111, "cropped pixel");

        fis.requestTopDownLeftRightResend(rc);
        check(true, "requestTopDownLeftRightResend callable");
        fis.removeConsumer(rc);
        check(!fis.isConsumer(rc), "isConsumer false after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

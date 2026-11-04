/*
 * @test
 * @summary java.awt.image.MemoryImageSource conformance: white-box structure + black-box behavior.
 * @run main MemoryImageSourceTest
 */

import java.awt.image.ColorModel;
import java.awt.image.ImageConsumer;
import java.awt.image.ImageProducer;
import java.awt.image.MemoryImageSource;
import java.util.Arrays;
import java.lang.reflect.*;

public class MemoryImageSourceTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... p) {
        try { c.getMethod(name, p); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.image.MemoryImageSource", false, MemoryImageSourceTest.class.getClassLoader());
        check(c != null, "type loads");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "public");
        check(ImageProducer.class.isAssignableFrom(c), "implements ImageProducer");
        check(hasMethod(c, "addConsumer", ImageConsumer.class), "addConsumer");
        check(hasMethod(c, "isConsumer", ImageConsumer.class), "isConsumer");
        check(hasMethod(c, "removeConsumer", ImageConsumer.class), "removeConsumer");
        check(hasMethod(c, "startProduction", ImageConsumer.class), "startProduction");
        check(hasMethod(c, "requestTopDownLeftRightResend", ImageConsumer.class), "requestTopDownLeftRightResend");
        check(hasMethod(c, "newPixels"), "newPixels()");
        check(hasMethod(c, "newPixels", int.class, int.class, int.class, int.class), "newPixels(x,y,w,h)");
        check(hasMethod(c, "setAnimated", boolean.class), "setAnimated");
        check(hasMethod(c, "setFullBufferUpdates", boolean.class), "setFullBufferUpdates");
    }

    static class Recorder implements ImageConsumer {
        int w = -1, h = -1, status = -1;
        int[] pixels;
        ColorModel cm;
        public void setDimensions(int width, int height) { this.w = width; this.h = height; }
        public void setProperties(java.util.Hashtable<?, ?> props) { }
        public void setColorModel(ColorModel model) { this.cm = model; }
        public void setHints(int hintflags) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, byte[] pixels, int off, int scansize) { }
        public void setPixels(int x, int y, int w, int h, ColorModel model, int[] pixels, int off, int scansize) {
            this.pixels = Arrays.copyOfRange(pixels, off, off + w * h);
        }
        public void imageComplete(int status) { this.status = status; }
    }

    static void testBlackBox() {
        int w = 3, h = 2;
        int[] pix = new int[] { 0xFF111111, 0xFF222222, 0xFF333333, 0xFF444444, 0xFF555555, 0xFF666666 };
        MemoryImageSource mis = new MemoryImageSource(w, h, pix, 0, w);
        Recorder rc = new Recorder();
        check(!mis.isConsumer(rc), "not consumer initially");
        mis.addConsumer(rc);
        check(rc.w == 3 && rc.h == 2, "dimensions delivered");
        check(rc.pixels != null && rc.pixels[0] == 0xFF111111 && rc.pixels[5] == 0xFF666666, "pixels delivered");
        check(rc.status == ImageConsumer.IMAGEERROR, "non-animating addConsumer ends IMAGEERROR");
        check(rc.cm != null, "color model delivered");
        check(!mis.isConsumer(rc), "non-animating consumer auto-removed");
        mis.startProduction(rc);
        check(rc.status == ImageConsumer.IMAGEERROR, "startProduction re-delivers then ends IMAGEERROR");

        mis.setAnimated(true);
        mis.setFullBufferUpdates(true);
        mis.newPixels();
        check(true, "newPixels() callable after animation flags");

        int[] upd = new int[] { 0xFFAAAAAA };
        mis.newPixels(0, 0, 1, 1);
        mis.newPixels(upd, ColorModel.getRGBdefault(), 0, 1);
        check(true, "newPixels variants callable");

        mis.requestTopDownLeftRightResend(rc);
        check(true, "requestTopDownLeftRightResend callable");
        mis.removeConsumer(rc);
        check(!mis.isConsumer(rc), "isConsumer false after remove");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

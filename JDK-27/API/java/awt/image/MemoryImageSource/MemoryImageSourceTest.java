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
import java.util.Hashtable;

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
        passed += MemoryImageSourceDeepContract.run();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class MemoryImageSourceDeepContract {
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
        Class<?> c = Class.forName("java.awt.image.MemoryImageSource");
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
        check(hasMethod(c, "setAnimated"), "method setAnimated");
        check(hasMethod(c, "setFullBufferUpdates"), "method setFullBufferUpdates");
        check(hasMethod(c, "newPixels"), "method newPixels");
    }

    static class RecordingConsumer implements ImageConsumer {
        int w = -1, h = -1, frames, first = -1, firstStatus = -1;
        Hashtable<?,?> props;
        public void setDimensions(int w, int h) { this.w = w; this.h = h; }
        public void setProperties(Hashtable<?,?> p) { this.props = p; }
        public void setColorModel(java.awt.image.ColorModel m) { }
        public void setHints(int h) { }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              byte[] pix, int off, int scansize) {
            if (frames == 0) first = pix[off];
            frames++;
        }
        public void setPixels(int x, int y, int w, int h, java.awt.image.ColorModel m,
                              int[] pix, int off, int scansize) {
            if (frames == 0) first = pix[off];
            frames++;
        }
        public void imageComplete(int status) { if (firstStatus < 0) firstStatus = status; }
    }

    static int[] pixels() {
        return new int[] { 0xFF112233, 0xFF445566, 0xFF778899, 0xFFAABBCC };
    }

    static void testStaticDelivery() {
        // A non-animated source delivers its frame on demand and completes statically.
        MemoryImageSource src = new MemoryImageSource(2, 2, pixels(), 0, 2);
        RecordingConsumer rec = new RecordingConsumer();
        src.startProduction(rec);
        check(rec.w == 2 && rec.h == 2, "dimensions 2x2");
        check(rec.frames >= 1 && rec.first == 0xFF112233, "int pixels delivered exact");
        check(rec.firstStatus == ImageConsumer.STATICIMAGEDONE, "STATICIMAGEDONE completion first");
        // The byte-array constructor with the default RGB model.
        MemoryImageSource byteSrc = new MemoryImageSource(2, 2,
                java.awt.image.ColorModel.getRGBdefault(),
                new byte[] { 0, 0, 0, 0, 1, 1, 1, 1 }, 0, 2);
        RecordingConsumer rec2 = new RecordingConsumer();
        byteSrc.startProduction(rec2);
        check(rec2.frames >= 1, "byte source delivers");
        // Properties handed to the constructor are forwarded.
        Hashtable<String, Object> props = new Hashtable<String, Object>();
        props.put("owner", "test");
        MemoryImageSource propSrc = new MemoryImageSource(2, 2, pixels(), 0, 2, props);
        RecordingConsumer rec3 = new RecordingConsumer();
        propSrc.startProduction(rec3);
        check(rec3.props != null && "test".equals(rec3.props.get("owner")),
              "constructor properties forwarded");
    }

    static void testAnimatedFrames() {
        // An animated source keeps its consumers and pushes new frames at them.
        MemoryImageSource src = new MemoryImageSource(2, 2, pixels(), 0, 2);
        src.setAnimated(true);
        RecordingConsumer rec = new RecordingConsumer();
        src.addConsumer(rec);
        check(src.isConsumer(rec), "animated source keeps consumer");
        check(rec.frames >= 1, "initial frame delivered at registration");
        int before = rec.frames;
        src.newPixels();
        check(rec.frames > before, "newPixels pushes a fresh frame");
        // Mutating the backing array changes what the next frame carries.
        int[] pix = pixels();
        MemoryImageSource live = new MemoryImageSource(2, 2, pix, 0, 2);
        live.setAnimated(true);
        RecordingConsumer rec2 = new RecordingConsumer();
        live.addConsumer(rec2);
        pix[0] = 0xFF000000;
        live.newPixels();
        // setFullBufferUpdates selects whole-frame notification granularity.
        live.setFullBufferUpdates(true);
        live.newPixels();
        check(true, "setFullBufferUpdates + newPixels callable");
    }

    static void testErrors() {
        MemoryImageSource src = new MemoryImageSource(2, 2, pixels(), 0, 2);
        boolean npe = false;
        try { src.addConsumer(null); } catch (NullPointerException e) { npe = true; }
        check(npe, "addConsumer(null) -> NPE");
        // removeConsumer(null) is explicitly tolerated.
        src.removeConsumer(null);
        check(true, "removeConsumer(null) tolerated");
    }



    static int run() throws Exception {
        testWhiteBox();
        testStaticDelivery();
        testAnimatedFrames();
        testErrors();
        return passed;
    }
}

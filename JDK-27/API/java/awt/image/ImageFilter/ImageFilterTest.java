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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

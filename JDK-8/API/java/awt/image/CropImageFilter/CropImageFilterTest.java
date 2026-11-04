/*
 * @test
 * @summary java.awt.image.CropImageFilter conformance: class reflection + cropping contract.
 * @run main CropImageFilterTest
 */

import java.awt.image.CropImageFilter;
import java.awt.image.FilteredImageSource;
import java.awt.image.ImageConsumer;
import java.awt.image.MemoryImageSource;
import java.util.Hashtable;

public class CropImageFilterTest {
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
        Class<?> c = Class.forName("java.awt.image.CropImageFilter");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.awt.image.ImageFilter"), "superclass java.awt.image.ImageFilter");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        check(hasMethod(c, "setProperties"), "method setProperties");
        check(hasMethod(c, "setDimensions"), "method setDimensions");
        check(hasMethod(c, "setPixels"), "method setPixels");
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

    /** 4x2 image with a distinct value per pixel. */
    static int[] sourcePixels() {
        return new int[] { 0xFF100000, 0xFF200000, 0xFF300000, 0xFF400000,
                           0xFF500000, 0xFF600000, 0xFF700000, 0xFF800000 };
    }

    static RecordingConsumer crop(int x, int y, int w, int h) {
        MemoryImageSource src = new MemoryImageSource(4, 2, sourcePixels(), 0, 4);
        FilteredImageSource fis = new FilteredImageSource(src, new CropImageFilter(x, y, w, h));
        RecordingConsumer rec = new RecordingConsumer();
        fis.startProduction(rec);
        return rec;
    }

    static void testCropRegion() {
        // Crop to the 2x1 rectangle at (1,1): exactly those source pixels survive.
        RecordingConsumer rec = crop(1, 1, 2, 1);
        check(rec.w == 2 && rec.h == 1, "cropped dims 2x1");
        check(rec.pixels == 2, "2 pixels delivered");
        check(rec.first == 0xFF600000, "first cropped pixel is source (1,1)");
        // Crop the left half.
        RecordingConsumer left = crop(0, 0, 2, 2);
        check(left.w == 2 && left.h == 2 && left.pixels == 4, "left half dims/pixels");
        check(left.first == 0xFF100000, "left half starts at source (0,0)");
        // Identity crop keeps everything.
        RecordingConsumer all = crop(0, 0, 4, 2);
        check(all.w == 4 && all.h == 2 && all.pixels == 8, "identity crop keeps whole image");
        // Degenerate 1x1 crop.
        RecordingConsumer one = crop(3, 1, 1, 1);
        check(one.w == 1 && one.h == 1 && one.pixels == 1, "1x1 crop");
        check(one.first == 0xFF800000, "1x1 crop takes source (3,1)");
    }

    static void testOverflowClip() {
        // A crop rectangle larger than the source clips to the available pixels.
        RecordingConsumer rec = crop(2, 1, 100, 100);
        check(rec.w == 100 && rec.h == 100, "requested crop dims reported");
        check(rec.pixels == 2, "only the overlapping pixels are delivered");
        check(rec.first == 0xFF700000, "clipped crop starts at source (2,1)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testCropRegion();
        testOverflowClip();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

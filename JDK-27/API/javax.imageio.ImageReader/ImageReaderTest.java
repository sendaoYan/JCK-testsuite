/*
 * @test
 * @summary javax.imageio.ImageReader conformance: white-box structure + black-box behavior.
 * @run main ImageReaderTest
 */

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.stream.ImageInputStream;

public class ImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageReader");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getFormatName"), "method getFormatName");
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("getInput"), "method getInput");
        check(dm.contains("isSeekForwardOnly"), "method isSeekForwardOnly");
        check(dm.contains("isIgnoringMetadata"), "method isIgnoringMetadata");
        check(dm.contains("getMinIndex"), "method getMinIndex");
        check(dm.contains("getNumImages"), "method getNumImages");
        check(dm.contains("getWidth"), "method getWidth");
        check(dm.contains("getHeight"), "method getHeight");
        check(dm.contains("getRawImageType"), "method getRawImageType");
        check(dm.contains("getImageTypes"), "method getImageTypes");
        check(dm.contains("getDefaultReadParam"), "method getDefaultReadParam");
        check(dm.contains("getStreamMetadata"), "method getStreamMetadata");
        check(dm.contains("getImageMetadata"), "method getImageMetadata");
        check(dm.contains("read"), "method read");
        check(dm.contains("readAll"), "method readAll");
        check(dm.contains("canReadRaster"), "method canReadRaster");
        check(dm.contains("isImageTiled"), "method isImageTiled");
        check(dm.contains("getTileWidth"), "method getTileWidth");
        check(dm.contains("getTileHeight"), "method getTileHeight");
        check(dm.contains("readerSupportsThumbnails"), "method readerSupportsThumbnails");
        check(dm.contains("hasThumbnails"), "method hasThumbnails");
        check(dm.contains("addIIOReadWarningListener"), "method addIIOReadWarningListener");
        check(dm.contains("removeIIOReadWarningListener"), "method removeIIOReadWarningListener");
        check(dm.contains("addIIOReadProgressListener"), "method addIIOReadProgressListener");
        check(dm.contains("removeIIOReadProgressListener"), "method removeIIOReadProgressListener");
        check(dm.contains("addIIOReadUpdateListener"), "method addIIOReadUpdateListener");
        check(dm.contains("removeIIOReadUpdateListener"), "method removeIIOReadUpdateListener");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // build a png in memory
        BufferedImage src = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        src.setRGB(0, 0, 0xFF0000FF);
        src.setRGB(3, 3, 0xFF00FF00);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(src, "png", bos);

        Iterator<ImageReader> it = ImageIO.getImageReadersByFormatName("png");
        check(it.hasNext(), "png reader available");
        ImageReader reader = it.next();

        // identity/info accessors
        check("png".equals(reader.getFormatName()), "getFormatName png");
        check(reader.getOriginatingProvider() != null, "getOriginatingProvider non-null");
        check(reader.getMinIndex() == 0, "getMinIndex 0");
        check(reader.getDefaultReadParam() != null, "getDefaultReadParam non-null");
        check(reader.getLocale() == null, "default locale null");
        check(reader.getAvailableLocales() == null, "PNG reader has no available locales");

        // input wiring
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bos.toByteArray()));
        reader.setInput(iis);
        check(reader.getInput() == iis, "getInput identity after setInput");
        check(!reader.isSeekForwardOnly(), "default seekForwardOnly false");
        check(!reader.isIgnoringMetadata(), "default ignoringMetadata false");

        // image metrics
        check(reader.getNumImages(true) == 1, "numImages 1");
        check(reader.getWidth(0) == 4, "width 4");
        check(reader.getHeight(0) == 4, "height 4");
        check(reader.getAspectRatio(0) == 1.0f, "aspect ratio 1.0");
        check(reader.getRawImageType(0) != null, "rawImageType non-null");
        Iterator<ImageTypeSpecifier> types = reader.getImageTypes(0);
        check(types.hasNext(), "getImageTypes has next");

        // tiling / thumbnails for a single png
        check(!reader.isImageTiled(0), "png not tiled");
        check(reader.getTileWidth(0) == 4, "tile width equals image width");
        check(reader.getTileHeight(0) == 4, "tile height equals image height");
        check(!reader.readerSupportsThumbnails(), "png reader no thumbnails");

        // decode round-trip through the abstract read(0)
        BufferedImage back = reader.read(0);
        check(back.getWidth() == 4 && back.getHeight() == 4, "read dimensions");
        check(back.getRGB(0, 0) == 0xFF0000FF, "read pixel (0,0)");
        check(back.getRGB(3, 3) == 0xFF00FF00, "read pixel (3,3)");

        // progress listener fired during a decode
        final int[] started = new int[1];
        final int[] completed = new int[1];
        IIOReadProgressListener l = new IIOReadProgressListener() {
            public void sequenceStarted(ImageReader r, int min) {}
            public void sequenceComplete(ImageReader r) {}
            public void imageStarted(ImageReader r, int index) { started[0]++; }
            public void imageProgress(ImageReader r, float pct) {}
            public void imageComplete(ImageReader r) { completed[0]++; }
            public void thumbnailStarted(ImageReader r, int i, int t) {}
            public void thumbnailProgress(ImageReader r, float pct) {}
            public void thumbnailComplete(ImageReader r) {}
            public void readAborted(ImageReader r) {}
        };
        reader.addIIOReadProgressListener(l);
        reader.read(0);
        check(started[0] == 1, "imageStarted fired once");
        check(completed[0] == 1, "imageComplete fired once");
        reader.removeIIOReadProgressListener(l);

        reader.dispose();
        iis.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

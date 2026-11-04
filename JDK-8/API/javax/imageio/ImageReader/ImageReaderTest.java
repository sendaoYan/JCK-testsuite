/*
 * @test
 * @summary javax.imageio.ImageReader conformance: abstract hierarchy and methods.
 * @run main ImageReaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.event.IIOReadUpdateListener;
import javax.imageio.event.IIOReadWarningListener;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.stream.ImageInputStream;

public class ImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected == t.getClass(),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    // Records the full IIOReadProgressListener callback stream
    static class ProgressRecorder implements IIOReadProgressListener {
        int sequenceStarted, sequenceComplete, imageStarted, imageComplete, readAborted;
        int lastMinIndex = -1, lastImageIndex = -1;
        ImageReader lastSource;
        public void sequenceStarted(ImageReader r, int min) { sequenceStarted++; lastMinIndex = min; lastSource = r; }
        public void sequenceComplete(ImageReader r) { sequenceComplete++; }
        public void imageStarted(ImageReader r, int i) { imageStarted++; lastImageIndex = i; lastSource = r; }
        public void imageProgress(ImageReader r, float pct) { }
        public void imageComplete(ImageReader r) { imageComplete++; }
        public void thumbnailStarted(ImageReader r, int i, int t) { }
        public void thumbnailProgress(ImageReader r, float pct) { }
        public void thumbnailComplete(ImageReader r) { }
        public void readAborted(ImageReader r) { readAborted++; }
    }

    static class StubReaderSpi extends ImageReaderSpi {
        StubReaderSpi() {
            super("coreb", "1.0", new String[]{"corebfmt"}, new String[]{"coreb"}, new String[]{"image/x-coreb"},
                "ImageReaderTest$StubReader", new Class[]{ImageInputStream.class, File.class}, null,
                false, null, null, null, null,
                false, null, null, null, null);
        }
        public boolean canDecodeInput(Object input) { return false; }
        public ImageReader createReaderInstance(Object ext) { return new StubReader(this); }
        public String getDescription(Locale locale) { return "coreb stub reader spi"; }
    }

    // IIOMetadata stub exposing standard + native + extra format names
    static class StubMetadata extends IIOMetadata {
        StubMetadata() {
            super(true, "coreb_native_1.0", null,
                  new String[]{"coreb_extra_1.0"}, new String[]{"ImageReaderTest$StubMetadata"});
        }
        public boolean isReadOnly() { return true; }
        public void reset() { }
        public org.w3c.dom.Node getAsTree(String formatName) { return new IIOMetadataNode("coreb_root"); }
        public void mergeTree(String formatName, org.w3c.dom.Node root) { }
    }

    // Minimal concrete reader: implements only the abstract methods and
    // drives the base-class listener/abort machinery like a real plugin.
    static class StubReader extends ImageReader {
        final StubMetadata streamMeta = new StubMetadata();
        final StubMetadata imageMeta = new StubMetadata();
        int reads;
        StubReader(ImageReaderSpi spi) { super(spi); }
        public int getNumImages(boolean allowSearch) { return 1; }
        public int getWidth(int imageIndex) { return 4; }
        public int getHeight(int imageIndex) { return 4; }
        public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
            return Collections.singletonList(
                ImageTypeSpecifier.createFromBufferedImageType(BufferedImage.TYPE_INT_RGB)).iterator();
        }
        public IIOMetadata getStreamMetadata() { return streamMeta; }
        public IIOMetadata getImageMetadata(int imageIndex) { return imageMeta; }
        public BufferedImage read(int imageIndex, ImageReadParam param) {
            clearAbortRequest();
            processImageStarted(imageIndex);
            processImageProgress(50.0f);
            if (abortRequested()) { processReadAborted(); return null; }
            processImageComplete();
            reads++;
            return new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        }
        void checkBands(ImageReadParam p, int sN, int dN) { checkReadParamBandSettings(p, sN, dN); }
        Rectangle[] compute(ImageReadParam p, int w, int h) {
            Rectangle src = new Rectangle(), dst = new Rectangle();
            computeRegions(p, w, h, new BufferedImage(w, h, BufferedImage.TYPE_INT_RGB), src, dst);
            return new Rectangle[]{src, dst};
        }
        void forceMinIndex(int i) { minIndex = i; }
    }

    static byte[] pngBytes() throws IOException {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        return bos.toByteArray();
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageReader");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getFormatName"), "method getFormatName");
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("setInput"), "method setInput");
        check(dm.contains("getInput"), "method getInput");
        check(dm.contains("isSeekForwardOnly"), "method isSeekForwardOnly");
        check(dm.contains("isIgnoringMetadata"), "method isIgnoringMetadata");
        check(dm.contains("getMinIndex"), "method getMinIndex");
        check(dm.contains("getAvailableLocales"), "method getAvailableLocales");
        check(dm.contains("setLocale"), "method setLocale");
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("getNumImages"), "method getNumImages");
        check(dm.contains("getWidth"), "method getWidth");
        check(dm.contains("getHeight"), "method getHeight");
        check(dm.contains("isRandomAccessEasy"), "method isRandomAccessEasy");
        check(dm.contains("getAspectRatio"), "method getAspectRatio");
        check(dm.contains("getRawImageType"), "method getRawImageType");
        check(dm.contains("getImageTypes"), "method getImageTypes");
        check(dm.contains("getDefaultReadParam"), "method getDefaultReadParam");
        check(dm.contains("getStreamMetadata"), "method getStreamMetadata");
        check(dm.contains("getStreamMetadata"), "method getStreamMetadata");
        check(dm.contains("getImageMetadata"), "method getImageMetadata");
        check(dm.contains("getImageMetadata"), "method getImageMetadata");
        check(dm.contains("read"), "method read");
        check(dm.contains("read"), "method read");
        check(dm.contains("readAll"), "method readAll");
        check(dm.contains("readAll"), "method readAll");
        check(dm.contains("canReadRaster"), "method canReadRaster");
        check(dm.contains("readRaster"), "method readRaster");
        check(dm.contains("isImageTiled"), "method isImageTiled");
        check(dm.contains("getTileWidth"), "method getTileWidth");
        check(dm.contains("getTileHeight"), "method getTileHeight");
        check(dm.contains("getTileGridXOffset"), "method getTileGridXOffset");
        check(dm.contains("getTileGridYOffset"), "method getTileGridYOffset");
        check(dm.contains("readTile"), "method readTile");
        check(dm.contains("readTileRaster"), "method readTileRaster");
        check(dm.contains("readAsRenderedImage"), "method readAsRenderedImage");
        check(dm.contains("readerSupportsThumbnails"), "method readerSupportsThumbnails");
        check(dm.contains("hasThumbnails"), "method hasThumbnails");
        check(dm.contains("getNumThumbnails"), "method getNumThumbnails");
        check(dm.contains("getThumbnailWidth"), "method getThumbnailWidth");
        check(dm.contains("getThumbnailHeight"), "method getThumbnailHeight");
        check(dm.contains("readThumbnail"), "method readThumbnail");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOReadWarningListener"), "method addIIOReadWarningListener");
        check(dm.contains("removeIIOReadWarningListener"), "method removeIIOReadWarningListener");
        check(dm.contains("removeAllIIOReadWarningListeners"), "method removeAllIIOReadWarningListeners");
        check(dm.contains("addIIOReadProgressListener"), "method addIIOReadProgressListener");
        check(dm.contains("removeIIOReadProgressListener"), "method removeIIOReadProgressListener");
        check(dm.contains("removeAllIIOReadProgressListeners"), "method removeAllIIOReadProgressListeners");
        check(dm.contains("addIIOReadUpdateListener"), "method addIIOReadUpdateListener");
        check(dm.contains("removeIIOReadUpdateListener"), "method removeIIOReadUpdateListener");
        check(dm.contains("removeAllIIOReadUpdateListeners"), "method removeAllIIOReadUpdateListeners");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
        check(Modifier.isProtected(c.getDeclaredConstructor(ImageReaderSpi.class).getModifiers()),
              "protected ImageReaderSpi ctor");
        testRealPngReader();
        testStubReaderBasics();
        testStubMetadataDispatch();
        testListenerAndAbort();
        testReadAllPaths();
        testBandAndRegionHelpers();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- track 1: real PNG reader through the abstract API ----
    static void testRealPngReader() throws Exception {
        byte[] bytes = pngBytes();
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        check("png".equals(r.getFormatName()), "format name png");
        check(r.getOriginatingProvider() != null, "originating provider non-null");
        check(r.getMinIndex() == 0, "min index 0");
        check(r.getDefaultReadParam() != null, "default read param non-null");
        check(r.getLocale() == null, "default locale null");
        check(r.getAvailableLocales() == null, "no available locales");
        // no input set: query and read methods throw IllegalStateException (probed)
        expectThrows(IllegalStateException.class, () -> r.read(0), "read without input");
        expectThrows(IllegalStateException.class, () -> r.getNumImages(false), "getNumImages without input");
        expectThrows(IllegalStateException.class, () -> r.getWidth(0), "getWidth without input");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis, true, true);
        check(r.getInput() == iis, "getInput identity");
        check(r.isSeekForwardOnly(), "seekForwardOnly true");
        check(r.isIgnoringMetadata(), "ignoringMetadata true");
        expectThrows(IllegalArgumentException.class, () -> r.setInput("banana"), "setInput wrong type");
        // index bounds: negative and past-end indices are IndexOutOfBoundsException (probed)
        expectThrows(IndexOutOfBoundsException.class, () -> r.read(-1), "read(-1)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.read(1), "read(1)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.getWidth(-1), "getWidth(-1)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.getHeight(2), "getHeight(2)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.getImageTypes(3).next(), "getImageTypes(3)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.getImageMetadata(-1), "getImageMetadata(-1)");
        expectThrows(IndexOutOfBoundsException.class, () -> r.getAspectRatio(1), "getAspectRatio(1)");
        // seekForwardOnly + allowSearch is rejected (probed)
        expectThrows(IllegalStateException.class, () -> r.getNumImages(true), "getNumImages(true) while seekForwardOnly");
        check(r.getNumImages(false) == 1, "num images 1");
        check(r.getWidth(0) == 4 && r.getHeight(0) == 4, "width/height 4");
        check(r.getAspectRatio(0) == 1.0f, "aspect ratio 1.0");
        check(!r.isRandomAccessEasy(0), "random access not easy");
        check(r.isImageTiled(0) == false, "png not tiled");
        check(r.getTileWidth(0) == 4 && r.getTileHeight(0) == 4, "tile equals image size");
        check(r.getTileGridXOffset(0) == 0 && r.getTileGridYOffset(0) == 0, "tile grid offset 0");
        check(r.getRawImageType(0) != null, "raw image type non-null");
        check(r.getImageTypes(0).hasNext(), "image types iterator non-empty");
        // metadata chain
        check(r.getStreamMetadata() == null, "png has no stream metadata");
        IIOMetadata meta = r.getImageMetadata(0);
        check(meta != null, "image metadata non-null");
        HashSet<String> nodes = new HashSet<String>();
        check(r.getImageMetadata(0, "javax_imageio_1.0", nodes) != null, "image metadata standard format");
        check(r.getImageMetadata(0, "bogus_format", nodes) == null, "unknown metadata format null");
        expectThrows(IllegalArgumentException.class, () -> r.getImageMetadata(0, null, nodes), "null formatName");
        expectThrows(IllegalArgumentException.class, () -> r.getImageMetadata(0, "javax_imageio_1.0", null), "null nodeNames");
        expectThrows(IllegalArgumentException.class, () -> r.getStreamMetadata(null, nodes), "stream null formatName");
        expectThrows(IllegalArgumentException.class, () -> r.getStreamMetadata("javax_imageio_1.0", null), "stream null nodeNames");
        // raster / tile behavior of the PNG plugin (probed)
        check(!r.canReadRaster(), "canReadRaster false");
        expectThrows(UnsupportedOperationException.class, () -> r.readRaster(0, null), "readRaster unsupported");
        expectThrows(UnsupportedOperationException.class, () -> r.readTileRaster(0, 0, 0), "readTileRaster unsupported");
        expectThrows(IllegalArgumentException.class, () -> r.readTile(0, 1, 0), "readTile bad tile index");
        expectThrows(IllegalArgumentException.class, () -> r.readTile(0, 0, 1), "readTile bad tile index y");
        BufferedImage tile = r.readTile(0, 0, 0);
        check(tile.getWidth() == 4 && tile.getHeight() == 4, "tile (0,0) is whole image");
        RenderedImage rendered = r.readAsRenderedImage(0, null);
        check(rendered.getWidth() == 4 && rendered.getHeight() == 4, "readAsRenderedImage size");
        // thumbnails: PNG has none (probed UOE)
        check(!r.readerSupportsThumbnails(), "no thumbnail support");
        check(!r.hasThumbnails(0), "no thumbnails");
        check(r.getNumThumbnails(0) == 0, "zero thumbnails");
        expectThrows(UnsupportedOperationException.class, () -> r.getThumbnailWidth(0, 0), "thumbnail width UOE");
        expectThrows(UnsupportedOperationException.class, () -> r.getThumbnailHeight(0, 0), "thumbnail height UOE");
        expectThrows(UnsupportedOperationException.class, () -> r.readThumbnail(0, 0), "readThumbnail UOE");
        // locale handling
        expectThrows(IllegalArgumentException.class, () -> r.setLocale(Locale.US), "invalid locale");
        r.setLocale(null);
        check(r.getLocale() == null, "locale stays null");
        // null listener handling: silently ignored (probed)
        r.addIIOReadProgressListener(null);
        r.removeIIOReadProgressListener(null);
        r.removeAllIIOReadProgressListeners();
        r.addIIOReadUpdateListener(null);
        r.removeIIOReadUpdateListener(null);
        r.removeAllIIOReadUpdateListeners();
        r.addIIOReadWarningListener(null);
        r.removeIIOReadWarningListener(null);
        r.removeAllIIOReadWarningListeners();
        check(true, "null listener calls are no-ops");
        // abort before read is harmless; the read itself clears stale aborts
        r.abort();
        BufferedImage back = r.read(0);
        check(back.getWidth() == 4 && back.getHeight() == 4, "read size after stale abort");
        check(back.getRGB(0, 0) == 0xFF0000FF, "pixel (0,0)");
        check(back.getRGB(3, 3) == 0xFF00FF00, "pixel (3,3)");
        check(r.getMinIndex() == 0, "min index still 0 after read");
        // setInput(null) removes the input but retains the flags (actual behavior)
        r.setInput(null);
        check(r.getInput() == null, "setInput(null) clears input");
        check(r.isSeekForwardOnly(), "setInput(null) retains seekForwardOnly flag");
        check(r.isIgnoringMetadata(), "setInput(null) retains ignoreMetadata flag");
        // a fresh setInput resets the flags to their 2-arg defaults
        ImageInputStream iis2 = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis2);
        check(!r.isSeekForwardOnly(), "re-setInput resets seekForwardOnly");
        check(!r.isIgnoringMetadata(), "re-setInput resets ignoreMetadata");
        check(r.getNumImages(true) == 1, "num images with allowSearch when not seekForwardOnly");
        r.reset();
        check(r.getInput() == null, "reset clears input");
        check(r.getLocale() == null, "reset clears locale");
        r.dispose();
        iis.close();
        iis2.close();
    }

    // ---- track 2: stub subclass drives base-class behavior ----
    static void testStubReaderBasics() throws Exception {
        StubReaderSpi spi = new StubReaderSpi();
        StubReader r = new StubReader(spi);
        check(r.getOriginatingProvider() == spi, "originating provider identity");
        // base getFormatName() delegates to the provider's format names
        check("corebfmt".equals(r.getFormatName()), "format name from provider");
        // base getFormatName() with a null provider NPEs (actual behavior; probed)
        StubReader noProv = new StubReader(null);
        expectThrows(NullPointerException.class, () -> noProv.getFormatName(), "getFormatName without provider");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(pngBytes()));
        // input type validation is provider-driven when a provider exists
        expectThrows(IllegalArgumentException.class, () -> r.setInput("banana"), "stub setInput wrong type with provider");
        r.setInput(iis);
        check(r.getInput() == iis, "stub input identity");
        // File is among the provider's input types, so it is accepted
        r.setInput(new File("whatever.png"));
        check(r.getInput() instanceof File, "File accepted via provider input types");
        // without a provider the input must be an ImageInputStream
        expectThrows(IllegalArgumentException.class, () -> noProv.setInput("banana"), "stub setInput wrong type without provider");
        noProv.setInput(iis);
        check(noProv.getInput() == iis, "ImageInputStream accepted without provider");
        // stub defaults
        check(r.getNumImages(false) == 1, "stub num images");
        check(r.getWidth(0) == 4 && r.getHeight(0) == 4, "stub width/height");
        check(r.getImageTypes(0).hasNext(), "stub image types");
        check(r.getDefaultReadParam() != null, "stub default read param");
        check(r.getDefaultReadParam() instanceof ImageReadParam, "default param type");
        check(!r.isImageTiled(0), "stub not tiled");
        check(!r.canReadRaster(), "stub cannot read raster");
        expectThrows(UnsupportedOperationException.class, () -> r.readRaster(0, null), "stub readRaster UOE");
        check(!r.readerSupportsThumbnails(), "stub no thumbnails");
        check(r.read(0) != null, "stub read");
        iis.close();
    }

    // format-name dispatch inside getStreamMetadata/getImageMetadata overloads
    static void testStubMetadataDispatch() throws Exception {
        StubReader r = new StubReader(new StubReaderSpi());
        r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(pngBytes())));
        HashSet<String> nodes = new HashSet<String>();
        // standard, native and extra formats return the same metadata object
        check(r.getStreamMetadata("javax_imageio_1.0", nodes) == r.streamMeta, "stream meta standard format");
        check(r.getStreamMetadata("coreb_native_1.0", nodes) == r.streamMeta, "stream meta native format");
        check(r.getStreamMetadata("coreb_extra_1.0", nodes) == r.streamMeta, "stream meta extra format");
        check(r.getStreamMetadata("no_such_format", nodes) == null, "stream meta unknown format null");
        check(r.getImageMetadata(0, "javax_imageio_1.0", nodes) == r.imageMeta, "image meta standard format");
        check(r.getImageMetadata(0, "coreb_native_1.0", nodes) == r.imageMeta, "image meta native format");
        check(r.getImageMetadata(0, "coreb_extra_1.0", nodes) == r.imageMeta, "image meta extra format");
        check(r.getImageMetadata(0, "no_such_format", nodes) == null, "image meta unknown format null");
        expectThrows(IllegalArgumentException.class, () -> r.getStreamMetadata(null, nodes), "stream meta null format");
        expectThrows(IllegalArgumentException.class, () -> r.getStreamMetadata("javax_imageio_1.0", null), "stream meta null nodes");
        expectThrows(IllegalArgumentException.class, () -> r.getImageMetadata(0, null, nodes), "image meta null format");
        expectThrows(IllegalArgumentException.class, () -> r.getImageMetadata(0, "javax_imageio_1.0", null), "image meta null nodes");
    }

    // listener registration and the abort protocol
    static void testListenerAndAbort() throws Exception {
        StubReader r = new StubReader(null);
        r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(pngBytes())));
        ProgressRecorder rec = new ProgressRecorder();
        // removing a listener that was never added is a no-op
        r.removeIIOReadProgressListener(rec);
        check(true, "remove of unregistered listener is a no-op");
        r.addIIOReadProgressListener(rec);
        r.read(0, null);
        check(rec.imageStarted == 1 && rec.imageComplete == 1, "one start/complete per read");
        check(rec.readAborted == 0, "no abort during normal read");
        check(rec.lastSource == r, "listener receives the reader instance");
        check(rec.lastImageIndex == 0, "listener receives image index");
        // duplicate registration receives events twice
        r.addIIOReadProgressListener(rec);
        r.read(0, null);
        check(rec.imageStarted == 3, "duplicate listener registered twice");
        r.removeIIOReadProgressListener(rec);
        r.removeIIOReadProgressListener(rec);
        r.read(0, null);
        check(rec.imageStarted == 3, "removal stops delivery");
        r.addIIOReadProgressListener(rec);
        r.removeAllIIOReadProgressListeners();
        r.read(0, null);
        check(rec.imageStarted == 3, "removeAll stops delivery");
        // abort raised from within imageStarted: readAborted fires, imageComplete does not
        StubReader ar = new StubReader(null);
        ar.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(pngBytes())));
        final ProgressRecorder arec = new ProgressRecorder();
        ar.addIIOReadProgressListener(new IIOReadProgressListener() {
            public void sequenceStarted(ImageReader x, int i) { }
            public void sequenceComplete(ImageReader x) { }
            public void imageStarted(ImageReader x, int i) { x.abort(); }
            public void imageProgress(ImageReader x, float p) { }
            public void imageComplete(ImageReader x) { arec.imageComplete++; }
            public void thumbnailStarted(ImageReader x, int i, int t) { }
            public void thumbnailProgress(ImageReader x, float p) { }
            public void thumbnailComplete(ImageReader x) { }
            public void readAborted(ImageReader x) { arec.readAborted++; }
        });
        ar.read(0, null);
        check(arec.readAborted == 1, "abort during read fires readAborted");
        check(arec.imageComplete == 0, "aborted read skips imageComplete");
        // a read started before abort() is requested clears the stale flag
        StubReader sr = new StubReader(null);
        sr.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(pngBytes())));
        sr.abort();
        ProgressRecorder srec = new ProgressRecorder();
        sr.addIIOReadProgressListener(srec);
        sr.read(0, null);
        check(srec.imageComplete == 1, "stale abort cleared by next read");
        check(srec.readAborted == 0, "stale abort does not fire readAborted");
    }

    // readAll(int,param) and readAll(Iterator) from the base class
    static void testReadAllPaths() throws Exception {
        byte[] bytes = pngBytes();
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis);
        IIOImage iio = r.readAll(0, null);
        check(iio.getRenderedImage() != null, "readAll image non-null");
        check(iio.getThumbnails() == null, "readAll thumbnails null for png");
        check(iio.getMetadata() != null, "readAll metadata non-null");
        // readAll(int, param) with imageIndex < getMinIndex() is IOOBE from the base class
        StubReader st = new StubReader(new StubReaderSpi());
        st.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));
        st.forceMinIndex(2);
        expectThrows(IndexOutOfBoundsException.class, () -> st.readAll(1, null), "readAll below minIndex");
        st.forceMinIndex(0);
        IIOImage stubIio = st.readAll(0, null);
        check(stubIio.getRenderedImage() != null, "stub readAll image");
        check(stubIio.getMetadata() == st.imageMeta, "stub readAll metadata identity");
        // iterator form: PNG supplies one image; the loop ends on IOOBE from read(1)
        ImageReader r2 = ImageIO.getImageReadersByFormatName("png").next();
        ImageInputStream iis2 = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r2.setInput(iis2);
        ProgressRecorder rec = new ProgressRecorder();
        r2.addIIOReadProgressListener(rec);
        java.util.List<ImageReadParam> params = new ArrayList<ImageReadParam>();
        params.add(null);
        Iterator<IIOImage> it = r2.readAll(params.iterator());
        check(it.hasNext(), "readAll iterator has an element");
        check(it.next().getRenderedImage() != null, "readAll iterator image");
        check(!it.hasNext(), "readAll iterator exhausted");
        check(rec.sequenceStarted == 1, "sequenceStarted fired once");
        check(rec.lastMinIndex == 0, "sequenceStarted minIndex 0");
        check(rec.sequenceComplete == 1, "sequenceComplete fired once");
        // a non-ImageReadParam element triggers IAE (probed)
        ImageReader r3 = ImageIO.getImageReadersByFormatName("png").next();
        r3.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));
        final Iterator<?> bad = Collections.nCopies(1, "notaparam").iterator();
        expectThrows(IllegalArgumentException.class, () -> {
            Iterator<?> raw = bad;
            r3.readAll((Iterator) raw);
        }, "readAll with non-ImageReadParam element");
        iis.close();
        iis2.close();
    }

    // protected helpers exercised through the stub: band settings + region math
    static void testBandAndRegionHelpers() {
        StubReader st = new StubReader(null);
        ImageReadParam ok = new ImageReadParam();
        ok.setSourceBands(new int[]{0, 1, 2});
        ok.setDestinationBands(new int[]{0, 1, 2});
        st.checkBands(ok, 3, 3); // no throw
        check(true, "matching band settings accepted");
        ImageReadParam lenMismatch = new ImageReadParam();
        lenMismatch.setSourceBands(new int[]{0, 1});
        lenMismatch.setDestinationBands(new int[]{0, 1, 2});
        expectThrows(IllegalArgumentException.class, () -> st.checkBands(lenMismatch, 3, 3), "band length mismatch");
        ImageReadParam srcOob = new ImageReadParam();
        srcOob.setSourceBands(new int[]{0, 1, 3});
        srcOob.setDestinationBands(new int[]{0, 1, 2});
        expectThrows(IllegalArgumentException.class, () -> st.checkBands(srcOob, 3, 3), "source band out of range");
        ImageReadParam dstOob = new ImageReadParam();
        dstOob.setSourceBands(new int[]{0, 1, 2});
        dstOob.setDestinationBands(new int[]{0, 1, 2});
        expectThrows(IllegalArgumentException.class, () -> st.checkBands(dstOob, 3, 2), "destination band out of range");
        // computeRegions: default params use the full source and destination
        ImageReadParam full = new ImageReadParam();
        Rectangle[] ro = st.compute(full, 4, 4);
        check(ro[0].equals(new Rectangle(0, 0, 4, 4)), "computeRegions default source");
        check(ro[1].equals(new Rectangle(0, 0, 4, 4)), "computeRegions default destination");
        ImageReadParam region = new ImageReadParam();
        region.setSourceRegion(new Rectangle(1, 1, 2, 2));
        Rectangle[] ro2 = st.compute(region, 4, 4);
        check(ro2[0].equals(new Rectangle(1, 1, 2, 2)), "computeRegions region source");
        check(ro2[1].equals(new Rectangle(0, 0, 2, 2)), "computeRegions region destination");
        ImageReadParam sub = new ImageReadParam();
        sub.setSourceSubsampling(2, 2, 0, 0);
        Rectangle[] ro3 = st.compute(sub, 4, 4);
        check(ro3[1].equals(new Rectangle(0, 0, 2, 2)), "computeRegions subsampled destination");
    }
}

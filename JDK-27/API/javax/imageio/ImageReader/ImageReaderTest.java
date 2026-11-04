/*
 * @test
 * @summary javax.imageio.ImageReader conformance: white-box structure + black-box behavior.
 * @run main ImageReaderTest
 */

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.event.IIOReadProgressListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.metadata.IIOMetadataNode;
import javax.imageio.spi.ImageReaderSpi;
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

    // ---- additions (CORE-B): negative contracts, stub tracks, abort/readAll ----
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected == t.getClass(),
                  msg + " expected exactly " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

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

    static class StubReader extends ImageReader {
        final StubMetadata streamMeta = new StubMetadata();
        final StubMetadata imageMeta = new StubMetadata();
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

    static byte[] corebPng() throws IOException {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(bi, "png", bos);
        return bos.toByteArray();
    }

    static void testRealPngNegativeContracts() throws Exception {
        byte[] bytes = corebPng();
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        // no input: queries and reads fail with IllegalStateException (probed)
        expectExact(IllegalStateException.class, () -> r.read(0), "read without input");
        expectExact(IllegalStateException.class, () -> r.getNumImages(false), "getNumImages without input");
        expectExact(IllegalStateException.class, () -> r.getWidth(0), "getWidth without input");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis, true, true);
        expectExact(IllegalArgumentException.class, () -> r.setInput("banana"), "setInput wrong type");
        // seekForwardOnly + allowSearch rejected (probed)
        expectExact(IllegalStateException.class, () -> r.getNumImages(true), "getNumImages(true) while seekForwardOnly");
        check(r.getNumImages(false) == 1, "num images 1 without search");
        // index bounds are IndexOutOfBoundsException (probed), not ISE
        expectExact(IndexOutOfBoundsException.class, () -> r.read(-1), "read(-1)");
        expectExact(IndexOutOfBoundsException.class, () -> r.read(1), "read(1)");
        expectExact(IndexOutOfBoundsException.class, () -> r.getWidth(-1), "getWidth(-1)");
        expectExact(IndexOutOfBoundsException.class, () -> r.getHeight(2), "getHeight(2)");
        expectExact(IndexOutOfBoundsException.class, () -> r.getImageTypes(3).next(), "getImageTypes(3)");
        expectExact(IndexOutOfBoundsException.class, () -> r.getImageMetadata(-1), "getImageMetadata(-1)");
        expectExact(IndexOutOfBoundsException.class, () -> r.getAspectRatio(1), "getAspectRatio(1)");
        check(!r.isRandomAccessEasy(0), "random access not easy");
        check(r.getTileGridXOffset(0) == 0 && r.getTileGridYOffset(0) == 0, "tile grid offsets 0");
        // metadata overloads and their null-argument exceptions
        check(r.getStreamMetadata() == null, "png stream metadata null");
        HashSet<String> nodes = new HashSet<String>();
        check(r.getImageMetadata(0, "javax_imageio_1.0", nodes) != null, "image metadata standard format");
        check(r.getImageMetadata(0, "bogus_format", nodes) == null, "unknown metadata format null");
        expectExact(IllegalArgumentException.class, () -> r.getImageMetadata(0, null, nodes), "null formatName");
        expectExact(IllegalArgumentException.class, () -> r.getImageMetadata(0, "javax_imageio_1.0", null), "null nodeNames");
        expectExact(IllegalArgumentException.class, () -> r.getStreamMetadata(null, nodes), "stream null formatName");
        expectExact(IllegalArgumentException.class, () -> r.getStreamMetadata("javax_imageio_1.0", null), "stream null nodeNames");
        // raster / tiles / thumbnails (probed)
        check(!r.canReadRaster(), "canReadRaster false");
        expectExact(UnsupportedOperationException.class, () -> r.readRaster(0, null), "readRaster unsupported");
        expectExact(UnsupportedOperationException.class, () -> r.readTileRaster(0, 0, 0), "readTileRaster unsupported");
        expectExact(IllegalArgumentException.class, () -> r.readTile(0, 1, 0), "readTile bad tile x");
        expectExact(IllegalArgumentException.class, () -> r.readTile(0, 0, 1), "readTile bad tile y");
        check(r.readTile(0, 0, 0).getWidth() == 4, "tile (0,0) is whole image");
        RenderedImage rendered = r.readAsRenderedImage(0, null);
        check(rendered.getWidth() == 4 && rendered.getHeight() == 4, "readAsRenderedImage size");
        expectExact(UnsupportedOperationException.class, () -> r.getThumbnailWidth(0, 0), "thumbnail width UOE");
        expectExact(UnsupportedOperationException.class, () -> r.getThumbnailHeight(0, 0), "thumbnail height UOE");
        expectExact(UnsupportedOperationException.class, () -> r.readThumbnail(0, 0), "readThumbnail UOE");
        // locale + null listeners
        expectExact(IllegalArgumentException.class, () -> r.setLocale(Locale.US), "invalid locale");
        r.setLocale(null);
        check(r.getLocale() == null, "locale stays null");
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
        // setInput(null) removes input but retains the flags (actual behavior)
        r.setInput(null);
        check(r.getInput() == null, "setInput(null) clears input");
        check(r.isSeekForwardOnly(), "setInput(null) retains seekForwardOnly flag");
        check(r.isIgnoringMetadata(), "setInput(null) retains ignoreMetadata flag");
        ImageInputStream iis2 = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis2);
        check(!r.isSeekForwardOnly() && !r.isIgnoringMetadata(), "re-setInput resets flags");
        check(r.getNumImages(true) == 1, "num images allowSearch after flag reset");
        iis.close();
        iis2.close();
    }

    static void testStubReaderBasics() throws Exception {
        StubReaderSpi spi = new StubReaderSpi();
        StubReader r = new StubReader(spi);
        check(r.getOriginatingProvider() == spi, "originating provider identity");
        check("corebfmt".equals(r.getFormatName()), "format name from provider via base impl");
        // base getFormatName() NPEs without a provider (actual behavior; probed)
        StubReader noProv = new StubReader(null);
        expectExact(NullPointerException.class, () -> noProv.getFormatName(), "getFormatName without provider");
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(corebPng()));
        expectExact(IllegalArgumentException.class, () -> r.setInput("banana"), "setInput wrong type with provider");
        r.setInput(iis);
        check(r.getInput() == iis, "stub input identity");
        r.setInput(new File("whatever.png"));
        check(r.getInput() instanceof File, "File accepted via provider input types");
        expectExact(IllegalArgumentException.class, () -> noProv.setInput("banana"), "setInput wrong type without provider");
        noProv.setInput(iis);
        check(noProv.getInput() == iis, "ImageInputStream accepted without provider");
        check(r.getDefaultReadParam() != null, "stub default read param");
        check(!r.canReadRaster(), "stub cannot read raster");
        expectExact(UnsupportedOperationException.class, () -> r.readRaster(0, null), "stub readRaster UOE");
        expectExact(UnsupportedOperationException.class, () -> r.readThumbnail(0, 0), "stub readThumbnail UOE");
        check(r.read(0) != null, "stub read");
        iis.close();
    }

    static void testStubMetadataDispatch() throws Exception {
        StubReader r = new StubReader(new StubReaderSpi());
        r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(corebPng())));
        HashSet<String> nodes = new HashSet<String>();
        check(r.getStreamMetadata("javax_imageio_1.0", nodes) == r.streamMeta, "stream meta standard format");
        check(r.getStreamMetadata("coreb_native_1.0", nodes) == r.streamMeta, "stream meta native format");
        check(r.getStreamMetadata("coreb_extra_1.0", nodes) == r.streamMeta, "stream meta extra format");
        check(r.getStreamMetadata("no_such_format", nodes) == null, "stream meta unknown format null");
        check(r.getImageMetadata(0, "javax_imageio_1.0", nodes) == r.imageMeta, "image meta standard format");
        check(r.getImageMetadata(0, "coreb_native_1.0", nodes) == r.imageMeta, "image meta native format");
        check(r.getImageMetadata(0, "coreb_extra_1.0", nodes) == r.imageMeta, "image meta extra format");
        check(r.getImageMetadata(0, "no_such_format", nodes) == null, "image meta unknown format null");
        expectExact(IllegalArgumentException.class, () -> r.getStreamMetadata(null, nodes), "stream meta null format");
        expectExact(IllegalArgumentException.class, () -> r.getStreamMetadata("javax_imageio_1.0", null), "stream meta null nodes");
        expectExact(IllegalArgumentException.class, () -> r.getImageMetadata(0, null, nodes), "image meta null format");
        expectExact(IllegalArgumentException.class, () -> r.getImageMetadata(0, "javax_imageio_1.0", null), "image meta null nodes");
    }

    static void testListenerAndAbort() throws Exception {
        StubReader r = new StubReader(null);
        r.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(corebPng())));
        ProgressRecorder rec = new ProgressRecorder();
        r.removeIIOReadProgressListener(rec);
        check(true, "remove of unregistered listener is a no-op");
        r.addIIOReadProgressListener(rec);
        r.read(0, null);
        check(rec.imageStarted == 1 && rec.imageComplete == 1, "one start/complete per read");
        check(rec.readAborted == 0, "no abort during normal read");
        check(rec.lastSource == r, "listener receives the reader instance");
        check(rec.lastImageIndex == 0, "listener receives image index");
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
        // abort raised from inside imageStarted
        StubReader ar = new StubReader(null);
        ar.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(corebPng())));
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
        // stale abort is cleared by the next read
        StubReader sr = new StubReader(null);
        sr.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(corebPng())));
        sr.abort();
        ProgressRecorder srec = new ProgressRecorder();
        sr.addIIOReadProgressListener(srec);
        sr.read(0, null);
        check(srec.imageComplete == 1, "stale abort cleared by next read");
        check(srec.readAborted == 0, "stale abort does not fire readAborted");
    }

    static void testReadAllPaths() throws Exception {
        byte[] bytes = corebPng();
        ImageReader r = ImageIO.getImageReadersByFormatName("png").next();
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(bytes));
        r.setInput(iis);
        IIOImage iio = r.readAll(0, null);
        check(iio.getRenderedImage() != null, "readAll image non-null");
        check(iio.getThumbnails() == null, "readAll thumbnails null for png");
        check(iio.getMetadata() != null, "readAll metadata non-null");
        // readAll below getMinIndex() throws IndexOutOfBoundsException from the base class
        StubReader st = new StubReader(new StubReaderSpi());
        st.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));
        st.forceMinIndex(2);
        expectExact(IndexOutOfBoundsException.class, () -> st.readAll(1, null), "readAll below minIndex");
        st.forceMinIndex(0);
        IIOImage stubIio = st.readAll(0, null);
        check(stubIio.getRenderedImage() != null, "stub readAll image");
        check(stubIio.getMetadata() == st.imageMeta, "stub readAll metadata identity");
        // iterator form over one PNG image
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
        // non-ImageReadParam element triggers IAE (probed)
        ImageReader r3 = ImageIO.getImageReadersByFormatName("png").next();
        r3.setInput(ImageIO.createImageInputStream(new ByteArrayInputStream(bytes)));
        expectExact(IllegalArgumentException.class, () -> {
            Iterator raw = Collections.nCopies(1, "notaparam").iterator();
            r3.readAll(raw);
        }, "readAll with non-ImageReadParam element");
        iis.close();
        iis2.close();
    }

    static void testBandAndRegionHelpers() {
        StubReader st = new StubReader(null);
        ImageReadParam ok = new ImageReadParam();
        ok.setSourceBands(new int[]{0, 1, 2});
        ok.setDestinationBands(new int[]{0, 1, 2});
        st.checkBands(ok, 3, 3);
        check(true, "matching band settings accepted");
        ImageReadParam lenMismatch = new ImageReadParam();
        lenMismatch.setSourceBands(new int[]{0, 1});
        lenMismatch.setDestinationBands(new int[]{0, 1, 2});
        expectExact(IllegalArgumentException.class, () -> st.checkBands(lenMismatch, 3, 3), "band length mismatch");
        ImageReadParam srcOob = new ImageReadParam();
        srcOob.setSourceBands(new int[]{0, 1, 3});
        srcOob.setDestinationBands(new int[]{0, 1, 2});
        expectExact(IllegalArgumentException.class, () -> st.checkBands(srcOob, 3, 3), "source band out of range");
        ImageReadParam dstOob = new ImageReadParam();
        dstOob.setSourceBands(new int[]{0, 1, 2});
        dstOob.setDestinationBands(new int[]{0, 1, 2});
        expectExact(IllegalArgumentException.class, () -> st.checkBands(dstOob, 3, 2), "destination band out of range");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testRealPngNegativeContracts();
        testStubReaderBasics();
        testStubMetadataDispatch();
        testListenerAndAbort();
        testReadAllPaths();
        testBandAndRegionHelpers();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.imageio.ImageWriter conformance: white-box structure + black-box behavior.
 * @run main ImageWriterTest
 */

import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageWriterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriter");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> si = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.ImageTranscoder"), "implements ImageTranscoder");
        java.util.Set<String> dm = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getOriginatingProvider"), "method getOriginatingProvider");
        check(dm.contains("setOutput"), "method setOutput");
        check(dm.contains("getOutput"), "method getOutput");
        check(dm.contains("getAvailableLocales"), "method getAvailableLocales");
        check(dm.contains("setLocale"), "method setLocale");
        check(dm.contains("getLocale"), "method getLocale");
        check(dm.contains("getDefaultWriteParam"), "method getDefaultWriteParam");
        check(dm.contains("getDefaultStreamMetadata"), "method getDefaultStreamMetadata");
        check(dm.contains("getDefaultImageMetadata"), "method getDefaultImageMetadata");
        check(dm.contains("convertStreamMetadata"), "method convertStreamMetadata");
        check(dm.contains("convertImageMetadata"), "method convertImageMetadata");
        check(dm.contains("canWriteRasters"), "method canWriteRasters");
        check(dm.contains("write"), "method write");
        check(dm.contains("canWriteSequence"), "method canWriteSequence");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOWriteWarningListener"), "method addIIOWriteWarningListener");
        check(dm.contains("removeIIOWriteWarningListener"), "method removeIIOWriteWarningListener");
        check(dm.contains("addIIOWriteProgressListener"), "method addIIOWriteProgressListener");
        check(dm.contains("removeIIOWriteProgressListener"), "method removeIIOWriteProgressListener");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_ARGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);

        Iterator<ImageWriter> it = ImageIO.getImageWritersByFormatName("png");
        check(it.hasNext(), "png writer available");
        ImageWriter writer = it.next();

        // identity/info
        check(writer.getOriginatingProvider() != null, "getOriginatingProvider non-null");
        check(writer.getLocale() == null, "default locale null");
        check(writer.getAvailableLocales() == null, "PNG writer has no available locales");

        // default write param
        ImageWriteParam param = writer.getDefaultWriteParam();
        check(param != null, "getDefaultWriteParam non-null");
        check(param.canWriteCompressed(), "PNG writer supports compression");

        // metadata accessors
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        check(writer.getDefaultStreamMetadata(param) == null, "PNG has no stream metadata");
        check(writer.getDefaultImageMetadata(its, param) != null, "default image metadata non-null");

        // output wiring + write(RenderedImage)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = new MemoryCacheImageOutputStream(bos);
        writer.setOutput(ios);
        check(writer.getOutput() == ios, "getOutput identity after setOutput");
        writer.write(bi);
        check(bos.size() > 0, "write(RenderedImage) produced bytes");

        // write(IIOImage)
        int afterFirst = bos.size();
        writer.write(new IIOImage(bi, null, null));
        check(bos.size() > afterFirst, "write(IIOImage) appended bytes");

        // progress listener fired during write
        final int[] started = new int[1];
        final int[] completed = new int[1];
        IIOWriteProgressListener l = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter w, int index) { started[0]++; }
            public void imageProgress(ImageWriter w, float pct) {}
            public void imageComplete(ImageWriter w) { completed[0]++; }
            public void thumbnailStarted(ImageWriter w, int i, int t) {}
            public void thumbnailProgress(ImageWriter w, float pct) {}
            public void thumbnailComplete(ImageWriter w) {}
            public void writeAborted(ImageWriter w) {}
        };
        writer.addIIOWriteProgressListener(l);
        writer.write(bi);
        check(started[0] == 1, "imageStarted fired once");
        check(completed[0] == 1, "imageComplete fired once");
        writer.removeIIOWriteProgressListener(l);

        writer.reset();
        check(writer.getOutput() == null, "reset clears output");
        writer.dispose();
        ios.close();
    }

    // ---- additions (CORE-B): capability matrix, stub tracks, abort protocol ----
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

    static ImageOutputStream corebMemoryStream() {
        return new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
    }

    static class StubWriterSpi extends ImageWriterSpi {
        StubWriterSpi() {
            super("coreb", "1.0", new String[]{"corebfmt"}, new String[]{"coreb"}, new String[]{"image/x-coreb"},
                "ImageWriterTest$StubWriter", new Class[]{ImageOutputStream.class}, null,
                false, null, null, null, null,
                false, null, null, null, null);
        }
        public boolean canEncodeImage(ImageTypeSpecifier type) { return true; }
        public ImageWriter createWriterInstance(Object ext) { return new StubWriter(this); }
        public String getDescription(Locale locale) { return "coreb stub writer spi"; }
    }

    static class StubWriter extends ImageWriter {
        IIOMetadata lastStreamMeta;
        IIOImage lastImage;
        ImageWriteParam lastParam;
        boolean abortedSeen;
        StubWriter(ImageWriterSpi spi) { super(spi); }
        public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) { return null; }
        public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier type, ImageWriteParam param) { return null; }
        public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) { return inData; }
        public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier type, ImageWriteParam param) { return inData; }
        public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) {
            lastStreamMeta = streamMetadata;
            lastImage = image;
            lastParam = param;
        }
        void writeWithCallbacks() {
            clearAbortRequest();
            processImageStarted(0);
            if (abortRequested()) { abortedSeen = true; processWriteAborted(); return; }
            processImageComplete();
        }
        void warn(int imageIndex, String warning) { processWarningOccurred(imageIndex, warning); }
        public Locale[] getAvailableLocales() { return new Locale[]{Locale.US, Locale.FRENCH}; }
    }

    static void testCoreBNegativeContracts() throws Exception {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        // protected ImageWriterSpi constructor
        Class<?> c = Class.forName("javax.imageio.ImageWriter");
        check(Modifier.isProtected(c.getDeclaredConstructor(ImageWriterSpi.class).getModifiers()),
              "protected ImageWriterSpi ctor");
        // writing without output fails with IllegalStateException (probed)
        expectExact(IllegalStateException.class, () -> w.write(bi), "write without output");
        expectExact(IllegalArgumentException.class, () -> w.setOutput("banana"), "setOutput wrong type");
        ImageOutputStream ios = corebMemoryStream();
        w.setOutput(ios);
        // null image arguments are IllegalArgumentException in the PNG writer (probed)
        expectExact(IllegalArgumentException.class, () -> w.write((IIOImage) null), "write null IIOImage");
        expectExact(IllegalArgumentException.class, () -> w.write((RenderedImage) null), "write null RenderedImage");
        // version difference (probed): JDK-9+ PNGImageWriteParam is compressible
        ImageWriteParam pngParam = w.getDefaultWriteParam();
        check(pngParam.canWriteCompressed(), "png param compressible on JDK-27");
        check(pngParam.canWriteProgressive(), "png param progressive");
        pngParam.setCompressionMode(ImageWriteParam.MODE_EXPLICIT);
        check(pngParam.getCompressionMode() == ImageWriteParam.MODE_EXPLICIT, "png compression mode settable");
        // convert metadata corner cases (probed)
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        check(w.convertStreamMetadata(null, null) == null, "convertStreamMetadata null in null out");
        check(w.convertImageMetadata(null, its, null) != null, "convertImageMetadata(null) produces metadata");
        check(!w.canWriteRasters(), "png cannot write rasters");
        check(w.getNumThumbnailsSupported(its, null, null, null) == 0, "no thumbnails supported");
        check(w.getPreferredThumbnailSizes(its, null, null, null) == null, "no preferred thumbnail sizes");
        // round-trip through ImageIO.read
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios2 = new MemoryCacheImageOutputStream(bos);
        w.setOutput(ios2);
        w.write(bi);
        BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getWidth() == 4 && back.getHeight() == 4, "round-trip dimensions");
        check(back.getRGB(0, 0) == 0xFF0000FF, "round-trip pixel (0,0)");
        check(back.getRGB(3, 3) == 0xFF00FF00, "round-trip pixel (3,3)");
        expectExact(IllegalArgumentException.class, () -> w.setLocale(Locale.US), "invalid locale");
        w.setLocale(null);
        check(w.getLocale() == null, "locale stays null");
        w.addIIOWriteProgressListener(null);
        w.removeIIOWriteProgressListener(null);
        w.removeAllIIOWriteProgressListeners();
        w.addIIOWriteWarningListener(null);
        w.removeIIOWriteWarningListener(null);
        w.removeAllIIOWriteWarningListeners();
        check(true, "null listener calls are no-ops");
        w.dispose();
        ios.close();
        ios2.close();
    }

    static void testCoreBCapabilityMatrix() throws Exception {
        BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        IIOImage iio = new IIOImage(bi, null, null);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        // no output: capability queries and mutators throw IllegalStateException
        ImageWriter w1 = ImageIO.getImageWritersByFormatName("png").next();
        expectExact(IllegalStateException.class, () -> w1.canInsertImage(1), "canInsertImage no output");
        expectExact(IllegalStateException.class, () -> w1.canRemoveImage(0), "canRemoveImage no output");
        expectExact(IllegalStateException.class, () -> w1.canReplacePixels(0), "canReplacePixels no output");
        expectExact(IllegalStateException.class, () -> w1.canReplaceStreamMetadata(), "canReplaceStreamMetadata no output");
        expectExact(IllegalStateException.class, () -> w1.canReplaceImageMetadata(0), "canReplaceImageMetadata no output");
        expectExact(IllegalStateException.class, () -> w1.canWriteEmpty(), "canWriteEmpty no output");
        expectExact(IllegalStateException.class, () -> w1.canInsertEmpty(0), "canInsertEmpty no output");
        expectExact(IllegalStateException.class, () -> w1.prepareWriteSequence(null), "prepareWriteSequence no output");
        expectExact(IllegalStateException.class, () -> w1.writeToSequence(iio, null), "writeToSequence no output");
        expectExact(IllegalStateException.class, () -> w1.endWriteSequence(), "endWriteSequence no output");
        expectExact(IllegalStateException.class, () -> w1.writeInsert(1, iio, null), "writeInsert no output");
        expectExact(IllegalStateException.class, () -> w1.removeImage(0), "removeImage no output");
        expectExact(IllegalStateException.class, () -> w1.replaceStreamMetadata(null), "replaceStreamMetadata no output");
        expectExact(IllegalStateException.class, () -> w1.replaceImageMetadata(0, null), "replaceImageMetadata no output");
        expectExact(IllegalStateException.class, () -> w1.prepareReplacePixels(0, new Rectangle(0, 0, 1, 1)), "prepareReplacePixels no output");
        expectExact(IllegalStateException.class, () -> w1.replacePixels(bi, null), "replacePixels no output");
        expectExact(IllegalStateException.class, () -> w1.endReplacePixels(), "endReplacePixels no output");
        expectExact(IllegalStateException.class, () -> w1.endWriteEmpty(), "endWriteEmpty no output");
        expectExact(IllegalStateException.class, () -> w1.endInsertEmpty(), "endInsertEmpty no output");
        // canWriteSequence performs no output check at all (probed)
        ImageWriter w2 = ImageIO.getImageWritersByFormatName("png").next();
        check(!w2.canWriteSequence(), "canWriteSequence false without output");
        w2.setOutput(corebMemoryStream());
        check(!w2.canWriteSequence(), "canWriteSequence false with output");
        check(!w2.canInsertImage(1), "canInsertImage false");
        check(!w2.canRemoveImage(0), "canRemoveImage false");
        check(!w2.canReplacePixels(0), "canReplacePixels false");
        check(!w2.canReplaceStreamMetadata(), "canReplaceStreamMetadata false");
        check(!w2.canReplaceImageMetadata(0), "canReplaceImageMetadata false");
        check(!w2.canWriteEmpty(), "canWriteEmpty false");
        check(!w2.canInsertEmpty(0), "canInsertEmpty false");
        expectExact(UnsupportedOperationException.class, () -> w2.prepareWriteSequence(null), "prepareWriteSequence UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.writeToSequence(iio, null), "writeToSequence UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.endWriteSequence(), "endWriteSequence UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.writeInsert(1, iio, null), "writeInsert UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.removeImage(0), "removeImage UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.replaceStreamMetadata(null), "replaceStreamMetadata UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.replaceImageMetadata(0, null), "replaceImageMetadata UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.prepareReplacePixels(0, new Rectangle(0, 0, 1, 1)), "prepareReplacePixels UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.replacePixels(bi, null), "replacePixels(RenderedImage) UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.replacePixels(bi.getRaster(), null), "replacePixels(Raster) UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.endReplacePixels(), "endReplacePixels UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.prepareWriteEmpty(null, its, 2, 2, null, null, null), "prepareWriteEmpty UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.prepareInsertEmpty(-1, its, 2, 2, null, null, null), "prepareInsertEmpty UOE");
        expectExact(UnsupportedOperationException.class, () -> w2.endInsertEmpty(), "endInsertEmpty UOE");
        expectExact(IllegalStateException.class, () -> w2.endWriteEmpty(), "endWriteEmpty without prepare");
        w2.dispose();
    }

    static void testCoreBStubWriter() throws Exception {
        StubWriterSpi spi = new StubWriterSpi();
        StubWriter w = new StubWriter(spi);
        check(w.getOriginatingProvider() == spi, "stub provider identity");
        expectExact(IllegalArgumentException.class, () -> w.setOutput("banana"), "stub setOutput wrong type with provider");
        w.setOutput(corebMemoryStream());
        check(w.getOutput() != null, "stub output accepted");
        // without a provider any output object is accepted (asymmetric with readers; probed)
        StubWriter free = new StubWriter(null);
        free.setOutput("banana");
        check("banana".equals(free.getOutput()), "any output without provider");
        free.setOutput(null);
        check(free.getOutput() == null, "setOutput(null) clears");
        // write(RenderedImage) delegates as write(null, new IIOImage(im, null, null), null)
        free.setOutput(corebMemoryStream());
        RenderedImage ri = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        free.write(ri);
        check(free.lastImage != null && free.lastImage.getRenderedImage() == ri, "write(RenderedImage) wraps image");
        check(free.lastImage.getThumbnails() == null, "write(RenderedImage) null thumbnails");
        check(free.lastImage.getMetadata() == null, "write(RenderedImage) null metadata");
        check(free.lastStreamMeta == null && free.lastParam == null, "write(RenderedImage) null meta/param");
        IIOImage custom = new IIOImage(ri, null, null);
        free.write(custom);
        check(free.lastImage == custom, "write(IIOImage) passes image through");
        check(free.lastStreamMeta == null && free.lastParam == null, "write(IIOImage) null meta/param");
        // the base class does not guard null IIOImage: it reaches the subclass (probed)
        free.write((IIOImage) null);
        check(free.lastImage == null, "write(null) reaches subclass write");
        ImageWriteParam param = free.getDefaultWriteParam();
        free.write(null, custom, param);
        check(free.lastStreamMeta == null && free.lastImage == custom && free.lastParam == param, "3-arg write passthrough");
        check(free.convertStreamMetadata(null, param) == null, "convertStreamMetadata passthrough null");
        IIOMetadata marker = free.getDefaultImageMetadata(ImageTypeSpecifier.createFromRenderedImage(ri), null);
        check(marker == null, "stub default image metadata null");
    }

    static void testCoreBStubCallbacks() {
        StubWriter w = new StubWriter(null);
        w.setOutput(corebMemoryStream());
        final int[] first = new int[3];
        final int[] second = new int[3];
        w.removeIIOWriteProgressListener(new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { }
        });
        check(true, "remove of unregistered listener is a no-op");
        IIOWriteProgressListener l1 = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { first[0]++; }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { first[1]++; }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { first[2]++; }
        };
        IIOWriteProgressListener l2 = new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { second[0]++; }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { second[1]++; }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { second[2]++; }
        };
        w.addIIOWriteProgressListener(l1);
        w.addIIOWriteProgressListener(l2);
        w.writeWithCallbacks();
        check(first[0] == 1 && first[1] == 1 && first[2] == 0, "first listener notified");
        check(second[0] == 1 && second[1] == 1 && second[2] == 0, "second listener notified");
        w.removeIIOWriteProgressListener(l1);
        w.writeWithCallbacks();
        check(first[0] == 1, "removed listener no longer notified");
        check(second[0] == 2, "remaining listener still notified");
        w.removeAllIIOWriteProgressListeners();
        w.writeWithCallbacks();
        check(second[0] == 2, "removeAll stops delivery");
        // abort raised from within imageStarted
        StubWriter aw = new StubWriter(null);
        aw.setOutput(corebMemoryStream());
        final int[] astat = new int[2];
        aw.addIIOWriteProgressListener(new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { x.abort(); }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { astat[0]++; }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { astat[1]++; }
        });
        aw.writeWithCallbacks();
        check(astat[1] == 1, "abort during write fires writeAborted");
        check(astat[0] == 0, "aborted write skips imageComplete");
        // stale abort is cleared by the next write
        StubWriter sw = new StubWriter(null);
        sw.setOutput(corebMemoryStream());
        sw.abort();
        final int[] sstat = new int[2];
        sw.addIIOWriteProgressListener(new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { sstat[0]++; }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { sstat[1]++; }
        });
        sw.writeWithCallbacks();
        check(sstat[0] == 1, "stale abort cleared by next write");
        check(sstat[1] == 0, "stale abort does not fire writeAborted");
        // null warning rejected only while a warning listener is attached (probed)
        StubWriter ww = new StubWriter(null);
        ww.setOutput(corebMemoryStream());
        ww.warn(0, null);
        check(true, "null warning without listeners is a no-op");
        ww.addIIOWriteWarningListener(new IIOWriteWarningListener() {
            public void warningOccurred(ImageWriter x, int i, String s) { }
        });
        expectExact(IllegalArgumentException.class, () -> ww.warn(0, null), "null warning with listener");
        ww.warn(0, "coreb warning");
        check(true, "non-null warning delivered");
        // locale contract
        StubWriter lw = new StubWriter(null);
        Locale[] locales = lw.getAvailableLocales();
        check(locales != null && locales.length == 2, "stub locales advertised");
        lw.setLocale(Locale.FRENCH);
        check(lw.getLocale() == Locale.FRENCH, "locale set");
        expectExact(IllegalArgumentException.class, () -> lw.setLocale(Locale.CANADA), "locale not in list");
        lw.setLocale(null);
        check(lw.getLocale() == null, "setLocale(null) clears");
        lw.setLocale(Locale.FRENCH);
        check(lw.getDefaultWriteParam().getLocale() == Locale.FRENCH, "default param inherits locale");
        expectExact(IllegalStateException.class, () -> lw.endWriteEmpty(), "endWriteEmpty without prepare");
        lw.dispose();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testCoreBNegativeContracts();
        testCoreBCapabilityMatrix();
        testCoreBStubWriter();
        testCoreBStubCallbacks();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.imageio.ImageWriter conformance: abstract hierarchy and methods.
 * @run main ImageWriterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.imageio.IIOImage;
import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriteParam;
import javax.imageio.ImageWriter;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.event.IIOWriteProgressListener;
import javax.imageio.event.IIOWriteWarningListener;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;
import javax.imageio.stream.MemoryCacheImageOutputStream;

public class ImageWriterTest {
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

    // Records the full IIOWriteProgressListener callback stream
    static class WriteProgressRecorder implements IIOWriteProgressListener {
        int imageStarted, imageComplete, writeAborted;
        int lastIndex = -1;
        ImageWriter lastSource;
        public void imageStarted(ImageWriter w, int i) { imageStarted++; lastIndex = i; lastSource = w; }
        public void imageProgress(ImageWriter w, float pct) { }
        public void imageComplete(ImageWriter w) { imageComplete++; }
        public void thumbnailStarted(ImageWriter w, int i, int t) { }
        public void thumbnailProgress(ImageWriter w, float pct) { }
        public void thumbnailComplete(ImageWriter w) { }
        public void writeAborted(ImageWriter w) { writeAborted++; }
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

    // Minimal concrete writer recording what the base class hands to write(...)
    static class StubWriter extends ImageWriter {
        IIOMetadata lastStreamMeta;
        IIOImage lastImage;
        ImageWriteParam lastParam;
        RenderedImage lastRendered;
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
            lastRendered = (image == null) ? null : image.getRenderedImage();
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

    static ImageOutputStream memoryStream() {
        return new MemoryCacheImageOutputStream(new ByteArrayOutputStream());
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.ImageWriter");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.ImageTranscoder"), "implements javax.imageio.ImageTranscoder");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
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
        check(dm.contains("getNumThumbnailsSupported"), "method getNumThumbnailsSupported");
        check(dm.contains("getPreferredThumbnailSizes"), "method getPreferredThumbnailSizes");
        check(dm.contains("canWriteRasters"), "method canWriteRasters");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("write"), "method write");
        check(dm.contains("canWriteSequence"), "method canWriteSequence");
        check(dm.contains("prepareWriteSequence"), "method prepareWriteSequence");
        check(dm.contains("writeToSequence"), "method writeToSequence");
        check(dm.contains("endWriteSequence"), "method endWriteSequence");
        check(dm.contains("canReplaceStreamMetadata"), "method canReplaceStreamMetadata");
        check(dm.contains("replaceStreamMetadata"), "method replaceStreamMetadata");
        check(dm.contains("canReplaceImageMetadata"), "method canReplaceImageMetadata");
        check(dm.contains("replaceImageMetadata"), "method replaceImageMetadata");
        check(dm.contains("canInsertImage"), "method canInsertImage");
        check(dm.contains("writeInsert"), "method writeInsert");
        check(dm.contains("canRemoveImage"), "method canRemoveImage");
        check(dm.contains("removeImage"), "method removeImage");
        check(dm.contains("canWriteEmpty"), "method canWriteEmpty");
        check(dm.contains("prepareWriteEmpty"), "method prepareWriteEmpty");
        check(dm.contains("endWriteEmpty"), "method endWriteEmpty");
        check(dm.contains("canInsertEmpty"), "method canInsertEmpty");
        check(dm.contains("prepareInsertEmpty"), "method prepareInsertEmpty");
        check(dm.contains("endInsertEmpty"), "method endInsertEmpty");
        check(dm.contains("canReplacePixels"), "method canReplacePixels");
        check(dm.contains("prepareReplacePixels"), "method prepareReplacePixels");
        check(dm.contains("replacePixels"), "method replacePixels");
        check(dm.contains("replacePixels"), "method replacePixels");
        check(dm.contains("endReplacePixels"), "method endReplacePixels");
        check(dm.contains("abort"), "method abort");
        check(dm.contains("addIIOWriteWarningListener"), "method addIIOWriteWarningListener");
        check(dm.contains("removeIIOWriteWarningListener"), "method removeIIOWriteWarningListener");
        check(dm.contains("removeAllIIOWriteWarningListeners"), "method removeAllIIOWriteWarningListeners");
        check(dm.contains("addIIOWriteProgressListener"), "method addIIOWriteProgressListener");
        check(dm.contains("removeIIOWriteProgressListener"), "method removeIIOWriteProgressListener");
        check(dm.contains("removeAllIIOWriteProgressListeners"), "method removeAllIIOWriteProgressListeners");
        check(dm.contains("reset"), "method reset");
        check(dm.contains("dispose"), "method dispose");
        check(Modifier.isProtected(c.getDeclaredConstructor(ImageWriterSpi.class).getModifiers()),
              "protected ImageWriterSpi ctor");
        testRealPngWriter();
        testCapabilityMatrix();
        testStubWriterBasics();
        testStubCallbacks();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- track 1: real PNG writer through the abstract API ----
    static void testRealPngWriter() throws Exception {
        BufferedImage bi = new BufferedImage(4, 4, BufferedImage.TYPE_INT_RGB);
        bi.setRGB(0, 0, 0xFF0000FF);
        bi.setRGB(3, 3, 0xFF00FF00);
        ImageWriter w = ImageIO.getImageWritersByFormatName("png").next();
        check(w.getOriginatingProvider() != null, "png writer provider non-null");
        check(w.getLocale() == null, "default locale null");
        check(w.getAvailableLocales() == null, "no available locales");
        check(!w.canWriteRasters(), "png cannot write rasters");
        check(w.getDefaultWriteParam() != null, "default write param non-null");
        // version difference (probed): JDK-8's PNGImageWriteParam is not compressible,
        // while JDK-9+ enables canWriteCompressed. The JDK-27 variant of this test
        // asserts the opposite (true).
        check(!w.getDefaultWriteParam().canWriteCompressed(), "png param not compressible on JDK-8");
        check(w.getDefaultWriteParam().canWriteProgressive(), "png param progressive");
        expectThrows(UnsupportedOperationException.class,
                     () -> w.getDefaultWriteParam().setCompressionMode(ImageWriteParam.MODE_EXPLICIT),
                     "png param compression unsupported on JDK-8");
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        check(w.getDefaultStreamMetadata(null) == null, "png default stream metadata null");
        check(w.getDefaultImageMetadata(its, null) != null, "png default image metadata non-null");
        // convertStreamMetadata(null) is null; convertImageMetadata(null) yields a fresh PNGMetadata (probed)
        check(w.convertStreamMetadata(null, null) == null, "convertStreamMetadata null in null out");
        IIOMetadata converted = w.convertImageMetadata(null, its, null);
        check(converted != null, "convertImageMetadata(null) produces default metadata");
        check(w.getNumThumbnailsSupported(its, null, null, null) == 0, "no thumbnails supported");
        check(w.getPreferredThumbnailSizes(its, null, null, null) == null, "no preferred thumbnail sizes");
        // writing without an output fails with IllegalStateException (probed)
        expectThrows(IllegalStateException.class, () -> w.write(bi), "write without output");
        // null image arguments are IllegalArgumentException in the PNG writer (probed)
        ImageOutputStream ios = memoryStream();
        w.setOutput(ios);
        expectThrows(IllegalArgumentException.class, () -> w.write((IIOImage) null), "write null IIOImage");
        expectThrows(IllegalArgumentException.class, () -> w.write((RenderedImage) null), "write null RenderedImage");
        expectThrows(IllegalArgumentException.class, () -> w.setOutput("banana"), "setOutput wrong type");
        check(w.getOutput() == ios, "output identity");
        w.write(bi);
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios2 = new MemoryCacheImageOutputStream(bos);
        w.setOutput(ios2);
        w.write(bi);
        check(bos.size() > 0, "write produced bytes");
        // round-trip the produced stream back through ImageIO.read
        BufferedImage back = ImageIO.read(new ByteArrayInputStream(bos.toByteArray()));
        check(back != null && back.getWidth() == 4 && back.getHeight() == 4, "round-trip dimensions");
        check(back.getRGB(0, 0) == 0xFF0000FF, "round-trip pixel (0,0)");
        check(back.getRGB(3, 3) == 0xFF00FF00, "round-trip pixel (3,3)");
        // locale handling
        expectThrows(IllegalArgumentException.class, () -> w.setLocale(Locale.US), "invalid locale");
        w.setLocale(null);
        check(w.getLocale() == null, "locale stays null");
        // null listeners are silently ignored (probed)
        w.addIIOWriteProgressListener(null);
        w.removeIIOWriteProgressListener(null);
        w.removeAllIIOWriteProgressListeners();
        w.addIIOWriteWarningListener(null);
        w.removeIIOWriteWarningListener(null);
        w.removeAllIIOWriteWarningListeners();
        check(true, "null listener calls are no-ops");
        // reset restores initial state
        w.reset();
        check(w.getOutput() == null, "reset clears output");
        check(w.getLocale() == null, "reset clears locale");
        w.dispose();
        ios.close();
        ios2.close();
    }

    // ---- track 2: capability matrix from the base class (probed) ----
    static void testCapabilityMatrix() throws Exception {
        BufferedImage bi = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        IIOImage iio = new IIOImage(bi, null, null);
        ImageTypeSpecifier its = ImageTypeSpecifier.createFromRenderedImage(bi);
        // without output: can* queries throw IllegalStateException
        ImageWriter w1 = ImageIO.getImageWritersByFormatName("png").next();
        expectThrows(IllegalStateException.class, () -> w1.canInsertImage(1), "canInsertImage no output");
        expectThrows(IllegalStateException.class, () -> w1.canRemoveImage(0), "canRemoveImage no output");
        expectThrows(IllegalStateException.class, () -> w1.canReplacePixels(0), "canReplacePixels no output");
        expectThrows(IllegalStateException.class, () -> w1.canReplaceStreamMetadata(), "canReplaceStreamMetadata no output");
        expectThrows(IllegalStateException.class, () -> w1.canReplaceImageMetadata(0), "canReplaceImageMetadata no output");
        expectThrows(IllegalStateException.class, () -> w1.canWriteEmpty(), "canWriteEmpty no output");
        expectThrows(IllegalStateException.class, () -> w1.canInsertEmpty(0), "canInsertEmpty no output");
        expectThrows(IllegalStateException.class, () -> w1.prepareWriteSequence(null), "prepareWriteSequence no output");
        expectThrows(IllegalStateException.class, () -> w1.writeToSequence(iio, null), "writeToSequence no output");
        expectThrows(IllegalStateException.class, () -> w1.endWriteSequence(), "endWriteSequence no output");
        expectThrows(IllegalStateException.class, () -> w1.writeInsert(1, iio, null), "writeInsert no output");
        expectThrows(IllegalStateException.class, () -> w1.removeImage(0), "removeImage no output");
        expectThrows(IllegalStateException.class, () -> w1.replaceStreamMetadata(null), "replaceStreamMetadata no output");
        expectThrows(IllegalStateException.class, () -> w1.replaceImageMetadata(0, null), "replaceImageMetadata no output");
        expectThrows(IllegalStateException.class, () -> w1.prepareReplacePixels(0, new Rectangle(0, 0, 1, 1)), "prepareReplacePixels no output");
        expectThrows(IllegalStateException.class, () -> w1.replacePixels(bi, null), "replacePixels no output");
        expectThrows(IllegalStateException.class, () -> w1.endReplacePixels(), "endReplacePixels no output");
        expectThrows(IllegalStateException.class, () -> w1.endWriteEmpty(), "endWriteEmpty no output");
        expectThrows(IllegalStateException.class, () -> w1.endInsertEmpty(), "endInsertEmpty no output");
        // canWriteSequence performs no output check at all (probed)
        ImageWriter w2 = ImageIO.getImageWritersByFormatName("png").next();
        check(!w2.canWriteSequence(), "canWriteSequence false without output");
        w2.setOutput(memoryStream());
        check(!w2.canWriteSequence(), "canWriteSequence false with output");
        // with output: can* queries return false, mutators throw UnsupportedOperationException
        check(!w2.canInsertImage(1), "canInsertImage false");
        check(!w2.canRemoveImage(0), "canRemoveImage false");
        check(!w2.canReplacePixels(0), "canReplacePixels false");
        check(!w2.canReplaceStreamMetadata(), "canReplaceStreamMetadata false");
        check(!w2.canReplaceImageMetadata(0), "canReplaceImageMetadata false");
        check(!w2.canWriteEmpty(), "canWriteEmpty false");
        check(!w2.canInsertEmpty(0), "canInsertEmpty false");
        expectThrows(UnsupportedOperationException.class, () -> w2.prepareWriteSequence(null), "prepareWriteSequence UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.writeToSequence(iio, null), "writeToSequence UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.endWriteSequence(), "endWriteSequence UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.writeInsert(1, iio, null), "writeInsert UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.removeImage(0), "removeImage UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.replaceStreamMetadata(null), "replaceStreamMetadata UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.replaceImageMetadata(0, null), "replaceImageMetadata UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.prepareReplacePixels(0, new Rectangle(0, 0, 1, 1)), "prepareReplacePixels UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.replacePixels(bi, null), "replacePixels(RenderedImage) UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.replacePixels(bi.getRaster(), null), "replacePixels(Raster) UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.endReplacePixels(), "endReplacePixels UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.prepareWriteEmpty(null, its, 2, 2, null, null, null), "prepareWriteEmpty UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.prepareInsertEmpty(-1, its, 2, 2, null, null, null), "prepareInsertEmpty UOE");
        expectThrows(UnsupportedOperationException.class, () -> w2.endInsertEmpty(), "endInsertEmpty UOE");
        // endWriteEmpty with output is still IllegalStateException (missing prepareWriteEmpty)
        expectThrows(IllegalStateException.class, () -> w2.endWriteEmpty(), "endWriteEmpty without prepare");
        w2.dispose();
    }

    // ---- track 3: stub writer drives base-class mechanics ----
    static void testStubWriterBasics() throws Exception {
        StubWriterSpi spi = new StubWriterSpi();
        StubWriter w = new StubWriter(spi);
        check(w.getOriginatingProvider() == spi, "stub provider identity");
        // output type enforced via the provider's output types
        expectThrows(IllegalArgumentException.class, () -> w.setOutput("banana"), "stub setOutput wrong type with provider");
        w.setOutput(memoryStream());
        check(w.getOutput() != null, "stub output accepted");
        // without a provider, any output object is accepted (asymmetric with readers; probed)
        StubWriter free = new StubWriter(null);
        free.setOutput("banana");
        check("banana".equals(free.getOutput()), "any output without provider");
        free.setOutput(null);
        check(free.getOutput() == null, "setOutput(null) clears");
        // write(RenderedImage) delegates as write(null, new IIOImage(im, null, null), null)
        free.setOutput(memoryStream());
        RenderedImage ri = new BufferedImage(2, 2, BufferedImage.TYPE_INT_RGB);
        free.write(ri);
        check(free.lastImage != null && free.lastImage.getRenderedImage() == ri, "write(RenderedImage) wraps image");
        check(free.lastImage.getThumbnails() == null, "write(RenderedImage) null thumbnails");
        check(free.lastImage.getMetadata() == null, "write(RenderedImage) null metadata");
        check(free.lastStreamMeta == null, "write(RenderedImage) null stream metadata");
        check(free.lastParam == null, "write(RenderedImage) null param");
        // write(IIOImage) delegates as write(null, image, null)
        IIOImage custom = new IIOImage(ri, null, null);
        free.write(custom);
        check(free.lastImage == custom, "write(IIOImage) passes image through");
        check(free.lastStreamMeta == null && free.lastParam == null, "write(IIOImage) null meta/param");
        // the base class does not guard null IIOImage: it reaches the subclass (probed)
        free.write((IIOImage) null);
        check(free.lastImage == null, "write(null) reaches subclass write");
        // full 3-arg write passes everything through
        ImageWriteParam param = free.getDefaultWriteParam();
        IIOMetadata sm = free.getDefaultStreamMetadata(null);
        free.write(sm, custom, param);
        check(free.lastStreamMeta == sm && free.lastImage == custom && free.lastParam == param, "3-arg write passthrough");
        // convert* are subclass-controlled passthroughs
        check(free.convertStreamMetadata(sm, param) == sm, "convertStreamMetadata passthrough");
        check(free.convertImageMetadata(sm, ImageTypeSpecifier.createFromRenderedImage(ri), param) == sm,
              "convertImageMetadata passthrough");
    }

    // listener callbacks, abort protocol and warning broadcast
    static void testStubCallbacks() throws Exception {
        StubWriter w = new StubWriter(null);
        w.setOutput(memoryStream());
        WriteProgressRecorder rec1 = new WriteProgressRecorder();
        WriteProgressRecorder rec2 = new WriteProgressRecorder();
        w.removeIIOWriteProgressListener(rec1);
        check(true, "remove of unregistered listener is a no-op");
        w.addIIOWriteProgressListener(rec1);
        w.addIIOWriteProgressListener(rec2);
        w.writeWithCallbacks();
        check(rec1.imageStarted == 1 && rec1.imageComplete == 1, "first listener notified");
        check(rec2.imageStarted == 1 && rec2.imageComplete == 1, "second listener notified");
        check(rec1.lastSource == w && rec1.lastIndex == 0, "listener receives writer and index");
        w.removeIIOWriteProgressListener(rec1);
        w.writeWithCallbacks();
        check(rec1.imageStarted == 1, "removed listener no longer notified");
        check(rec2.imageStarted == 2, "remaining listener still notified");
        w.removeAllIIOWriteProgressListeners();
        w.writeWithCallbacks();
        check(rec2.imageStarted == 2, "removeAll stops delivery");
        // abort raised from within imageStarted: writeAborted fires, imageComplete does not
        StubWriter aw = new StubWriter(null);
        aw.setOutput(memoryStream());
        final WriteProgressRecorder arec = new WriteProgressRecorder();
        aw.addIIOWriteProgressListener(new IIOWriteProgressListener() {
            public void imageStarted(ImageWriter x, int i) { x.abort(); }
            public void imageProgress(ImageWriter x, float p) { }
            public void imageComplete(ImageWriter x) { arec.imageComplete++; }
            public void thumbnailStarted(ImageWriter x, int i, int t) { }
            public void thumbnailProgress(ImageWriter x, float p) { }
            public void thumbnailComplete(ImageWriter x) { }
            public void writeAborted(ImageWriter x) { arec.writeAborted++; }
        });
        aw.writeWithCallbacks();
        check(arec.writeAborted == 1, "abort during write fires writeAborted");
        check(arec.imageComplete == 0, "aborted write skips imageComplete");
        // a stale abort request is cleared by the next write
        StubWriter sw = new StubWriter(null);
        sw.setOutput(memoryStream());
        sw.abort();
        WriteProgressRecorder srec = new WriteProgressRecorder();
        sw.addIIOWriteProgressListener(srec);
        sw.writeWithCallbacks();
        check(srec.imageComplete == 1, "stale abort cleared by next write");
        check(srec.writeAborted == 0, "stale abort does not fire writeAborted");
        // processWarningOccurred: null warning rejected only when a listener is attached (probed)
        StubWriter ww = new StubWriter(null);
        ww.setOutput(memoryStream());
        ww.warn(0, null); // no listeners registered: silently ignored
        check(true, "null warning without listeners is a no-op");
        ww.addIIOWriteWarningListener(new IIOWriteWarningListener() {
            public void warningOccurred(ImageWriter x, int i, String s) { }
        });
        expectThrows(IllegalArgumentException.class, () -> ww.warn(0, null), "null warning with listener");
        ww.warn(0, "coreb warning"); // delivered without throwing
        check(true, "non-null warning delivered");
        ww.removeAllIIOWriteWarningListeners();
        // locale contract via the stub's getAvailableLocales
        StubWriter lw = new StubWriter(null);
        Locale[] locales = lw.getAvailableLocales();
        check(locales != null && locales.length == 2, "stub locales advertised");
        lw.setLocale(Locale.FRENCH);
        check(lw.getLocale() == Locale.FRENCH, "locale set");
        expectThrows(IllegalArgumentException.class, () -> lw.setLocale(Locale.CANADA), "locale not in list");
        lw.setLocale(null);
        check(lw.getLocale() == null, "setLocale(null) clears");
        lw.setLocale(Locale.FRENCH);
        check(lw.getDefaultWriteParam().getLocale() == Locale.FRENCH, "default param inherits locale");
        // endWriteEmpty on the stub: output set but no prepare call
        expectThrows(IllegalStateException.class, () -> lw.endWriteEmpty(), "endWriteEmpty without prepare");
        lw.dispose();
    }
}

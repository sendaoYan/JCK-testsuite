/*
 * @test
 * @summary javax.imageio.spi.ImageWriterSpi conformance: white-box structure + black-box behavior.
 * @run main ImageWriterSpiTest
 */

import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.ImageWriter;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class ImageWriterSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    interface ThrowingRunnable { void run() throws Exception; }
    static void expectThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        boolean thrown = false;
        try { r.run(); } catch (Throwable t) {
            thrown = true;
            check(expected.isAssignableFrom(t.getClass()),
                  msg + " expected " + expected.getSimpleName() + " but got " + t.getClass().getSimpleName());
        }
        check(thrown, msg + " should throw " + expected.getSimpleName());
    }

    static ImageWriterSpi pngWriterSpi() {
        Iterator<ImageWriterSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageWriterSpi.class, false);
        while (it.hasNext()) {
            ImageWriterSpi sp = it.next();
            String[] names = sp.getFormatNames();
            if (names != null) {
                for (String n : names) if ("png".equalsIgnoreCase(n)) return sp;
            }
        }
        throw new AssertionError("no png ImageWriterSpi");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageWriterSpi");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.imageio.spi.ImageReaderWriterSpi"),
              "extends ImageReaderWriterSpi");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ImageWriterSpi sp = pngWriterSpi();

        String[] names = sp.getFormatNames();
        check(containsIgnoreCase(names, "png"), "format names contain png");

        Class<?>[] outputTypes = sp.getOutputTypes();
        check(outputTypes != null && outputTypes.length >= 1, "output types non-empty");
        boolean hasIOS = false;
        for (Class<?> t : outputTypes) if (t == ImageOutputStream.class) hasIOS = true;
        check(hasIOS, "output types include ImageOutputStream.class");

        check(sp.isFormatLossless() == true, "png writer SPI is lossless");

        // encode-capability on a headless-safe BufferedImage
        BufferedImage img = new BufferedImage(16, 16, BufferedImage.TYPE_INT_RGB);
        ImageTypeSpecifier spec = ImageTypeSpecifier.createFromRenderedImage(img);
        check(sp.canEncodeImage(spec) == true, "canEncodeImage(ImageTypeSpecifier) true for RGB");
        check(sp.canEncodeImage((RenderedImage) img) == true, "canEncodeImage(RenderedImage) true");

        // reader SPI names advertised by this writer
        String[] readerSpiNames = sp.getImageReaderSpiNames();
        check(readerSpiNames != null && readerSpiNames.length >= 1, "png writer advertises reader SPIs");
        check(readerSpiNames[0].indexOf("PNGImageReaderSpi") >= 0, "preferred reader SPI is PNGImageReaderSpi");

        // create + own-writer round-trip
        ImageWriter w1 = sp.createWriterInstance();
        check(w1 != null, "createWriterInstance non-null");
        check(sp.isOwnWriter(w1) == true, "isOwnWriter true for own writer");
        ImageWriter w2 = sp.createWriterInstance(null);
        check(w2 != null, "createWriterInstance(extension=null) non-null");
        check(sp.isOwnWriter(w2) == true, "isOwnWriter true for extension-null writer");
        expectThrows(IllegalArgumentException.class, () -> sp.isOwnWriter(null), "isOwnWriter(null)");
        w1.dispose();
        w2.dispose();
    }

    static boolean containsIgnoreCase(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) if (target.equalsIgnoreCase(s)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantsAndPngIdentity();
        testCanEncodeImageMatrix();
        testCanEncodeImageNulls();
        testCreateWriterInstanceExtension();
        testTinyWriterSpiRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { caught = t; }
        check(caught != null, msg + ": nothing thrown, expected " + expected.getSimpleName());
        if (caught == null) return;
        check(caught.getClass() == expected,
              msg + ": expected " + expected.getSimpleName() + " but got "
              + caught.getClass().getName() + " (" + caught.getMessage() + ")");
    }

    /** Concrete ImageWriter with a stable binary name for isOwnWriter name matching. */
    static class NamedWriter extends ImageWriter {
        NamedWriter(ImageWriterSpi originatingProvider) { super(originatingProvider); }
        public javax.imageio.metadata.IIOMetadata getDefaultStreamMetadata(javax.imageio.ImageWriteParam param) { return null; }
        public javax.imageio.metadata.IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier type, javax.imageio.ImageWriteParam param) { return null; }
        public javax.imageio.metadata.IIOMetadata convertStreamMetadata(javax.imageio.metadata.IIOMetadata inData, javax.imageio.ImageWriteParam param) { return null; }
        public javax.imageio.metadata.IIOMetadata convertImageMetadata(javax.imageio.metadata.IIOMetadata inData, ImageTypeSpecifier type, javax.imageio.ImageWriteParam param) { return null; }
        public void write(javax.imageio.metadata.IIOMetadata streamMetadata, javax.imageio.IIOImage image, javax.imageio.ImageWriteParam param) { }
    }

    /** Self-written minimal writer SPI with recordable createWriterInstance. */
    static class TinyWriterSpi extends ImageWriterSpi {
        Object lastExt;
        TinyWriterSpi(String pluginClassName, Class<?>[] outputTypes) {
            super("TinyVendor", "5.3", new String[] { "tny" }, new String[] { "tsw" },
                  new String[] { "image/x-tiny-w" }, pluginClassName, outputTypes, null,
                  true, null, null, null, null,
                  false, null, null, null, null);
        }
        public String getDescription(java.util.Locale l) { return "tiny writer spi"; }
        public boolean canEncodeImage(ImageTypeSpecifier type) { return true; }
        public ImageWriter createWriterInstance(Object ext) {
            lastExt = ext;
            return new NamedWriter(this);
        }
    }

    // constants and identity of the registered PNG writer provider
    static void testConstantsAndPngIdentity() {
        ImageWriterSpi sp = pngWriterSpi();
        check("Oracle Corporation".equals(sp.getVendorName()),
              "OpenJDK-built png spi vendor Oracle Corporation");
        check("1.0".equals(sp.getVersion()), "OpenJDK-built png spi version 1.0");
        check(sp.getPluginClassName().endsWith("PNGImageWriter"), "plugin class name is PNGImageWriter");
        check(sp.getDescription(java.util.Locale.US) != null, "description non-null");
        Class<?>[] outputTypes = sp.getOutputTypes();
        check(outputTypes != null && outputTypes.length == 1, "png output types single element");
        check(outputTypes[0] == ImageOutputStream.class, "png output type is ImageOutputStream");
        check(sp.getOutputTypes() != sp.getOutputTypes(), "getOutputTypes fresh clone each call");
        String[] readers = sp.getImageReaderSpiNames();
        boolean sawPngReader = false;
        for (String rn : readers) if (rn.contains("PNGImageReaderSpi")) sawPngReader = true;
        check(sawPngReader, "advertised reader spi names include PNGImageReaderSpi");
    }

    // png writer accepts every standard BufferedImage type
    static void testCanEncodeImageMatrix() {
        ImageWriterSpi sp = pngWriterSpi();
        int[] types = {
            BufferedImage.TYPE_INT_RGB, BufferedImage.TYPE_INT_ARGB, BufferedImage.TYPE_INT_ARGB_PRE,
            BufferedImage.TYPE_INT_BGR, BufferedImage.TYPE_3BYTE_BGR, BufferedImage.TYPE_4BYTE_ABGR,
            BufferedImage.TYPE_4BYTE_ABGR_PRE, BufferedImage.TYPE_USHORT_565_RGB,
            BufferedImage.TYPE_USHORT_555_RGB, BufferedImage.TYPE_BYTE_GRAY,
            BufferedImage.TYPE_USHORT_GRAY, BufferedImage.TYPE_BYTE_BINARY,
            BufferedImage.TYPE_BYTE_INDEXED,
        };
        for (int t : types) {
            BufferedImage img = new BufferedImage(2, 2, t);
            check(sp.canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(img)) == true,
                  "canEncodeImage(spec) true for BufferedImage type " + t);
            check(sp.canEncodeImage((RenderedImage) img) == true,
                  "canEncodeImage(RenderedImage) true for BufferedImage type " + t);
        }
    }

    // null arguments of both canEncodeImage overloads (recorded actual behavior)
    static void testCanEncodeImageNulls() {
        final ImageWriterSpi sp = pngWriterSpi();
        // the ImageTypeSpecifier overload throws NPE (not the IAE of the javadoc)
        expectExact(NullPointerException.class,
                    () -> sp.canEncodeImage((ImageTypeSpecifier) null),
                    "canEncodeImage(null spec)");
        // the RenderedImage overload validates with IAE before delegation
        expectExact(IllegalArgumentException.class,
                    () -> sp.canEncodeImage((RenderedImage) null),
                    "canEncodeImage(null image)");
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        check(sp.canEncodeImage((RenderedImage) img) == sp.canEncodeImage(
                  ImageTypeSpecifier.createFromRenderedImage(img)),
              "RenderedImage overload agrees with specifier overload");
    }

    // createWriterInstance with a non-null extension; isOwnWriter name matching
    static void testCreateWriterInstanceExtension() throws Exception {
        ImageWriterSpi sp = pngWriterSpi();
        ImageWriter w2 = sp.createWriterInstance(new Object());
        check(w2 != null, "createWriterInstance(extension) non-null for png spi");
        check(w2.getOriginatingProvider() == sp, "writer reports originating provider");
        check(sp.isOwnWriter(w2), "isOwnWriter true regardless of extension");
        w2.dispose();

        // isOwnWriter matches writer.getClass().getName() against pluginClassName
        TinyWriterSpi match = new TinyWriterSpi(NamedWriter.class.getName(),
                                                ImageWriterSpi.STANDARD_OUTPUT_TYPE);
        ImageWriter own = match.createWriterInstance(null);
        check(match.isOwnWriter(own), "isOwnWriter true when plugin class name matches writer class");
        TinyWriterSpi mismatch = new TinyWriterSpi("com.no.SuchPlugin",
                                                   ImageWriterSpi.STANDARD_OUTPUT_TYPE);
        check(!mismatch.isOwnWriter(own), "isOwnWriter false when plugin class name differs");
    }

    // self-written minimal subclass drives the full constructor and every getter
    static void testTinyWriterSpiRoundTrip() throws Exception {
        TinyWriterSpi sp = new TinyWriterSpi("com.probe.TinyPlugin", ImageWriterSpi.STANDARD_OUTPUT_TYPE);
        check("TinyVendor".equals(sp.getVendorName()), "tiny vendor round-trips");
        check("5.3".equals(sp.getVersion()), "tiny version round-trips");
        check(containsIgnoreCase(sp.getFormatNames(), "tny"), "tiny format names round-trip");
        check(containsIgnoreCase(sp.getFileSuffixes(), "tsw"), "tiny suffixes round-trip");
        check(containsIgnoreCase(sp.getMIMETypes(), "image/x-tiny-w"), "tiny mime types round-trip");
        check("com.probe.TinyPlugin".equals(sp.getPluginClassName()), "tiny plugin class name round-trips");
        check("tiny writer spi".equals(sp.getDescription(java.util.Locale.US)), "tiny description round-trips");
        check(sp.getOutputTypes().length == 1
              && sp.getOutputTypes()[0] == ImageOutputStream.class, "tiny output types round-trip");
        check(sp.isFormatLossless() == true, "tiny lossless flag round-trips");
        check(sp.getImageReaderSpiNames() == null, "reader spi names null when ctor passed null");
        check(sp.canEncodeImage(ImageTypeSpecifier.createFromRenderedImage(
                  new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB))) == true,
              "tiny canEncodeImage delegates to subclass");
        ImageWriter w = sp.createWriterInstance("EXT");
        check("EXT".equals(sp.lastExt), "createWriterInstance passes extension verbatim");
        check(sp.createWriterInstance(null) != null && sp.lastExt == null,
              "createWriterInstance(null) records null extension");
        check(w.getOriginatingProvider() == sp, "tiny writer origin round-trips");
        expectExact(IllegalArgumentException.class,
                    () -> new TinyWriterSpi("com.probe.P", null),
                    "ctor null outputTypes");
    }
}

/*
 * @test
 * @summary javax.imageio.spi.ImageReaderSpi conformance: white-box structure + black-box behavior.
 * @run main ImageReaderSpiTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.ImageReadParam;
import javax.imageio.ImageReader;
import javax.imageio.ImageTypeSpecifier;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;
import javax.imageio.stream.ImageInputStream;

public class ImageReaderSpiTest {
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

    static ImageReaderSpi pngReaderSpi() {
        Iterator<ImageReaderSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageReaderSpi.class, false);
        while (it.hasNext()) {
            ImageReaderSpi sp = it.next();
            String[] names = sp.getFormatNames();
            if (names != null) {
                for (String n : names) if ("png".equalsIgnoreCase(n)) return sp;
            }
        }
        throw new AssertionError("no png ImageReaderSpi");
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageReaderSpi");
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
        ImageReaderSpi sp = pngReaderSpi();

        // format identity (inherited)
        String[] names = sp.getFormatNames();
        check(containsIgnoreCase(names, "png"), "format names contain png");

        // input types
        Class<?>[] inputTypes = sp.getInputTypes();
        check(inputTypes != null && inputTypes.length >= 1, "input types non-empty");
        boolean hasIIS = false;
        for (Class<?> t : inputTypes) if (t == ImageInputStream.class) hasIIS = true;
        check(hasIIS, "input types include ImageInputStream.class");

        // canDecodeInput on a real PNG signature stream
        byte[] pngSig = new byte[] {
            (byte)0x89, 'P', 'N', 'G', 0x0D, 0x0A, 0x1A, 0x0A
        };
        ImageInputStream iis = ImageIO.createImageInputStream(new ByteArrayInputStream(pngSig));
        check(sp.canDecodeInput(iis) == true, "canDecodeInput recognizes PNG signature");
        check(sp.canDecodeInput("not-a-stream") == false, "canDecodeInput rejects non-ImageInputStream");
        iis.close();

        // writer SPI names advertised by this reader
        String[] writerSpiNames = sp.getImageWriterSpiNames();
        check(writerSpiNames != null && writerSpiNames.length >= 1, "png reader advertises writer SPIs");
        check(writerSpiNames[0].indexOf("PNGImageWriterSpi") >= 0, "preferred writer SPI is PNGImageWriterSpi");

        // create + own-reader round-trip
        ImageReader r1 = sp.createReaderInstance();
        check(r1 != null, "createReaderInstance non-null");
        check(sp.isOwnReader(r1) == true, "isOwnReader true for own reader");
        ImageReader r2 = sp.createReaderInstance(null);
        check(r2 != null, "createReaderInstance(extension=null) non-null");
        check(sp.isOwnReader(r2) == true, "isOwnReader true for extension-null reader");
        expectThrows(IllegalArgumentException.class, () -> sp.isOwnReader(null), "isOwnReader(null)");
        r1.dispose();
        r2.dispose();
    }

    static boolean containsIgnoreCase(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) if (target.equalsIgnoreCase(s)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstants();
        testPngSpiIdentity();
        testCanDecodeInputEdgeCases();
        testCreateReaderInstanceExtension();
        testMetadataFormatAccessors();
        testTinyReaderSpiRoundTrip();
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

    /** Concrete ImageReader with a stable binary name for isOwnReader name matching. */
    static class NamedReader extends ImageReader {
        NamedReader(ImageReaderSpi originatingProvider) { super(originatingProvider); }
        public int getNumImages(boolean allowSearch) { return 0; }
        public int getWidth(int imageIndex) { return 1; }
        public int getHeight(int imageIndex) { return 1; }
        public Iterator<ImageTypeSpecifier> getImageTypes(int imageIndex) {
            return java.util.Collections.<ImageTypeSpecifier>emptyList().iterator();
        }
        public javax.imageio.metadata.IIOMetadata getStreamMetadata() { return null; }
        public javax.imageio.metadata.IIOMetadata getImageMetadata(int imageIndex) { return null; }
        public java.awt.image.BufferedImage read(int imageIndex, ImageReadParam param) { return null; }
    }

    /** Self-written minimal reader SPI with recordable createReaderInstance. */
    static class TinyReaderSpi extends ImageReaderSpi {
        Object lastExt;
        TinyReaderSpi(String pluginClassName, Class<?>[] inputTypes, String[] names) {
            super("TinyVendor", "4.2", names, new String[] { "tsx" }, new String[] { "image/x-tiny" },
                  pluginClassName, inputTypes, null,
                  false, null, null, null, null,
                  false, null, null, null, null);
        }
        public String getDescription(Locale l) { return "tiny reader spi"; }
        public boolean canDecodeInput(Object input) { return "tiny".equals(input); }
        public ImageReader createReaderInstance(Object ext) {
            lastExt = ext;
            return new NamedReader(this);
        }
    }

    // STANDARD_INPUT_TYPE / STANDARD_OUTPUT_TYPE constants
    static void testConstants() {
        Class<?>[] in = ImageReaderSpi.STANDARD_INPUT_TYPE;
        check(in != null && in.length == 1, "STANDARD_INPUT_TYPE single element");
        check(in[0] == ImageInputStream.class, "STANDARD_INPUT_TYPE is ImageInputStream.class");
        Class<?>[] out = javax.imageio.spi.ImageWriterSpi.STANDARD_OUTPUT_TYPE;
        check(out != null && out.length == 1, "STANDARD_OUTPUT_TYPE single element");
        check(out[0] == javax.imageio.stream.ImageOutputStream.class,
              "STANDARD_OUTPUT_TYPE is ImageOutputStream.class");
    }

    // identity of the registered PNG reader provider
    static void testPngSpiIdentity() {
        ImageReaderSpi sp = pngReaderSpi();
        check("Oracle Corporation".equals(sp.getVendorName()),
              "OpenJDK-built png spi vendor Oracle Corporation");
        check("1.0".equals(sp.getVersion()), "OpenJDK-built png spi version 1.0");
        check(sp.getPluginClassName().endsWith("PNGImageReader"), "plugin class name is PNGImageReader");
        check(sp.getDescription(Locale.US) != null, "description non-null");
        Class<?>[] inputTypes = sp.getInputTypes();
        check(inputTypes != null && inputTypes.length == 1, "png input types single element");
        check(inputTypes[0] == ImageInputStream.class, "png input type is ImageInputStream");
        check(sp.getInputTypes() != sp.getInputTypes(), "getInputTypes fresh clone each call");
        String[] writers = sp.getImageWriterSpiNames();
        boolean sawPngWriter = false;
        for (String w : writers) if (w.contains("PNGImageWriterSpi")) sawPngWriter = true;
        check(sawPngWriter, "advertised writer spi names include PNGImageWriterSpi");
    }

    // canDecodeInput edge cases beyond the magic-number happy path
    static void testCanDecodeInputEdgeCases() throws Exception {
        final ImageReaderSpi sp = pngReaderSpi();
        ImageInputStream text = new javax.imageio.stream.MemoryCacheImageInputStream(
            new java.io.ByteArrayInputStream("plain text stream".getBytes("US-ASCII")));
        check(sp.canDecodeInput(text) == false, "canDecodeInput false for text stream");
        text.close();

        // non-ImageInputStream objects (including null) are answered with false,
        // not with the IllegalArgumentException promised by the javadoc (recorded actual behavior)
        check(sp.canDecodeInput(null) == false, "canDecodeInput(null) returns false (javadoc says IAE)");
        check(sp.canDecodeInput("junk") == false, "canDecodeInput(non-stream Object) returns false");

        // a stream shorter than the 8-byte signature is answered with false on JDK-9+
        // (JDK-8 threw EOFException here: recorded version difference)
        ImageInputStream shortStream = new javax.imageio.stream.MemoryCacheImageInputStream(
            new java.io.ByteArrayInputStream(new byte[] { 1, 2, 3, 4 }));
        check(sp.canDecodeInput(shortStream) == false, "canDecodeInput false for 4-byte stream");
        shortStream.close();

        ImageInputStream jpeg = new javax.imageio.stream.MemoryCacheImageInputStream(
            new java.io.ByteArrayInputStream(
                new byte[] { (byte)0xFF, (byte)0xD8, (byte)0xFF, (byte)0xE0, 0, 0, 0, 0 }));
        check(sp.canDecodeInput(jpeg) == false, "canDecodeInput false for JPEG signature");
        jpeg.close();
    }

    // createReaderInstance with a non-null extension; isOwnReader name matching
    static void testCreateReaderInstanceExtension() throws Exception {
        ImageReaderSpi sp = pngReaderSpi();
        ImageReader r2 = sp.createReaderInstance(new Object());
        check(r2 != null, "createReaderInstance(extension) non-null for png spi");
        check(r2.getOriginatingProvider() == sp, "reader reports originating provider");
        check(sp.isOwnReader(r2), "isOwnReader true regardless of extension");
        r2.dispose();

        // isOwnReader matches reader.getClass().getName() against pluginClassName
        TinyReaderSpi match = new TinyReaderSpi(NamedReader.class.getName(),
                                                ImageReaderSpi.STANDARD_INPUT_TYPE, new String[] { "tiny" });
        ImageReader own = match.createReaderInstance(null);
        check(match.isOwnReader(own), "isOwnReader true when plugin class name matches reader class");
        TinyReaderSpi mismatch = new TinyReaderSpi("com.no.SuchPlugin",
                                                   ImageReaderSpi.STANDARD_INPUT_TYPE, new String[] { "tiny" });
        check(!mismatch.isOwnReader(own), "isOwnReader false when plugin class name differs");
    }

    // metadata format accessors of the png reader spi
    static void testMetadataFormatAccessors() {
        ImageReaderSpi sp = pngReaderSpi();
        check("javax_imageio_png_1.0".equals(sp.getNativeImageMetadataFormatName()),
              "png native image metadata format name");
        check(sp.getNativeStreamMetadataFormatName() == null, "png native stream format null");
        check(sp.getExtraImageMetadataFormatNames() == null, "png extra image formats null");
        check(sp.getExtraStreamMetadataFormatNames() == null, "png extra stream formats null");
        check(sp.getImageMetadataFormat("javax_imageio_png_1.0") != null,
              "getImageMetadataFormat(native) non-null");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getImageMetadataFormat("no_such_format"),
                    "getImageMetadataFormat(unknown)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getImageMetadataFormat(null),
                    "getImageMetadataFormat(null)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat("javax_imageio_1.0"),
                    "getStreamMetadataFormat(std) unsupported for png");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat("no_such_format"),
                    "getStreamMetadataFormat(unknown)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat(null),
                    "getStreamMetadataFormat(null)");
    }

    // self-written minimal subclass drives the full constructor and every getter
    static void testTinyReaderSpiRoundTrip() throws Exception {
        TinyReaderSpi sp = new TinyReaderSpi("com.probe.TinyPlugin",
                                             ImageReaderSpi.STANDARD_INPUT_TYPE, new String[] { "tny" });
        check("TinyVendor".equals(sp.getVendorName()), "tiny vendor round-trips");
        check("4.2".equals(sp.getVersion()), "tiny version round-trips");
        check(containsIgnoreCase(sp.getFormatNames(), "tny"), "tiny format names round-trip");
        check(containsIgnoreCase(sp.getFileSuffixes(), "tsx"), "tiny suffixes round-trip");
        check(containsIgnoreCase(sp.getMIMETypes(), "image/x-tiny"), "tiny mime types round-trip");
        check("com.probe.TinyPlugin".equals(sp.getPluginClassName()), "tiny plugin class name round-trips");
        check("tiny reader spi".equals(sp.getDescription(Locale.US)), "tiny description round-trips");
        check(sp.getInputTypes().length == 1
              && sp.getInputTypes()[0] == ImageInputStream.class, "tiny input types round-trip");
        check(sp.getImageWriterSpiNames() == null, "writer spi names null when ctor passed null");
        check(sp.isStandardStreamMetadataFormatSupported() == false, "tiny std stream meta false");
        check(sp.isStandardImageMetadataFormatSupported() == false, "tiny std image meta false");
        check(sp.getNativeStreamMetadataFormatName() == null, "tiny native stream meta null");
        check(sp.getNativeImageMetadataFormatName() == null, "tiny native image meta null");
        check(sp.getExtraStreamMetadataFormatNames() == null, "tiny extra stream meta null");
        check(sp.getExtraImageMetadataFormatNames() == null, "tiny extra image meta null");
        check(sp.canDecodeInput("tiny") == true, "tiny canDecodeInput accepts marker string");
        check(sp.canDecodeInput("other") == false, "tiny canDecodeInput rejects other input");
        check(sp.canDecodeInput(null) == false, "tiny canDecodeInput handles null");
        ImageReader r = sp.createReaderInstance("EXT");
        check("EXT".equals(sp.lastExt), "createReaderInstance passes extension verbatim");
        check(sp.createReaderInstance(null) != null && sp.lastExt == null,
              "createReaderInstance(null) records null extension");
        check(r.getOriginatingProvider() == sp, "tiny reader origin round-trips");
        expectExact(IllegalArgumentException.class,
                    () -> new TinyReaderSpi("com.probe.P", null, new String[] { "tny" }),
                    "ctor null inputTypes");
    }
}

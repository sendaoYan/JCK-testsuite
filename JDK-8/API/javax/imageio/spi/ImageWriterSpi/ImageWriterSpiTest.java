/*
 * @test
 * @summary javax.imageio.spi.ImageWriterSpi conformance: abstract hierarchy and methods.
 * @run main ImageWriterSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import javax.imageio.*;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.stream.ImageOutputStream;

public class ImageWriterSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageWriterSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.ImageReaderWriterSpi"), "extends javax.imageio.spi.ImageReaderWriterSpi");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("isFormatLossless"), "method isFormatLossless");
        check(dm.contains("getOutputTypes"), "method getOutputTypes");
        check(dm.contains("canEncodeImage"), "method canEncodeImage");
        check(dm.contains("canEncodeImage"), "method canEncodeImage");
        check(dm.contains("createWriterInstance"), "method createWriterInstance");
        check(dm.contains("createWriterInstance"), "method createWriterInstance");
        check(dm.contains("isOwnWriter"), "method isOwnWriter");
        check(dm.contains("getImageReaderSpiNames"), "method getImageReaderSpiNames");
        testConstantsAndPngIdentity();
        testCanEncodeImageMatrix();
        testCanEncodeImageNulls();
        testCreateWriterInstance();
        testTinyWriterSpiRoundTrip();
        System.out.println("PASS (" + passed + " assertions)");
    }

    interface ThrowingRunnable { void run() throws Exception; }
    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { caught = t; }
        check(caught != null, msg + ": nothing thrown, expected " + expected.getSimpleName());
        if (caught == null) return;
        check(caught.getClass() == expected,
              msg + ": expected " + expected.getSimpleName() + " but got "
              + caught.getClass().getName() + " (" + caught.getMessage() + ")");
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

    /** Concrete ImageWriter with a stable binary name for isOwnWriter name matching. */
    static class NamedWriter extends ImageWriter {
        NamedWriter(ImageWriterSpi originatingProvider) { super(originatingProvider); }
        public IIOMetadata getDefaultStreamMetadata(ImageWriteParam param) { return null; }
        public IIOMetadata getDefaultImageMetadata(ImageTypeSpecifier type, ImageWriteParam param) { return null; }
        public IIOMetadata convertStreamMetadata(IIOMetadata inData, ImageWriteParam param) { return null; }
        public IIOMetadata convertImageMetadata(IIOMetadata inData, ImageTypeSpecifier type,
                                                ImageWriteParam param) { return null; }
        public void write(IIOMetadata streamMetadata, IIOImage image, ImageWriteParam param) { }
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
        public String getDescription(Locale l) { return "tiny writer spi"; }
        public boolean canEncodeImage(ImageTypeSpecifier type) { return true; }
        public ImageWriter createWriterInstance(Object ext) {
            lastExt = ext;
            return new NamedWriter(this);
        }
    }

    static boolean containsIgnoreCase(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) if (target.equalsIgnoreCase(s)) return true;
        return false;
    }

    // constants and identity of the registered PNG writer provider
    static void testConstantsAndPngIdentity() {
        ImageWriterSpi sp = pngWriterSpi();
        check(containsIgnoreCase(sp.getFormatNames(), "png"), "format names contain png");
        check("Oracle Corporation".equals(sp.getVendorName()),
              "OpenJDK-built png spi vendor Oracle Corporation");
        check("1.0".equals(sp.getVersion()), "OpenJDK-built png spi version 1.0");
        check(sp.getPluginClassName().endsWith("PNGImageWriter"), "plugin class name is PNGImageWriter");
        check(sp.getDescription(Locale.US) != null, "description non-null");
        Class<?>[] outputTypes = sp.getOutputTypes();
        check(outputTypes != null && outputTypes.length == 1, "png output types single element");
        check(outputTypes[0] == ImageOutputStream.class, "png output type is ImageOutputStream");
        check(sp.getOutputTypes() != sp.getOutputTypes(), "getOutputTypes fresh clone each call");
        check(sp.isFormatLossless() == true, "png writer is lossless");
        String[] readers = sp.getImageReaderSpiNames();
        check(readers != null && readers.length >= 1, "png advertises reader spi names");
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
        // a RenderedImage is delegated through ImageTypeSpecifier
        BufferedImage img = new BufferedImage(3, 3, BufferedImage.TYPE_INT_RGB);
        check(sp.canEncodeImage((RenderedImage) img) == sp.canEncodeImage(
                  ImageTypeSpecifier.createFromRenderedImage(img)),
              "RenderedImage overload agrees with specifier overload");
        expectExact(IllegalArgumentException.class, () -> sp.isOwnWriter(null), "isOwnWriter(null)");
    }

    // createWriterInstance: null and non-null extension, isOwnWriter name matching
    static void testCreateWriterInstance() throws Exception {
        ImageWriterSpi sp = pngWriterSpi();
        ImageWriter w1 = sp.createWriterInstance(null);
        check(w1 != null, "createWriterInstance(null) non-null");
        ImageWriter w2 = sp.createWriterInstance(new Object());
        check(w2 != null, "createWriterInstance(extension) non-null for png spi");
        check(w1 != w2, "two createWriterInstance calls return fresh instances");
        check(w1.getOriginatingProvider() == sp, "writer reports originating provider");
        check(sp.isOwnWriter(w1), "isOwnWriter true for own writer");
        check(sp.isOwnWriter(w2), "isOwnWriter true regardless of extension");
        w1.dispose();
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
        check("tiny writer spi".equals(sp.getDescription(Locale.US)), "tiny description round-trips");
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
        // constructor validation
        expectExact(IllegalArgumentException.class,
                    () -> new TinyWriterSpi("com.probe.P", null),
                    "ctor null outputTypes");
    }
}

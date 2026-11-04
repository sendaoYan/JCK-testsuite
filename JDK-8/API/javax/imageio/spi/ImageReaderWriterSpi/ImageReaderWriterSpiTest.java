/*
 * @test
 * @summary javax.imageio.spi.ImageReaderWriterSpi conformance: abstract hierarchy and methods.
 * @run main ImageReaderWriterSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;

public class ImageReaderWriterSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageReaderWriterSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getFormatNames"), "method getFormatNames");
        check(dm.contains("getFileSuffixes"), "method getFileSuffixes");
        check(dm.contains("getMIMETypes"), "method getMIMETypes");
        check(dm.contains("getPluginClassName"), "method getPluginClassName");
        check(dm.contains("isStandardStreamMetadataFormatSupported"), "method isStandardStreamMetadataFormatSupported");
        check(dm.contains("getNativeStreamMetadataFormatName"), "method getNativeStreamMetadataFormatName");
        check(dm.contains("getExtraStreamMetadataFormatNames"), "method getExtraStreamMetadataFormatNames");
        check(dm.contains("isStandardImageMetadataFormatSupported"), "method isStandardImageMetadataFormatSupported");
        check(dm.contains("getNativeImageMetadataFormatName"), "method getNativeImageMetadataFormatName");
        check(dm.contains("getExtraImageMetadataFormatNames"), "method getExtraImageMetadataFormatNames");
        check(dm.contains("getStreamMetadataFormat"), "method getStreamMetadataFormat");
        check(dm.contains("getImageMetadataFormat"), "method getImageMetadataFormat");
        testPngInheritedAccessors();
        testMetadataFormatNegatives();
        testConstructorValidation();
        testTinySubclassRoundTrip();
        testCloneSemantics();
        testNoArgConstructor();
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

    /** Minimal concrete subclass of the class under test. */
    static class TinyRW extends ImageReaderWriterSpi {
        TinyRW(String[] names, String[] suffixes, String[] mime, String plugin,
               boolean stdStream, String nativeStream, String[] extraStream,
               boolean stdImage, String nativeImage, String[] extraImage) {
            super("TinyVendor", "6.1", names, suffixes, mime, plugin,
                  stdStream, nativeStream, nativeStream + "Fmt", extraStream, null,
                  stdImage, nativeImage, nativeImage + "Fmt", extraImage, null);
        }
        public String getDescription(Locale locale) { return "tiny rw spi"; }
    }

    /** no-arg constructor leaves every field null (getFormatNames then throws NPE). */
    static class NoArgRW extends ImageReaderWriterSpi {
        public String getDescription(Locale locale) { return "no-arg rw"; }
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

    static boolean contains(String[] arr, String target) {
        if (arr == null) return false;
        for (String s : arr) if (target.equals(s)) return true;
        return false;
    }

    // inherited accessors observed through the registered PNG reader spi
    static void testPngInheritedAccessors() {
        ImageReaderWriterSpi sp = pngReaderSpi();
        check(contains(sp.getFormatNames(), "png"), "format names contain png");
        check(contains(sp.getFileSuffixes(), "png"), "file suffixes contain png");
        check(contains(sp.getMIMETypes(), "image/png"), "MIME types contain image/png");
        check(sp.getPluginClassName() != null, "plugin class name non-null");
        check(sp.getPluginClassName().indexOf("PNGImageReader") >= 0, "png reader plugin class name");
        check("Oracle Corporation".equals(sp.getVendorName()), "png reader vendor");
        check("1.0".equals(sp.getVersion()), "png reader version");
        check(sp.isStandardImageMetadataFormatSupported() == true,
              "png supports standard image metadata format");
        check(sp.isStandardStreamMetadataFormatSupported() == false,
              "png does not support standard stream metadata format");
        String nativeImage = sp.getNativeImageMetadataFormatName();
        check("javax_imageio_png_1.0".equals(nativeImage), "png native image format name exact value");
        IIOMetadataFormat fmt = sp.getImageMetadataFormat(nativeImage);
        check(fmt != null, "getImageMetadataFormat(native) non-null");
        check(sp.getNativeStreamMetadataFormatName() == null,
              "png native stream metadata format name null");
        check(sp.getExtraImageMetadataFormatNames() == null,
              "png extra image metadata format names null");
        check(sp.getExtraStreamMetadataFormatNames() == null,
              "png extra stream metadata format names null");
    }

    // metadata format name lookup failures
    static void testMetadataFormatNegatives() {
        final ImageReaderWriterSpi sp = pngReaderSpi();
        expectExact(IllegalArgumentException.class,
                    () -> sp.getImageMetadataFormat(null), "getImageMetadataFormat(null)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getImageMetadataFormat("no_such_format"),
                    "getImageMetadataFormat(unknown)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat(null), "getStreamMetadataFormat(null)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat("no_such_format"),
                    "getStreamMetadataFormat(unknown)");
        expectExact(IllegalArgumentException.class,
                    () -> sp.getStreamMetadataFormat("javax_imageio_1.0"),
                    "getStreamMetadataFormat(std) unsupported for png");
    }

    // constructor argument validation
    static void testConstructorValidation() {
        expectExact(IllegalArgumentException.class,
                    () -> new TinyRW(null, null, null, "pkg.Plugin", false, null, null, false, null, null),
                    "names null");
        expectExact(IllegalArgumentException.class,
                    () -> new TinyRW(new String[0], null, null, "pkg.Plugin", false, null, null, false, null, null),
                    "names empty");
        expectExact(IllegalArgumentException.class,
                    () -> new TinyRW(new String[] { "x" }, null, null, null, false, null, null, false, null, null),
                    "pluginClassName null");
    }

    // full-constructor round trip of every stored field
    static void testTinySubclassRoundTrip() {
        TinyRW tiny = new TinyRW(new String[] { "tf" }, new String[] { "tfs" },
                                 new String[] { "image/x-tiny" }, "pkg.TinyPlugin",
                                 true, "tiny_stream_fmt", new String[] { "tiny_stream_extra" },
                                 true, "tiny_image_fmt", new String[] { "tiny_image_extra" });
        check("TinyVendor".equals(tiny.getVendorName()), "tiny vendor round-trips");
        check("6.1".equals(tiny.getVersion()), "tiny version round-trips");
        check(contains(tiny.getFormatNames(), "tf"), "tiny format names round-trip");
        check(contains(tiny.getFileSuffixes(), "tfs"), "tiny suffixes round-trip");
        check(contains(tiny.getMIMETypes(), "image/x-tiny"), "tiny MIME round-trip");
        check("pkg.TinyPlugin".equals(tiny.getPluginClassName()), "tiny plugin class name round-trip");
        check("tiny rw spi".equals(tiny.getDescription(Locale.US)), "tiny description round-trip");
        check("tiny rw spi".equals(tiny.getDescription(null)), "tiny description null locale ok");
        check(tiny.isStandardStreamMetadataFormatSupported() == true, "tiny std stream flag round-trips");
        check("tiny_stream_fmt".equals(tiny.getNativeStreamMetadataFormatName()),
              "tiny native stream format name round-trips");
        check(contains(tiny.getExtraStreamMetadataFormatNames(), "tiny_stream_extra"),
              "tiny extra stream formats round-trip");
        check(tiny.isStandardImageMetadataFormatSupported() == true, "tiny std image flag round-trips");
        check("tiny_image_fmt".equals(tiny.getNativeImageMetadataFormatName()),
              "tiny native image format name round-trips");
        check(contains(tiny.getExtraImageMetadataFormatNames(), "tiny_image_extra"),
              "tiny extra image formats round-trip");

        // null suffixes / null MIME types are permitted and surface as null getters
        TinyRW sparse = new TinyRW(new String[] { "tf" }, null, null, "pkg.TinyPlugin",
                                   false, null, null, false, null, null);
        check(sparse.getFileSuffixes() == null, "null suffixes ctor accepted, getter null");
        check(sparse.getMIMETypes() == null, "null mime ctor accepted, getter null");
        check(sparse.getExtraStreamMetadataFormatNames() == null, "null extra stream accepted");
        check(sparse.getExtraImageMetadataFormatNames() == null, "null extra image accepted");
    }

    // array getters return defensive clones
    static void testCloneSemantics() {
        ImageReaderWriterSpi sp = pngReaderSpi();
        check(sp.getFormatNames() != sp.getFormatNames(), "getFormatNames fresh clone each call");
        check(sp.getFileSuffixes() != sp.getFileSuffixes(), "getFileSuffixes fresh clone each call");
        check(sp.getMIMETypes() != sp.getMIMETypes(), "getMIMETypes fresh clone each call");
        String[] names = sp.getFormatNames();
        String first = names[0];
        names[0] = "CORRUPTED";
        check(sp.getFormatNames()[0].equals(first), "mutating returned array does not corrupt spi");
        String[] suffixes = sp.getFileSuffixes();
        if (suffixes != null && suffixes.length > 0) {
            suffixes[0] = "ZZZ";
        }
        check(contains(sp.getFileSuffixes(), "png"), "suffix clone defensive: png still present");
        // extra-format getters also clone
        TinyRW tiny = new TinyRW(new String[] { "tf" }, null, null, "pkg.TinyPlugin",
                                 false, null, new String[] { "e1" }, false, null, new String[] { "e2" });
        check(tiny.getExtraStreamMetadataFormatNames() != tiny.getExtraStreamMetadataFormatNames(),
              "extra stream formats fresh clone each call");
        check(tiny.getExtraImageMetadataFormatNames() != tiny.getExtraImageMetadataFormatNames(),
              "extra image formats fresh clone each call");
    }

    // no-arg constructor: all getters null; getFormatNames NPEs on names.clone() (recorded behavior)
    static void testNoArgConstructor() {
        NoArgRW sp = new NoArgRW();
        check(sp.getPluginClassName() == null, "no-arg plugin class name null");
        check(sp.getFileSuffixes() == null, "no-arg suffixes null");
        check(sp.getMIMETypes() == null, "no-arg mime types null");
        check(sp.getNativeStreamMetadataFormatName() == null, "no-arg native stream format null");
        check(sp.getNativeImageMetadataFormatName() == null, "no-arg native image format null");
        expectExact(NullPointerException.class, () -> sp.getFormatNames(),
                    "no-arg getFormatNames (names.clone() on null)");
    }
}

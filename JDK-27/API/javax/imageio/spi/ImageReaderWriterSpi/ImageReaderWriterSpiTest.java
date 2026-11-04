/*
 * @test
 * @summary javax.imageio.spi.ImageReaderWriterSpi conformance: white-box structure + black-box behavior.
 * @run main ImageReaderWriterSpiTest
 */

import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.metadata.IIOMetadataFormat;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageReaderWriterSpi;

public class ImageReaderWriterSpiTest {
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

    static class TinyRW extends ImageReaderWriterSpi {
        TinyRW(String[] names, String[] suffixes, String[] mime, String plugin) {
            super("V", "1", names, suffixes, mime, plugin,
                  false, null, null, null, null,
                  false, null, null, null, null);
        }
        public String getDescription(java.util.Locale locale) { return "tiny"; }
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageReaderWriterSpi");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"),
              "extends IIOServiceProvider");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // --- concrete png reader SPI (inherited accessors) ---
        ImageReaderWriterSpi sp = pngReaderSpi();
        check(contains(sp.getFormatNames(), "png"), "format names contain png");
        check(contains(sp.getFileSuffixes(), "png"), "file suffixes contain png");
        check(contains(sp.getMIMETypes(), "image/png"), "MIME types contain image/png");
        check(sp.getPluginClassName() != null, "plugin class name non-null");
        check(sp.getPluginClassName().indexOf("PNGImageReader") >= 0, "png reader plugin class name");

        check(sp.isStandardImageMetadataFormatSupported() == true,
              "png supports standard image metadata format");
        check(sp.isStandardStreamMetadataFormatSupported() == false,
              "png does not support standard stream metadata format");
        String nativeImage = sp.getNativeImageMetadataFormatName();
        check(nativeImage != null, "png native image metadata format name non-null");
        IIOMetadataFormat fmt = sp.getImageMetadataFormat(nativeImage);
        check(fmt != null, "getImageMetadataFormat(native) non-null");
        check(sp.getNativeStreamMetadataFormatName() == null,
              "png native stream metadata format name null");
        check(sp.getExtraImageMetadataFormatNames() == null,
              "png extra image metadata format names null");
        check(sp.getExtraStreamMetadataFormatNames() == null,
              "png extra stream metadata format names null");
        expectThrows(IllegalArgumentException.class,
                     () -> sp.getImageMetadataFormat(null), "getImageMetadataFormat(null)");

        // getFormatNames returns a defensive clone
        String[] a = sp.getFormatNames();
        String first = a[0];
        a[0] = "CORRUPTED";
        check(sp.getFormatNames()[0].equals(first), "getFormatNames returns a clone");

        // --- tiny subclass constructor validation + round-trip ---
        expectThrows(IllegalArgumentException.class,
                     () -> new TinyRW(null, null, null, "pkg.Plugin"), "names null");
        expectThrows(IllegalArgumentException.class,
                     () -> new TinyRW(new String[0], null, null, "pkg.Plugin"), "names empty");
        expectThrows(IllegalArgumentException.class,
                     () -> new TinyRW(new String[] { "x" }, null, null, null), "pluginClassName null");

        TinyRW tiny = new TinyRW(new String[] { "x" }, new String[] { "xx" },
                                 new String[] { "image/x" }, "pkg.Plugin");
        check(contains(tiny.getFormatNames(), "x"), "tiny format names round-trip");
        check(contains(tiny.getFileSuffixes(), "xx"), "tiny suffixes round-trip");
        check(contains(tiny.getMIMETypes(), "image/x"), "tiny MIME round-trip");
        check("pkg.Plugin".equals(tiny.getPluginClassName()), "tiny plugin class name round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

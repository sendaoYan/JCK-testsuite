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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.imageio.spi.ImageReaderSpi conformance: white-box structure + black-box behavior.
 * @run main ImageReaderSpiTest
 */

import java.io.ByteArrayInputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.ImageIO;
import javax.imageio.ImageReader;
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

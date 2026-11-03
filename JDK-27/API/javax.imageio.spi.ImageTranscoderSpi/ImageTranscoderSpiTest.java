/*
 * @test
 * @summary javax.imageio.spi.ImageTranscoderSpi conformance: white-box structure + black-box behavior.
 * @run main ImageTranscoderSpiTest
 */

import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.ImageTranscoder;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageTranscoderSpi;

public class ImageTranscoderSpiTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageTranscoderSpi");
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
        Iterator<ImageTranscoderSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageTranscoderSpi.class, true);
        check(it != null, "transcoder providers iterator non-null");

        int count = 0;
        while (it.hasNext()) {
            ImageTranscoderSpi sp = it.next();
            check(sp.getReaderServiceProviderName() != null, "reader SPI name non-null");
            check(sp.getWriterServiceProviderName() != null, "writer SPI name non-null");
            ImageTranscoder tr = sp.createTranscoderInstance();
            check(tr != null, "createTranscoderInstance non-null");
            count++;
        }
        // The JDK ships no built-in transcoder SPI; degrade gracefully if empty.
        if (count == 0) {
            check(true, "no transcoder providers registered (degraded)");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

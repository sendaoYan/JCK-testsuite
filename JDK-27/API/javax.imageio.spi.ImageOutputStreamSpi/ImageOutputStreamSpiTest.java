/*
 * @test
 * @summary javax.imageio.spi.ImageOutputStreamSpi conformance: white-box structure + black-box behavior.
 * @run main ImageOutputStreamSpiTest
 */

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.stream.ImageOutputStream;

public class ImageOutputStreamSpiTest {
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

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageOutputStreamSpi");
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
        Iterator<ImageOutputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageOutputStreamSpi.class, false);
        check(it != null, "providers iterator non-null");
        ImageOutputStreamSpi outputStreamSpi = null;
        int count = 0;
        while (it.hasNext()) {
            ImageOutputStreamSpi sp = it.next();
            check(sp.getOutputClass() != null, "getOutputClass non-null");
            sp.canUseCacheFile();
            sp.needsCacheFile();
            check(true, "cache-file queries return normally");
            if (sp.getOutputClass() == OutputStream.class) outputStreamSpi = sp;
            count++;
        }
        check(count > 0, "at least one ImageOutputStreamSpi registered");
        check(outputStreamSpi != null, "OutputStream provider present");

        check(outputStreamSpi.getOutputClass() == OutputStream.class,
              "getOutputClass == OutputStream.class");
        check(outputStreamSpi.canUseCacheFile() == true, "OutputStream SPI can use cache file");
        check(outputStreamSpi.needsCacheFile() == false, "OutputStream SPI does not need cache file");

        // 3-arg overload: memory cache (no temp file)
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageOutputStream ios = outputStreamSpi.createOutputStreamInstance(bos, false, null);
        check(ios != null, "createOutputStreamInstance 3-arg non-null");
        ios.write(new byte[] { 6, 7, 8 });
        ios.flush();
        ios.close();
        check(java.util.Arrays.equals(bos.toByteArray(), new byte[] { 6, 7, 8 }),
              "round-trip bytes via 3-arg overload");

        // 1-arg convenience overload
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        ImageOutputStream ios2 = outputStreamSpi.createOutputStreamInstance(bos2);
        check(ios2 != null, "createOutputStreamInstance 1-arg non-null");
        ios2.close();

        // wrong output type rejected
        final ImageOutputStreamSpi osp = outputStreamSpi;
        expectThrows(IllegalArgumentException.class,
                     () -> osp.createOutputStreamInstance("not-a-stream", false, null),
                     "wrong output type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

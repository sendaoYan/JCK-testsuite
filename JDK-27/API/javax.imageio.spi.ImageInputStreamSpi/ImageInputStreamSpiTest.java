/*
 * @test
 * @summary javax.imageio.spi.ImageInputStreamSpi conformance: white-box structure + black-box behavior.
 * @run main ImageInputStreamSpiTest
 */

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.stream.ImageInputStream;

public class ImageInputStreamSpiTest {
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
        Class<?> c = Class.forName("javax.imageio.spi.ImageInputStreamSpi");
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
        Iterator<ImageInputStreamSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageInputStreamSpi.class, false);
        check(it != null, "providers iterator non-null");
        ImageInputStreamSpi inputStreamSpi = null;
        int count = 0;
        while (it.hasNext()) {
            ImageInputStreamSpi sp = it.next();
            check(sp.getInputClass() != null, "getInputClass non-null");
            sp.canUseCacheFile();
            sp.needsCacheFile();
            check(true, "cache-file queries return normally");
            if (sp.getInputClass() == InputStream.class) inputStreamSpi = sp;
            count++;
        }
        check(count > 0, "at least one ImageInputStreamSpi registered");
        check(inputStreamSpi != null, "InputStream provider present");

        // input class round-trip
        check(inputStreamSpi.getInputClass() == InputStream.class, "getInputClass == InputStream.class");
        check(inputStreamSpi.canUseCacheFile() == true, "InputStream SPI can use cache file");
        check(inputStreamSpi.needsCacheFile() == false, "InputStream SPI does not need cache file");

        byte[] data = new byte[] { 1, 2, 3, 4, 5 };
        // 3-arg overload: memory cache (no temp file)
        ImageInputStream iis =
            inputStreamSpi.createInputStreamInstance(new ByteArrayInputStream(data), false, null);
        check(iis != null, "createInputStreamInstance 3-arg non-null");
        byte[] buf = new byte[data.length];
        iis.readFully(buf);
        check(java.util.Arrays.equals(buf, data), "round-trip bytes via 3-arg overload");
        check(iis.read() == -1, "stream exhausted after reading all bytes");
        iis.close();

        // 1-arg convenience overload
        ImageInputStream iis2 =
            inputStreamSpi.createInputStreamInstance(new ByteArrayInputStream(data));
        check(iis2 != null, "createInputStreamInstance 1-arg non-null");
        iis2.close();

        // wrong input type rejected
        final ImageInputStreamSpi isp = inputStreamSpi;
        expectThrows(IllegalArgumentException.class,
                     () -> isp.createInputStreamInstance("not-a-stream", false, null),
                     "wrong input type");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

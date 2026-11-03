/*
 * @test
 * @summary javax.imageio.spi.IIORegistry conformance: white-box structure + black-box behavior.
 * @run main IIORegistryTest
 */

import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;

public class IIORegistryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.IIORegistry");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isFinal(mods), "final");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null &&
              c.getSuperclass().getName().equals("javax.imageio.spi.ServiceRegistry"),
              "extends ServiceRegistry");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIORegistry r1 = IIORegistry.getDefaultInstance();
        IIORegistry r2 = IIORegistry.getDefaultInstance();
        check(r1 == r2, "getDefaultInstance returns same singleton");

        // categories
        Set<String> categories = new HashSet<String>();
        for (Iterator<Class<?>> it = r1.getCategories(); it.hasNext(); ) {
            categories.add(it.next().getName());
        }
        check(categories.size() == 5, "five default categories");
        check(categories.contains(ImageReaderSpi.class.getName()), "has ImageReaderSpi category");
        check(categories.contains(ImageWriterSpi.class.getName()), "has ImageWriterSpi category");
        check(categories.contains(ImageTranscoderSpi.class.getName()), "has ImageTranscoderSpi category");
        check(categories.contains(ImageInputStreamSpi.class.getName()), "has ImageInputStreamSpi category");
        check(categories.contains(ImageOutputStreamSpi.class.getName()), "has ImageOutputStreamSpi category");

        // reader providers are present and correctly typed
        Iterator<ImageReaderSpi> readers = r1.getServiceProviders(ImageReaderSpi.class, true);
        check(readers != null, "reader providers iterator non-null");
        int readerCount = 0;
        boolean sawPng = false;
        while (readers.hasNext()) {
            ImageReaderSpi sp = readers.next();
            check(sp != null, "reader provider non-null");
            String[] names = sp.getFormatNames();
            if (names != null) {
                for (String n : names) {
                    if ("png".equalsIgnoreCase(n)) sawPng = true;
                }
            }
            readerCount++;
        }
        check(readerCount > 0, "at least one reader provider registered");
        check(sawPng, "png reader provider present");

        // stream providers are present too
        Iterator<ImageInputStreamSpi> inputs = r1.getServiceProviders(ImageInputStreamSpi.class, true);
        check(inputs != null, "input stream providers iterator non-null");
        int inputCount = 0;
        while (inputs.hasNext()) { check(inputs.next() != null, "input provider non-null"); inputCount++; }
        check(inputCount > 0, "at least one ImageInputStreamSpi registered");

        // getServiceProviderByClass round-trips the concrete leaf class
        Iterator<ImageReaderSpi> again = r1.getServiceProviders(ImageReaderSpi.class, false);
        check(again.hasNext(), "a reader provider exists");
        ImageReaderSpi one = again.next();
        Object byClass = r1.getServiceProviderByClass(one.getClass());
        check(byClass == one, "getServiceProviderByClass returns same instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

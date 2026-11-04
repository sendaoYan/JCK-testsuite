/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Iterator;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;

public class FilterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasFormatIgnoreCase(ImageReaderSpi sp, String target) {
        String[] names = sp.getFormatNames();
        if (names == null) return false;
        for (String n : names) if (target.equalsIgnoreCase(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ServiceRegistry$Filter");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(c.getSuperclass() == null, "no superclass");
        Method[] methods = c.getDeclaredMethods();
        check(methods.length == 1, "single abstract method");
        check(methods[0].getName().equals("filter"), "method name filter");
        check(methods[0].getReturnType() == boolean.class, "filter returns boolean");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        IIORegistry reg = IIORegistry.getDefaultInstance();

        // direct functional use
        ServiceRegistry.Filter pngFilter = (Object provider) -> {
            return (provider instanceof ImageReaderSpi) &&
                   hasFormatIgnoreCase((ImageReaderSpi) provider, "png");
        };
        Iterator<ImageReaderSpi> all = reg.getServiceProviders(ImageReaderSpi.class, false);
        ImageReaderSpi pngSpi = null;
        int allCount = 0;
        while (all.hasNext()) {
            ImageReaderSpi sp = all.next();
            allCount++;
            if (hasFormatIgnoreCase(sp, "png")) pngSpi = sp;
        }
        check(pngSpi != null, "a png reader SPI exists");
        check(pngFilter.filter(pngSpi), "filter accepts png provider");
        check(!pngFilter.filter("not-a-provider"), "filter rejects non-provider");

        // filtering getServiceProviders(Class, Filter, boolean)
        Iterator<ImageReaderSpi> filtered =
            reg.getServiceProviders(ImageReaderSpi.class, pngFilter, false);
        check(filtered != null, "filtered iterator non-null");
        int filteredCount = 0;
        while (filtered.hasNext()) {
            ImageReaderSpi sp = filtered.next();
            check(hasFormatIgnoreCase(sp, "png"), "filtered provider is png");
            filteredCount++;
        }
        check(filteredCount >= 1, "filter returns at least one png provider");
        check(filteredCount < allCount, "filter actually excluded non-png providers");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

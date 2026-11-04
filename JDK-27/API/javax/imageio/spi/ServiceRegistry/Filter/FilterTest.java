/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry.Filter conformance: white-box structure + black-box behavior.
 * @run main FilterTest
 */

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

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
        testSignatureDetails();
        testInvocationSemantics();
        testOrderingFlagIrrelevant();
        testNullAndThrowingFilter();
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

    // exact parameter/exception signature of the single abstract method
    static void testSignatureDetails() throws Exception {
        Method m = ServiceRegistry.Filter.class.getDeclaredMethods()[0];
        Class<?>[] params = m.getParameterTypes();
        check(params.length == 1 && params[0] == Object.class, "single Object parameter");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
              "filter is public abstract");
        check(m.getExceptionTypes().length == 0, "filter declares no checked exceptions");
    }

    // distinct provider classes: the registry keys providers by leaf class
    static class TinyA extends ImageInputStreamSpi {
        public String getDescription(Locale l) { return "A"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
    }
    static class TinyB extends ImageInputStreamSpi {
        public String getDescription(Locale l) { return "B"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
    }
    static class TinyC extends ImageInputStreamSpi {
        public String getDescription(Locale l) { return "C"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
    }

    static ServiceRegistry inputRegistry() {
        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageInputStreamSpi.class);
        return new ServiceRegistry(cats.iterator());
    }

    static <T> List<T> drain(Iterator<T> it) {
        List<T> l = new ArrayList<T>();
        while (it.hasNext()) l.add(it.next());
        return l;
    }

    // the filter runs once per registered provider; the first call happens
    // eagerly inside getServiceProviders (recorded behavior: one-element prefetch)
    static void testInvocationSemantics() {
        ServiceRegistry reg = inputRegistry();
        final TinyA a = new TinyA(); final TinyB b = new TinyB(); final TinyC c = new TinyC();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.registerServiceProvider(c, ImageInputStreamSpi.class);

        final Map<Object,Integer> counts = new IdentityHashMap<Object,Integer>();
        ServiceRegistry.Filter keepB = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) {
                Integer n = counts.get(provider);
                counts.put(provider, n == null ? 1 : n + 1);
                return provider == b;
            }
        };
        Iterator<ImageInputStreamSpi> it =
            reg.getServiceProviders(ImageInputStreamSpi.class, keepB, false);
        check(!counts.isEmpty(), "filter already invoked by getServiceProviders (eager prefetch)");
        while (it.hasNext()) it.next();
        check(counts.size() == 3, "filter saw every registered provider");
        check(counts.get(a) == 1 && counts.get(b) == 1 && counts.get(c) == 1,
              "each provider presented to the filter exactly once");

        List<ImageInputStreamSpi> kept =
            drain(reg.getServiceProviders(ImageInputStreamSpi.class, keepB, false));
        check(kept.size() == 1 && kept.get(0) == b, "only the accepted provider is iterated");
    }

    // useOrdering=true/false yields the same filtered membership
    static void testOrderingFlagIrrelevant() {
        ServiceRegistry reg = inputRegistry();
        TinyA a = new TinyA(); TinyB b = new TinyB(); TinyC c = new TinyC();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.registerServiceProvider(c, ImageInputStreamSpi.class);
        ServiceRegistry.Filter acceptAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return true; }
        };
        List<ImageInputStreamSpi> unordered =
            drain(reg.getServiceProviders(ImageInputStreamSpi.class, acceptAll, false));
        check(unordered.size() == 3, "useOrdering=false keeps every provider");
        check(reg.setOrdering(ImageInputStreamSpi.class, a, b), "order a before b");
        List<ImageInputStreamSpi> ordered =
            drain(reg.getServiceProviders(ImageInputStreamSpi.class, acceptAll, true));
        check(ordered.size() == 3, "useOrdering=true keeps every provider");
        check(new HashSet<ImageInputStreamSpi>(unordered).equals(new HashSet<ImageInputStreamSpi>(ordered)),
              "ordering flag does not change filtered membership");
        check(ordered.indexOf(a) < ordered.indexOf(b), "useOrdering=true respects the partial order");
        reg.unsetOrdering(ImageInputStreamSpi.class, a, b);
    }

    // null filter rejected with NPE; a throwing filter's exception is relayed
    // as the exact same instance from getServiceProviders itself
    static void testNullAndThrowingFilter() {
        ServiceRegistry reg = inputRegistry();
        reg.registerServiceProvider(new TinyA(), ImageInputStreamSpi.class);
        expectExact(NullPointerException.class,
                    () -> reg.getServiceProviders(ImageInputStreamSpi.class, null, false),
                    "null filter");
        expectExact(NullPointerException.class,
                    () -> reg.getServiceProviders(ImageInputStreamSpi.class, null, true),
                    "null filter (useOrdering=true)");

        final RuntimeException boom = new IllegalStateException("relay-me");
        ServiceRegistry.Filter throwing = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { throw boom; }
        };
        Throwable caught = null;
        try { reg.getServiceProviders(ImageInputStreamSpi.class, throwing, false); }
        catch (Throwable t) { caught = t; }
        check(caught == boom, "filter exception relayed as the exact same instance");

        ServiceRegistry.Filter rejectAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return false; }
        };
        check(drain(reg.getServiceProviders(ImageInputStreamSpi.class, rejectAll, false)).isEmpty(),
              "reject-all filter yields empty iteration");
    }
}

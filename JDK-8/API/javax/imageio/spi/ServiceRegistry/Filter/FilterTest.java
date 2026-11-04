/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry.Filter conformance: interface hierarchy and methods.
 * @run main FilterTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class FilterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ServiceRegistry$Filter");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("filter"), "method filter");
        testWhiteBoxSignatures();
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

    // exact single-method signature: boolean filter(Object provider)
    static void testWhiteBoxSignatures() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ServiceRegistry$Filter");
        Method[] methods = c.getDeclaredMethods();
        check(methods.length == 1, "exactly one declared method (functional interface)");
        Method m = methods[0];
        check(m.getName().equals("filter"), "method name filter");
        check(m.getReturnType() == boolean.class, "filter returns boolean");
        Class<?>[] params = m.getParameterTypes();
        check(params.length == 1 && params[0] == Object.class, "single Object parameter");
        check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
              "filter is public abstract");
        check(m.getExceptionTypes().length == 0, "filter declares no checked exceptions");
        // usable directly as a lambda target (SAM)
        ServiceRegistry.Filter f = (Object provider) -> provider != null;
        check(f.filter(new Object()), "lambda implements the SAM");
    }

    // distinct provider classes: the registry keys providers by leaf class
    static class InA extends ImageInputStreamSpi {
        public String getDescription(Locale l) { return "A"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
    }
    static class InB extends ImageInputStreamSpi {
        public String getDescription(Locale l) { return "B"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
    }
    static class InC extends ImageInputStreamSpi {
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

    // the filter runs once per registered provider; the first call happens
    // eagerly inside getServiceProviders (recorded behavior: one-element prefetch)
    static void testInvocationSemantics() {
        ServiceRegistry reg = inputRegistry();
        final InA a = new InA(); final InB b = new InB(); final InC c = new InC();
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

    static <T> List<T> drain(Iterator<T> it) {
        List<T> l = new ArrayList<T>();
        while (it.hasNext()) l.add(it.next());
        return l;
    }

    // useOrdering=true/false yields the same filtered membership
    static void testOrderingFlagIrrelevant() {
        ServiceRegistry reg = inputRegistry();
        InA a = new InA(); InB b = new InB(); InC c = new InC();
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
        reg.registerServiceProvider(new InA(), ImageInputStreamSpi.class);
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

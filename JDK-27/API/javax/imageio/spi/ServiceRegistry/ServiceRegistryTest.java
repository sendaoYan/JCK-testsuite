/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry conformance: white-box structure + black-box behavior.
 * @run main ServiceRegistryTest
 */

import java.io.File;
import java.io.InputStream;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class ServiceRegistryTest {
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

    static class TinyInputSpiA extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        TinyInputSpiA() { super("VendorA", "1.0", InputStream.class); }
        public String getDescription(Locale l) { return "A"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File dir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; }
    }
    static class TinyInputSpiB extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        TinyInputSpiB() { super("VendorB", "2.0", InputStream.class); }
        public String getDescription(Locale l) { return "B"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File dir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; }
    }

    static ServiceRegistry newRegistry() {
        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageInputStreamSpi.class);
        return new ServiceRegistry(cats.iterator());
    }

    static List<ImageInputStreamSpi> collect(Iterator<ImageInputStreamSpi> it) {
        List<ImageInputStreamSpi> out = new ArrayList<ImageInputStreamSpi>();
        while (it.hasNext()) out.add(it.next());
        return out;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ServiceRegistry");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(!Modifier.isAbstract(mods), "not abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"),
              "superclass Object");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        ServiceRegistry reg = newRegistry();

        // categories
        Set<String> cats = new HashSet<String>();
        for (Iterator<Class<?>> it = reg.getCategories(); it.hasNext(); ) {
            cats.add(it.next().getName());
        }
        check(cats.size() == 1, "single category");
        check(cats.contains(ImageInputStreamSpi.class.getName()), "category is ImageInputStreamSpi");

        // register + callbacks
        TinyInputSpiA a = new TinyInputSpiA();
        TinyInputSpiB b = new TinyInputSpiB();
        check(reg.registerServiceProvider(a, ImageInputStreamSpi.class), "register A returns true");
        check(a.regCalls == 1, "A onRegistration called once");
        check(reg.registerServiceProvider(b, ImageInputStreamSpi.class), "register B returns true");
        check(b.regCalls == 1, "B onRegistration called once");

        // contains / getServiceProviderByClass
        check(reg.contains(a), "contains A");
        check(reg.contains(b), "contains B");
        check(!reg.contains(new Object()), "contains unrelated false");
        check(reg.getServiceProviderByClass(TinyInputSpiA.class) == a, "byClass A identity");
        check(reg.getServiceProviderByClass(TinyInputSpiB.class) == b, "byClass B identity");

        // unordered providers
        List<ImageInputStreamSpi> all = collect(reg.getServiceProviders(ImageInputStreamSpi.class, false));
        check(all.size() == 2, "two providers unordered");
        check(all.contains(a) && all.contains(b), "unordered contains both");

        // pairwise ordering
        check(reg.setOrdering(ImageInputStreamSpi.class, a, b), "setOrdering A before B true");
        List<ImageInputStreamSpi> ordered = collect(reg.getServiceProviders(ImageInputStreamSpi.class, true));
        check(ordered.size() == 2, "ordered size 2");
        check(ordered.indexOf(a) >= 0 && ordered.indexOf(b) >= 0, "ordered contains both");
        check(ordered.indexOf(a) < ordered.indexOf(b), "A ordered before B");
        check(reg.unsetOrdering(ImageInputStreamSpi.class, a, b), "unsetOrdering true");
        check(!reg.unsetOrdering(ImageInputStreamSpi.class, a, b), "unsetOrdering again false");

        // ordering with unregistered provider does nothing
        TinyInputSpiB notRegistered = new TinyInputSpiB();
        check(!reg.setOrdering(ImageInputStreamSpi.class, a, notRegistered),
              "setOrdering with unregistered provider false");

        // same-provider ordering rejected
        expectThrows(IllegalArgumentException.class,
                     () -> reg.setOrdering(ImageInputStreamSpi.class, a, a),
                     "setOrdering same provider");

        // re-registering same leaf class replaces the old instance
        TinyInputSpiA a2 = new TinyInputSpiA();
        check(!reg.registerServiceProvider(a2, ImageInputStreamSpi.class), "re-register same leaf false");
        check(a.deregCalls == 1, "old A deregistered on replace");
        check(a2.regCalls == 1, "new A registered on replace");
        check(!reg.contains(a), "old A no longer contained");
        check(reg.contains(a2), "new A contained");

        // deregister by category
        check(reg.deregisterServiceProvider(a2, ImageInputStreamSpi.class), "deregister a2 true");
        check(!reg.deregisterServiceProvider(a2, ImageInputStreamSpi.class), "deregister a2 again false");
        check(!reg.contains(a2), "a2 no longer contained");

        // Object overloads
        TinyInputSpiA a3 = new TinyInputSpiA();
        reg.registerServiceProvider(a3, ImageInputStreamSpi.class);
        reg.deregisterServiceProvider(a3);
        check(!reg.contains(a3), "Object overload deregister");

        TinyInputSpiA a4 = new TinyInputSpiA();
        reg.registerServiceProvider(a4);
        check(reg.contains(a4), "Object overload register");

        TinyInputSpiB b4 = new TinyInputSpiB();
        List<ImageInputStreamSpi> batch = new ArrayList<ImageInputStreamSpi>();
        batch.add(b4);
        reg.registerServiceProviders(batch.iterator());
        check(reg.contains(b4), "registerServiceProviders registers provider");

        // deregisterAll(category) / deregisterAll()
        reg.deregisterAll(ImageInputStreamSpi.class);
        check(!reg.contains(a4) && !reg.contains(b4) && !reg.contains(b),
              "deregisterAll(category) empties category");
        TinyInputSpiA a5 = new TinyInputSpiA();
        reg.registerServiceProvider(a5, ImageInputStreamSpi.class);
        reg.deregisterAll();
        check(!reg.contains(a5), "deregisterAll empties all categories");

        // constructor validation
        expectThrows(IllegalArgumentException.class, () -> new ServiceRegistry(null),
                     "constructor null categories");
        List<Class<?>> bad = new ArrayList<Class<?>>();
        bad.add(String.class);
        expectThrows(IllegalArgumentException.class, () -> new ServiceRegistry(bad.iterator()),
                     "disallowed category String.class");

        // unknown category / null provider errors
        expectThrows(IllegalArgumentException.class,
                     () -> reg.getServiceProviders(ImageWriterSpi.class, false),
                     "unknown category getServiceProviders");
        expectThrows(IllegalArgumentException.class,
                     () -> reg.deregisterAll(ImageWriterSpi.class),
                     "unknown category deregisterAll");
        expectThrows(IllegalArgumentException.class,
                     () -> reg.registerServiceProvider(null, ImageInputStreamSpi.class),
                     "null provider register");
        expectThrows(IllegalArgumentException.class,
                     () -> reg.contains(null),
                     "contains(null)");

        // static lookupProviders
        Iterator<ImageInputStreamSpi> lookup = ServiceRegistry.lookupProviders(ImageInputStreamSpi.class);
        check(lookup != null, "lookupProviders(category) non-null");
        Iterator<ImageInputStreamSpi> lookup2 =
            ServiceRegistry.lookupProviders(ImageInputStreamSpi.class, null);
        check(lookup2 != null, "lookupProviders(category, null loader) non-null");
        expectThrows(IllegalArgumentException.class,
                     () -> ServiceRegistry.lookupProviders((Class<ImageInputStreamSpi>) null),
                     "lookupProviders(null)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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
        testIteratorRemoveSemantics();
        testFailFastSemantics();
        testFilterMechanics();
        testOrderingEdgeCases();
        testQueryEdgeCases();
        testNoCategoryRegistration();
        testVersionSpecificBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void expectExact(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        Throwable caught = null;
        try { r.run(); } catch (Throwable t) { caught = t; }
        check(caught != null, msg + ": nothing thrown, expected " + expected.getSimpleName());
        if (caught == null) return;
        check(caught.getClass() == expected,
              msg + ": expected " + expected.getSimpleName() + " but got "
              + caught.getClass().getName() + " (" + caught.getMessage() + ")");
    }

    // distinct provider classes: the registry keeps at most one instance per leaf class
    static class InC extends BaseInSpi { }
    static class BaseInSpi extends ImageInputStreamSpi {
        int regCalls2, deregCalls2;
        BaseInSpi() { super("RegVendor", "1.0", InputStream.class); }
        public String getDescription(Locale l) { return "in"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls2++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls2++; }
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

    static int providerCount(ServiceRegistry reg) {
        return drain(reg.getServiceProviders(ImageInputStreamSpi.class, false)).size();
    }

    // iterator remove(): before next() -> IllegalStateException; after next() it
    // really mutates the registry (getCategories removes the category itself)
    static void testIteratorRemoveSemantics() {
        ServiceRegistry reg = inputRegistry();
        TinyInputSpiA a = new TinyInputSpiA();
        TinyInputSpiB b = new TinyInputSpiB();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        Iterator<ImageInputStreamSpi> pit = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        expectExact(IllegalStateException.class, () -> pit.remove(),
                    "providers iterator remove before next");
        ImageInputStreamSpi first = pit.next();
        pit.remove();
        check(!reg.contains(first), "providers iterator remove after next deregisters provider");
        check(providerCount(reg) == 1, "one provider left after iterator remove");
        // recorded actual behavior: iterator remove() bypasses the onDeregistration callback
        check((a.deregCalls + b.deregCalls) == 0,
              "iterator remove bypasses onDeregistration (recorded behavior)");

        // categories iterator: remove before next -> IllegalStateException
        ServiceRegistry reg2 = inputRegistry();
        reg2.registerServiceProvider(new TinyInputSpiA(), ImageInputStreamSpi.class);
        Iterator<Class<?>> cit = reg2.getCategories();
        expectExact(IllegalStateException.class, () -> cit.remove(),
                    "categories iterator remove before next");
        cit.next();
        cit.remove();
        expectExact(IllegalArgumentException.class,
                    () -> reg2.getServiceProviders(ImageInputStreamSpi.class, false),
                    "category removed through live categories iterator");
    }

    // provider iterators are fail-fast live views: structural modification of the
    // registry while iterating throws ConcurrentModificationException (recorded behavior)
    static void testFailFastSemantics() {
        ServiceRegistry reg = inputRegistry();
        TinyInputSpiA a = new TinyInputSpiA();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);

        Iterator<ImageInputStreamSpi> it = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        reg.registerServiceProvider(new TinyInputSpiB(), ImageInputStreamSpi.class);
        expectExact(java.util.ConcurrentModificationException.class,
                    () -> drain(it), "iterator next after registration");

        Iterator<ImageInputStreamSpi> it2 = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        reg.deregisterAll(ImageInputStreamSpi.class);
        expectExact(java.util.ConcurrentModificationException.class,
                    () -> drain(it2), "iterator next after deregisterAll");

        reg.registerServiceProvider(new InC(), ImageInputStreamSpi.class);
        Iterator<ImageInputStreamSpi> it3 = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        check(drain(it3).size() == 1, "unmodified iterator drains normally");
    }

    // Filter: per-provider invocation, membership, eager prefetch,
    // null filter NPE, and exception relay of the exact instance
    static void testFilterMechanics() {
        ServiceRegistry reg = inputRegistry();
        TinyInputSpiA a = new TinyInputSpiA();
        TinyInputSpiB b = new TinyInputSpiB();
        InC c = new InC();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.registerServiceProvider(c, ImageInputStreamSpi.class);

        final List<Object> seenByFilter = new ArrayList<Object>();
        ServiceRegistry.Filter keepB = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { seenByFilter.add(provider); return provider == b; }
        };
        // the iterator prefetches eagerly: the filter has already run when
        // getServiceProviders returns
        Iterator<ImageInputStreamSpi> it = reg.getServiceProviders(ImageInputStreamSpi.class, keepB, false);
        check(seenByFilter.size() >= 1, "filter already invoked by getServiceProviders");
        List<ImageInputStreamSpi> kept = drain(it);
        check(kept.size() == 1 && kept.get(0) == b, "only the accepted provider survives");
        check(seenByFilter.contains(a) && seenByFilter.contains(b) && seenByFilter.contains(c),
              "after draining, filter saw every registered provider");

        ServiceRegistry.Filter rejectAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return false; }
        };
        check(drain(reg.getServiceProviders(ImageInputStreamSpi.class, rejectAll, true)).isEmpty(),
              "reject-all filter yields empty iteration");

        ServiceRegistry.Filter acceptAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return true; }
        };
        check(drain(reg.getServiceProviders(ImageInputStreamSpi.class, acceptAll, false)).size() == 3,
              "accept-all filter keeps every provider");

        expectExact(NullPointerException.class,
                    () -> reg.getServiceProviders(ImageInputStreamSpi.class, null, false),
                    "null filter");

        // exception thrown by the filter is relayed from getServiceProviders itself
        final RuntimeException boom = new IllegalStateException("relay-me");
        ServiceRegistry.Filter throwing = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { throw boom; }
        };
        Throwable caught = null;
        try { reg.getServiceProviders(ImageInputStreamSpi.class, throwing, false); }
        catch (Throwable t) { caught = t; }
        check(caught == boom, "filter exception relayed as the exact same instance");
    }

    // setOrdering/unsetOrdering: reversal, transitivity, category/provider edge cases
    static void testOrderingEdgeCases() {
        ServiceRegistry reg = inputRegistry();
        TinyInputSpiA a = new TinyInputSpiA();
        TinyInputSpiB b = new TinyInputSpiB();
        InC c = new InC();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.registerServiceProvider(c, ImageInputStreamSpi.class);

        // setting the reverse direction replaces the original edge (bug 4936445 semantics)
        check(reg.setOrdering(ImageInputStreamSpi.class, a, b), "set(a,b) returns true");
        List<ImageInputStreamSpi> o1 = drain(reg.getServiceProviders(ImageInputStreamSpi.class, true));
        check(o1.indexOf(a) < o1.indexOf(b), "a ordered before b");
        check(reg.setOrdering(ImageInputStreamSpi.class, b, a), "set(b,a) reverse returns true");
        List<ImageInputStreamSpi> o2 = drain(reg.getServiceProviders(ImageInputStreamSpi.class, true));
        check(o2.indexOf(b) < o2.indexOf(a), "reverse ordering flipped the iteration");
        check(reg.unsetOrdering(ImageInputStreamSpi.class, a, b), "unset(a,b) removes current edge");
        check(!reg.unsetOrdering(ImageInputStreamSpi.class, a, b), "unset(a,b) again false");
        check(!reg.unsetOrdering(ImageInputStreamSpi.class, b, a), "unset(b,a) also false");

        // transitivity: a<b and b<c implies a<b<c
        check(reg.setOrdering(ImageInputStreamSpi.class, a, b), "transitive edge 1");
        check(reg.setOrdering(ImageInputStreamSpi.class, b, c), "transitive edge 2");
        List<ImageInputStreamSpi> o3 = drain(reg.getServiceProviders(ImageInputStreamSpi.class, true));
        check(o3.indexOf(a) < o3.indexOf(b) && o3.indexOf(b) < o3.indexOf(c),
              "ordered iteration respects transitive order");
        reg.unsetOrdering(ImageInputStreamSpi.class, a, b);
        reg.unsetOrdering(ImageInputStreamSpi.class, b, c);

        // unsetOrdering of a never-ordered pair returns false (no exception)
        check(!reg.unsetOrdering(ImageInputStreamSpi.class, a, b), "unset never-set pair false");

        // ordering with an unregistered provider returns false
        TinyInputSpiA unregistered = new TinyInputSpiA();
        check(!reg.setOrdering(ImageInputStreamSpi.class, unregistered, b),
              "setOrdering with unregistered provider false");

        // unknown category with distinct providers -> IllegalArgumentException
        expectExact(IllegalArgumentException.class, () -> {
            ServiceRegistry raw = reg;
            raw.setOrdering((Class) ImageWriterSpi.class, (Object) a, (Object) b);
        }, "setOrdering unknown category");
        expectExact(IllegalArgumentException.class, () -> {
            ServiceRegistry raw = reg;
            raw.unsetOrdering((Class) ImageWriterSpi.class, (Object) a, (Object) b);
        }, "unsetOrdering unknown category");
    }

    // getServiceProviderByClass / contains / raw-type category mismatch edges
    static void testQueryEdgeCases() {
        ServiceRegistry reg = inputRegistry();
        TinyInputSpiA a = new TinyInputSpiA();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        expectExact(IllegalArgumentException.class, () -> reg.getServiceProviderByClass(null),
                    "getServiceProviderByClass(null)");
        check(reg.getServiceProviderByClass(String.class) == null,
              "non-category class lookup returns null");
        check(reg.getServiceProviderByClass(TinyInputSpiB.class) == null,
              "unregistered spi class lookup returns null");

        // registering a provider under a category it does not implement (raw types)
        // fails with ClassCastException
        ServiceRegistry raw = reg;
        Throwable caught = null;
        try {
            raw.registerServiceProvider((Object) new TinyOutSpi(), (Class) ImageInputStreamSpi.class);
        } catch (Throwable t) { caught = t; }
        check(caught != null && caught.getClass() == ClassCastException.class,
              "register under wrong category (raw) throws ClassCastException, got " + caught);
        check(providerCount(reg) == 1, "failed registration left state unchanged");
        expectExact(IllegalArgumentException.class, () -> new ServiceRegistry(null),
                    "new ServiceRegistry(null)");
    }

    static class TinyOutSpi extends javax.imageio.spi.ImageOutputStreamSpi {
        TinyOutSpi() { super("RegVendor", "1.0", java.io.OutputStream.class); }
        public String getDescription(Locale l) { return "out"; }
        public javax.imageio.stream.ImageOutputStream createOutputStreamInstance(
                Object out, boolean useCache, File cacheDir) { return null; }
    }

    // providers that implement none of the registry's categories register nowhere;
    // duplicate registration re-fires the callbacks
    static void testNoCategoryRegistration() {
        ServiceRegistry reg = inputRegistry();
        Object alien = new Object();
        reg.registerServiceProvider(alien);
        check(!reg.contains(alien), "no-category object not contained");
        check(providerCount(reg) == 0, "no-category object did not change providers");
        reg.deregisterServiceProvider(alien);
        check(true, "deregister of no-category object is a no-op");
        ServiceRegistry empty = new ServiceRegistry(new ArrayList<Class<?>>().iterator());
        check(empty != null && !empty.getCategories().hasNext(), "empty categories registry usable");

        // duplicate registration of the same instance: returns false but actually
        // deregisters and re-registers the instance (recorded callback counts)
        TinyInputSpiA a = new TinyInputSpiA();
        check(reg.registerServiceProvider(a, ImageInputStreamSpi.class), "first register true");
        check(a.regCalls == 1, "first registration callback");
        check(!reg.registerServiceProvider(a, ImageInputStreamSpi.class), "duplicate register false");
        check(a.regCalls == 2, "duplicate register fires onRegistration again");
        check(a.deregCalls == 1, "duplicate register fires onDeregistration for the old slot");
        check(providerCount(reg) == 1, "duplicate register keeps single instance");
        check(!reg.deregisterServiceProvider(new TinyInputSpiB(), ImageInputStreamSpi.class),
              "deregister unknown instance false");
        TinyInputSpiB b = new TinyInputSpiB();
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.deregisterAll(ImageInputStreamSpi.class);
        check(a.deregCalls == 2 && b.deregCalls == 1, "deregisterAll(category) callbacks");
        check(!reg.contains(a) && !reg.contains(b), "deregisterAll(category) cleared category");
    }

    // JDK-9+ (bug 8068749): categories are restricted to the standard Image I/O
    // SPI types; ServiceRegistry no longer declares finalize().
    static void testVersionSpecificBehavior() throws Exception {
        // null category element rejected
        List<Class<?>> withNull = new ArrayList<Class<?>>();
        withNull.add(null);
        expectExact(IllegalArgumentException.class, () -> new ServiceRegistry(withNull.iterator()),
                    "JDK-9+: null category element rejected");

        // static lookup restricted to SPI classes
        expectExact(IllegalArgumentException.class,
                    () -> ServiceRegistry.lookupProviders(String.class),
                    "JDK-9+: lookupProviders(String.class) rejected");
        expectExact(IllegalArgumentException.class,
                    () -> ServiceRegistry.lookupProviders(String.class,
                                                          ServiceRegistryTest.class.getClassLoader()),
                    "JDK-9+: lookupProviders(String.class, loader) rejected");

        Iterator<ImageInputStreamSpi> found = ServiceRegistry.lookupProviders(ImageInputStreamSpi.class);
        check(found != null, "lookupProviders(ImageInputStreamSpi) non-null");

        boolean hasFinalize = false;
        for (java.lang.reflect.Method m : ServiceRegistry.class.getDeclaredMethods()) {
            if (m.getName().equals("finalize")) hasFinalize = true;
        }
        check(!hasFinalize, "JDK-9+: ServiceRegistry no longer declares finalize()");
    }
}

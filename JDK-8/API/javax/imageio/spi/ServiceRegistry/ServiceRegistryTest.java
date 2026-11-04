/*
 * @test
 * @summary javax.imageio.spi.ServiceRegistry conformance: behavioral.
 * @run main ServiceRegistryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class ServiceRegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        java.util.List<java.lang.Class<?>> cats = new java.util.ArrayList<>();
        cats.add(javax.imageio.spi.ImageReaderSpi.class);
        javax.imageio.spi.ServiceRegistry reg = new javax.imageio.spi.ServiceRegistry(cats.iterator());
        check(reg.getCategories().hasNext(), "categories");
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> it = reg.getServiceProviders(javax.imageio.spi.ImageReaderSpi.class, true);
        check(it != null && !it.hasNext(), "empty providers");
        check(!reg.contains(new Object()), "contains false");
        reg.deregisterAll();
        testIteratorRemoveSemantics();
        testFailFastSemantics();
        testFilterMechanics();
        testOrderingEdgeCases();
        testQueryEdgeCases();
        testNoCategoryRegistration();
        testVersionSpecificBehavior();
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

    // distinct provider classes: the registry keeps at most one instance per leaf class
    static class InA extends BaseInSpi { }
    static class InB extends BaseInSpi { }
    static class InC extends BaseInSpi { }
    static class OutA extends BaseOutSpi { }

    static class BaseInSpi extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        BaseInSpi() { super("RegVendor", "1.0", InputStream.class); }
        public String getDescription(Locale l) { return "in"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; }
    }
    static class BaseOutSpi extends ImageOutputStreamSpi {
        BaseOutSpi() { super("RegVendor", "1.0", OutputStream.class); }
        public String getDescription(Locale l) { return "out"; }
        public javax.imageio.stream.ImageOutputStream createOutputStreamInstance(
                Object out, boolean useCache, File cacheDir) { return null; }
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
        // providers iterator
        ServiceRegistry reg = inputRegistry();
        InA a = new InA(); InB b = new InB();
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
        reg2.registerServiceProvider(new InA(), ImageInputStreamSpi.class);
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
        InA a = new InA();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);

        Iterator<ImageInputStreamSpi> it = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        reg.registerServiceProvider(new InB(), ImageInputStreamSpi.class);
        expectExact(java.util.ConcurrentModificationException.class,
                    () -> drain(it), "iterator next after registration");

        Iterator<ImageInputStreamSpi> it2 = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        reg.deregisterAll(ImageInputStreamSpi.class);
        expectExact(java.util.ConcurrentModificationException.class,
                    () -> drain(it2), "iterator next after deregisterAll");

        // without modification the same iterator drains fine
        reg.registerServiceProvider(new InC(), ImageInputStreamSpi.class);
        Iterator<ImageInputStreamSpi> it3 = reg.getServiceProviders(ImageInputStreamSpi.class, false);
        check(drain(it3).size() == 1, "unmodified iterator drains normally");
    }

    // Filter: per-provider invocation, membership, eager application,
    // null filter NPE, and exception relay of the exact instance
    static void testFilterMechanics() {
        ServiceRegistry reg = inputRegistry();
        InA a = new InA(); InB b = new InB(); InC c = new InC();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.registerServiceProvider(c, ImageInputStreamSpi.class);

        final List<Object> seenByFilter = new ArrayList<Object>();
        ServiceRegistry.Filter keepB = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { seenByFilter.add(provider); return provider instanceof InB; }
        };
        // the iterator prefetches eagerly: the filter has already run when
        // getServiceProviders returns (the throwing-filter case below proves the
        // first filter call happens inside getServiceProviders itself)
        Iterator<ImageInputStreamSpi> it = reg.getServiceProviders(ImageInputStreamSpi.class, keepB, false);
        check(seenByFilter.size() >= 1, "filter already invoked by getServiceProviders");
        List<ImageInputStreamSpi> kept = drain(it);
        check(kept.size() == 1 && kept.get(0) == b, "only the accepted provider survives");
        check(seenByFilter.contains(a) && seenByFilter.contains(b) && seenByFilter.contains(c),
              "after draining, filter saw every registered provider");

        // reject-all
        ServiceRegistry.Filter rejectAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return false; }
        };
        check(drain(reg.getServiceProviders(ImageInputStreamSpi.class, rejectAll, true)).isEmpty(),
              "reject-all filter yields empty iteration");

        // accept-all equals unfiltered membership
        ServiceRegistry.Filter acceptAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return true; }
        };
        check(drain(reg.getServiceProviders(ImageInputStreamSpi.class, acceptAll, false)).size() == 3,
              "accept-all filter keeps every provider");

        // null filter
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
        InA a = new InA(); InB b = new InB(); InC c = new InC();
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

        // self-edges rejected with IllegalArgumentException
        expectExact(IllegalArgumentException.class,
                    () -> reg.setOrdering(ImageInputStreamSpi.class, a, a), "set(a,a)");
        expectExact(IllegalArgumentException.class,
                    () -> reg.unsetOrdering(ImageInputStreamSpi.class, a, a), "unset(a,a)");

        // ordering with an unregistered provider returns false
        InA unregistered = new InA();
        check(!reg.setOrdering(ImageInputStreamSpi.class, unregistered, b),
              "setOrdering with unregistered provider false");

        // unknown category with distinct providers -> IllegalArgumentException
        expectExact(IllegalArgumentException.class, () -> {
            ServiceRegistry raw = reg;
            raw.setOrdering((Class) ImageReaderSpi.class, (Object) a, (Object) b);
        }, "setOrdering unknown category");
        expectExact(IllegalArgumentException.class, () -> {
            ServiceRegistry raw = reg;
            raw.unsetOrdering((Class) ImageReaderSpi.class, (Object) a, (Object) b);
        }, "unsetOrdering unknown category");

        // providers from different categories: refused with false (no exception)
        List<Class<?>> cats2 = new ArrayList<Class<?>>();
        cats2.add(ImageInputStreamSpi.class);
        cats2.add(ImageOutputStreamSpi.class);
        ServiceRegistry reg2 = new ServiceRegistry(cats2.iterator());
        InB ib = new InB(); OutA oa = new OutA();
        reg2.registerServiceProvider(ib, ImageInputStreamSpi.class);
        reg2.registerServiceProvider(oa, ImageOutputStreamSpi.class);
        ServiceRegistry raw2 = reg2;
        check(!raw2.setOrdering((Class) ImageInputStreamSpi.class, (Object) ib, (Object) oa),
              "setOrdering across categories returns false");
    }

    // getServiceProviderByClass / contains / raw-type category mismatch edges
    static void testQueryEdgeCases() {
        ServiceRegistry reg = inputRegistry();
        InA a = new InA();
        reg.registerServiceProvider(a, ImageInputStreamSpi.class);
        expectExact(IllegalArgumentException.class, () -> reg.getServiceProviderByClass(null),
                    "getServiceProviderByClass(null)");
        check(reg.getServiceProviderByClass(String.class) == null,
              "non-category class lookup returns null");
        check(reg.getServiceProviderByClass(InB.class) == null,
              "unregistered spi class lookup returns null");
        check(reg.getServiceProviderByClass(InA.class) == a, "registered class lookup identity");

        // registering a provider under a category it does not implement (raw types)
        // fails with ClassCastException
        ServiceRegistry raw = reg;
        Throwable caught = null;
        try {
            raw.registerServiceProvider((Object) new OutA(), (Class) ImageInputStreamSpi.class);
        } catch (Throwable t) { caught = t; }
        check(caught != null && caught.getClass() == ClassCastException.class,
              "register under wrong category (raw) throws ClassCastException, got " + caught);
        check(providerCount(reg) == 1, "failed registration left state unchanged");

        expectExact(IllegalArgumentException.class,
                    () -> reg.registerServiceProvider(null, ImageInputStreamSpi.class),
                    "register(null, cat)");
        expectExact(IllegalArgumentException.class,
                    () -> reg.getServiceProviders(ImageWriterSpi.class, false),
                    "getServiceProviders unknown category");
        expectExact(IllegalArgumentException.class,
                    () -> reg.deregisterAll(ImageWriterSpi.class),
                    "deregisterAll unknown category");
        expectExact(IllegalArgumentException.class, () -> reg.contains(null), "contains(null)");
        expectExact(IllegalArgumentException.class,
                    () -> new ServiceRegistry(null), "new ServiceRegistry(null)");
    }

    // providers that implement none of the registry's categories register nowhere
    static void testNoCategoryRegistration() {
        ServiceRegistry reg = inputRegistry();
        Object alien = new Object();
        reg.registerServiceProvider(alien);
        check(!reg.contains(alien), "no-category object not contained");
        check(providerCount(reg) == 0, "no-category object did not change providers");
        reg.deregisterServiceProvider(alien);
        check(true, "deregister of no-category object is a no-op");
        // empty category list is permitted
        ServiceRegistry empty = new ServiceRegistry(new ArrayList<Class<?>>().iterator());
        check(empty != null && !empty.getCategories().hasNext(), "empty categories registry usable");
        // duplicate registration of the same instance: returns false but actually
        // deregisters and re-registers the instance (recorded callback counts)
        InA a = new InA();
        check(reg.registerServiceProvider(a, ImageInputStreamSpi.class), "first register true");
        check(a.regCalls == 1, "first registration callback");
        check(!reg.registerServiceProvider(a, ImageInputStreamSpi.class), "duplicate register false");
        check(a.regCalls == 2, "duplicate register fires onRegistration again");
        check(a.deregCalls == 1, "duplicate register fires onDeregistration for the old slot");
        check(providerCount(reg) == 1, "duplicate register keeps single instance");
        // deregistering an unregistered distinct instance returns false
        check(!reg.deregisterServiceProvider(new InB(), ImageInputStreamSpi.class),
              "deregister unknown instance false");
        // deregisterAll(category) drives onDeregistration for each provider
        InB b = new InB();
        reg.registerServiceProvider(b, ImageInputStreamSpi.class);
        reg.deregisterAll(ImageInputStreamSpi.class);
        check(a.deregCalls == 2 && b.deregCalls == 1, "deregisterAll(category) callbacks");
        check(!reg.contains(a) && !reg.contains(b), "deregisterAll(category) cleared category");
    }

    // JDK-8 specific: the registry is NOT restricted to Image I/O SPI categories
    // (the restriction of bug 8068749 only exists on JDK 9+), and ServiceRegistry
    // still declares the removed-in-later-JDKs finalize() method.
    static void testVersionSpecificBehavior() throws Exception {
        List<Class<?>> bad = new ArrayList<Class<?>>();
        bad.add(String.class);
        ServiceRegistry reg = new ServiceRegistry(bad.iterator());
        List<String> names = new ArrayList<String>();
        for (Iterator<Class<?>> it = reg.getCategories(); it.hasNext(); ) {
            names.add(it.next().getName());
        }
        check(names.contains(String.class.getName()),
              "JDK-8: non-SPI category String.class accepted");

        // a null category element is accepted by the constructor on JDK-8
        List<Class<?>> withNull = new ArrayList<Class<?>>();
        withNull.add(null);
        ServiceRegistry reg2 = new ServiceRegistry(withNull.iterator());
        check(reg2 != null, "JDK-8: null category element accepted");

        // static lookup accepts arbitrary classes on JDK-8
        check(ServiceRegistry.lookupProviders(String.class) != null,
              "JDK-8: lookupProviders(String.class) returns iterator");
        check(ServiceRegistry.lookupProviders(String.class, ServiceRegistryTest.class.getClassLoader()) != null,
              "JDK-8: lookupProviders(String.class, loader) returns iterator");
        expectExact(IllegalArgumentException.class,
                    () -> ServiceRegistry.lookupProviders(null), "lookupProviders(null)");

        // known category lookup still works through the classpath
        Iterator<ImageInputStreamSpi> found = ServiceRegistry.lookupProviders(ImageInputStreamSpi.class);
        check(found != null, "lookupProviders(ImageInputStreamSpi) non-null");

        boolean hasFinalize = false;
        for (Method m : ServiceRegistry.class.getDeclaredMethods()) {
            if (m.getName().equals("finalize")) hasFinalize = true;
        }
        check(hasFinalize, "JDK-8: ServiceRegistry declares finalize()");
    }
}

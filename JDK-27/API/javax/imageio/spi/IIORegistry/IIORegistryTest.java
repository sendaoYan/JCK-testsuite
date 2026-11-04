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
import javax.imageio.spi.ServiceRegistry;

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
        testRegisterDeregisterRoundTrip();
        testOrderingRoundTrip();
        testFilterWithOrdering();
        testNegativeCases();
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

    /** Self-written minimal reader SPI: registration callbacks are recorded. */
    static class ProbeReaderSpi extends ImageReaderSpi {
        int regCalls, deregCalls;
        ServiceRegistry lastReg, lastDereg;
        Class<?> lastRegCat, lastDeregCat;
        ProbeReaderSpi() {
            super("ProbeVendor", "1.0",
                  new String[] { "probe" }, new String[] { "prb" }, new String[] { "image/x-probe" },
                  "com.probe.ProbeReader", STANDARD_INPUT_TYPE,
                  null, false, null, null, null, null,
                  false, null, null, null, null);
        }
        public String getDescription(java.util.Locale locale) { return "probe reader spi"; }
        public boolean canDecodeInput(Object input) { return false; }
        public javax.imageio.ImageReader createReaderInstance(Object ext) { return null; }
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; lastReg = r; lastRegCat = c; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; lastDereg = r; lastDeregCat = c; }
    }

    static boolean containsProvider(IIORegistry reg, Object provider) {
        Iterator<ImageReaderSpi> it = reg.getServiceProviders(ImageReaderSpi.class, false);
        while (it.hasNext()) if (it.next() == provider) return true;
        return false;
    }

    static ImageReaderSpi pngSpi(IIORegistry reg) {
        Iterator<ImageReaderSpi> it = reg.getServiceProviders(ImageReaderSpi.class, false);
        while (it.hasNext()) {
            ImageReaderSpi sp = it.next();
            String[] names = sp.getFormatNames();
            if (names != null) {
                for (String n : names) if ("png".equalsIgnoreCase(n)) return sp;
            }
        }
        throw new AssertionError("no png ImageReaderSpi");
    }

    static <T> java.util.List<T> drain(java.util.Iterator<T> it) {
        java.util.List<T> l = new java.util.ArrayList<T>();
        while (it.hasNext()) l.add(it.next());
        return l;
    }

    // registerServiceProvider / deregisterServiceProvider round trip on the global singleton.
    // Global state is restored in finally.
    static void testRegisterDeregisterRoundTrip() {
        IIORegistry reg = IIORegistry.getDefaultInstance();
        ProbeReaderSpi sp = new ProbeReaderSpi();
        try {
            check(reg.registerServiceProvider(sp, ImageReaderSpi.class),
                  "registerServiceProvider(T,Class) returns true for new provider");
            check(sp.regCalls == 1, "onRegistration invoked exactly once");
            check(sp.lastReg == reg, "onRegistration receives the registry itself");
            check(sp.lastRegCat == ImageReaderSpi.class, "onRegistration receives category ImageReaderSpi.class");
            check(containsProvider(reg, sp), "registered provider visible in iteration");
            check(reg.contains(sp), "contains true after register");
            check(reg.getServiceProviderByClass(ProbeReaderSpi.class) == sp,
                  "getServiceProviderByClass identity round trip");

            check(reg.deregisterServiceProvider(sp, ImageReaderSpi.class), "deregisterServiceProvider returns true");
            check(sp.deregCalls == 1, "onDeregistration invoked exactly once");
            check(sp.lastDereg == reg, "onDeregistration receives the registry itself");
            check(sp.lastDeregCat == ImageReaderSpi.class, "onDeregistration receives category");
            check(!containsProvider(reg, sp), "provider invisible after deregister");
            check(!reg.contains(sp), "contains false after deregister");
            check(reg.getServiceProviderByClass(ProbeReaderSpi.class) == null,
                  "getServiceProviderByClass null after deregister");
            check(!reg.deregisterServiceProvider(sp, ImageReaderSpi.class), "second deregister returns false");

            // Object overloads: register into every matching category / deregister everywhere
            reg.registerServiceProvider((Object) sp);
            check(containsProvider(reg, sp), "Object-overload register visible");
            check(sp.regCalls == 2, "onRegistration invoked again on re-register");
            check(sp.lastRegCat == ImageReaderSpi.class, "Object overload passes category too");
            reg.deregisterServiceProvider((Object) sp);
            check(!containsProvider(reg, sp), "Object-overload deregister removes");
            check(sp.deregCalls == 2, "onDeregistration invoked again");

            // deregistering a never-registered object is a silent no-op
            reg.deregisterServiceProvider(new Object());
            check(true, "deregister of unregistered object is a no-op");
        } finally {
            if (containsProvider(reg, sp)) reg.deregisterServiceProvider((Object) sp);
        }
    }

    // setOrdering / unsetOrdering contract between a self-written spi and the png spi.
    static void testOrderingRoundTrip() {
        IIORegistry reg = IIORegistry.getDefaultInstance();
        ImageReaderSpi png = pngSpi(reg);
        ProbeReaderSpi sp = new ProbeReaderSpi();
        try {
            reg.registerServiceProvider(sp, ImageReaderSpi.class);
            check(reg.setOrdering(ImageReaderSpi.class, sp, png), "setOrdering(probe,png) returns true");
            check(!reg.setOrdering(ImageReaderSpi.class, sp, png), "setOrdering same direction again returns false");
            java.util.List<ImageReaderSpi> ordered = drain(reg.getServiceProviders(ImageReaderSpi.class, true));
            check(ordered.contains(sp) && ordered.contains(png), "ordered iteration contains both");
            check(ordered.indexOf(sp) < ordered.indexOf(png), "probe ordered before png");
            java.util.List<ImageReaderSpi> unordered = drain(reg.getServiceProviders(ImageReaderSpi.class, false));
            check(unordered.contains(sp) && unordered.contains(png), "unordered iteration still contains both");
            check(reg.unsetOrdering(ImageReaderSpi.class, sp, png), "unsetOrdering returns true");
            check(!reg.unsetOrdering(ImageReaderSpi.class, sp, png), "unsetOrdering again returns false");
            // ordering involving an unregistered provider is refused (returns false, no exception)
            ProbeReaderSpi unregistered = new ProbeReaderSpi();
            check(!reg.setOrdering(ImageReaderSpi.class, unregistered, png),
                  "setOrdering with unregistered first provider returns false");
            check(!reg.setOrdering(ImageReaderSpi.class, png, unregistered),
                  "setOrdering with unregistered second provider returns false");
            check(!reg.unsetOrdering(ImageReaderSpi.class, unregistered, png),
                  "unsetOrdering with unregistered provider returns false");
            // self-loop rejected
            expectExact(IllegalArgumentException.class,
                        () -> reg.setOrdering(ImageReaderSpi.class, png, png),
                        "setOrdering(p,p)");
            expectExact(IllegalArgumentException.class,
                        () -> reg.unsetOrdering(ImageReaderSpi.class, png, png),
                        "unsetOrdering(p,p)");
        } finally {
            reg.unsetOrdering(ImageReaderSpi.class, sp, png);
            if (containsProvider(reg, sp)) reg.deregisterServiceProvider((Object) sp);
        }
    }

    // getServiceProviders(Class, Filter, useOrdering) with both useOrdering states.
    static void testFilterWithOrdering() {
        final IIORegistry reg = IIORegistry.getDefaultInstance();
        ServiceRegistry.Filter pngOnly = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) {
                String[] names = ((ImageReaderSpi) provider).getFormatNames();
                if (names == null) return false;
                for (String n : names) if ("png".equalsIgnoreCase(n)) return true;
                return false;
            }
        };
        java.util.List<ImageReaderSpi> filtered = drain(reg.getServiceProviders(ImageReaderSpi.class, pngOnly, false));
        check(filtered.size() >= 1, "filter keeps the png provider");
        boolean sawPng = false;
        for (ImageReaderSpi sp : filtered) {
            boolean isPng = false;
            String[] names = sp.getFormatNames();
            if (names != null) for (String n : names) if ("png".equalsIgnoreCase(n)) isPng = true;
            check(isPng, "filtered element is a png spi");
            if (isPng) sawPng = true;
        }
        check(sawPng, "filtered result actually contains png");
        java.util.List<ImageReaderSpi> filteredOrdered = drain(reg.getServiceProviders(ImageReaderSpi.class, pngOnly, true));
        check(filteredOrdered.size() == filtered.size(),
              "useOrdering=true does not change filter membership");
        // reject-all filter yields an empty (but non-null) iteration
        ServiceRegistry.Filter rejectAll = new ServiceRegistry.Filter() {
            public boolean filter(Object provider) { return false; }
        };
        check(drain(reg.getServiceProviders(ImageReaderSpi.class, rejectAll, false)).isEmpty(),
              "reject-all filter yields empty iteration");
    }

    static void testNegativeCases() {
        final IIORegistry reg = IIORegistry.getDefaultInstance();
        expectExact(IllegalArgumentException.class, () -> reg.registerServiceProvider(null),
                    "registerServiceProvider(null)");
        expectExact(IllegalArgumentException.class, () -> reg.registerServiceProvider(null, ImageReaderSpi.class),
                    "registerServiceProvider(null, cat)");
        expectExact(IllegalArgumentException.class, () -> reg.deregisterServiceProvider(null),
                    "deregisterServiceProvider(null)");
        expectExact(IllegalArgumentException.class, () -> reg.contains(null), "contains(null)");
        expectExact(IllegalArgumentException.class, () -> reg.getServiceProviders(String.class, false),
                    "getServiceProviders unknown category");
        expectExact(IllegalArgumentException.class, () -> reg.deregisterAll(String.class),
                    "deregisterAll unknown category");
        expectExact(NullPointerException.class,
                    () -> reg.getServiceProviders(ImageReaderSpi.class, null, false),
                    "getServiceProviders null filter");
        expectExact(IllegalArgumentException.class, () -> reg.getServiceProviderByClass(null),
                    "getServiceProviderByClass(null)");
        check(reg.getServiceProviderByClass(String.class) == null,
              "lookup of non-category class returns null (no exception)");
        reg.registerApplicationClasspathSpis();
        check(true, "registerApplicationClasspathSpis returns normally");
        reg.registerApplicationClasspathSpis();
        check(true, "registerApplicationClasspathSpis is repeatable");
    }
}

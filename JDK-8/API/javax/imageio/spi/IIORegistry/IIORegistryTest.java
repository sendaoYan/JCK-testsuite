/*
 * @test
 * @summary javax.imageio.spi.IIORegistry conformance: behavioral.
 * @run main IIORegistryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageReader;
import javax.imageio.metadata.IIOMetadata;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageOutputStreamSpi;
import javax.imageio.spi.ImageReaderSpi;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class IIORegistryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.imageio.spi.IIORegistry reg = javax.imageio.spi.IIORegistry.getDefaultInstance();
        check(reg != null, "non-null");
        check(javax.imageio.spi.IIORegistry.getDefaultInstance() == reg, "singleton");
        check(reg instanceof javax.imageio.spi.ServiceRegistry, "instanceof ServiceRegistry");
        check(reg.getCategories() != null, "categories non-null");
        java.util.Iterator<javax.imageio.spi.ImageReaderSpi> readers = reg.getServiceProviders(javax.imageio.spi.ImageReaderSpi.class, true);
        check(readers != null, "readers non-null");
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
        public String getDescription(Locale locale) { return "probe reader spi"; }
        public boolean canDecodeInput(Object input) { return false; }
        public ImageReader createReaderInstance(Object ext) { return null; }
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

    static <T> List<T> drain(Iterator<T> it) {
        List<T> l = new ArrayList<T>();
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
            List<ImageReaderSpi> ordered = drain(reg.getServiceProviders(ImageReaderSpi.class, true));
            check(ordered.contains(sp) && ordered.contains(png), "ordered iteration contains both");
            check(ordered.indexOf(sp) < ordered.indexOf(png), "probe ordered before png");
            List<ImageReaderSpi> unordered = drain(reg.getServiceProviders(ImageReaderSpi.class, false));
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
        List<ImageReaderSpi> filtered = drain(reg.getServiceProviders(ImageReaderSpi.class, pngOnly, false));
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
        List<ImageReaderSpi> filteredOrdered = drain(reg.getServiceProviders(ImageReaderSpi.class, pngOnly, true));
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

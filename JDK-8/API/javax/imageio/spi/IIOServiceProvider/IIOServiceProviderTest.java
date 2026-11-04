/*
 * @test
 * @summary javax.imageio.spi.IIOServiceProvider conformance: abstract hierarchy and methods.
 * @run main IIOServiceProviderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class IIOServiceProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.IIOServiceProvider");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        Set<String> si = new HashSet<>(); for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("javax.imageio.spi.RegisterableService"), "implements javax.imageio.spi.RegisterableService");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("onRegistration"), "method onRegistration");
        check(dm.contains("onDeregistration"), "method onDeregistration");
        check(dm.contains("getVendorName"), "method getVendorName");
        check(dm.contains("getVersion"), "method getVersion");
        check(dm.contains("getDescription"), "method getDescription");
        testNoArgConstructorDefaults();
        testConstructorValidation();
        testRegistrationCallbackPassThrough();
        testRealProviders();
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

    static class NoArgProvider extends IIOServiceProvider {
        public String getDescription(Locale l) { return "no-arg"; }
    }
    static class BlankProvider extends IIOServiceProvider {
        BlankProvider() { super("", ""); }
        public String getDescription(Locale l) { return "blank"; }
    }
    static class NullVendorProvider extends IIOServiceProvider {
        NullVendorProvider() { super(null, "1.0"); }
        public String getDescription(Locale l) { return "x"; }
    }
    static class NullVersionProvider extends IIOServiceProvider {
        NullVersionProvider() { super("Vendor", null); }
        public String getDescription(Locale l) { return "x"; }
    }
    /** Concrete SPI whose inherited callbacks record their arguments. */
    static class RecordingProvider extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        ServiceRegistry lastReg, lastDereg;
        Class<?> lastRegCat, lastDeregCat;
        RecordingProvider() { super("RecVendor", "7.7", InputStream.class); }
        public String getDescription(Locale l) { return "recording"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> cat) { regCalls++; lastReg = r; lastRegCat = cat; }
        public void onDeregistration(ServiceRegistry r, Class<?> cat) { deregCalls++; lastDereg = r; lastDeregCat = cat; }
    }

    // no-arg constructor leaves vendorName/version null (recorded actual behavior)
    static void testNoArgConstructorDefaults() {
        NoArgProvider p = new NoArgProvider();
        check(p.getVendorName() == null, "no-arg ctor leaves vendorName null");
        check(p.getVersion() == null, "no-arg ctor leaves version null");
        BlankProvider b = new BlankProvider();
        check("".equals(b.getVendorName()), "empty vendor accepted and round-trips");
        check("".equals(b.getVersion()), "empty version accepted and round-trips");
    }

    static void testConstructorValidation() {
        expectExact(IllegalArgumentException.class, () -> new NullVendorProvider(), "vendorName null");
        expectExact(IllegalArgumentException.class, () -> new NullVersionProvider(), "version null");
        // vendor/version getters return the exact injected strings
        RecordingProvider p = new RecordingProvider();
        check("RecVendor".equals(p.getVendorName()), "getVendorName round-trips ctor value");
        check("7.7".equals(p.getVersion()), "getVersion round-trips ctor value");
    }

    // onRegistration/onDeregistration are public no-ops in the base class and are
    // really dispatched (with registry/category arguments) when registered.
    static void testRegistrationCallbackPassThrough() {
        RecordingProvider p = new RecordingProvider();
        // base-class implementation is a no-op: any arguments accepted
        new NoArgProvider().onRegistration(null, null);
        check(true, "base onRegistration accepts null args");
        new NoArgProvider().onDeregistration(null, null);
        check(true, "base onDeregistration accepts null args");

        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageInputStreamSpi.class);
        ServiceRegistry reg = new ServiceRegistry(cats.iterator());
        check(reg.registerServiceProvider(p, ImageInputStreamSpi.class), "register recording provider");
        check(p.regCalls == 1, "overridden onRegistration dispatched once");
        check(p.lastReg == reg, "onRegistration receives the registry instance");
        check(p.lastRegCat == ImageInputStreamSpi.class, "onRegistration receives the category");
        check(reg.deregisterServiceProvider(p, ImageInputStreamSpi.class), "deregister recording provider");
        check(p.deregCalls == 1, "overridden onDeregistration dispatched once");
        check(p.lastDereg == reg, "onDeregistration receives the registry instance");
        check(p.lastDeregCat == ImageInputStreamSpi.class, "onDeregistration receives the category");
    }

    // the concrete providers shipped with the JDK are IIOServiceProvider subclasses
    static void testRealProviders() throws Exception {
        javax.imageio.ImageWriter w = javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
        IIOServiceProvider real = w.getOriginatingProvider();
        check(real != null, "png writer has an originating provider");
        check(real.getVendorName() != null, "real provider vendor non-null");
        check("Oracle Corporation".equals(real.getVendorName()),
              "OpenJDK-built providers hardcode vendor Oracle Corporation");
        check(real.getVersion() != null, "real provider version non-null");
        check("1.0".equals(real.getVersion()), "OpenJDK-built providers report version 1.0");
        check(real.getDescription(Locale.US) != null, "real provider description non-null");
        check(real.getDescription(Locale.US).equals(real.getDescription(null)),
              "description independent of locale for png writer spi");
        check(real instanceof javax.imageio.spi.RegisterableService,
              "concrete provider is a RegisterableService");
    }
}

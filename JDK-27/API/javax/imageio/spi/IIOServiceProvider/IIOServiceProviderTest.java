/*
 * @test
 * @summary javax.imageio.spi.IIOServiceProvider conformance: white-box structure + black-box behavior.
 * @run main IIOServiceProviderTest
 */

import java.lang.reflect.Modifier;
import java.util.Locale;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

public class IIOServiceProviderTest {
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

    static class SampleProvider extends IIOServiceProvider {
        SampleProvider(String vendor, String version) { super(vendor, version); }
        public String getDescription(Locale locale) { return "Sample description"; }
    }
    static class NullVendorProvider extends IIOServiceProvider {
        NullVendorProvider() { super(null, "1.0"); }
        public String getDescription(Locale locale) { return "x"; }
    }
    static class NullVersionProvider extends IIOServiceProvider {
        NullVersionProvider() { super("Vendor", null); }
        public String getDescription(Locale locale) { return "x"; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.IIOServiceProvider");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(!c.isInterface(), "not interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(Modifier.isAbstract(mods), "abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"),
              "superclass Object");
        check(c.getInterfaces().length == 1, "one interface");
        check(c.getInterfaces()[0].getName().equals("javax.imageio.spi.RegisterableService"),
              "implements RegisterableService");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SampleProvider p = new SampleProvider("VendorA", "1.2.3");
        check("VendorA".equals(p.getVendorName()), "getVendorName round-trip");
        check("1.2.3".equals(p.getVersion()), "getVersion round-trip");
        check("Sample description".equals(p.getDescription(Locale.US)), "getDescription round-trip");

        // real concrete provider from ImageIO
        IIOServiceProvider real =
            ImageIO.getImageWritersByFormatName("png").next().getOriginatingProvider();
        check(real.getVendorName() != null, "concrete vendor name non-null");
        check(real.getVersion() != null, "concrete version non-null");
        check(real.getDescription(Locale.US) != null, "concrete description non-null");

        // public no-op registration callbacks
        ServiceRegistry reg = IIORegistry.getDefaultInstance();
        real.onRegistration(reg, ImageWriterSpi.class);
        check(true, "onRegistration returns normally");
        real.onDeregistration(reg, ImageWriterSpi.class);
        check(true, "onDeregistration returns normally");

        // null-argument constructor validation (via superclass constructor)
        expectThrows(IllegalArgumentException.class, () -> new NullVendorProvider(), "null vendor");
        expectThrows(IllegalArgumentException.class, () -> new NullVersionProvider(), "null version");

        check(p instanceof RegisterableService, "IIOServiceProvider instance is RegisterableService");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testNoArgConstructorDefaults();
        testRegistrationCallbackPassThrough();
        testRealProviderIdentity();
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

    static class NoArgProvider extends IIOServiceProvider {
        public String getDescription(Locale locale) { return "no-arg"; }
    }
    static class BlankProvider extends IIOServiceProvider {
        BlankProvider() { super("", ""); }
        public String getDescription(Locale locale) { return "blank"; }
    }
    /** Concrete SPI whose inherited callbacks record their arguments. */
    static class RecordingProvider extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        ServiceRegistry lastReg, lastDereg;
        Class<?> lastRegCat, lastDeregCat;
        RecordingProvider() { super("RecVendor", "7.7", java.io.InputStream.class); }
        public String getDescription(Locale l) { return "recording"; }
        public javax.imageio.stream.ImageInputStream createInputStreamInstance(
                Object in, boolean useCache, java.io.File cacheDir) {
            return null;
        }
        public void onRegistration(ServiceRegistry r, Class<?> cat) { regCalls++; lastReg = r; lastRegCat = cat; }
        public void onDeregistration(ServiceRegistry r, Class<?> cat) { deregCalls++; lastDereg = r; lastDeregCat = cat; }
    }

    // no-arg constructor leaves vendorName/version null (recorded actual behavior);
    // empty strings are accepted by the (String,String) constructor.
    static void testNoArgConstructorDefaults() {
        NoArgProvider p = new NoArgProvider();
        check(p.getVendorName() == null, "no-arg ctor leaves vendorName null");
        check(p.getVersion() == null, "no-arg ctor leaves version null");
        BlankProvider b = new BlankProvider();
        check("".equals(b.getVendorName()), "empty vendor accepted and round-trips");
        check("".equals(b.getVersion()), "empty version accepted and round-trips");
        // base-class callbacks are public no-ops: any arguments accepted
        new NoArgProvider().onRegistration(null, null);
        check(true, "base onRegistration accepts null args");
        new NoArgProvider().onDeregistration(null, null);
        check(true, "base onDeregistration accepts null args");
    }

    // onRegistration/onDeregistration really dispatched (with registry/category
    // arguments) when the provider is registered in a private registry.
    static void testRegistrationCallbackPassThrough() {
        RecordingProvider p = new RecordingProvider();
        java.util.List<Class<?>> cats = new java.util.ArrayList<Class<?>>();
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
    static void testRealProviderIdentity() throws Exception {
        javax.imageio.ImageWriter w = javax.imageio.ImageIO.getImageWritersByFormatName("png").next();
        IIOServiceProvider real = w.getOriginatingProvider();
        check(real != null, "png writer has an originating provider");
        check("Oracle Corporation".equals(real.getVendorName()),
              "OpenJDK-built providers hardcode vendor Oracle Corporation");
        check("1.0".equals(real.getVersion()), "OpenJDK-built providers report version 1.0");
        check(real.getDescription(Locale.US).equals(real.getDescription(null)),
              "description independent of locale for png writer spi");
    }
}

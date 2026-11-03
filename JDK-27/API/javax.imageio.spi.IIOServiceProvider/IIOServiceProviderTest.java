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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

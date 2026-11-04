/*
 * @test
 * @summary javax.imageio.spi.RegisterableService conformance: white-box structure + black-box behavior.
 * @run main RegisterableServiceTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.ImageIO;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.ImageWriterSpi;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;

public class RegisterableServiceTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.RegisterableService");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(!c.isAnnotation(), "not annotation");
        check(!c.isEnum(), "not enum");
        check(Modifier.isPublic(mods), "public");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getDeclaredMethods().length == 2, "two declared methods");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // A concrete IIOServiceProvider is also a RegisterableService.
        RegisterableService svc =
            (RegisterableService) ImageIO.getImageWritersByFormatName("png").next().getOriginatingProvider();
        ServiceRegistry reg = IIORegistry.getDefaultInstance();
        svc.onRegistration(reg, ImageWriterSpi.class);
        check(true, "onRegistration returns normally");
        svc.onDeregistration(reg, ImageWriterSpi.class);
        check(true, "onDeregistration returns normally");

        // direct functional use of the interface
        final int[] calls = new int[2];
        RegisterableService rs = new RegisterableService() {
            public void onRegistration(ServiceRegistry r, Class<?> c) { calls[0]++; }
            public void onDeregistration(ServiceRegistry r, Class<?> c) { calls[1]++; }
        };
        rs.onRegistration(reg, ImageWriterSpi.class);
        rs.onDeregistration(reg, ImageWriterSpi.class);
        check(calls[0] == 1, "onRegistration dispatched once");
        check(calls[1] == 1, "onDeregistration dispatched once");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMethodSignatures();
        testEndToEndRegistration();
        testNoCategoryMemberNoCallbacks();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // exact signatures: both methods take (ServiceRegistry, Class<?>) and return void
    static void testMethodSignatures() throws Exception {
        Method[] methods = RegisterableService.class.getDeclaredMethods();
        check(methods.length == 2, "exactly two declared methods");
        for (Method m : methods) {
            check(m.getReturnType() == void.class, m.getName() + " returns void");
            check(Modifier.isPublic(m.getModifiers())
                  && Modifier.isAbstract(m.getModifiers()),
                  m.getName() + " public abstract");
            Class<?>[] params = m.getParameterTypes();
            check(params.length == 2, m.getName() + " has two parameters");
            check(params[0] == ServiceRegistry.class,
                  m.getName() + " first parameter is ServiceRegistry");
            check(params[1] == Class.class,
                  m.getName() + " second parameter is Class");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
        }
    }

    /** Minimal concrete SPI whose interface callbacks record invocations. */
    static class RecordingSpi extends javax.imageio.spi.ImageInputStreamSpi {
        int regCalls, deregCalls;
        ServiceRegistry lastReg, lastDereg;
        Class<?> lastRegCat, lastDeregCat;
        RecordingSpi() { super("RegVendor", "9.1", java.io.InputStream.class); }
        public String getDescription(java.util.Locale l) { return "recording spi"; }
        public javax.imageio.stream.ImageInputStream createInputStreamInstance(
                Object in, boolean useCache, java.io.File cacheDir) {
            return null;
        }
        public void onRegistration(ServiceRegistry registry, Class<?> category) {
            regCalls++; lastReg = registry; lastRegCat = category;
        }
        public void onDeregistration(ServiceRegistry registry, Class<?> category) {
            deregCalls++; lastDereg = registry; lastDeregCat = category;
        }
    }

    /** Plain implementation of the interface (not an SPI category member). */
    static class PlainService implements RegisterableService {
        int regCalls, deregCalls;
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; }
    }

    // the callbacks are genuinely dispatched by a ServiceRegistry registration,
    // with the exact registry and category arguments passed through
    static void testEndToEndRegistration() {
        java.util.List<Class<?>> cats = new java.util.ArrayList<Class<?>>();
        cats.add(javax.imageio.spi.ImageInputStreamSpi.class);
        ServiceRegistry reg = new ServiceRegistry(cats.iterator());
        RecordingSpi sp = new RecordingSpi();
        check(sp instanceof RegisterableService, "concrete SPI implements RegisterableService");

        reg.registerServiceProvider(sp, javax.imageio.spi.ImageInputStreamSpi.class);
        check(sp.regCalls == 1, "onRegistration dispatched by registerServiceProvider");
        check(sp.lastReg == reg, "registry argument passed through verbatim");
        check(sp.lastRegCat == javax.imageio.spi.ImageInputStreamSpi.class,
              "category argument passed through verbatim");
        check(sp.deregCalls == 0, "onDeregistration not called yet");

        // re-registering a second instance of the same class deregisters the first
        RecordingSpi replacement = new RecordingSpi();
        reg.registerServiceProvider(replacement, javax.imageio.spi.ImageInputStreamSpi.class);
        check(sp.deregCalls == 1, "replacing an instance deregisters the old one");
        check(sp.lastDeregCat == javax.imageio.spi.ImageInputStreamSpi.class,
              "dereg category passed through");
        check(replacement.regCalls == 1, "replacement got its own onRegistration");

        reg.deregisterServiceProvider(replacement, javax.imageio.spi.ImageInputStreamSpi.class);
        check(replacement.deregCalls == 1, "onDeregistration dispatched by deregisterServiceProvider");
        check(replacement.lastDereg == reg, "dereg registry argument passed through");

        RecordingSpi third = new RecordingSpi();
        reg.registerServiceProvider(third, javax.imageio.spi.ImageInputStreamSpi.class);
        reg.deregisterAll(javax.imageio.spi.ImageInputStreamSpi.class);
        check(third.deregCalls == 1, "onDeregistration dispatched by deregisterAll(category)");
    }

    // a plain RegisterableService (no SPI category membership) registers nothing
    // and its callbacks are never invoked
    static void testNoCategoryMemberNoCallbacks() {
        java.util.List<Class<?>> cats = new java.util.ArrayList<Class<?>>();
        cats.add(javax.imageio.spi.ImageInputStreamSpi.class);
        ServiceRegistry private1 = new ServiceRegistry(cats.iterator());
        PlainService plain = new PlainService();
        private1.registerServiceProvider(plain);
        check(plain.regCalls == 0, "no category member: onRegistration not called");
        check(!private1.contains(plain), "no category member: not contained");
        private1.deregisterServiceProvider(plain);
        check(plain.deregCalls == 0, "no category member: onDeregistration not called");
    }
}

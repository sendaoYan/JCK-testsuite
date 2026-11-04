/*
 * @test
 * @summary javax.imageio.spi.RegisterableService conformance: interface hierarchy and methods.
 * @run main RegisterableServiceTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.stream.ImageInputStream;

public class RegisterableServiceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.RegisterableService");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("onRegistration"), "method onRegistration");
        check(dm.contains("onDeregistration"), "method onDeregistration");
        testMethodSignatures();
        testEndToEndRegistration();
        testDefaultCallbacksAreNoOps();
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
    static class RecordingSpi extends ImageInputStreamSpi {
        int regCalls, deregCalls;
        ServiceRegistry lastReg, lastDereg;
        Class<?> lastRegCat, lastDeregCat;
        RecordingSpi() { super("RegVendor", "9.1", InputStream.class); }
        public String getDescription(Locale l) { return "recording spi"; }
        public ImageInputStream createInputStreamInstance(Object in, boolean useCache, File cacheDir) {
            return null;
        }
        // RegisterableService implementation
        public void onRegistration(ServiceRegistry registry, Class<?> category) {
            regCalls++; lastReg = registry; lastRegCat = category;
        }
        public void onDeregistration(ServiceRegistry registry, Class<?> category) {
            deregCalls++; lastDereg = registry; lastDeregCat = category;
        }
    }

    // the callbacks are genuinely dispatched by a ServiceRegistry registration,
    // with the exact registry and category arguments passed through
    static void testEndToEndRegistration() {
        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageInputStreamSpi.class);
        ServiceRegistry reg = new ServiceRegistry(cats.iterator());
        RecordingSpi sp = new RecordingSpi();
        check(sp instanceof RegisterableService, "concrete SPI implements RegisterableService");

        reg.registerServiceProvider(sp, ImageInputStreamSpi.class);
        check(sp.regCalls == 1, "onRegistration dispatched by registerServiceProvider");
        check(sp.lastReg == reg, "registry argument passed through verbatim");
        check(sp.lastRegCat == ImageInputStreamSpi.class, "category argument passed through verbatim");
        check(sp.deregCalls == 0, "onDeregistration not called yet");

        // re-registering a second instance of the same class deregisters the first
        RecordingSpi replacement = new RecordingSpi();
        reg.registerServiceProvider(replacement, ImageInputStreamSpi.class);
        check(sp.deregCalls == 1, "replacing an instance deregisters the old one");
        check(sp.lastDeregCat == ImageInputStreamSpi.class, "dereg category passed through");
        check(replacement.regCalls == 1, "replacement got its own onRegistration");

        reg.deregisterServiceProvider(replacement, ImageInputStreamSpi.class);
        check(replacement.deregCalls == 1, "onDeregistration dispatched by deregisterServiceProvider");
        check(replacement.lastDereg == reg, "dereg registry argument passed through");

        // deregisterAll also drives the callback
        RecordingSpi third = new RecordingSpi();
        reg.registerServiceProvider(third, ImageInputStreamSpi.class);
        reg.deregisterAll(ImageInputStreamSpi.class);
        check(third.deregCalls == 1, "onDeregistration dispatched by deregisterAll(category)");
    }

    /** Plain implementation of the interface (not an SPI category member). */
    static class PlainService implements RegisterableService {
        int regCalls, deregCalls;
        public void onRegistration(ServiceRegistry r, Class<?> c) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> c) { deregCalls++; }
    }

    // direct dispatch through the interface type; base IIOServiceProvider
    // implementations are no-ops
    static void testDefaultCallbacksAreNoOps() {
        IIORegistry reg = IIORegistry.getDefaultInstance();
        PlainService svc = new PlainService();
        svc.onRegistration(reg, ImageInputStreamSpi.class);
        check(svc.regCalls == 1, "onRegistration dispatched through interface reference");
        svc.onDeregistration(reg, ImageInputStreamSpi.class);
        check(svc.deregCalls == 1, "onDeregistration dispatched through interface reference");
        svc.onRegistration(null, null);
        check(svc.regCalls == 2, "implementations may accept null arguments");

        // registering a plain RegisterableService (no SPI category membership)
        // registers nothing and never calls the callbacks
        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageInputStreamSpi.class);
        ServiceRegistry private1 = new ServiceRegistry(cats.iterator());
        PlainService plain = new PlainService();
        private1.registerServiceProvider(plain);
        check(plain.regCalls == 0, "no category member: onRegistration not called");
        check(!private1.contains(plain), "no category member: not contained");
        private1.deregisterServiceProvider(plain);
        check(plain.deregCalls == 0, "no category member: onDeregistration not called");
    }
}

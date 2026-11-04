/*
 * @test
 * @summary javax.imageio.spi.ImageTranscoderSpi conformance: abstract hierarchy and methods.
 * @run main ImageTranscoderSpiTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.ImageTranscoder;
import javax.imageio.spi.IIORegistry;
import javax.imageio.spi.IIOServiceProvider;
import javax.imageio.spi.ImageTranscoderSpi;
import javax.imageio.spi.RegisterableService;
import javax.imageio.spi.ServiceRegistry;
import javax.imageio.spi.ImageInputStreamSpi;
import javax.imageio.stream.ImageInputStream;

public class ImageTranscoderSpiTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.spi.ImageTranscoderSpi");
        check(!c.isInterface(), "not interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass().getName().equals("javax.imageio.spi.IIOServiceProvider"), "extends javax.imageio.spi.IIOServiceProvider");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("getReaderServiceProviderName"), "method getReaderServiceProviderName");
        check(dm.contains("getWriterServiceProviderName"), "method getWriterServiceProviderName");
        check(dm.contains("createTranscoderInstance"), "method createTranscoderInstance");
        testRegisteredTranscoders();
        testTinySubclassProbe();
        testConstructorValidation();
        testRegistrationCallbacks();
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

    /** Self-written minimal transcoder SPI. */
    static class TinyTranscoderSpi extends ImageTranscoderSpi {
        int createCalls;
        TinyTranscoderSpi(String vendorName, String version) { super(vendorName, version); }
        public String getReaderServiceProviderName() { return "com.probe.ProbeReaderSpi"; }
        public String getWriterServiceProviderName() { return "com.probe.ProbeWriterSpi"; }
        public ImageTranscoder createTranscoderInstance() { createCalls++; return null; }
        public String getDescription(Locale l) { return "tiny transcoder spi"; }
        public void onRegistration(ServiceRegistry r, Class<?> cat) { regCalls++; }
        public void onDeregistration(ServiceRegistry r, Class<?> cat) { deregCalls++; }
        int regCalls, deregCalls;
    }

    // the JDK ships no transcoder providers: iteration is well-formed and empty
    static void testRegisteredTranscoders() {
        Iterator<ImageTranscoderSpi> it =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageTranscoderSpi.class, true);
        check(it != null, "transcoder providers iterator non-null");
        int count = 0;
        while (it.hasNext()) {
            ImageTranscoderSpi sp = it.next();
            check(sp.getReaderServiceProviderName() != null, "reader SPI name non-null");
            check(sp.getWriterServiceProviderName() != null, "writer SPI name non-null");
            check(sp.createTranscoderInstance() != null, "createTranscoderInstance non-null");
            count++;
        }
        check(count == 0, "no built-in transcoder providers (degraded gracefully)");
        Iterator<ImageTranscoderSpi> it2 =
            IIORegistry.getDefaultInstance().getServiceProviders(ImageTranscoderSpi.class, false);
        check(it2 != null && !it2.hasNext(), "unordered iteration also empty");
    }

    // subclass drives all three abstract methods and the (String,String) constructor
    static void testTinySubclassProbe() throws Exception {
        TinyTranscoderSpi sp = new TinyTranscoderSpi("TransVendor", "8.4");
        check("TransVendor".equals(sp.getVendorName()), "vendor round-trips");
        check("8.4".equals(sp.getVersion()), "version round-trips");
        check("com.probe.ProbeReaderSpi".equals(sp.getReaderServiceProviderName()),
              "reader spi name round-trips");
        check("com.probe.ProbeWriterSpi".equals(sp.getWriterServiceProviderName()),
              "writer spi name round-trips");
        check("tiny transcoder spi".equals(sp.getDescription(Locale.US)), "description round-trips");
        check("tiny transcoder spi".equals(sp.getDescription(null)), "description null locale ok");
        check(sp.createTranscoderInstance() == null, "createTranscoderInstance returns subclass value");
        check(sp.createCalls == 1, "createTranscoderInstance dispatched");
        check(sp instanceof IIOServiceProvider, "is an IIOServiceProvider");
        check(sp instanceof RegisterableService, "is a RegisterableService");
    }

    // (String,String) constructor validation inherited from IIOServiceProvider
    static void testConstructorValidation() {
        expectExact(IllegalArgumentException.class,
                    () -> new TinyTranscoderSpi(null, "1.0"), "null vendorName");
        expectExact(IllegalArgumentException.class,
                    () -> new TinyTranscoderSpi("V", null), "null version");
    }

    // registering a self-written transcoder SPI drives the RegisterableService callbacks
    static void testRegistrationCallbacks() {
        List<Class<?>> cats = new ArrayList<Class<?>>();
        cats.add(ImageTranscoderSpi.class);
        ServiceRegistry reg = new ServiceRegistry(cats.iterator());
        TinyTranscoderSpi sp = new TinyTranscoderSpi("TransVendor", "8.4");
        check(reg.registerServiceProvider(sp, ImageTranscoderSpi.class), "transcoder registers");
        check(sp.regCalls == 1, "onRegistration dispatched once");
        check(reg.contains(sp), "registry contains the transcoder spi");
        check(reg.getServiceProviderByClass(TinyTranscoderSpi.class) == sp, "byClass identity");
        check(reg.deregisterServiceProvider(sp, ImageTranscoderSpi.class), "transcoder deregisters");
        check(sp.deregCalls == 1, "onDeregistration dispatched once");
        check(!reg.contains(sp), "registry no longer contains it");
    }
}

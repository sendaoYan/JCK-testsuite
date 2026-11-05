/*
 * @test
 * @summary jdk.management.jfr.internal.FlightRecorderMXBeanProvider conformance: reflection shape + static factory semantics.
 * @run main FlightRecorderMXBeanProviderTest
 */

import java.lang.management.ManagementFactory;
import java.lang.reflect.*;
import java.util.*;
import java.util.concurrent.Callable;

public class FlightRecorderMXBeanProviderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        // ---------- original shape checks (kept verbatim) ----------
        Class<?> c = Class.forName("jdk.management.jfr.internal.FlightRecorderMXBeanProvider");
        check(c != null, "jdk.management.jfr.internal.FlightRecorderMXBeanProvider loads");
        check(c.getName().equals("jdk.management.jfr.internal.FlightRecorderMXBeanProvider"), "binary name is jdk.management.jfr.internal.FlightRecorderMXBeanProvider");
        check(Modifier.isPublic(c.getModifiers()), "jdk.management.jfr.internal.FlightRecorderMXBeanProvider is public");
        check(!c.isInterface(), "jdk.management.jfr.internal.FlightRecorderMXBeanProvider is not an interface");
        check(!Modifier.isAbstract(c.getModifiers()), "jdk.management.jfr.internal.FlightRecorderMXBeanProvider is concrete");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass is java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");

        // ---------- JDK-8 surface: plain static factory (no PlatformMBeanProvider SPI exists) ----------
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(c.getDeclaredConstructors().length == 1 && Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers()),
              "single public no-arg constructor");
        TreeSet<String> pubStatic = new TreeSet<String>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) pubStatic.add(m.getName() + ":" + Modifier.isStatic(mod));
        }
        check(pubStatic.equals(new TreeSet<String>(Arrays.asList("getFlightRecorderMXBean:true", "setFlightRecorderMXBeanFactory:true"))),
              "public static methods are the factory pair, got " + pubStatic);
        for (Field f : c.getDeclaredFields())
            check(!Modifier.isPublic(f.getModifiers()) && !Modifier.isProtected(f.getModifiers()), "no public/protected fields");

        // the JDK-9+ PlatformMBeanProvider SPI does not exist on JDK-8
        boolean spiAbsent = false;
        try { Class.forName("sun.management.spi.PlatformMBeanProvider"); }
        catch (ClassNotFoundException expected) { spiAbsent = true; }
        check(spiAbsent, "sun.management.spi.PlatformMBeanProvider absent on JDK-8");

        // ---------- static factory semantics ----------
        Class<?> beanIface = Class.forName("jdk.management.jfr.FlightRecorderMXBean");
        Object viaFactory = c.getMethod("getFlightRecorderMXBean").invoke(null);
        check(viaFactory != null && beanIface.isInstance(viaFactory), "getFlightRecorderMXBean returns the bean");
        Object viaMgmt = ManagementFactory.getPlatformMXBean((Class) beanIface);
        check(viaFactory == viaMgmt, "factory instance == ManagementFactory instance (singleton)");
        check(c.getMethod("getFlightRecorderMXBean").invoke(null) == viaFactory, "getFlightRecorderMXBean idempotent");

        // verified: once instantiated, installing another factory never takes effect (cached instance)
        final boolean[] invoked = {false};
        Callable<?> noop = new Callable<Object>() {
            public Object call() { invoked[0] = true; return null; }
        };
        c.getMethod("setFlightRecorderMXBeanFactory", Callable.class).invoke(null, noop);
        check(c.getMethod("getFlightRecorderMXBean").invoke(null) == viaFactory, "late factory install does not replace the bean");
        check(invoked[0] == false, "late factory never invoked");
        // null factory tolerated after instantiation
        c.getMethod("setFlightRecorderMXBeanFactory", Callable.class).invoke(null, (Object) null);
        check(c.getMethod("getFlightRecorderMXBean").invoke(null) == viaFactory, "null factory tolerated (cached bean)");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

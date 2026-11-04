/*
 * @test
 * @summary java.net.spi.InetAddressResolverProvider.Configuration conformance: API type verification.
 * @run main ConfigurationTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class ConfigurationTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
static boolean hasMethod(Class<?> c, String n) {
    for (java.lang.reflect.Method m : c.getMethods()) {
        if (m.getName().equals(n)) return true;
    }
    return false;
}

    static Class<?> confClass() throws Exception {
        return Class.forName("java.net.spi.InetAddressResolverProvider$Configuration");
    }

    // ---- Part 1: white-box shape (probed on jdk-27.0.1.0.2+1) ----
    static void testShape() throws Exception {
        Class<?> c = confClass();
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(hasMethod(c, "builtinResolver"), "builtinResolver present");
        check(hasMethod(c, "lookupLocalHostName"), "lookupLocalHostName present");

        check("Configuration".equals(c.getSimpleName()), "simple name Configuration");
        check("java.net.spi.InetAddressResolverProvider$Configuration".equals(c.getName()),
                "binary name");
        check(c.getDeclaringClass() == java.net.spi.InetAddressResolverProvider.class,
                "member of InetAddressResolverProvider");
        check(c.isMemberClass(), "isMemberClass");
        check(c.getPackage() != null && "java.net.spi".equals(c.getPackage().getName()),
                "package java.net.spi");
        check(c.getTypeParameters().length == 0, "no type parameters");

        // Exactly two declared methods, both public abstract, no defaults/statics.
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly two declared methods");
        for (Method m : ms) {
            check(Modifier.isPublic(m.getModifiers()), m.getName() + " public");
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " abstract");
            check(!m.isDefault(), m.getName() + " not default");
            check(!Modifier.isStatic(m.getModifiers()), m.getName() + " not static");
            check(m.getParameterCount() == 0, m.getName() + " takes no parameters");
            check(m.getExceptionTypes().length == 0, m.getName() + " declares no exceptions");
            check(!m.isSynthetic(), m.getName() + " not synthetic");
        }
        Method builtin = c.getMethod("builtinResolver");
        Method local = c.getMethod("lookupLocalHostName");
        check(builtin.getReturnType() == java.net.spi.InetAddressResolver.class,
                "builtinResolver returns InetAddressResolver");
        check(local.getReturnType() == String.class, "lookupLocalHostName returns String");
        check(c.getDeclaredFields().length == 0, "no fields (no constants)");
        check(c.getAnnotations().length == 0, "no annotations (not @FunctionalInterface)");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
                "not @FunctionalInterface: two abstract methods");

        // getMethods() exposure: the two SPI methods are part of the public surface.
        int mine = 0;
        for (Method m : c.getMethods()) {
            if (m.getDeclaringClass() == c) mine++;
        }
        check(mine == 2, "getMethods exposes exactly the two Configuration methods");

        // Assignability relationships.
        check(java.net.spi.InetAddressResolver.class.isAssignableFrom(
                builtin.getReturnType()), "resolver type assignable");
    }

    // ---- Part 2: sealing (probed) ----
    static void testSealing() throws Exception {
        Class<?> c = confClass();
        check(c.isSealed(), "Configuration is sealed");
        Class<?>[] permitted = c.getPermittedSubclasses();
        check(permitted != null, "getPermittedSubclasses non-null (sealed marker)");
        check(permitted.length == 1, "exactly one permitted subclass");
        check("sun.net.ResolverProviderConfiguration".equals(permitted[0].getName()),
                "permitted subclass name (probed)");
        check(!permitted[0].isInterface(), "permitted subclass is a class");
        // Probed: the permitted class is public final but its package sun.net is NOT
        // exported by java.base, so it is unreachable at compile time and via reflection.
        check(Modifier.isPublic(permitted[0].getModifiers()) && Modifier.isFinal(permitted[0].getModifiers()),
                "permitted subclass is public final (probed)");
        check("java.base".equals(permitted[0].getModule().getName()),
                "permitted subclass lives in java.base");
        check(c.isAssignableFrom(permitted[0]), "permitted subclass implements Configuration");

        // Runtime enforcement: dynamic proxies of a sealed interface are refused (probed).
        try {
            java.lang.reflect.Proxy.newProxyInstance(
                    c.getClassLoader(), new Class<?>[]{c}, (p, m, a) -> null);
            check(false, "Proxy creation must fail for sealed interface");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("is a sealed interface"),
                    "Proxy refusal message mentions sealed interface");
        }
        // Refusal is deterministic.
        try {
            java.lang.reflect.Proxy.newProxyInstance(
                    Thread.currentThread().getContextClassLoader(), new Class<?>[]{c},
                    (p, m, a) -> null);
            check(false, "Proxy creation must fail again");
        } catch (IllegalArgumentException e) {
            check(true, "Proxy refusal repeatable across classloaders");
        }
    }

    // ---- Part 3: configuration instance unobtainability + impl signature (probed) ----
    static void testUnobtainable() throws Exception {
        Class<?> c = confClass();
        // No public constructors (interfaces never have any).
        check(c.getConstructors().length == 0, "no public constructors");
        // Probed: the permitted implementation has exactly one public constructor
        // (InetAddressResolver, Supplier<String>) - the built-in resolver plus a
        // local-hostname supplier. Its shape is asserted as API documentation fact.
        Class<?> impl = Class.forName("sun.net.ResolverProviderConfiguration");
        check(impl != null, "permitted impl class loadable by name (java.base internal)");
        Constructor<?>[] ctors = impl.getDeclaredConstructors();
        check(ctors.length == 1, "impl has exactly one constructor (probed)");
        Constructor<?> ctor = ctors[0];
        check(Modifier.isPublic(ctor.getModifiers()), "impl constructor is public");
        Class<?>[] pt = ctor.getParameterTypes();
        check(pt.length == 2
                && pt[0] == java.net.spi.InetAddressResolver.class
                && pt[1] == java.util.function.Supplier.class,
                "impl constructor takes (InetAddressResolver, Supplier<String>) - probed");
        // Reflective construction from an unnamed module is refused because java.base
        // does not export sun.net (probed message). If a future run environment adds
        // exports, fall back to behavioral checks on the obtained instance.
        java.net.spi.InetAddressResolver dummy = new java.net.spi.InetAddressResolver() {
            public java.util.stream.Stream<java.net.InetAddress> lookupByName(String h,
                    java.net.spi.InetAddressResolver.LookupPolicy p) {
                return java.util.stream.Stream.empty();
            }
            public String lookupByAddress(byte[] x) { return "dummy"; }
        };
        try {
            Object o = ctor.newInstance(dummy,
                    (java.util.function.Supplier<String>) () -> "local-host");
            // Unexpected success (open modules): verify behavioral contract anyway.
            java.net.spi.InetAddressResolverProvider.Configuration conf =
                    (java.net.spi.InetAddressResolverProvider.Configuration) o;
            check(conf != null, "impl instance implements Configuration");
            check(conf.builtinResolver() != null, "builtinResolver non-null");
            check("local-host".equals(conf.lookupLocalHostName()), "hostname supplier honored");
        } catch (IllegalAccessException e) {
            check(e.getMessage() != null && e.getMessage().contains("does not export sun.net"),
                    "impl ctor invoke -> IllegalAccessException (sun.net not exported)");
        }
        // javadoc contract wording (method set only): the configuration gives access to
        // the built-in resolver and the local hostname; both verified as resolvable methods.
        // Note: Class.getMethods() on an interface does NOT include Object's methods.
        check(java.net.spi.InetAddressResolverProvider.Configuration.class
                .getMethods().length == 2, "public method surface is exactly the two SPI methods");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolverProvider$Configuration");
check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
check(c.isInterface(), "is interface");
check(c.getSuperclass() == null, "no superclass");
check(hasMethod(c, "builtinResolver"), "builtinResolver present");
check(hasMethod(c, "lookupLocalHostName"), "lookupLocalHostName present");
        testShape();
        testSealing();
        testUnobtainable();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

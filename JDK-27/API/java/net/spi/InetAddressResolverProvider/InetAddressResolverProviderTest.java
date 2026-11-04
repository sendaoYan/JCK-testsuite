/*
 * @test
 * @summary java.net.spi.InetAddressResolverProvider conformance: white-box structure + black-box anonymous impl.
 * @run main InetAddressResolverProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.spi.InetAddressResolver;
import java.net.spi.InetAddressResolverProvider;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;

public class InetAddressResolverProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolverProvider");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == Object.class, "superclass is Object");
        check(c.getMethod("get", InetAddressResolverProvider.Configuration.class) != null, "get present");
        check(c.getMethod("name") != null, "name present");

        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor != null, "no-arg constructor present");
        check(Modifier.isProtected(ctor.getModifiers()), "constructor is protected");
    }

    static void testBlackBox() throws Exception {
        final InetAddress loopback = InetAddress.getLoopbackAddress();
        final InetAddressResolver resolver = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy policy) {
                return Stream.of(loopback);
            }
            public String lookupByAddress(byte[] addr) { return "loopback"; }
        };

        InetAddressResolverProvider provider = new InetAddressResolverProvider() {
            public InetAddressResolver get(InetAddressResolverProvider.Configuration configuration) {
                return resolver;
            }
            public String name() { return "test-provider"; }
        };

        check("test-provider".equals(provider.name()), "name()");
        check(provider.get(null) == resolver, "get() returns the resolver");

        // Configuration is a sealed interface with exactly one (non-public) permitted subclass,
        // so it cannot be anonymously implemented here; assert its structural shape instead.
        Class<?> conf = Class.forName("java.net.spi.InetAddressResolverProvider$Configuration");
        check(conf.isInterface(), "Configuration is an interface");
        check(conf.getPermittedSubclasses() != null && conf.getPermittedSubclasses().length == 1,
                "Configuration is sealed with one permitted subclass");
        check(conf.getMethod("builtinResolver") != null, "Configuration.builtinResolver present");
        check(conf.getMethod("lookupLocalHostName") != null, "Configuration.lookupLocalHostName present");
    }

    // ====================== deepening below ======================

    /** Named subclass with explicit state, exercising the protected constructor chain. */
    static class NamedProvider extends InetAddressResolverProvider {
        final String providerName;
        final InetAddressResolver resolverToReturn;
        final List<Object> configurationsSeen = new ArrayList<>();
        int getNameCalls, getResolverCalls;

        NamedProvider(String providerName, InetAddressResolver resolverToReturn) {
            // implicit super() invokes the protected no-arg constructor of the SPI class
            this.providerName = providerName;
            this.resolverToReturn = resolverToReturn;
        }

        public InetAddressResolver get(InetAddressResolverProvider.Configuration configuration) {
            getResolverCalls++;
            configurationsSeen.add(configuration);
            return resolverToReturn;
        }

        public String name() {
            getNameCalls++;
            return providerName;
        }
    }

    static final class ThrowingProvider extends InetAddressResolverProvider {
        public InetAddressResolver get(InetAddressResolverProvider.Configuration cfg) {
            throw new IllegalStateException("provider-not-ready");
        }
        public String name() {
            throw new UnsupportedOperationException("name-unavailable");
        }
    }

    // ---- Part 1: white-box shape, full precision (probed) ----
    static void testWhiteBoxShape() throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolverProvider");
        check(c.getDeclaredConstructors().length == 1, "exactly one declared constructor");
        Constructor<?> ctor = c.getDeclaredConstructor();
        check(ctor.getParameterCount() == 0, "constructor takes no arguments (probed: no name param)");
        check("protected".equals(Modifier.toString(ctor.getModifiers()
                & (Modifier.PUBLIC | Modifier.PROTECTED | Modifier.PRIVATE))),
                "constructor modifier string is 'protected'");

        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly two declared methods: name(), get(Configuration)");
        java.lang.reflect.Method get = c.getMethod("get", InetAddressResolverProvider.Configuration.class);
        java.lang.reflect.Method name = c.getMethod("name");
        check(Modifier.isAbstract(get.getModifiers()), "get is abstract");
        check(Modifier.isPublic(get.getModifiers()), "get is public");
        check(get.getReturnType() == InetAddressResolver.class, "get returns InetAddressResolver");
        check(get.getParameterCount() == 1
                && get.getParameterTypes()[0] == InetAddressResolverProvider.Configuration.class,
                "get takes exactly one Configuration parameter");
        check(get.getExceptionTypes().length == 0, "get declares no checked exceptions");
        check(Modifier.isAbstract(name.getModifiers()), "name is abstract");
        check(Modifier.isPublic(name.getModifiers()), "name is public");
        check(name.getReturnType() == String.class, "name returns String");
        check(name.getParameterCount() == 0, "name takes no parameters");
        check(name.getExceptionTypes().length == 0, "name declares no checked exceptions");
        check(!get.isSynthetic() && !name.isSynthetic(), "no synthetic bridge methods declared");
        check(!ctor.isSynthetic(), "constructor not synthetic");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(!Modifier.isFinal(c.getModifiers()), "not final: open for subclassing");
        check(!c.isSealed(), "not sealed");
        check(c.getPackage() != null && "java.net.spi".equals(c.getPackage().getName()), "package java.net.spi");
        check(c.getAnnotations().length == 0, "no annotations");

        // Reflection instantiation of the abstract class is blocked twice over (probed):
        // (a) protected constructor not accessible from an unnamed-module caller.
        try {
            ctor.newInstance();
            check(false, "newInstance must not succeed");
        } catch (IllegalAccessException e) {
            check(true, "newInstance -> IllegalAccessException (protected ctor)");
        }
        // (b) forcing accessibility is refused by the module system.
        try {
            ctor.setAccessible(true);
            check(false, "setAccessible must be refused");
        } catch (java.lang.reflect.InaccessibleObjectException e) {
            check(true, "setAccessible -> InaccessibleObjectException (module java.base does not open java.net.spi)");
        }
        // Array creation is allowed (arrays of abstract types are fine).
        InetAddressResolverProvider[] arr = new InetAddressResolverProvider[3];
        check(arr.length == 3 && arr[0] == null, "array of providers allowed, null-initialized");
    }

    // ---- Part 2: subclass contract - state, call recording, identity ----
    static void testSubclassContract() throws Exception {
        final InetAddress loop = InetAddress.getLoopbackAddress();
        InetAddressResolver r1 = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.of(loop);
            }
            public String lookupByAddress(byte[] a) { return "r1"; }
        };
        InetAddressResolver r2 = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.empty();
            }
            public String lookupByAddress(byte[] a) { return "r2"; }
        };

        NamedProvider p1 = new NamedProvider("alpha", r1);
        NamedProvider p2 = new NamedProvider("beta", r2);
        check(p1.resolverToReturn == r1 && p2.resolverToReturn == r2,
                "constructor-injected state is per-instance and independent");

        check("alpha".equals(p1.name()), "named provider returns configured name");
        check("beta".equals(p2.name()), "second provider returns its own name");
        check(!p1.name().equals(p2.name()), "provider names are per-instance state");
        check(p1.getNameCalls == 2, "name() invocation counted (2 calls so far)");
        p1.name();
        p1.name();
        check(p1.getNameCalls == 4, "name() call counter reaches 4");
        check(p1.name().equals(p1.name()), "name() repeatable with same result");
        check(p1.getNameCalls == 6, "two repeat calls counted");

        check(p1.get(null) == r1, "get(null) returns configured resolver");
        check(p1.get(null) == r1, "get(null) repeatable, same resolver instance");
        check(p1.getResolverCalls == 2, "get() call counter reaches 2");
        check(p1.configurationsSeen.get(0) == null && p1.configurationsSeen.get(1) == null,
                "null configuration argument recorded transparently");
        check(p2.get(null) == r2 && p2.getResolverCalls == 1, "independent instance counters");

        // Identity semantics inherited from Object.
        check(p1 != p2 && !p1.equals(p2), "two providers distinct");
        check(p1.equals(p1), "equals reflexive");
        check(p1.hashCode() == p1.hashCode(), "hashCode stable");
        check(p1.toString().contains("NamedProvider"), "default toString names subclass");
        check(InetAddressResolverProvider.class.isInstance(p1),
                "SPI class isInstance accepts subclass");
        check(Class.forName("java.net.spi.InetAddressResolverProvider").isInstance(new ThrowingProvider()),
                "reflection isInstance accepts anonymous-style subclass");
    }

    // ---- Part 3: provider -> resolver composition chain ----
    static void testComposition() throws Exception {
        final InetAddress loop = InetAddress.getLoopbackAddress();
        final List<String> seenHosts = new ArrayList<>();
        InetAddressResolver recording = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy p) {
                seenHosts.add(host);
                return Stream.of(loop);
            }
            public String lookupByAddress(byte[] a) { return "composed"; }
        };
        NamedProvider provider = new NamedProvider("composed", recording);
        InetAddressResolver viaGet = provider.get(null);

        InetAddressResolver.LookupPolicy pol = InetAddressResolver.LookupPolicy.of(
                InetAddressResolver.LookupPolicy.IPV4);
        List<InetAddress> out = viaGet.lookupByName("chain-host", pol)
                .collect(java.util.stream.Collectors.toList());
        check(out.size() == 1 && out.get(0) == loop, "provider.get() result usable for lookupByName");
        check(seenHosts.equals(java.util.Arrays.asList("chain-host")),
                "host argument flows through the whole provider->resolver chain");
        check("composed".equals(viaGet.lookupByAddress(new byte[]{127, 0, 0, 1})),
                "reverse lookup flows through the chain");

        // Heterogeneous provider list.
        List<InetAddressResolverProvider> providers = new ArrayList<>();
        providers.add(new NamedProvider("p-a", recording));
        providers.add(new NamedProvider("p-b", recording));
        providers.add(new ThrowingProvider());
        check(providers.size() == 3, "provider list holds heterogeneous subclasses");
        for (int i = 0; i < 2; i++) {
            check(providers.get(i) != null && providers.get(i).name().startsWith("p-"),
                    "polymorphic name() call " + i);
        }
    }

    // ---- Part 4: implementer freedom - nulls and exceptions ----
    static void testImplementerFreedom() throws Exception {
        // name() may return null: no validation at the SPI level (javadoc-only contract).
        InetAddressResolverProvider nullNamed = new InetAddressResolverProvider() {
            public InetAddressResolver get(InetAddressResolverProvider.Configuration cfg) { return null; }
            public String name() { return null; }
        };
        check(nullNamed.name() == null, "name() null return is not rejected");
        check(nullNamed.get(null) == null, "get() null return is not rejected (provider decides)");

        // Thrown exceptions relay unchanged from subclass methods.
        ThrowingProvider throwing = new ThrowingProvider();
        try {
            throwing.name();
            check(false, "name() exception must relay");
        } catch (UnsupportedOperationException e) {
            check("name-unavailable".equals(e.getMessage()), "name() relays exact UOE");
        }
        try {
            throwing.get(null);
            check(false, "get() exception must relay");
        } catch (IllegalStateException e) {
            check("provider-not-ready".equals(e.getMessage()), "get() relays exact ISE");
        }

        // Overriding toString is permitted and observable.
        InetAddressResolverProvider pretty = new InetAddressResolverProvider() {
            public InetAddressResolver get(InetAddressResolverProvider.Configuration cfg) { return null; }
            public String name() { return "pretty"; }
            @Override public String toString() { return "Provider(pretty)"; }
        };
        check("Provider(pretty)".equals(pretty.toString()), "toString override honored");

        // Subclass with additional constructor parameters of any type.
        final AtomicInteger counter = new AtomicInteger();
        class CountingProvider extends InetAddressResolverProvider {
            final int id;
            CountingProvider(int id) { this.id = id; }
            public InetAddressResolver get(InetAddressResolverProvider.Configuration cfg) {
                counter.incrementAndGet();
                return null;
            }
            public String name() { return "cp-" + id; }
        }
        CountingProvider cp = new CountingProvider(7);
        cp.get(null);
        cp.get(null);
        check("cp-7".equals(cp.name()), "constructor state reachable");
        check(counter.get() == 2, "side-effecting get() invoked twice");
    }

    // ---- Part 5: Configuration relationship (shape-only; instance unobtainable) ----
    static void testConfigurationRelationship() throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolverProvider");
        Class<?> conf = Class.forName("java.net.spi.InetAddressResolverProvider$Configuration");
        check(conf.getDeclaringClass() == c, "Configuration is nested in the provider class");
        check("Configuration".equals(conf.getSimpleName()), "Configuration simple name");
        check(InetAddressResolverProvider.Configuration.class == conf,
                "public nested type reference matches reflective lookup");
        check(conf.isSealed(), "Configuration is sealed (probed)");
        Class<?>[] permitted = conf.getPermittedSubclasses();
        check(permitted != null && permitted.length == 1, "one permitted subclass");
        check("sun.net.ResolverProviderConfiguration".equals(permitted[0].getName()),
                "permitted subclass is sun.net.ResolverProviderConfiguration (probed)");
        check(conf.isAssignableFrom(permitted[0]), "permitted class implements Configuration");
        // The configuration instance is only handed out by the JDK to providers being
        // installed via ServiceLoader; it cannot be constructed in-test (sealed + non-public
        // permitted class + Proxy refused). Recorded as a limitation in DESIGN.md.
        check(InetAddressResolverProvider.Configuration.class.getMethod("builtinResolver") != null,
                "builtinResolver method resolvable via the public nested type");
        check(InetAddressResolverProvider.Configuration.class.getMethod("lookupLocalHostName") != null,
                "lookupLocalHostName method resolvable via the public nested type");
        // ServiceLoader can query the provider type without any provider installed.
        try {
            java.util.ServiceLoader<InetAddressResolverProvider> loader =
                    java.util.ServiceLoader.load(InetAddressResolverProvider.class);
            check(loader != null, "ServiceLoader.load returns a loader");
            check(loader.iterator() != null, "ServiceLoader iterator obtainable");
            loader.reload();
            check(true, "reload() harmless with no installed providers");
        } catch (Throwable t) {
            check(false, "ServiceLoader must not throw: " + t);
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWhiteBoxShape();
        testSubclassContract();
        testComposition();
        testImplementerFreedom();
        testConfigurationRelationship();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

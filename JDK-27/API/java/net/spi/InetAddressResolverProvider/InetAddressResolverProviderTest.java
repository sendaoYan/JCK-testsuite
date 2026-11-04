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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

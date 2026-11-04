/*
 * @test
 * @summary java.net.spi.InetAddressResolver conformance: white-box structure + black-box anonymous impl + LookupPolicy.
 * @run main InetAddressResolverTest
 */

import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.spi.InetAddressResolver;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class InetAddressResolverTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolver");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getMethod("lookupByName", String.class, InetAddressResolver.LookupPolicy.class) != null,
                "lookupByName present");
        check(c.getMethod("lookupByAddress", byte[].class) != null, "lookupByAddress present");
    }

    static void testBlackBox() throws Exception {
        final InetAddress loopback = InetAddress.getLoopbackAddress();

        InetAddressResolver resolver = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy policy)
                    throws java.net.UnknownHostException {
                return Stream.of(loopback);
            }
            public String lookupByAddress(byte[] addr) {
                return "loopback";
            }
        };

        List<InetAddress> byName = resolver.lookupByName("localhost",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4)).collect(Collectors.toList());
        check(byName.size() == 1 && byName.get(0) == loopback, "lookupByName returns resolver stream");
        check("loopback".equals(resolver.lookupByAddress(new byte[]{127, 0, 0, 1})), "lookupByAddress returns name");

        // LookupPolicy factory and characteristics.
        int ipv4 = InetAddressResolver.LookupPolicy.IPV4;
        int ipv6 = InetAddressResolver.LookupPolicy.IPV6;
        int ipv4First = InetAddressResolver.LookupPolicy.IPV4_FIRST;
        int ipv6First = InetAddressResolver.LookupPolicy.IPV6_FIRST;

        check(InetAddressResolver.LookupPolicy.of(ipv4).characteristics() == ipv4, "of(IPV4)");
        check(InetAddressResolver.LookupPolicy.of(ipv6).characteristics() == ipv6, "of(IPV6)");
        check(InetAddressResolver.LookupPolicy.of(ipv4 | ipv6).characteristics() == (ipv4 | ipv6), "of(IPV4|IPV6)");
        check(InetAddressResolver.LookupPolicy.of(ipv4 | ipv4First).characteristics() == (ipv4 | ipv4First),
                "of(IPV4|IPV4_FIRST)");

        expect(IllegalArgumentException.class, () -> InetAddressResolver.LookupPolicy.of(0), "of(0) throws");
        expect(IllegalArgumentException.class, () -> InetAddressResolver.LookupPolicy.of(ipv4First), "IPV4_FIRST without IPV4 throws");
        expect(IllegalArgumentException.class,
                () -> InetAddressResolver.LookupPolicy.of(ipv4 | ipv6 | ipv4First | ipv6First),
                "both FIRST flags throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

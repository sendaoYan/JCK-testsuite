/*
 * @test
 * @summary java.net.spi.InetAddressResolver conformance: white-box structure + black-box anonymous impl + LookupPolicy.
 * @run main InetAddressResolverTest
 */

import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.net.spi.InetAddressResolver;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
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
            public Stream< InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy policy)
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

    // ====================== deepening below ======================

    /** Recording implementation: captures every argument of every call. */
    static class RecordingResolver implements InetAddressResolver {
        final List<String> hosts = new ArrayList<>();
        final List<InetAddressResolver.LookupPolicy> policies = new ArrayList<>();
        final List<byte[]> addrs = new ArrayList<>();
        int byNameCalls, byAddressCalls;

        public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy policy) {
            byNameCalls++;
            hosts.add(host);
            policies.add(policy);
            return Stream.of(InetAddress.getLoopbackAddress());
        }

        public String lookupByAddress(byte[] addr) {
            byAddressCalls++;
            addrs.add(addr);
            return "recorded";
        }
    }

    // ---- Part 1: full white-box shape of the two abstract methods (probed) ----
    static void testMethodShape() throws Exception {
        Class<?> c = Class.forName("java.net.spi.InetAddressResolver");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length == 2, "exactly two abstract methods");
        boolean hasUnknownHostException = false;
        for (java.lang.reflect.Method m : ms) {
            check(Modifier.isAbstract(m.getModifiers()), m.getName() + " is abstract");
            check(m.getExceptionTypes().length == 1
                    && m.getExceptionTypes()[0] == UnknownHostException.class,
                    m.getName() + " declares throws UnknownHostException");
            check(!m.isDefault(), m.getName() + " is not a default method");
            check(!Modifier.isStatic(m.getModifiers()), m.getName() + " is not static");
        }
        java.lang.reflect.Method byName = c.getMethod("lookupByName",
                String.class, InetAddressResolver.LookupPolicy.class);
        check(byName.getReturnType() == Stream.class, "lookupByName returns Stream");
        check(byName.getGenericReturnType().getTypeName().contains("java.net.InetAddress"),
                "lookupByName generic return is Stream<InetAddress>");
        check(byName.getDeclaringClass() == c, "lookupByName declared by the interface itself");
        java.lang.reflect.Method byAddr = c.getMethod("lookupByAddress", byte[].class);
        check(byAddr.getReturnType() == String.class, "lookupByAddress returns String");
        check(byAddr.getDeclaringClass() == c, "lookupByAddress declared by the interface itself");
        check(InetAddressResolver.LookupPolicy.of(
                InetAddressResolver.LookupPolicy.IPV4 | InetAddressResolver.LookupPolicy.IPV6) != null,
                "nested LookupPolicy reachable from the interface constant scope");
        check(c.getDeclaredFields().length == 0, "no fields (no interface constants)");
        check(!c.isAnnotationPresent(FunctionalInterface.class),
                "not @FunctionalInterface (two abstract methods)");
        check(!c.isSealed(), "not sealed: open for implementation");
        check(c.getAnnotations().length == 0, "no annotations on the interface");
    }

    // ---- Part 2: argument transparency through a custom implementation ----
    static void testArgumentTransparency() throws Exception {
        RecordingResolver r = new RecordingResolver();

        InetAddressResolver.LookupPolicy p1 = InetAddressResolver.LookupPolicy.of(
                InetAddressResolver.LookupPolicy.IPV4);
        InetAddressResolver.LookupPolicy p2 = InetAddressResolver.LookupPolicy.of(
                InetAddressResolver.LookupPolicy.IPV4 | InetAddressResolver.LookupPolicy.IPV4_FIRST);
        InetAddressResolver.LookupPolicy p3 = InetAddressResolver.LookupPolicy.of(
                InetAddressResolver.LookupPolicy.IPV4 | InetAddressResolver.LookupPolicy.IPV6
                        | InetAddressResolver.LookupPolicy.IPV6_FIRST);

        r.lookupByName("hostA", p1);
        r.lookupByName("hostB", p2);
        r.lookupByName("127.0.0.1", p3);

        check(r.byNameCalls == 3, "lookupByName invoked 3 times");
        check(r.hosts.equals(java.util.Arrays.asList("hostA", "hostB", "127.0.0.1")),
                "host arguments recorded in call order");
        check(r.policies.get(0) == p1 && r.policies.get(1) == p2 && r.policies.get(2) == p3,
                "LookupPolicy instances passed by reference (identity transparency)");
        check(r.policies.get(2).characteristics() == (InetAddressResolver.LookupPolicy.IPV4
                | InetAddressResolver.LookupPolicy.IPV6 | InetAddressResolver.LookupPolicy.IPV6_FIRST),
                "policy characteristics transparent at the implementation side");

        // null host / null policy are only javadoc-level contracts on the built-in resolver;
        // a custom implementation is free to observe them passed through unchanged.
        r.lookupByName(null, null);
        check(r.hosts.get(3) == null, "null host passed through (custom impl contract-free)");
        check(r.policies.get(3) == null, "null policy passed through");

        byte[] a1 = {127, 0, 0, 1};
        byte[] a2 = {0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1};
        r.lookupByAddress(a1);
        r.lookupByAddress(a2);
        check(r.byAddressCalls == 2, "lookupByAddress invoked twice");
        check(r.addrs.get(0) == a1, "byte[] passed by reference");
        check(java.util.Arrays.equals(r.addrs.get(1), a2), "IPv6-length byte[] content transparent");
        check(r.addrs.get(1).length == 16, "IPv6 address length 16 preserved");
        check("recorded".equals(r.lookupByAddress(new byte[]{10, 0, 0, 1})),
                "return value is whatever the implementation chooses");
    }

    // ---- Part 3: Stream semantics of lookupByName ----
    static void testStreamSemantics() throws Exception {
        final InetAddress loop = InetAddress.getByName("127.0.0.1");
        final InetAddress v6 = InetAddress.getByName("::1");

        InetAddressResolver empty = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.empty();
            }
            public String lookupByAddress(byte[] a) { return null; }
        };
        InetAddressResolver dual = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.of(loop, v6);
            }
            public String lookupByAddress(byte[] a) { return null; }
        };
        MutableSingle one = new MutableSingle();

        List<InetAddress> none = empty.lookupByName("x",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4))
                .collect(Collectors.toList());
        check(none.isEmpty(), "empty stream allowed");

        List<InetAddress> both = dual.lookupByName("y",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4
                        | InetAddressResolver.LookupPolicy.IPV6))
                .collect(Collectors.toList());
        check(both.size() == 2, "multi-element stream");
        check(both.get(0) == loop && both.get(1) == v6, "stream preserves encounter order");
        long v4Count = dual.lookupByName("y2",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4))
                .filter(a -> a instanceof java.net.Inet4Address).count();
        check(v4Count == 1, "stream pipeline filter count over dual-family result");
        check(dual.lookupByName("y3",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4))
                .allMatch(InetAddress::isLoopbackAddress), "allMatch over returned stream");

        // A returned stream is single-use (Stream contract surfaces through the SPI).
        Stream<InetAddress> s = one.lookupByName("z",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV6));
        check(s.count() == 1, "single element stream");
        expect(IllegalStateException.class, () -> s.count(), "second terminal operation rejects");

        // Close handler is honored on streams crossing the SPI boundary.
        AtomicInteger closed = new AtomicInteger();
        InetAddressResolver closing = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.of(loop).onClose(closed::incrementAndGet);
            }
            public String lookupByAddress(byte[] a) { return null; }
        };
        try (Stream<InetAddress> cs = closing.lookupByName("w",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4))) {
            check(cs.findFirst().isPresent(), "findFirst works");
        }
        check(closed.get() == 1, "onClose runnable executed by try-with-resources");
    }

    static class MutableSingle implements InetAddressResolver {
        public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
            return Stream.of(InetAddress.getLoopbackAddress());
        }
        public String lookupByAddress(byte[] a) { return null; }
    }

    // ---- Part 4: exception relay ----
    static void testExceptionRelay() throws Exception {
        final UnknownHostException uhe = new UnknownHostException("probe-uhe");
        final IllegalStateException ise = new IllegalStateException("probe-ise");
        InetAddressResolver r = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p)
                    throws UnknownHostException {
                if ("miss".equals(h)) throw uhe;
                if ("state".equals(h)) throw ise;
                return Stream.empty();
            }
            public String lookupByAddress(byte[] a) throws UnknownHostException {
                throw uhe;
            }
        };
        InetAddressResolver.LookupPolicy any =
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4);
        try {
            r.lookupByName("miss", any);
            check(false, "must relay UnknownHostException");
        } catch (UnknownHostException e) {
            check(e == uhe, "exact UnknownHostException instance relayed (identity)");
        }
        try {
            r.lookupByName("state", any);
            check(false, "must relay RuntimeException");
        } catch (IllegalStateException e) {
            check(e == ise, "exact runtime exception instance relayed");
        }
        try {
            r.lookupByAddress(new byte[]{1, 2, 3, 4});
            check(false, "lookupByAddress must relay UHE");
        } catch (UnknownHostException e) {
            check(e == uhe, "lookupByAddress relays exact UHE instance");
        }
        // null return from lookupByAddress is observable (no implicit wrapping).
        InetAddressResolver nullName = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) {
                return Stream.empty();
            }
            public String lookupByAddress(byte[] a) { return null; }
        };
        check(nullName.lookupByAddress(new byte[]{127, 0, 0, 1}) == null,
                "null host name return is not wrapped");
    }

    // ---- Part 5: dynamic-proxy implementation path (probed: interface is proxyable) ----
    static void testProxyImplementation() throws Exception {
        final List<String> called = new ArrayList<>();
        final AtomicReference<Object> lastArg = new AtomicReference<>();
        InetAddressResolver proxy = (InetAddressResolver) java.lang.reflect.Proxy.newProxyInstance(
                InetAddressResolver.class.getClassLoader(),
                new Class<?>[]{InetAddressResolver.class},
                (p, m, as) -> {
                    called.add(m.getName());
                    if (m.getName().equals("lookupByName")) return Stream.of(InetAddress.getLoopbackAddress());
                    if (m.getName().equals("lookupByAddress")) { lastArg.set(as[0]); return "proxied"; }
                    if (m.getName().equals("toString")) return "proxy-resolver";
                    if (m.getName().equals("hashCode")) return System.identityHashCode(p);
                    if (m.getName().equals("equals")) return p == as[0];
                    return null;
                });
        check(proxy != null, "Proxy implements InetAddressResolver");
        check(proxy instanceof InetAddressResolver, "proxy instanceof InetAddressResolver");
        InetAddressResolver.LookupPolicy pol =
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4);
        long n = proxy.lookupByName("proxied-host", pol).count();
        check(n == 1, "proxy lookupByName returns stream of one");
        check(called.get(called.size() - 1).equals("lookupByName"), "invocation handler saw lookupByName");
        byte[] arg = {127, 0, 0, 1};
        check("proxied".equals(proxy.lookupByAddress(arg)), "proxy lookupByAddress return");
        check(lastArg.get() == arg, "proxy relays byte[] argument to handler");
        check(proxy.toString().equals("proxy-resolver"), "proxy toString handled");
    }

    // ---- Part 6: realistic semantics + isolation from the built-in resolver ----
    static void testRealisticSemantics() throws Exception {
        InetAddressResolver literal = new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String host, InetAddressResolver.LookupPolicy p)
                    throws UnknownHostException {
                try {
                    return Stream.of(InetAddress.getByAddress(host,
                            new byte[]{127, 0, 0, 1}));
                } catch (UnknownHostException impossible) {
                    throw new RuntimeException(impossible);
                }
            }
            public String lookupByAddress(byte[] a) throws UnknownHostException {
                try {
                    return InetAddress.getByAddress(a).getHostAddress();
                } catch (UnknownHostException e) {
                    throw e;
                }
            }
        };
        InetAddress r = literal.lookupByName("myhost",
                InetAddressResolver.LookupPolicy.of(InetAddressResolver.LookupPolicy.IPV4))
                .collect(Collectors.toList()).get(0);
        check("myhost".equals(r.getHostName()), "hostname round-trips through resolver implementation");
        check("127.0.0.1".equals(r.getHostAddress()), "address bytes round-trip");
        check(r.isLoopbackAddress(), "loopback classification from byte content");
        check("127.0.0.1".equals(literal.lookupByAddress(new byte[]{127, 0, 0, 1})),
                "reverse lookup implementation returns literal form (no DNS)");
        String v6Text = literal.lookupByAddress(new byte[16]);
        check(v6Text != null && v6Text.contains(":"), "IPv6 textual form contains colon separator");
        // A bad address length surfaces per the SPI's documented IAE/UHE contract on the
        // implementation side (our impl delegates to InetAddress.getByAddress: 5 bytes -> UHE).
        try {
            literal.lookupByAddress(new byte[5]);
            check(false, "5-byte address must fail");
        } catch (UnknownHostException e) {
            check(true, "5-byte address -> UnknownHostException via InetAddress contract");
        }
        // The built-in resolution path is unaffected by custom SPI implementations
        // when no provider is installed (probed: unnamed module, no provider on classpath).
        InetAddress[] builtIn = InetAddress.getAllByName("127.0.0.1");
        check(builtIn.length >= 1 && builtIn[0].isLoopbackAddress(),
                "built-in literal resolution still loopback");
        check(InetAddress.getByName(null) != null, "built-in null-host resolution still works");
    }

    // ---- Part 7: implementer freedom (extra interfaces, state, identity) ----
    static void testImplementerFreedom() {
        RecordingResolver a = new RecordingResolver();
        RecordingResolver b = new RecordingResolver();
        check(a != b && !a.equals(b), "two implementations are distinct objects");
        check(a.equals(a), "implementation equals reflexive");
        check(a.toString() != null && a.toString().contains("RecordingResolver"),
                "default toString names the implementation class");
        check(a.hashCode() == a.hashCode(), "hashCode stable");
        // Interface reference is assignable from any implementer.
        check(InetAddressResolver.class.isInstance(a), "isInstance accepts named subclass");
        List<InetAddressResolver> rs = new ArrayList<>();
        rs.add(a);
        rs.add(new MutableSingle());
        rs.add(new InetAddressResolver() {
            public Stream<InetAddress> lookupByName(String h, InetAddressResolver.LookupPolicy p) { return Stream.empty(); }
            public String lookupByAddress(byte[] x) { return "anon"; }
        });
        check(rs.size() == 3, "heterogeneous implementations coexist");
        try {
            check("anon".equals(rs.get(2).lookupByAddress(new byte[4])), "anonymous impl callable via interface");
        } catch (UnknownHostException e) {
            throw new AssertionError("anonymous impl must not throw", e);
        }
        // Interface object array (Covariant) holds nulls and instances.
        InetAddressResolver[] arr = new InetAddressResolver[]{null, a, null};
        check(arr.length == 3 && arr[1] == a, "arrays accept null elements");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testMethodShape();
        testArgumentTransparency();
        testStreamSemantics();
        testExceptionRelay();
        testProxyImplementation();
        testRealisticSemantics();
        testImplementerFreedom();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

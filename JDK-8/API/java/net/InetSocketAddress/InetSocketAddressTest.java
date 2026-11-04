/*
 * @test
 * @summary java.net.InetSocketAddress conformance: full contract coverage.
 *          all three constructors + createUnresolved, wildcard semantics for a
 *          null InetAddress, exact IllegalArgumentException for out-of-range
 *          ports, getPort/getAddress/getHostName/getHostString/isUnresolved/
 *          toString, equals/hashCode across resolved and unresolved forms,
 *          and serialization round-trip. Resolution uses only the loopback
 *          literal or explicitly supplied host names (no network I/O).
 * @run main InetSocketAddressTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;

public class InetSocketAddressTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    @SuppressWarnings("unchecked")
    static <T> T roundTrip(T obj) throws Exception {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(obj);
        oos.close();
        ObjectInputStream ois = new ObjectInputStream(new ByteArrayInputStream(bos.toByteArray()));
        return (T) ois.readObject();
    }

    static void testPortConstructor() throws Exception {
        for (int port : new int[]{0, 1, 2, 12345, 65534, 65535}) {
            InetSocketAddress a = new InetSocketAddress(port);
            check(a.getPort() == port, "valid port " + port);
            check(a.getAddress().isAnyLocalAddress(), "wildcard address for port " + port);
            check(!a.isUnresolved(), "resolved for port " + port);
        }
        for (int port : new int[]{Integer.MIN_VALUE, -1000, -1, 65536, 65537, Integer.MAX_VALUE}) {
            final int p = port;
            checkThrows(IllegalArgumentException.class, () -> new InetSocketAddress(p), "invalid port " + p);
        }
    }

    static void testInetAddressConstructor() throws Exception {
        InetAddress addr = InetAddress.getByAddress("myhost", new byte[]{10, 0, 0, 1});
        InetSocketAddress a = new InetSocketAddress(addr, 8080);
        check(a.getPort() == 8080, "address ctor port");
        check(addr.equals(a.getAddress()), "address ctor address");
        check("myhost".equals(a.getHostName()), "address ctor hostName");
        check("myhost".equals(a.getHostString()), "address ctor hostString");
        check(!a.isUnresolved(), "address ctor resolved");
        check("myhost/10.0.0.1:8080".equals(a.toString()), "address ctor toString");

        // null address means wildcard
        InetSocketAddress wild = new InetSocketAddress((InetAddress) null, 80);
        check(wild.getAddress().isAnyLocalAddress(), "null address wildcard");
        check(!wild.isUnresolved(), "null address still resolved");
        check(wild.getPort() == 80, "null address port");

        checkThrows(IllegalArgumentException.class, () -> new InetSocketAddress(addr, -1), "address ctor invalid port -1");
        checkThrows(IllegalArgumentException.class, () -> new InetSocketAddress(addr, 65536), "address ctor invalid port 65536");
    }

    static void testStringConstructor() throws Exception {
        InetSocketAddress a = new InetSocketAddress("127.0.0.1", 80);
        check(!a.isUnresolved(), "literal host resolves");
        check(a.getAddress().isLoopbackAddress(), "literal host loopback");
        check("127.0.0.1".equals(a.getHostString()), "literal hostString preserved");
        check(a.getPort() == 80, "literal host port");

        checkThrows(IllegalArgumentException.class, () -> new InetSocketAddress("127.0.0.1", -1), "string ctor invalid port");
        checkThrows(IllegalArgumentException.class, () -> new InetSocketAddress("127.0.0.1", 70000), "string ctor port too large");
    }

    static void testUnresolved() throws Exception {
        InetSocketAddress u = InetSocketAddress.createUnresolved("host", 443);
        check(u.isUnresolved(), "createUnresolved isUnresolved");
        check("host".equals(u.getHostName()), "unresolved getHostName");
        check("host".equals(u.getHostString()), "unresolved getHostString");
        check(null == u.getAddress(), "unresolved getAddress null");
        check(u.getPort() == 443, "unresolved port");
        check("host:443".equals(u.toString()), "unresolved toString");

        checkThrows(IllegalArgumentException.class, () -> InetSocketAddress.createUnresolved(null, 80), "createUnresolved null host");
        checkThrows(IllegalArgumentException.class, () -> InetSocketAddress.createUnresolved("host", -1), "createUnresolved invalid port");
        checkThrows(IllegalArgumentException.class, () -> InetSocketAddress.createUnresolved("host", 65536), "createUnresolved port too large");
    }

    static void testEqualsHashCode() throws Exception {
        InetSocketAddress u1 = InetSocketAddress.createUnresolved("host", 80);
        InetSocketAddress u2 = InetSocketAddress.createUnresolved("HOST", 80);
        check(u1.equals(u2), "unresolved equals host case-insensitive");
        check(u1.hashCode() == u2.hashCode(), "unresolved hashCode");
        check(!u1.equals(InetSocketAddress.createUnresolved("host", 81)), "unresolved different port");
        check(!u1.equals(null), "equals null false");
        check(!u1.equals("host:80"), "equals non-ISA false");

        InetAddress addr = InetAddress.getByAddress("h", new byte[]{10, 0, 0, 1});
        InetSocketAddress r1 = new InetSocketAddress(addr, 80);
        InetSocketAddress r2 = new InetSocketAddress(InetAddress.getByAddress(new byte[]{10, 0, 0, 1}), 80);
        check(r1.equals(r2), "resolved equals on address bytes");
        check(r1.hashCode() == r2.hashCode(), "resolved hashCode");
        check(!r1.equals(InetSocketAddress.createUnresolved("10.0.0.1", 80)), "resolved != unresolved");
        check(!r1.equals(new InetSocketAddress(addr, 81)), "resolved different port");
    }

    static void testSerialization() throws Exception {
        InetAddress addr = InetAddress.getByAddress("h", new byte[]{10, 0, 0, 1});
        InetSocketAddress r = roundTrip(new InetSocketAddress(addr, 8080));
        check(!r.isUnresolved(), "serialized resolved");
        check(r.getPort() == 8080, "serialized port");
        check(r.getAddress().equals(addr), "serialized address");

        InetSocketAddress u = roundTrip(InetSocketAddress.createUnresolved("host", 443));
        check(u.isUnresolved(), "serialized unresolved");
        check("host".equals(u.getHostString()), "serialized unresolved host");
        check(u.getPort() == 443, "serialized unresolved port");
    }

    // ------------------------------------------------------------------
    // Deepening: exact negative messages + wildcard/unresolved semantics
    // (all probed identical on JDK-8 and JDK-27 unless noted)
    // ------------------------------------------------------------------

    static void testPortValidationExactMessages() {
        int[] bad = {-1, -1000, Integer.MIN_VALUE, 65536, 65537, Integer.MAX_VALUE};
        for (int port : bad) {
            final int p = port;
            try {
                new InetSocketAddress(p);
                check(false, "ISA(" + p + ") must throw");
            } catch (IllegalArgumentException e) {
                check(("port out of range:" + p).equals(e.getMessage()),
                        "exact message for port " + p);
            }
            try {
                new InetSocketAddress(InetAddress.getLoopbackAddress(), p);
                check(false, "ISA(addr," + p + ") must throw");
            } catch (IllegalArgumentException e) {
                check(("port out of range:" + p).equals(e.getMessage()),
                        "exact message ISA(addr," + p + ")");
            }
            try {
                new InetSocketAddress("127.0.0.1", p);
                check(false, "ISA(str," + p + ") must throw");
            } catch (IllegalArgumentException e) {
                check(("port out of range:" + p).equals(e.getMessage()),
                        "exact message ISA(str," + p + ")");
            }
            try {
                InetSocketAddress.createUnresolved("h", p);
                check(false, "createUnresolved(h," + p + ") must throw");
            } catch (IllegalArgumentException e) {
                check(("port out of range:" + p).equals(e.getMessage()),
                        "exact message createUnresolved(h," + p + ")");
            }
        }
        // boundary values are legal
        for (int port : new int[]{0, 1, 65535}) {
            check(new InetSocketAddress(port).getPort() == port, "legal port " + port);
            check(InetSocketAddress.createUnresolved("h", port).getPort() == port,
                    "legal unresolved port " + port);
        }
    }

    static void testNullHostRejected() {
        // exact probed message "hostname can't be null"
        try {
            InetSocketAddress.createUnresolved(null, 80);
            check(false, "createUnresolved(null,80) must throw");
        } catch (IllegalArgumentException e) {
            check("hostname can't be null".equals(e.getMessage()), "createUnresolved(null) message");
        }
        try {
            new InetSocketAddress((String) null, 80);
            check(false, "ISA((String)null,80) must throw");
        } catch (IllegalArgumentException e) {
            check("hostname can't be null".equals(e.getMessage()), "ISA((String)null) message");
        }
    }

    static void testWildcardSemantics() throws Exception {
        InetSocketAddress w1 = new InetSocketAddress(80);
        InetSocketAddress w2 = new InetSocketAddress(80);
        check(w1.getAddress() != null, "wildcard address non-null");
        check(w1.getAddress().isAnyLocalAddress(), "wildcard isAnyLocalAddress");
        check("0.0.0.0".equals(w1.getAddress().getHostAddress()), "wildcard host address 0.0.0.0");
        check("0.0.0.0/0.0.0.0:80".equals(w1.toString()), "wildcard toString exact");
        check(w1.equals(w2), "two wildcards equal");
        check(w1.hashCode() == w2.hashCode(), "wildcard hashCode equal");
        check(!w1.isUnresolved(), "wildcard resolved");
        check(w1.getHostString() != null, "wildcard hostString non-null");
        check("0.0.0.0".equals(w1.getHostString()), "wildcard hostString 0.0.0.0");
        check(!w1.equals(new InetSocketAddress(81)), "wildcard different port not equal");
        check(!w1.equals(new InetSocketAddress("127.0.0.1", 80)),
                "wildcard not equal loopback-resolved same port");
        // null InetAddress constructor = wildcard
        InetSocketAddress w3 = new InetSocketAddress((InetAddress) null, 80);
        check(w3.equals(w1), "null-address ctor equals wildcard");
        check(w3.hashCode() == w1.hashCode(), "null-address hashCode equals wildcard");
        check(w3.getAddress().isAnyLocalAddress(), "null-address wildcard address");
    }

    static void testUnresolvedDeep() {
        // reserved .invalid TLD can never resolve; no DNS dependence
        InetSocketAddress u = InetSocketAddress.createUnresolved("no.such.host.invalid", 80);
        check(u.isUnresolved(), "invalid host unresolved");
        check("no.such.host.invalid".equals(u.getHostName()),
                "unresolved getHostName returns literal without resolution");
        check("no.such.host.invalid".equals(u.getHostString()), "unresolved hostString");
        check(null == u.getAddress(), "unresolved address null");
        check(u.getPort() == 80, "unresolved port");
        // host string case is preserved by both accessors (probed)
        InetSocketAddress mixed = InetSocketAddress.createUnresolved("HoSt", 80);
        check("HoSt".equals(mixed.getHostString()), "hostString preserves case");
        check("HoSt".equals(mixed.getHostName()), "getHostName preserves case");
        // equals compares hostname case-insensitively + port
        InetSocketAddress lower = InetSocketAddress.createUnresolved("host", 80);
        check(mixed.equals(lower), "unresolved equals case-insensitive");
        check(mixed.hashCode() == lower.hashCode(), "unresolved hashCode case-insensitive");
        check(!mixed.equals(InetSocketAddress.createUnresolved("host", 81)), "unresolved port differs");
        check(!mixed.equals(InetSocketAddress.createUnresolved("other", 80)), "unresolved host differs");
        check(!mixed.equals(null), "unresolved equals null false");
        check(!mixed.equals("HoSt:80"), "unresolved equals other type false");
        check(mixed.toString() != null && mixed.toString().contains("80"), "unresolved toString has port");
    }

    static void testResolvedVsUnresolvedEquality() throws Exception {
        InetAddress addr = InetAddress.getByAddress("h", new byte[]{10, 0, 0, 1});
        InetSocketAddress res = new InetSocketAddress(addr, 80);
        InetSocketAddress unres = InetSocketAddress.createUnresolved("10.0.0.1", 80);
        check(!res.equals(unres), "resolved != unresolved (same literal text)");
        check(!unres.equals(res), "unresolved != resolved symmetric");
        // resolved equality is on InetAddress + port; host name irrelevant
        InetAddress sameBytesUnnamed = InetAddress.getByAddress(new byte[]{10, 0, 0, 1});
        check(res.equals(new InetSocketAddress(sameBytesUnnamed, 80)),
                "resolved equality ignores host name of InetAddress");
        check(res.hashCode() == new InetSocketAddress(sameBytesUnnamed, 80).hashCode(),
                "resolved hashCode ignores host name");
        // literal-resolved loopback
        InetSocketAddress loop = new InetSocketAddress("127.0.0.1", 80);
        check(!loop.isUnresolved(), "literal loopback resolved");
        check(loop.getAddress().isLoopbackAddress(), "literal loopback address");
        check(loop.getAddress().equals(InetAddress.getByName("127.0.0.1")),
                "literal address bytes equal");
        check(!loop.equals(new InetSocketAddress("127.0.0.1", 81)), "different port not equal");
        check(loop.equals(new InetSocketAddress(InetAddress.getByName("127.0.0.1"), 80)),
                "literal equals address-constructed");
        // getHostString on a resolved-from-literal socket address never triggers DNS
        check("127.0.0.1".equals(loop.getHostString()), "loopback hostString literal");
        // getHostName may consult reverse DNS: tolerate literal or hosts-file name
        String hn = loop.getHostName();
        check("127.0.0.1".equals(hn) || "localhost".equals(hn),
                "loopback getHostName is literal or localhost (env dependent)");
    }

    static void testIPv6Forms() throws Exception {
        InetSocketAddress v6 = new InetSocketAddress("::1", 80);
        check(!v6.isUnresolved(), "v6 literal resolves");
        check(v6.getAddress() instanceof java.net.Inet6Address, "v6 address type");
        check(v6.getAddress().isLoopbackAddress(), "v6 literal loopback");
        // hostString is the normalised full IPv6 text, not the compressed input (probed)
        check("0:0:0:0:0:0:0:1".equals(v6.getHostString()), "v6 hostString normalised form");
        check(v6.getPort() == 80, "v6 port");
        // JDK-8 toString has no brackets around the v6 text (probed; JDK-27 adds them)
        check("/0:0:0:0:0:0:0:1:80".equals(v6.toString()), "v6 toString exact (JDK-8 form)");
        check(v6.equals(new InetSocketAddress(InetAddress.getByName("::1"), 80)),
                "v6 equals address-constructed");
        check(!v6.equals(new InetSocketAddress("127.0.0.1", 80)), "v6 != v4 loopback same port");
        InetSocketAddress namedV6 = new InetSocketAddress(
                InetAddress.getByAddress("v6host", new byte[16]), 9);
        check("v6host".equals(namedV6.getHostString()), "named v6 hostString");
        check(namedV6.getAddress().isAnyLocalAddress(), "named v6 any-local address");
    }

    static void testSerializationDeep() throws Exception {
        // wildcard round trip
        InetSocketAddress w = roundTrip(new InetSocketAddress(77));
        check(!w.isUnresolved(), "serialized wildcard resolved");
        check(w.getPort() == 77, "serialized wildcard port");
        check(w.getAddress().isAnyLocalAddress(), "serialized wildcard any-local");
        check("0.0.0.0/0.0.0.0:77".equals(w.toString()), "serialized wildcard toString");
        // resolved keeps name, port, bytes
        InetAddress addr = InetAddress.getByAddress("keep", new byte[]{10, 9, 8, 7});
        InetSocketAddress r = roundTrip(new InetSocketAddress(addr, 1234));
        check("keep".equals(r.getHostString()), "serialized resolved host name kept");
        check("10.9.8.7".equals(r.getAddress().getHostAddress()), "serialized resolved address");
        check(r.getPort() == 1234, "serialized resolved port");
        check(r.getAddress().equals(InetAddress.getByAddress(new byte[]{10, 9, 8, 7})),
                "serialized resolved equals fresh");
        check(r.hashCode() == new InetSocketAddress(
                InetAddress.getByAddress(new byte[]{10, 9, 8, 7}), 1234).hashCode(),
                "serialized hashCode contract");
        // unresolved round trip
        InetSocketAddress u = roundTrip(InetSocketAddress.createUnresolved("gone.host.invalid", 99));
        check(u.isUnresolved(), "serialized unresolved stays unresolved");
        check("gone.host.invalid".equals(u.getHostString()), "serialized unresolved host");
        check(u.getPort() == 99, "serialized unresolved port");
        check(null == u.getAddress(), "serialized unresolved address null");
        // usable as SocketAddress (superclass) after round trip
        java.net.SocketAddress sa = roundTrip((java.net.SocketAddress) new InetSocketAddress(5));
        check(sa instanceof InetSocketAddress, "round trip via SocketAddress keeps class");
        check(((InetSocketAddress) sa).getPort() == 5, "SocketAddress round trip port");
    }

    public static void main(String[] args) throws Exception {
        testPortConstructor();
        testInetAddressConstructor();
        testStringConstructor();
        testUnresolved();
        testEqualsHashCode();
        testSerialization();
        testPortValidationExactMessages();
        testNullHostRejected();
        testWildcardSemantics();
        testUnresolvedDeep();
        testResolvedVsUnresolvedEquality();
        testIPv6Forms();
        testSerializationDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

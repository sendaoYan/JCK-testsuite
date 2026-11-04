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

    public static void main(String[] args) throws Exception {
        testPortConstructor();
        testInetAddressConstructor();
        testStringConstructor();
        testUnresolved();
        testEqualsHashCode();
        testSerialization();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

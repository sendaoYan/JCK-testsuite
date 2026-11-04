/*
 * @test
 * @summary java.net.DatagramPacket conformance: full contract coverage. all six
 *          constructors, getAddress/getPort/getData/getOffset/getLength,
 *          setData/setAddress/setPort/setSocketAddress/setLength, buffer/offset/
 *          length boundary validation (IAE/NPE), unresolved-address rejection
 *          (IAE), setData(byte[]) resetting offset, getSocketAddress round-trip,
 *          and defensive behavior of the address accessors. No network I/O.
 * @run main DatagramPacketTest
 */

import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

public class DatagramPacketTest {
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

    static class PlainAddress extends SocketAddress {
    }

    static void testSimpleConstructors() throws Exception {
        byte[] buf = new byte[16];
        DatagramPacket p = new DatagramPacket(buf, 16);
        check(p.getData() == buf, "getData returns same buffer");
        check(p.getLength() == 16, "length");
        check(p.getOffset() == 0, "offset default 0");
        check(p.getAddress() == null, "no address by default");
        check(p.getPort() == -1, "port -1 by default");
        // JDK-8 getSocketAddress() rebuilds InetSocketAddress(null, -1) -> IAE.
        checkThrows(IllegalArgumentException.class, () -> p.getSocketAddress(), "getSocketAddress with no address -> IAE");

        DatagramPacket q = new DatagramPacket(buf, 3, 8);
        check(q.getOffset() == 3, "explicit offset");
        check(q.getLength() == 8, "explicit length");
        check(q.getData() == buf, "explicit offset same buffer");
    }

    static void testAddressConstructors() throws Exception {
        byte[] buf = new byte[16];
        InetAddress la = InetAddress.getLoopbackAddress();

        DatagramPacket p = new DatagramPacket(buf, 8, la, 9999);
        check(p.getAddress().equals(la), "4-arg getAddress");
        check(p.getPort() == 9999, "4-arg getPort");
        check(p.getLength() == 8, "4-arg length");
        check(p.getOffset() == 0, "4-arg offset 0");

        DatagramPacket q = new DatagramPacket(buf, 4, 6, la, 8080);
        check(q.getOffset() == 4, "6-arg offset");
        check(q.getLength() == 6, "6-arg length");
        check(q.getAddress().equals(la), "6-arg address");
        check(q.getPort() == 8080, "6-arg port");

        InetSocketAddress sa = new InetSocketAddress(la, 1234);
        DatagramPacket r = new DatagramPacket(buf, 8, sa);
        check(r.getSocketAddress().equals(sa), "SocketAddress ctor getSocketAddress");
        check(r.getAddress().equals(la), "SocketAddress ctor address");
        check(r.getPort() == 1234, "SocketAddress ctor port");

        DatagramPacket s = new DatagramPacket(buf, 3, 9, sa);
        check(s.getOffset() == 3, "offset+SocketAddress offset");
        check(s.getLength() == 9, "offset+SocketAddress length");
        check(s.getSocketAddress().equals(sa), "offset+SocketAddress addr");
    }

    static void testSetters() throws Exception {
        byte[] buf = new byte[16];
        DatagramPacket p = new DatagramPacket(buf, 8, InetAddress.getLoopbackAddress(), 9999);

        p.setAddress(InetAddress.getLoopbackAddress());
        p.setPort(1000);
        check(p.getPort() == 1000, "setPort");
        check(p.getAddress().isLoopbackAddress(), "setAddress");

        p.setLength(4);
        check(p.getLength() == 4, "setLength");

        byte[] newBuf = new byte[32];
        p.setData(newBuf);
        check(p.getData() == newBuf, "setData(byte[]) buffer");
        check(p.getOffset() == 0, "setData(byte[]) resets offset");
        check(p.getLength() == 32, "setData(byte[]) length = buffer length");

        p.setData(buf, 2, 6);
        check(p.getData() == buf, "setData(buf,off,len) buffer");
        check(p.getOffset() == 2, "setData offset");
        check(p.getLength() == 6, "setData length");

        InetSocketAddress sa = new InetSocketAddress(InetAddress.getLoopbackAddress(), 1234);
        p.setSocketAddress(sa);
        check(p.getSocketAddress().equals(sa), "setSocketAddress round trip");
        check(p.getPort() == 1234, "setSocketAddress port");
    }

    static void testBoundaries() throws Exception {
        byte[] buf = new byte[10];
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, -1), "negative length");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 11), "length > buffer");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 2, 9), "offset+length > buffer");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, -1, 1), "negative offset");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(new byte[1024], Integer.MAX_VALUE, 1), "offset overflow");
        checkThrows(NullPointerException.class, () -> new DatagramPacket(null, 10), "null buffer");

        InetAddress la = InetAddress.getLoopbackAddress();
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 10, la, -1), "negative port");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 10, la, Integer.MAX_VALUE), "port too large");

        DatagramPacket p = new DatagramPacket(buf, 10);
        checkThrows(IllegalArgumentException.class, () -> p.setLength(-1), "setLength negative");
        checkThrows(IllegalArgumentException.class, () -> p.setLength(11), "setLength too large");
        checkThrows(NullPointerException.class, () -> p.setData(null, 0, 0), "setData null buffer");
        checkThrows(NullPointerException.class, () -> p.setData(null), "setData(byte[]) null");
        checkThrows(IllegalArgumentException.class, () -> p.setPort(-1), "setPort negative");
        checkThrows(IllegalArgumentException.class, () -> p.setPort(65536), "setPort too large");
    }

    static void testUnresolvedAndBadAddress() throws Exception {
        byte[] buf = new byte[10];
        InetSocketAddress unresolved = InetSocketAddress.createUnresolved("foo.bar", 161);
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 5, unresolved), "unresolved SocketAddress ctor");

        DatagramPacket p = new DatagramPacket(buf, 10);
        checkThrows(IllegalArgumentException.class, () -> p.setSocketAddress(unresolved), "setSocketAddress unresolved");
        checkThrows(IllegalArgumentException.class, () -> p.setSocketAddress(new PlainAddress()), "setSocketAddress wrong type");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 5, new PlainAddress()), "ctor wrong SocketAddress type");
    }

    public static void main(String[] args) throws Exception {
        testSimpleConstructors();
        testAddressConstructors();
        testSetters();
        testBoundaries();
        testUnresolvedAndBadAddress();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

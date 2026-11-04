/*
 * @test
 * @summary java.net.DatagramPacket conformance: full contract coverage. all six
 *          constructors, getAddress/getPort/getData/getOffset/getLength,
 *          setData/setAddress/setPort/setSocketAddress/setLength, buffer/offset/
 *          length boundary validation (IAE/NPE), unresolved-address rejection
 *          (IAE), setData(byte[]) resetting offset, getSocketAddress round-trip,
 *          and defensive behavior of the address accessors. Deepened: exact
 *          "Port out of range:" messages, legal boundary matrix (len 0,
 *          offset+length == buf.length, port 0/65535), setLength capacity
 *          semantics, setData reset matrix, null-address legality, live-buffer
 *          visibility, and a loopback send/receive round-trip proving receive
 *          fills address/port, shrinks length and preserves offset.
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

    // Deepened: legal constructor boundary matrix. Each legal cell asserts the
    // exact offset/length/data-identity triple; each illegal cell asserts IAE.
    static void testConstructorBoundaryMatrix() throws Exception {
        byte[] buf = new byte[10];

        DatagramPacket a = new DatagramPacket(buf, 0);
        check(a.getOffset() == 0 && a.getLength() == 0, "len 0 legal");
        check(a.getData() == buf, "len 0 same buffer");

        DatagramPacket b = new DatagramPacket(buf, buf.length);
        check(b.getOffset() == 0 && b.getLength() == buf.length, "len == buf.length legal");
        check(b.getData() == buf, "full buffer identity");

        DatagramPacket c = new DatagramPacket(buf, buf.length, 0);
        check(c.getOffset() == buf.length && c.getLength() == 0, "offset == buf.length, len 0 legal");

        DatagramPacket d = new DatagramPacket(buf, 0, 0);
        check(d.getOffset() == 0 && d.getLength() == 0, "offset 0 len 0 legal");

        DatagramPacket e = new DatagramPacket(buf, 0, buf.length);
        check(e.getOffset() == 0 && e.getLength() == buf.length, "offset 0, len == buf.length legal");

        DatagramPacket f = new DatagramPacket(buf, 4, buf.length - 4);
        check(f.getOffset() == 4 && f.getLength() == 6, "offset+len == buf.length legal");

        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, buf.length + 1, 0),
                "offset just past buffer illegal");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 0, -0 - 1),
                "negative length illegal");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, Integer.MIN_VALUE, 1),
                "Integer.MIN_VALUE offset illegal");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 0, Integer.MIN_VALUE),
                "Integer.MIN_VALUE length illegal");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(buf, 5, 6),
                "offset+len one past buffer illegal");
        checkThrows(IllegalArgumentException.class, () -> new DatagramPacket(new byte[0], 1),
                "empty buffer, len 1 illegal");
        DatagramPacket empty = new DatagramPacket(new byte[0], 0);
        check(empty.getLength() == 0 && empty.getOffset() == 0, "empty buffer len 0 legal");
    }

    // Deepened: port boundary matrix with exact messages. "Port out of range:<v>"
    // is produced by both JDK-8 and JDK-27 (capital P), probed 2026-11-05.
    static void testPortBoundaryMatrix() throws Exception {
        byte[] buf = new byte[4];
        InetAddress la = InetAddress.getLoopbackAddress();
        int[] bad = {-2, -1, 65536, 65537, Integer.MAX_VALUE, Integer.MIN_VALUE};
        for (int p : bad) {
            final int port = p;
            try {
                new DatagramPacket(buf, 4, la, port);
                throw new AssertionError("ctor 4-arg port " + port + " accepted");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().equals("Port out of range:" + port),
                        "4-arg ctor msg for " + port + ": " + e.getMessage());
            }
            try {
                new DatagramPacket(buf, 0, 4, la, port);
                throw new AssertionError("ctor 6-arg port " + port + " accepted");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().equals("Port out of range:" + port),
                        "6-arg ctor msg for " + port + ": " + e.getMessage());
            }
            DatagramPacket q = new DatagramPacket(buf, 4);
            try {
                q.setPort(port);
                throw new AssertionError("setPort " + port + " accepted");
            } catch (IllegalArgumentException e) {
                check(e.getMessage().equals("Port out of range:" + port),
                        "setPort msg for " + port + ": " + e.getMessage());
            }
        }
        // legal extremes round-trip in both directions
        DatagramPacket lo0 = new DatagramPacket(buf, 4, la, 0);
        check(lo0.getPort() == 0, "port 0 legal");
        DatagramPacket hi = new DatagramPacket(buf, 4, la, 65535);
        check(hi.getPort() == 65535, "port 65535 legal");
        DatagramPacket six = new DatagramPacket(buf, 0, 4, la, 65535);
        check(six.getPort() == 65535, "6-arg port 65535 legal");
        six.setPort(0);
        check(six.getPort() == 0, "setPort 0 legal");
        six.setPort(1);
        check(six.getPort() == 1, "setPort 1 legal");
        six.setPort(65535);
        check(six.getPort() == 65535, "setPort 65535 legal");
        // SocketAddress flavour
        DatagramPacket sa0 = new DatagramPacket(buf, 4, new InetSocketAddress(la, 0));
        check(sa0.getPort() == 0, "SA ctor port 0 legal");
        DatagramPacket sa65535 = new DatagramPacket(buf, 4, new InetSocketAddress(la, 65535));
        check(sa65535.getPort() == 65535, "SA ctor port 65535 legal");
    }

    // Deepened: null semantics across constructors and setters.
    static void testNullSemantics() throws Exception {
        byte[] buf = new byte[4];
        InetAddress la = InetAddress.getLoopbackAddress();

        checkThrows(NullPointerException.class, () -> new DatagramPacket(null, 4), "ctor(null,len) NPE");
        checkThrows(NullPointerException.class, () -> new DatagramPacket(null, 0, 4), "ctor(null,off,len) NPE");
        checkThrows(NullPointerException.class, () -> new DatagramPacket(null, 4, la, 9), "4-arg null buf NPE");
        checkThrows(NullPointerException.class, () -> new DatagramPacket(null, 0, 4, la, 9), "6-arg null buf NPE");

        // null address in InetAddress ctor is legal ("not yet set")
        DatagramPacket p = new DatagramPacket(buf, 4, (InetAddress) null, 9);
        check(p.getAddress() == null, "null address stored as null");
        check(p.getPort() == 9, "port kept with null address");

        // setAddress(null) legal, clears address only
        DatagramPacket q = new DatagramPacket(buf, 4, la, 7);
        q.setAddress(null);
        check(q.getAddress() == null, "setAddress(null) clears address");
        check(q.getPort() == 7, "setAddress(null) keeps port");

        // setSocketAddress(null)/wrong-type rejected with exact shared message
        try {
            q.setSocketAddress(null);
            throw new AssertionError("setSocketAddress(null) accepted");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("unsupported address type"),
                    "setSocketAddress(null) msg: " + e.getMessage());
        }
        checkThrows(IllegalArgumentException.class,
                () -> new DatagramPacket(buf, 4, (SocketAddress) null), "ctor null SocketAddress");
    }

    // Deepened: setLength capacity semantics (bufLen/capLen). Probed: capacity is
    // the buffer length as installed by the last setData; offset+length must fit.
    static void testSetLengthCapacityMatrix() throws Exception {
        byte[] buf = new byte[10];
        byte[] big = new byte[100];

        DatagramPacket a = new DatagramPacket(buf, 5);
        check(a.getLength() == 5, "initial len 5");
        a.setLength(10);
        check(a.getLength() == 10, "grow to buf.length legal");
        checkThrows(IllegalArgumentException.class, () -> a.setLength(11), "past buf.length illegal");
        a.setLength(0);
        check(a.getLength() == 0, "shrink to 0 legal");
        a.setLength(5);
        check(a.getLength() == 5, "re-grow to 5");

        // offset participates in the check: offset+length <= buf.length
        DatagramPacket b = new DatagramPacket(buf, 2, 5);
        b.setLength(8);
        check(b.getOffset() == 2 && b.getLength() == 8, "off+grow to exact end legal");
        checkThrows(IllegalArgumentException.class, () -> b.setLength(9), "off+9 one past end illegal");
        b.setLength(0);
        check(b.getLength() == 0 && b.getOffset() == 2, "shrink keeps offset");
        checkThrows(IllegalArgumentException.class, () -> b.setLength(-1), "negative setLength");

        // capacity follows the last setData, not the original constructor length
        DatagramPacket c = new DatagramPacket(buf, 2);
        c.setData(big, 0, 3);
        check(c.getLength() == 3, "setData installs new length");
        c.setLength(100);
        check(c.getLength() == 100, "grow to new buffer length legal after setData");
        checkThrows(IllegalArgumentException.class, () -> c.setLength(101), "past new buffer illegal");

        DatagramPacket d = new DatagramPacket(big, 3);
        d.setLength(100);
        check(d.getLength() == 100, "ctor(big,3) grow to 100 legal");

        // getData().length is the raw buffer, unaffected by length shrink
        DatagramPacket e = new DatagramPacket(big, 100);
        e.setLength(7);
        check(e.getData().length == 100, "buffer length unchanged by setLength");
        check(e.getLength() == 7, "logical length shrunk");
    }

    // Deepened: setData reset matrix and live-buffer visibility.
    static void testSetDataMatrix() throws Exception {
        byte[] b1 = new byte[8];
        byte[] b2 = new byte[32];
        InetAddress la = InetAddress.getLoopbackAddress();

        DatagramPacket p = new DatagramPacket(b1, 2, 6, la, 5);
        check(p.getOffset() == 2 && p.getLength() == 6, "pre-set state");
        p.setData(b2);
        check(p.getData() == b2, "setData(byte[]) swaps buffer");
        check(p.getOffset() == 0, "setData(byte[]) resets offset");
        check(p.getLength() == b2.length, "setData(byte[]) length = buffer length");
        check(p.getAddress() != null && p.getPort() == 5, "setData keeps address/port");

        p.setData(b2, 3, 4);
        check(p.getOffset() == 3 && p.getLength() == 4, "setData(b,3,4) exact");
        p.setData(b2, 0, 0);
        check(p.getOffset() == 0 && p.getLength() == 0, "setData(b,0,0) legal");
        p.setData(b2, b2.length, 0);
        check(p.getOffset() == b2.length && p.getLength() == 0, "setData(b,len,0) legal");
        checkThrows(IllegalArgumentException.class, () -> p.setData(b2, 1, b2.length), "setData overruns");
        checkThrows(IllegalArgumentException.class, () -> p.setData(b2, -1, 2), "setData negative offset");
        checkThrows(IllegalArgumentException.class, () -> p.setData(b2, 1, -1), "setData negative length");
        checkThrows(IllegalArgumentException.class, () -> p.setData(b2, 0, b2.length + 1), "setData length past buffer");

        // buffer is live: external writes are visible through getData()
        DatagramPacket live = new DatagramPacket(b1, b1.length);
        b1[3] = 42;
        check(live.getData()[3] == 42, "live buffer write visible");
        live.getData()[3] = 43;
        check(b1[3] == 43, "getData writes propagate");
        // setData(byte[]) then shrink does not copy
        byte[] b3 = {9, 8, 7};
        DatagramPacket q = new DatagramPacket(b3, 3);
        q.setLength(1);
        check(q.getData() == b3, "shrink keeps same buffer reference");
        check(b3[2] == 7, "trailing bytes intact after shrink");
    }

    // Deepened: address/port/setSocketAddress cross matrix.
    static void testAddressPortMatrix() throws Exception {
        byte[] buf = new byte[4];
        InetAddress la = InetAddress.getLoopbackAddress();
        DatagramPacket p = new DatagramPacket(buf, 4);

        p.setSocketAddress(new InetSocketAddress(la, 40000));
        check(p.getAddress().equals(la), "setSocketAddress sets address");
        check(p.getPort() == 40000, "setSocketAddress sets port");
        check(p.getSocketAddress().equals(new InetSocketAddress(la, 40000)), "round trip equals");

        p.setAddress(InetAddress.getByName("127.0.0.1"));
        check(p.getAddress().isLoopbackAddress(), "setAddress loopback literal");
        p.setPort(1);
        check(p.getPort() == 1, "setPort minimal");
        SocketAddress sa = p.getSocketAddress();
        check(sa instanceof InetSocketAddress, "getSocketAddress type");
        check(((InetSocketAddress) sa).getPort() == 1, "getSocketAddress port");
        check(((InetSocketAddress) sa).getAddress().isLoopbackAddress(), "getSocketAddress address");

        // exchange buffers/addresses independently: state orthogonal
        p.setData(new byte[6]);
        check(p.getPort() == 1 && p.getAddress() != null, "setData leaves routing state");
        p.setSocketAddress(new InetSocketAddress(la, 65535));
        check(p.getPort() == 65535, "setSocketAddress replaces port");
    }

    // Deepened: identity/toString/hashCode (Object defaults, not overridden).
    static void testObjectContracts() throws Exception {
        byte[] buf = new byte[4];
        DatagramPacket p = new DatagramPacket(buf, 4);
        DatagramPacket q = new DatagramPacket(buf, 4);
        check(p.equals(p), "reflexive equals");
        check(!p.equals(q), "distinct packets not equal (no value equals)");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
        check(p.toString() != null && p.toString().contains("DatagramPacket"), "toString mentions class");
    }

    // Deepened: loopback send/receive round-trip. Receive must fill the packet
    // with sender address/port, shrink length to the actual payload and keep
    // the caller's offset; bytes beyond the received length stay untouched.
    static void testLoopbackRoundTrip() throws Exception {
        java.net.DatagramSocket sender = null;
        java.net.DatagramSocket receiver = null;
        try {
            sender = new java.net.DatagramSocket(0);
            receiver = new java.net.DatagramSocket(0);
            receiver.setSoTimeout(500);

            byte[] payload = {(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE, (byte) 0x42};
            DatagramPacket out = new DatagramPacket(payload, 1, 4,
                    InetAddress.getLoopbackAddress(), receiver.getLocalPort());
            sender.send(out);

            byte[] rbuf = new byte[64];
            java.util.Arrays.fill(rbuf, (byte) 0x7F);
            DatagramPacket in = new DatagramPacket(rbuf, 8, 48);
            receiver.receive(in);
            check(in.getLength() == 4, "length shrunk to actual payload, got " + in.getLength());
            check(in.getOffset() == 8, "receive preserves offset");
            check(in.getAddress() != null && in.getAddress().isLoopbackAddress(), "receive fills sender address");
            check(in.getPort() == sender.getLocalPort(), "receive fills sender port");
            // send window is payload[offset, offset+length) = [1,5) = FE BA BE 42
            check(rbuf[8] == (byte) 0xFE && rbuf[9] == (byte) 0xBA && rbuf[10] == (byte) 0xBE
                    && rbuf[11] == (byte) 0x42, "payload bytes exact at offset");
            check(rbuf[7] == (byte) 0x7F && rbuf[12] == (byte) 0x7F, "bytes outside window untouched");
            check(in.getSocketAddress() instanceof InetSocketAddress, "getSocketAddress after receive");

            // empty datagram: length 0 is deliverable on loopback
            byte[] zero = new byte[1];
            sender.send(new DatagramPacket(zero, 0, InetAddress.getLoopbackAddress(), receiver.getLocalPort()));
            DatagramPacket in2 = new DatagramPacket(new byte[4], 4);
            receiver.receive(in2);
            check(in2.getLength() == 0, "zero-length datagram received as length 0");
            check(in2.getPort() == sender.getLocalPort(), "zero-length sender port filled");

            // reusing a packet: receive again shrinks length from previous value
            byte[] rbuf2 = new byte[16];
            DatagramPacket in3 = new DatagramPacket(rbuf2, 2, 14);
            byte[] second = {1, 2, 3};
            sender.send(new DatagramPacket(second, 3, InetAddress.getLoopbackAddress(), receiver.getLocalPort()));
            receiver.receive(in3);
            check(in3.getLength() == 3 && in3.getOffset() == 2, "reused packet length shrunk to 3, offset kept");
        } finally {
            if (sender != null) sender.close();
            if (receiver != null) receiver.close();
        }
    }

    // Deepened: exhaustive table-driven (offset,length) acceptance matrix for
    // every offset/length combination against a 10-byte buffer: accepted iff
    // offset >= 0 && length >= 0 && offset + length <= buf.length.
    static void testOffsetLengthAcceptanceMatrix() throws Exception {
        byte[] buf = new byte[10];
        int[] offsets = {Integer.MIN_VALUE, -1, 0, 3, 10, 11, Integer.MAX_VALUE};
        int[] lengths = {Integer.MIN_VALUE, -1, 0, 3, 10, 11, Integer.MAX_VALUE};
        for (int off : offsets) {
            for (int len : lengths) {
                boolean expectOk = off >= 0 && len >= 0 && len <= buf.length
                        && off <= buf.length - len;
                boolean gotOk;
                try {
                    DatagramPacket pp = new DatagramPacket(buf, off, len);
                    gotOk = true;
                    if (expectOk) {
                        check(pp.getOffset() == off && pp.getLength() == len,
                                "matrix cell ok(" + off + "," + len + ")");
                    }
                } catch (IllegalArgumentException e) {
                    gotOk = false;
                }
                check(gotOk == expectOk, "matrix cell (" + off + "," + len + ") expected "
                        + (expectOk ? "legal" : "IAE"));
            }
        }
    }
    // Deepened: packet with null address but a port reports the wildcard address
    // with that port (probed on both JDKs: "0.0.0.0/0.0.0.0:7").
    static void testNullAddressSocketAddressForm() throws Exception {
        byte[] buf = new byte[4];
        DatagramPacket p = new DatagramPacket(buf, 4);
        p.setPort(7);
        SocketAddress sa = p.getSocketAddress();
        check(sa instanceof InetSocketAddress, "null-address SA type");
        InetSocketAddress isa = (InetSocketAddress) sa;
        check(isa.getPort() == 7, "null-address SA keeps port 7");
        check(isa.getAddress() != null && isa.getAddress().isAnyLocalAddress(),
                "null-address SA yields wildcard");
    }
    // Deepened: structure - exactly six public constructors, every accessor and
    // mutator is public synchronized (javap-verified on both JDKs), final class.
    static void testStructuralReflection() throws Exception {
        Class<?> c = DatagramPacket.class;
        check(c.getConstructors().length == 6, "six public constructors, got " + c.getConstructors().length);
        String[] accessorNames = {"getAddress", "getPort", "getData", "getOffset", "getLength", "getSocketAddress"};
        for (String n : accessorNames) {
            java.lang.reflect.Method m = c.getMethod(n);
            check(java.lang.reflect.Modifier.isSynchronized(m.getModifiers()), n + " synchronized");
            check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), n + " public");
        }
        check(c.getMethod("setData", byte[].class, int.class, int.class) != null, "setData(b,o,l)");
        check(c.getMethod("setData", byte[].class) != null, "setData(b)");
        check(c.getMethod("setLength", int.class) != null, "setLength");
        check(c.getMethod("setPort", int.class) != null, "setPort");
        check(c.getMethod("setAddress", InetAddress.class) != null, "setAddress");
        check(c.getMethod("setSocketAddress", SocketAddress.class) != null, "setSocketAddress");
        java.lang.reflect.Method setDataFull = c.getMethod("setData", byte[].class, int.class, int.class);
        check(java.lang.reflect.Modifier.isSynchronized(setDataFull.getModifiers()), "setData(b,o,l) synchronized");
        java.lang.reflect.Method setDataOne = c.getMethod("setData", byte[].class);
        check(java.lang.reflect.Modifier.isSynchronized(setDataOne.getModifiers()), "setData(b) synchronized");
        java.lang.reflect.Method setLength = c.getMethod("setLength", int.class);
        check(java.lang.reflect.Modifier.isSynchronized(setLength.getModifiers()), "setLength synchronized");
    }

    public static void main(String[] args) throws Exception {
        testSimpleConstructors();
        testAddressConstructors();
        testSetters();
        testBoundaries();
        testUnresolvedAndBadAddress();
        testConstructorBoundaryMatrix();
        testPortBoundaryMatrix();
        testNullSemantics();
        testSetLengthCapacityMatrix();
        testSetDataMatrix();
        testAddressPortMatrix();
        testObjectContracts();
        testLoopbackRoundTrip();
        testOffsetLengthAcceptanceMatrix();
        testNullAddressSocketAddressForm();
        testStructuralReflection();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

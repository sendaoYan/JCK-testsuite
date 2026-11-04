/*
 * @test
 * @summary java.net.Socket conformance: unconnected-state semantics and
 *          close/state invariants only (no network connects, no fixed-port
 *          binds). new Socket()/new Socket(Proxy.NO_PROXY) initial state,
 *          SocketAddress/InetAddress/port accessors before and after close,
 *          exact SocketException for I/O on unconnected/closed sockets,
 *          SO_TIMEOUT/TCP_NODELAY/KEEPALIVE/REUSEADDR/TRAFFIC_CLASS/LINGER
 *          option round-trips, traffic-class range validation (IAE), bind to
 *          an ephemeral wildcard port, and idempotent close.
 * @run main SocketTest
 */

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketException;

public class SocketTest {
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

    static void testUnconnectedState() throws Exception {
        Socket s = new Socket();
        check(!s.isConnected(), "not connected");
        check(!s.isBound(), "not bound");
        check(!s.isClosed(), "not closed");
        check(s.getInetAddress() == null, "remote address null");
        check(s.getPort() == 0, "remote port 0");
        check(s.getLocalPort() == -1, "local port -1");
        check(s.getLocalAddress().isAnyLocalAddress(), "local address wildcard");
        check(s.getRemoteSocketAddress() == null, "remote socket address null");
        check(s.getLocalSocketAddress() == null, "local socket address null");
        check(s.getChannel() == null, "channel null");
        check(!s.isInputShutdown(), "not input shutdown");
        check(!s.isOutputShutdown(), "not output shutdown");
        check("Socket[unconnected]".equals(s.toString()), "unconnected toString");
        check(s.getSoTimeout() == 0, "default SO_TIMEOUT 0");
        s.close();
    }

    static void testNoProxyUnconnected() throws Exception {
        Socket s = new Socket(Proxy.NO_PROXY);
        check(!s.isConnected(), "NO_PROXY not connected");
        check(!s.isBound(), "NO_PROXY not bound");
        check(s.getInetAddress() == null, "NO_PROXY remote null");
        s.close();

        // The DIRECT proxy type cannot be built through the public constructor
        // (only the Proxy.NO_PROXY singleton); a SOCKS proxy stays unconnected
        // until connect() is invoked, so constructing it is offline.
        Socket s2 = new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress("127.0.0.1", 1080)));
        check(!s2.isConnected(), "SOCKS proxy socket not connected");
        check(!s2.isBound(), "SOCKS proxy socket not bound");
        s2.close();
    }

    static void testOptions() throws Exception {
        Socket s = new Socket();
        s.setSoTimeout(100);
        check(s.getSoTimeout() == 100, "SO_TIMEOUT round trip");

        check(!s.getTcpNoDelay(), "default TCP_NODELAY false");
        s.setTcpNoDelay(true);
        check(s.getTcpNoDelay(), "TCP_NODELAY round trip");

        s.setKeepAlive(true);
        check(s.getKeepAlive(), "KEEPALIVE round trip");

        s.setReuseAddress(true);
        check(s.getReuseAddress(), "REUSEADDR round trip");

        s.setTrafficClass(0x10);
        check(s.getTrafficClass() == 0x10, "TRAFFIC_CLASS round trip");
        checkThrows(IllegalArgumentException.class, () -> s.setTrafficClass(256), "TRAFFIC_CLASS > 255 -> IAE");
        checkThrows(IllegalArgumentException.class, () -> s.setTrafficClass(-1), "TRAFFIC_CLASS negative -> IAE");

        s.setSoLinger(true, 5);
        check(s.getSoLinger() == 5, "SO_LINGER round trip");
        s.setSoLinger(false, 0);
        check(s.getSoLinger() == -1, "SO_LINGER disabled");
        s.close();
    }

    static void testUnconnectedIO() throws Exception {
        Socket s = new Socket();
        checkThrows(SocketException.class, () -> s.getInputStream(), "getInputStream unconnected");
        checkThrows(SocketException.class, () -> s.getOutputStream(), "getOutputStream unconnected");
        checkThrows(SocketException.class, () -> s.shutdownInput(), "shutdownInput unconnected");
        checkThrows(SocketException.class, () -> s.shutdownOutput(), "shutdownOutput unconnected");
        checkThrows(IOException.class, () -> s.sendUrgentData(1), "sendUrgentData unconnected");
        s.close();
    }

    static void testCloseSemantics() throws Exception {
        Socket s = new Socket();
        s.close();
        check(s.isClosed(), "isClosed after close");
        s.close(); // idempotent
        check(s.isClosed(), "close idempotent");
        check(s.getInetAddress() == null, "closed remote null");
        check(s.getPort() == 0, "closed remote port 0");
        checkThrows(SocketException.class, () -> s.getInputStream(), "closed getInputStream");
        checkThrows(SocketException.class, () -> s.getOutputStream(), "closed getOutputStream");
        checkThrows(SocketException.class, () -> s.setSoTimeout(100), "closed setSoTimeout");
    }

    static void testEphemeralBind() throws Exception {
        Socket s = new Socket();
        s.bind(new InetSocketAddress(0));
        check(s.isBound(), "bound after bind(0)");
        check(s.getLocalPort() > 0, "ephemeral local port assigned");
        check(s.getLocalSocketAddress() instanceof InetSocketAddress, "local socket address typed");
        check(s.getLocalAddress().isAnyLocalAddress(), "bound wildcard local address");
        s.close();
        check(s.isClosed(), "closed after bind");

        Socket s2 = new Socket();
        s2.bind(null); // null address binds to a wildcard ephemeral port
        check(s2.isBound(), "bind(null) binds");
        check(s2.getLocalPort() > 0, "bind(null) ephemeral port");
        s2.close();
    }

    public static void main(String[] args) throws Exception {
        testUnconnectedState();
        testNoProxyUnconnected();
        testOptions();
        testUnconnectedIO();
        testCloseSemantics();
        testEphemeralBind();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

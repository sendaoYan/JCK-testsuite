/*
 * @test
 * @summary java.net.Socket conformance: unconnected-state semantics,
 *          close/state invariants, and the full loopback connection state
 *          machine (ServerSocket(0,50,127.0.0.1) + daemon accept thread):
 *          connect(SocketAddress,timeout) postconditions, stream identity,
 *          byte round-trip, shutdownOutput/shutdownInput half-close
 *          semantics on both ends, exact SocketException messages for
 *          closed/already-connected/already-bound states, ConnectException
 *          on refused loopback port, IAE matrices for negative timeouts,
 *          buffer sizes, SO_LINGER and traffic class, OOBINLINE round-trip,
 *          sendUrgentData on a live connection, and close-after-shutdown.
 * @run main SocketTest
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.BindException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

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
        // Traffic class is a hint the OS may mask or ignore (Windows/AIX do not
        // round-trip IP_TOS); assert only the spec contract [0,255].
        int tc = s.getTrafficClass();
        check(tc >= 0 && tc <= 255, "TRAFFIC_CLASS accepted, reads back " + tc);
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

    // ------------------------------------------------------------------
    // Loopback connection state machine. Every helper below binds a
    // ServerSocket to 127.0.0.1 on an ephemeral port, accepts on a daemon
    // thread with a bounded timeout, and closes everything it created.
    // ------------------------------------------------------------------

    static class Pair {
        java.net.ServerSocket server;
        Socket client;
        Socket accepted;
    }

    static void closePair(Pair p) {
        try { if (p.client != null) p.client.close(); } catch (IOException ignore) { }
        try { if (p.accepted != null) p.accepted.close(); } catch (IOException ignore) { }
        try { if (p.server != null) p.server.close(); } catch (IOException ignore) { }
    }

    static Pair connectPair() throws Exception {
        final java.net.ServerSocket server =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        server.setSoTimeout(500);
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(1);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try { q.put(server.accept()); } catch (IOException ignore) { } catch (InterruptedException ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        Pair p = new Pair();
        p.server = server;
        p.client = new Socket();
        p.client.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 5000);
        p.accepted = q.poll(2, TimeUnit.SECONDS);
        acceptor.join(2000);
        if (p.accepted == null) throw new AssertionError("accept did not produce a socket in time");
        return p;
    }

    static void testLoopbackConnectPair() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;
            check(c.isConnected(), "connected after connect");
            check(c.isBound(), "client bound after connect");
            check(!c.isClosed(), "client open");
            check(c.getInetAddress() != null && c.getInetAddress().isLoopbackAddress(), "remote is loopback");
            check(c.getPort() == p.server.getLocalPort(), "getPort == server port");
            check(c.getLocalPort() > 0, "ephemeral local port assigned");
            check(c.getLocalAddress() != null && c.getLocalAddress().isLoopbackAddress(), "local address loopback");
            check(c.getLocalSocketAddress() != null, "local socket address non-null");
            check(c.getLocalSocketAddress() instanceof InetSocketAddress, "local socket address typed");
            check(c.getRemoteSocketAddress() != null, "remote socket address non-null");
            check(c.getRemoteSocketAddress() instanceof InetSocketAddress, "remote socket address typed");
            InetSocketAddress rsa = (InetSocketAddress) c.getRemoteSocketAddress();
            check(rsa.getPort() == p.server.getLocalPort(), "remote SA port matches");
            String ts = c.toString();
            check(ts.contains("addr="), "toString contains addr=");
            check(ts.contains("port="), "toString contains port=");
            check(ts.contains("localport="), "toString contains localport=");

            // Peer (accepted) side of the same connection.
            check(a.isConnected(), "accepted connected");
            check(a.isBound(), "accepted bound");
            check(a.getPort() == c.getLocalPort(), "accepted.getPort == client local port (reciprocal)");
            check(a.getLocalPort() == p.server.getLocalPort(), "accepted.localPort == server port");
            check(a.getInetAddress() != null && a.getInetAddress().isLoopbackAddress(), "accepted remote loopback");
            check(a.getLocalPort() > 0, "accepted local port positive");
            check(a.toString().contains("addr=") && a.toString().contains("port="), "accepted toString");

            // Stream identity: repeated fetches return the same object.
            InputStream is1 = c.getInputStream();
            InputStream is2 = c.getInputStream();
            OutputStream os1 = c.getOutputStream();
            OutputStream os2 = c.getOutputStream();
            check(is1 == is2, "getInputStream same object");
            check(os1 == os2, "getOutputStream same object");
            InputStream ais1 = a.getInputStream(), ais2 = a.getInputStream();
            OutputStream aos1 = a.getOutputStream(), aos2 = a.getOutputStream();
            check(ais1 == ais2, "accepted getInputStream same object");
            check(aos1 == aos2, "accepted getOutputStream same object");
            check(is1 != ais1, "client and peer streams distinct");

            // Exact byte round-trip in both directions.
            os1.write("ping".getBytes("UTF-8"));
            os1.flush();
            byte[] buf = new byte[8];
            int n = ais1.read(buf);
            check(n == 4, "peer read exact length");
            check(new String(buf, 0, n, "UTF-8").equals("ping"), "peer read exact bytes");
            aos1.write("pong".getBytes("UTF-8"));
            aos1.flush();
            byte[] buf2 = new byte[8];
            int n2 = is1.read(buf2);
            check(n2 == 4, "client read exact length");
            check(new String(buf2, 0, n2, "UTF-8").equals("pong"), "client read exact bytes");

            // connect() on an already connected socket.
            try {
                c.connect(new InetSocketAddress("127.0.0.1", p.server.getLocalPort()), 200);
                check(false, "reconnect should fail");
            } catch (SocketException e) {
                // JDK-8: "already connected"; JDK-27: "Already connected" - assert common part case-insensitively.
                check(e.getMessage() != null
                        && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("already connected"),
                        "reconnect message, got: " + e.getMessage());
            }
            // Single-arg connect on connected socket fails identically.
            try {
                c.connect(new InetSocketAddress("127.0.0.1", p.server.getLocalPort()));
                check(false, "reconnect (1-arg) should fail");
            } catch (SocketException e) {
                passed++;
            }

            // Options keep working on a live connection.
            c.setSoTimeout(250);
            check(c.getSoTimeout() == 250, "connected SO_TIMEOUT round trip");
            c.setTcpNoDelay(true);
            check(c.getTcpNoDelay(), "connected TCP_NODELAY round trip");
        } finally {
            closePair(p);
        }
    }

    static void testShutdownSemantics() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;

            // shutdownOutput: local write side dies, peer sees EOF.
            c.shutdownOutput();
            check(c.isOutputShutdown(), "isOutputShutdown after shutdownOutput");
            check(!c.isInputShutdown(), "input still open after shutdownOutput");
            check(!c.isClosed(), "socket still open after shutdownOutput");
            try {
                c.getOutputStream().write(65);
                check(false, "write after shutdownOutput must fail");
            } catch (SocketException e) {
                check("Socket output is shutdown".equals(e.getMessage()),
                        "write-after-shutdownOutput message, got: " + e.getMessage());
            }
            a.setSoTimeout(500);
            int eof = a.getInputStream().read();
            check(eof == -1, "peer reads EOF after shutdownOutput");
            try {
                c.shutdownOutput();
                check(false, "double shutdownOutput must fail");
            } catch (SocketException e) {
                check("Socket output is already shutdown".equals(e.getMessage()),
                        "double shutdownOutput message, got: " + e.getMessage());
            }

            // Input direction still open: peer data can still be read.
            a.getOutputStream().write(99);
            a.getOutputStream().flush();
            c.setSoTimeout(500);
            int r = c.getInputStream().read();
            check(r == 99, "read still works after shutdownOutput");

            // shutdownInput: local read side dies with exact message.
            c.shutdownInput();
            check(c.isInputShutdown(), "isInputShutdown after shutdownInput");
            check(c.isOutputShutdown(), "output shutdown flag preserved");
            try {
                c.getInputStream().read();
                check(false, "read after shutdownInput must fail");
            } catch (SocketException e) {
                check("Socket input is shutdown".equals(e.getMessage()),
                        "read-after-shutdownInput message, got: " + e.getMessage());
            }
            try {
                c.shutdownInput();
                check(false, "double shutdownInput must fail");
            } catch (SocketException e) {
                check("Socket input is already shutdown".equals(e.getMessage()),
                        "double shutdownInput message, got: " + e.getMessage());
            }

            // close after shutdown is legal and keeps shutdown flags.
            c.close();
            check(c.isClosed(), "close after shutdown works");
            check(c.isInputShutdown(), "input shutdown flag survives close");
            check(c.isOutputShutdown(), "output shutdown flag survives close");
        } finally {
            closePair(p);
        }
    }

    static void testConnectFailures() throws Exception {
        // Refused loopback port: bind an ephemeral port, close it, reconnect.
        java.net.ServerSocket tmp =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int deadPort = tmp.getLocalPort();
        tmp.close();
        Socket r = new Socket();
        try {
            r.connect(new InetSocketAddress("127.0.0.1", deadPort), 200);
            check(false, "connect to refused port must fail");
        } catch (ConnectException e) {
            check(e.getMessage() != null && e.getMessage().contains("Connection refused"),
                    "refused message, got: " + e.getMessage());
        } catch (java.net.SocketTimeoutException e) {
            // Windows may report a dropped SYN to a freshly released loopback
            // port as a timeout rather than an explicit refusal.
            passed++;
        }
        check(!r.isConnected(), "still not connected after refusal");
        r.close();

        // Null address / negative timeout argument validation.
        Socket s1 = new Socket();
        try {
            s1.connect((SocketAddress) null);
            check(false, "connect(null) must fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("The address can't be null"),
                    "connect(null) message, got: " + e.getMessage());
        }
        try {
            s1.connect(new InetSocketAddress("127.0.0.1", 1), -1);
            check(false, "connect(timeout<0) must fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("timeout can't be negative"),
                    "connect(timeout<0) message, got: " + e.getMessage());
        }
        try {
            s1.connect(null, 100);
            check(false, "connect(null,100) must fail");
        } catch (IllegalArgumentException e) {
            check(e.getMessage() != null && e.getMessage().contains("The address can't be null"),
                    "connect(null,100) message, got: " + e.getMessage());
        }
        s1.close();

        // Unresolved address never touches DNS servers of the network beyond
        // the local resolver contract: exact UnknownHostException here.
        Socket s2 = new Socket();
        try {
            s2.connect(InetSocketAddress.createUnresolved("no.such.host.invalid", 1234), 200);
            check(false, "connect unresolved must fail");
        } catch (java.net.UnknownHostException e) {
            check("no.such.host.invalid".equals(e.getMessage()),
                    "unresolved message, got: " + e.getMessage());
        }
        s2.close();
    }

    static void testCloseStateMachine() throws Exception {
        // A socket that connected once preserves its endpoint state after close.
        Pair p = connectPair();
        Socket c = p.client;
        int localPort = c.getLocalPort();
        int remotePort = c.getPort();
        OutputStream preCloseOut = c.getOutputStream();
        InputStream preCloseIn = c.getInputStream();
        try {
            c.close();
            check(c.isClosed(), "closed after close");
            check(c.isConnected(), "isConnected preserved after close");
            check(c.isBound(), "isBound preserved after close");
            check(c.getPort() == remotePort, "remote port preserved after close");
            check(c.getLocalPort() == localPort, "local port preserved after close");
            check(c.getInetAddress() != null && c.getInetAddress().isLoopbackAddress(),
                    "remote address preserved after close");
            check(c.getRemoteSocketAddress() != null, "remote SA preserved after close");
            check(c.getLocalSocketAddress() != null, "local SA preserved after close");

            // Every state-changing operation on a closed socket fails with the
            // exact message "Socket is closed" (identical on JDK-8/JDK-27).
            try { c.getInputStream(); check(false, "closed getInputStream"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed getInputStream msg"); }
            try { c.getOutputStream(); check(false, "closed getOutputStream"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed getOutputStream msg"); }
            try { c.setSoTimeout(100); check(false, "closed setSoTimeout"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed setSoTimeout msg"); }
            try { c.bind(new InetSocketAddress(0)); check(false, "closed bind"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed bind msg"); }
            try { c.connect(new InetSocketAddress("127.0.0.1", remotePort), 200); check(false, "closed connect"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed connect msg"); }
            try { c.shutdownInput(); check(false, "closed shutdownInput"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed shutdownInput msg"); }
            try { c.shutdownOutput(); check(false, "closed shutdownOutput"); }
            catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed shutdownOutput msg"); }
            // Streams obtained before close fail with the stream-level message
            // "Socket closed" (distinct from the accessor-level "Socket is closed").
            try { preCloseOut.write(65); check(false, "pre-close stream write must fail"); }
            catch (SocketException e) { check(e.getMessage() != null && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("closed"), "pre-close write msg, got: " + e.getMessage()); }
            try { int rr = preCloseIn.read(); check(false, "pre-close stream read must fail, got " + rr); }
            catch (SocketException e) { check(e.getMessage() != null && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("closed"), "pre-close read msg, got: " + e.getMessage()); }
            // flush() on a closed socket's stream is permitted (no-op).
            try { preCloseOut.flush(); passed++; } catch (SocketException e) {
                check(false, "flush after close should not throw, got: " + e.getMessage());
            }
            // close is idempotent
            c.close();
            check(c.isClosed(), "close idempotent on connected socket");
        } finally {
            closePair(p);
        }
    }

    static void testBindFailures() throws Exception {
        // Bind twice on the same socket.
        Socket b1 = new Socket();
        b1.bind(new InetSocketAddress(0));
        try {
            b1.bind(new InetSocketAddress(0));
            check(false, "double bind must fail");
        } catch (SocketException e) {
            check("Already bound".equals(e.getMessage()), "double bind message, got: " + e.getMessage());
        }
        b1.close();

        // Bind to a port already occupied by a live loopback listener.
        java.net.ServerSocket occ =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Socket b2 = new Socket();
        try {
            b2.bind(new InetSocketAddress("127.0.0.1", occ.getLocalPort()));
            check(false, "bind occupied port must fail");
        } catch (BindException e) {
            // Precise type (BindException, not plain SocketException);
            // message suffix differs: JDK-8 appends "(Bind failed)".
            check(e.getMessage() != null && e.getMessage().contains("Address already in use"),
                    "occupied bind message, got: " + e.getMessage());
        }
        b2.close();
        occ.close();

        // Out-of-range ports are rejected before hitting the OS.
        Socket b3 = new Socket();
        try { b3.bind(new InetSocketAddress(65536)); check(false, "bind 65536 must fail"); }
        catch (IllegalArgumentException e) {
            check("port out of range:65536".equals(e.getMessage()), "bind 65536 message");
        }
        try { b3.bind(new InetSocketAddress(-1)); check(false, "bind -1 must fail"); }
        catch (IllegalArgumentException e) {
            check("port out of range:-1".equals(e.getMessage()), "bind -1 message");
        }
        b3.close();
    }

    static void testOptionMatrix() throws Exception {
        Socket s = new Socket();
        try {
            // SO_TIMEOUT validation (exact messages probed on both JDKs).
            try { s.setSoTimeout(-1); check(false, "setSoTimeout(-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("timeout can't be negative".equals(e.getMessage()), "setSoTimeout(-1) message");
            }
            // Send buffer size.
            try { s.setSendBufferSize(0); check(false, "setSendBufferSize(0) must fail"); }
            catch (IllegalArgumentException e) {
                check("negative send size".equals(e.getMessage()), "setSendBufferSize(0) message");
            }
            try { s.setSendBufferSize(-1); check(false, "setSendBufferSize(-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("negative send size".equals(e.getMessage()), "setSendBufferSize(-1) message");
            }
            check(s.getSendBufferSize() > 0, "default send buffer positive");
            s.setSendBufferSize(4096);
            check(s.getSendBufferSize() >= 4096, "send buffer never shrinks below request");
            // Receive buffer size.
            try { s.setReceiveBufferSize(0); check(false, "setReceiveBufferSize(0) must fail"); }
            catch (IllegalArgumentException e) {
                check("invalid receive size".equals(e.getMessage()), "setReceiveBufferSize(0) message");
            }
            try { s.setReceiveBufferSize(-1); check(false, "setReceiveBufferSize(-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("invalid receive size".equals(e.getMessage()), "setReceiveBufferSize(-1) message");
            }
            check(s.getReceiveBufferSize() > 0, "default receive buffer positive");
            s.setReceiveBufferSize(4096);
            check(s.getReceiveBufferSize() >= 4096, "receive buffer never shrinks below request");
            // Traffic class default and range.
            check(s.getTrafficClass() == 0, "default traffic class 0");
            try { s.setTrafficClass(256); check(false, "setTrafficClass(256) must fail"); }
            catch (IllegalArgumentException e) {
                check("tc is not in range 0 -- 255".equals(e.getMessage()), "setTrafficClass(256) message");
            }
            // SO_LINGER: negative linger with on=true is invalid.
            try { s.setSoLinger(true, -1); check(false, "setSoLinger(true,-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("invalid value for SO_LINGER".equals(e.getMessage()), "setSoLinger(true,-1) message");
            }
            try { s.setSoLinger(true, -5); check(false, "setSoLinger(true,-5) must fail"); }
            catch (IllegalArgumentException e) {
                check("invalid value for SO_LINGER".equals(e.getMessage()), "setSoLinger(true,-5) message");
            }
            // OOBINLINE: default off, round-trips.
            check(!s.getOOBInline(), "default OOBINLINE false");
            s.setOOBInline(true);
            check(s.getOOBInline(), "OOBINLINE round trip");
            // Platform defaults recorded as facts (Linux, JDK-8 and JDK-27 agree).
            check(!s.getKeepAlive(), "default KEEPALIVE false (Linux)");
            check(!s.getReuseAddress(), "default REUSEADDR false before bind (Linux)");
            // Pure hint method: never throws.
            s.setPerformancePreferences(1, 2, 3);
            passed++;
        } finally {
            s.close();
        }
    }

    static void testUrgentData() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client;
            check(!c.getOOBInline(), "OOBINLINE off by default on live socket");
            c.setOOBInline(true);
            check(c.getOOBInline(), "OOBINLINE on after set on live socket");
            // With OOBINLINE enabled urgent data is in-band readable; sending
            // itself must not throw on a live connection.
            c.sendUrgentData(0x42);
            passed++;
            // sendUrgentData on a closed socket fails. JDK-8 surfaces the raw
            // native failure (IOException "Broken pipe (Write failed)"); JDK-27
            // maps it to SocketException "Socket is closed". Type-only here.
            Socket d = new Socket();
            d.close();
            try {
                d.sendUrgentData(1);
                check(false, "sendUrgentData on closed socket must fail");
            } catch (IOException e) {
                passed++;
            }
        } finally {
            closePair(p);
        }
    }

    static void testReadTimeout() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client;
            c.setSoTimeout(100);
            check(c.getSoTimeout() == 100, "SoTimeout round trip on live socket");
            try {
                c.getInputStream().read();
                check(false, "read with SoTimeout must time out");
            } catch (java.net.SocketTimeoutException e) {
                check("Read timed out".equals(e.getMessage()),
                        "read timeout message, got: " + e.getMessage());
                check(e.bytesTransferred == 0, "bytesTransferred 0 on read timeout");
                check(!SocketException.class.isInstance(e),
                        "SocketTimeoutException is NOT a SocketException");
            }
            // The socket remains fully usable after a read timeout.
            c.getOutputStream().write(7);
            c.getOutputStream().flush();
            Socket a = p.accepted;
            a.setSoTimeout(500);
            check(a.getInputStream().read() == 7, "socket usable after read timeout");
        } finally {
            closePair(p);
        }
    }

    static void testByteStreamTransfer() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;
            // Deterministic 8192-byte pattern crossing typical buffer sizes.
            byte[] out = new byte[8192];
            for (int i = 0; i < out.length; i++) out[i] = (byte) (i * 31 + 7);
            c.getOutputStream().write(out);
            c.getOutputStream().flush();
            // available() counts pending bytes after the first arrives.
            a.setSoTimeout(500);
            int first = a.getInputStream().read();
            check(first == (out[0] & 0xff), "first byte exact");
            int avail = a.getInputStream().available();
            check(avail > 0 && avail <= out.length - 1, "available in (0, remaining], got " + avail);
            byte[] in = new byte[out.length];
            in[0] = (byte) first;
            int off = 1;
            while (off < in.length) {
                int n = a.getInputStream().read(in, off, in.length - off);
                check(n > 0, "blocking read progresses");
                off += n;
            }
            check(java.util.Arrays.equals(in, out), "8192-byte pattern round trip exact");
            // Peer close propagates as EOF to the reader.
            a.close();
            c.setSoTimeout(500);
            int eof = c.getInputStream().read();
            check(eof == -1, "peer close seen as EOF");
        } finally {
            closePair(p);
        }
    }

    static void testLiveOptionRoundTrips() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client;
            c.setSoLinger(true, 100);
            check(c.getSoLinger() == 100, "live SO_LINGER round trip");
            c.setSoLinger(false, 0);
            check(c.getSoLinger() == -1, "live SO_LINGER disabled");
            c.setKeepAlive(true);
            check(c.getKeepAlive(), "live KEEPALIVE round trip");
            c.setTrafficClass(0x10);
            int liveTc = c.getTrafficClass();
            check(liveTc >= 0 && liveTc <= 255, "live TRAFFIC_CLASS accepted, reads back " + liveTc);
            check(!c.getTcpNoDelay() || true, "live TCP_NODELAY readable");
            c.setTcpNoDelay(false);
            check(!c.getTcpNoDelay(), "live TCP_NODELAY off");
            c.setReceiveBufferSize(16384);
            check(c.getReceiveBufferSize() >= 16384, "live RCVBUF floor");
            c.setSendBufferSize(16384);
            check(c.getSendBufferSize() >= 16384, "live SNDBUF floor");
        } finally {
            closePair(p);
        }
    }

    static void testObjectContract() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;
            check(c.equals(c), "socket equals itself");
            check(!c.equals(a), "distinct sockets not equal");
            check(c.hashCode() == c.hashCode(), "hashCode stable");
            check(!c.equals(null), "not equal to null");
            check(!c.equals(new Object()), "not equal to unrelated type");
            check(c.toString() != null && !c.toString().isEmpty(), "toString non-empty");
            // Bound-but-not-connected socket keeps the unconnected toString form.
            Socket b = new Socket();
            try {
                b.bind(new InetSocketAddress("127.0.0.1", 0));
                check(b.isBound(), "bound-only socket bound");
                check("Socket[unconnected]".equals(b.toString()), "bound-only toString");
                check(b.getRemoteSocketAddress() == null, "bound-only remote SA null");
                check(b.getLocalPort() > 0, "bound-only local port");
                check(b.getLocalAddress() != null, "bound-only local address");
                check(b.getLocalAddress().isLoopbackAddress(), "bound-only local loopback");
            } finally {
                b.close();
            }
        } finally {
            closePair(p);
        }
    }

    static void testUnconnectedMessages() throws Exception {
        // A never-connected, still-open socket reports "Socket is not connected"
        // (distinct from the closed-socket message).
        Socket s = new Socket();
        try {
            try { s.getInputStream(); check(false, "unconnected getInputStream"); }
            catch (SocketException e) { check("Socket is not connected".equals(e.getMessage()), "unconnected getInputStream msg"); }
            try { s.getOutputStream(); check(false, "unconnected getOutputStream"); }
            catch (SocketException e) { check("Socket is not connected".equals(e.getMessage()), "unconnected getOutputStream msg"); }
            try { s.shutdownInput(); check(false, "unconnected shutdownInput"); }
            catch (SocketException e) { check("Socket is not connected".equals(e.getMessage()), "unconnected shutdownInput msg"); }
            try { s.shutdownOutput(); check(false, "unconnected shutdownOutput"); }
            catch (SocketException e) { check("Socket is not connected".equals(e.getMessage()), "unconnected shutdownOutput msg"); }
            // SO_LINGER with on=true and timeout 0 is valid (abort on close).
            s.setSoLinger(true, 0);
            check(s.getSoLinger() == 0, "SO_LINGER 0 (abort) round trip");
            s.setSoLinger(false, 0);
            check(s.getSoLinger() == -1, "SO_LINGER back to disabled");
        } finally {
            s.close();
        }
    }

    static void testPreBoundConnect() throws Exception {
        java.net.ServerSocket server =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Socket c = new Socket();
        Socket acc = null;
        try {
            c.bind(new InetSocketAddress("127.0.0.1", 0));
            int boundPort = c.getLocalPort();
            c.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 200);
            check(c.getLocalPort() == boundPort, "pre-bound local port preserved by connect");
            check(c.getLocalAddress().isLoopbackAddress(), "pre-bound local address preserved");
            check(c.getLocalSocketAddress() instanceof InetSocketAddress, "local SA typed");
            check(((InetSocketAddress) c.getLocalSocketAddress()).getPort() == boundPort,
                    "local SA port equals local port");
            acc = server.accept();
            check(acc.getPort() == boundPort, "peer observes pre-bound port");
            // Remote SocketAddress value equals an equivalent InetSocketAddress.
            SocketAddress expect = new InetSocketAddress(InetAddress.getByName("127.0.0.1"), server.getLocalPort());
            check(c.getRemoteSocketAddress().equals(expect), "remote SA equals equivalent ISA");
            check(c.getRemoteSocketAddress().hashCode() == expect.hashCode(), "remote SA hashCode agrees");
            // Accepted side local endpoint is the server's bound endpoint.
            SocketAddress accLocal = acc.getLocalSocketAddress();
            check(accLocal != null && accLocal instanceof InetSocketAddress, "accepted local SA typed");
            check(((InetSocketAddress) accLocal).getPort() == server.getLocalPort(),
                    "accepted local port equals server port");
            check(acc.getLocalAddress() != null && acc.getLocalAddress().isLoopbackAddress(),
                    "accepted local address loopback");
        } finally {
            try { c.close(); } catch (IOException ignore) { }
            try { if (acc != null) acc.close(); } catch (IOException ignore) { }
            server.close();
        }
    }

    static void testFullDuplex() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;
            // Both directions write before either reads.
            c.getOutputStream().write(11);
            c.getOutputStream().flush();
            a.getOutputStream().write(22);
            a.getOutputStream().flush();
            c.setSoTimeout(500);
            a.setSoTimeout(500);
            check(c.getInputStream().read() == 22, "client receives peer byte");
            check(a.getInputStream().read() == 11, "peer receives client byte");
            check(c.getInputStream().available() == 0, "no stray bytes on client");
            check(a.getInputStream().available() == 0, "no stray bytes on peer");
        } finally {
            closePair(p);
        }
    }

    static void testSequentialConnections() throws Exception {
        java.net.ServerSocket server =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        server.setSoTimeout(500);
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(4);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 2; i++) q.put(server.accept());
                } catch (Exception ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        try {
            Socket c1 = new Socket();
            c1.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 200);
            Socket c2 = new Socket();
            c2.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()), 200);
            Socket a1 = q.poll(2, TimeUnit.SECONDS);
            Socket a2 = q.poll(2, TimeUnit.SECONDS);
            acceptor.join(2000);
            check(a1 != null && a2 != null, "both accepts completed");
            check(c1.getLocalPort() != c2.getLocalPort(), "distinct client local ports");
            check(a1.getPort() != a2.getPort(), "distinct peer remote ports");
            // Both connections are independently functional.
            c1.getOutputStream().write(1);
            c1.getOutputStream().flush();
            c2.getOutputStream().write(2);
            c2.getOutputStream().flush();
            a1.setSoTimeout(500);
            a2.setSoTimeout(500);
            check(a1.getInputStream().read() == 1, "pair 1 functional");
            check(a2.getInputStream().read() == 2, "pair 2 functional");
            check(a1.getLocalPort() == server.getLocalPort() && a2.getLocalPort() == server.getLocalPort(),
                    "both accepted share the server port");
            try { c1.close(); c2.close(); a1.close(); a2.close(); } catch (IOException ignore) { }
        } finally {
            server.close();
        }
    }

    static void testPostRefusalState() throws Exception {
        java.net.ServerSocket tmp =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int deadPort = tmp.getLocalPort();
        tmp.close();
        java.net.ServerSocket live =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Socket r = new Socket();
        boolean refused = false;
        try {
            try { r.connect(new InetSocketAddress("127.0.0.1", deadPort), 200); check(false, "should refuse"); }
            catch (ConnectException e) { refused = true; passed++; }
            catch (java.net.SocketTimeoutException e) { passed++; }
            // After a refused connect the underlying impl is dead: the public
            // flag still reads false on JDK-8 (JDK-27 reports isClosed=true).
            // Only meaningful when the OS reported an explicit refusal.
            if (refused) {
                check(!r.isClosed(), "JDK-8 keeps isClosed=false after refused connect");
                try {
                    r.connect(new InetSocketAddress("127.0.0.1", live.getLocalPort()), 200);
                    check(false, "retry on refused socket must fail");
                } catch (SocketException e) {
                    check(e.getMessage() != null && e.getMessage().toLowerCase(java.util.Locale.ROOT).contains("closed"),
                            "retry-after-refusal message, got: " + e.getMessage());
                }
            }
        } finally {
            r.close();
            live.close();
        }
    }

    static void testClosedAccessorDetail() throws Exception {
        // A never-connected socket after close keeps the unconnected view.
        Socket s = new Socket();
        s.close();
        check("Socket[unconnected]".equals(s.toString()), "closed-unconnected toString");
        check(s.getLocalSocketAddress() == null, "closed-unconnected local SA null");
        check(s.getLocalPort() == -1, "closed-unconnected local port -1");
        check(s.getRemoteSocketAddress() == null, "closed-unconnected remote SA null");
        check(s.getInetAddress() == null, "closed-unconnected remote address null");
        check(s.getPort() == 0, "closed-unconnected remote port 0");
        check(!s.isConnected(), "closed-unconnected not connected");
        check(!s.isBound(), "closed-unconnected not bound");
        check(!s.isInputShutdown(), "closed-unconnected input not shutdown");
        check(!s.isOutputShutdown(), "closed-unconnected output not shutdown");
        check(s.getLocalAddress() != null && s.getLocalAddress().isAnyLocalAddress(),
                "closed-unconnected local address wildcard");
        // A connected socket after close keeps the endpoint view in toString.
        Pair p = connectPair();
        try {
            p.client.close();
            check(p.client.toString().contains("addr="), "closed-connected toString keeps addr=");
            check(p.client.toString().contains("port=" + p.server.getLocalPort()),
                    "closed-connected toString keeps remote port");
            check(p.client.toString().contains("localport=" + p.client.getLocalPort()),
                    "closed-connected toString keeps local port");
        } finally {
            closePair(p);
        }
    }

    static void testHalfClosePeerWrite() throws Exception {
        Pair p = connectPair();
        try {
            Socket c = p.client, a = p.accepted;
            c.shutdownInput();
            boolean posix = java.io.File.separatorChar == '/';
            // Peer writes succeed (kernel ACKs); data is discarded locally.
            // Windows aborts the connection when data arrives after a receive
            // half-close (it answers with RST and later local sends fail with
            // WSAECONNABORTED), so either write may throw there. POSIX keeps
            // the original exact behaviour.
            try {
                a.getOutputStream().write(5);
                a.getOutputStream().flush();
            } catch (SocketException e) {
                if (posix) throw e;
                System.out.println("NOTE: peer write after shutdownInput failed on this"
                        + " platform (receive half-close aborts the connection): " + e);
            }
            passed++;
            try {
                c.getInputStream().read();
                check(false, "read after shutdownInput must fail");
            } catch (SocketException e) {
                if (posix) {
                    check("Socket input is shutdown".equals(e.getMessage()),
                            "read-after-shutdownInput exact message");
                } else {
                    // 对端写入触发的 RST 可能先于 Java 层的 shutdown 检查到达，
                    // Windows 于是直接报 OS 文本（WSAECONNABORTED）；语义仍是“读失败”。
                    check(e.getMessage() != null && !e.getMessage().isEmpty(),
                            "read-after-shutdownInput fails with a message: " + e.getMessage());
                }
            }
            // Peer can still read whatever the local end wrote before shutdown.
            boolean localWriteFailed = false;
            try {
                c.getOutputStream().write(77);
                c.getOutputStream().flush();
            } catch (SocketException e) {
                if (posix) throw e;
                localWriteFailed = true;
                System.out.println("NOTE: local write after a peer write into a"
                        + " half-closed input failed on this platform: " + e);
            }
            c.shutdownOutput();
            a.setSoTimeout(500);
            if (!localWriteFailed) {
                try {
                    check(a.getInputStream().read() == 77, "peer reads data sent before shutdownInput");
                } catch (SocketException e) {
                    // 接收半关后到达的数据会让 Windows 回 RST，对端读到的不是数据而是复位。
                    if (posix) throw e;
                    check(e.getMessage() != null && !e.getMessage().isEmpty(),
                          "peer read aborted by the platform reset: " + e.getMessage());
                }
            }
            try {
                check(a.getInputStream().read() == -1, "peer sees EOF after shutdownOutput");
            } catch (SocketException e) {
                // 同上：RST 可能让对端读到 WSAECONNRESET 而不是干净的 EOF。
                if (posix) throw e;
                check(e.getMessage() != null && !e.getMessage().isEmpty(),
                      "peer observes a reset instead of EOF: " + e.getMessage());
            }
        } finally {
            closePair(p);
        }
    }

    static void testAcceptedStateDetail() throws Exception {
        Pair p = connectPair();
        try {
            Socket a = p.accepted;
            check(a.isConnected(), "accepted connected");
            check(a.isBound(), "accepted bound");
            check(!a.isClosed(), "accepted open");
            check(a.getChannel() == null, "accepted channel null");
            check(!a.isInputShutdown(), "accepted input open");
            check(!a.isOutputShutdown(), "accepted output open");
            check(a.getSoTimeout() == 0, "accepted default SoTimeout 0");
            a.setSoTimeout(150);
            check(a.getSoTimeout() == 150, "accepted SoTimeout round trip");
            check(a.getInputStream() == a.getInputStream(), "accepted stream identity");
            check(a.getOutputStream() == a.getOutputStream(), "accepted stream identity out");
        } finally {
            closePair(p);
        }
    }

    static void testSingleArgConnect() throws Exception {
        java.net.ServerSocket server =
                new java.net.ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(1);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try { q.put(server.accept()); } catch (Exception ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        Socket c = new Socket();
        Socket acc = null;
        try {
            c.connect(new InetSocketAddress("127.0.0.1", server.getLocalPort()));
            check(c.isConnected(), "single-arg connect connects");
            check(c.getPort() == server.getLocalPort(), "single-arg connect remote port");
            acc = q.poll(2, TimeUnit.SECONDS);
            acceptor.join(2000);
            check(acc != null, "accept completed for single-arg connect");
            c.getOutputStream().write(123);
            c.getOutputStream().flush();
            acc.setSoTimeout(500);
            check(acc.getInputStream().read() == 123, "byte round trip after single-arg connect");
        } finally {
            try { c.close(); } catch (IOException ignore) { }
            try { if (acc != null) acc.close(); } catch (IOException ignore) { }
            server.close();
        }
    }

    static void testTrafficClassBounds() throws Exception {
        Socket s = new Socket();
        try {
            // Traffic class / TOS is a hint: the OS may mask or ignore it
            // (Linux JDK-8 drops the low ECN bits via the 0xFC DSCP mask;
            // Windows/AIX do not round-trip IP_TOS at all). Assert only the
            // spec contract: every value in [0,255] is accepted and reads back
            // within [0,255].
            for (int tc : new int[]{0, 1, 128, 255}) {
                s.setTrafficClass(tc);
                int got = s.getTrafficClass();
                check(got >= 0 && got <= 255, "traffic class " + tc + " accepted, reads back " + got);
            }
        } finally {
            s.close();
        }
    }

    static void testPortRanges() throws Exception {
        Pair p = connectPair();
        try {
            check(p.server.getLocalPort() >= 1 && p.server.getLocalPort() <= 65535, "server port in range");
            check(p.client.getLocalPort() >= 1 && p.client.getLocalPort() <= 65535, "client local port in range");
            check(p.accepted.getPort() >= 1 && p.accepted.getPort() <= 65535, "accepted remote port in range");
            check(p.client.getPort() == p.server.getLocalPort(), "client remote port equals server port");
            check(p.accepted.getLocalPort() == p.server.getLocalPort(), "accepted local port equals server port");
            check(p.client != p.accepted, "client and accepted are distinct objects");
            // OOBINLINE toggles back off.
            p.client.setOOBInline(false);
            check(!p.client.getOOBInline(), "OOBINLINE toggles off");
        } finally {
            closePair(p);
        }
    }

    public static void main(String[] args) throws Exception {
        testUnconnectedState();
        testNoProxyUnconnected();
        testOptions();
        testUnconnectedIO();
        testCloseSemantics();
        testEphemeralBind();
        testLoopbackConnectPair();
        testShutdownSemantics();
        testConnectFailures();
        testCloseStateMachine();
        testBindFailures();
        testOptionMatrix();
        testUrgentData();
        testReadTimeout();
        testByteStreamTransfer();
        testLiveOptionRoundTrips();
        testObjectContract();
        testUnconnectedMessages();
        testPreBoundConnect();
        testFullDuplex();
        testSequentialConnections();
        testPostRefusalState();
        testClosedAccessorDetail();
        testHalfClosePeerWrite();
        testAcceptedStateDetail();
        testSingleArgConnect();
        testTrafficClassBounds();
        testPortRanges();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

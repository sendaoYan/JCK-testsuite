/*
 * @test
 * @summary java.net.ServerSocket conformance: full bind/accept/close state
 *          machine on loopback ephemeral ports - unbound semantics (accept
 *          throws "Socket is not bound yet"), bind(null/ephemeral/loopback)
 *          variants and toString forms, BindException on occupied ports,
 *          IAE port-range validation, exact "Socket is closed" failures after
 *          close, accept timeout (SocketTimeoutException "Accept timed out"),
 *          daemon-thread accept pairs with reciprocal-port assertions, option
 *          validation, backlog constructors, and idempotent close.
 * @run main ServerSocketTest
 */

import java.io.IOException;
import java.net.BindException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.TimeUnit;

public class ServerSocketTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowR { void run() throws Exception; }
    static void fails(Class<? extends Throwable> ex, String m, ThrowR r) {
        try { r.run(); } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(m + " wrong exception " + t);
        }
        throw new AssertionError(m + " no exception");
    }

    static void testUnboundState() throws Exception {
        ServerSocket ss = new ServerSocket();
        try {
            check(!ss.isBound(), "unbound not bound");
            check(!ss.isClosed(), "unbound not closed");
            check(ss.getLocalPort() == -1, "unbound local port -1");
            check(ss.getInetAddress() == null, "unbound inet address null");
            check(ss.getLocalSocketAddress() == null, "unbound local SA null");
            check(ss.getChannel() == null, "unbound channel null");
            check("ServerSocket[unbound]".equals(ss.toString()), "unbound toString");
            try {
                ss.accept();
                check(false, "unbound accept must fail");
            } catch (SocketException e) {
                check("Socket is not bound yet".equals(e.getMessage()),
                        "unbound accept message, got: " + e.getMessage());
            }
        } finally {
            ss.close();
        }
        // Closing an unbound server is legal and preserves the -1 port view.
        check(ss.isClosed(), "unbound closed after close");
        check(ss.getLocalPort() == -1, "closed unbound local port still -1");
        check(!ss.isBound(), "closed unbound still reports not bound");
    }

    static void testBindVariants() throws Exception {
        // bind(null): wildcard ephemeral.
        ServerSocket b1 = new ServerSocket();
        try {
            b1.bind(null);
            check(b1.isBound(), "bind(null) bound");
            check(b1.getLocalPort() > 0, "bind(null) ephemeral port");
            check(b1.getLocalSocketAddress() != null, "bind(null) local SA non-null");
            check(b1.getInetAddress() != null && b1.getInetAddress().isAnyLocalAddress(),
                    "bind(null) wildcard address");
            check(b1.toString().contains("localport=" + b1.getLocalPort()), "bind(null) toString port");
        } finally { b1.close(); }

        // bind(InetSocketAddress(0)): wildcard ephemeral.
        ServerSocket b2 = new ServerSocket();
        try {
            b2.bind(new InetSocketAddress(0));
            check(b2.isBound(), "bind((0)) bound");
            check(b2.getLocalPort() > 0, "bind((0)) ephemeral port");
            check(b2.getInetAddress() != null && b2.getInetAddress().isAnyLocalAddress(),
                    "bind((0)) wildcard address");
        } finally { b2.close(); }

        // bind(loopback, 0, backlog): loopback endpoint, backlog accepted.
        ServerSocket b3 = new ServerSocket();
        try {
            InetAddress lo = InetAddress.getByName("127.0.0.1");
            b3.bind(new InetSocketAddress(lo, 0), 5);
            check(b3.isBound(), "bind(loop,0,5) bound");
            check(b3.getLocalPort() > 0, "bind(loop,0,5) ephemeral port");
            check(b3.getInetAddress() != null && b3.getInetAddress().isLoopbackAddress(),
                    "bind(loop,0,5) loopback address");
            check(b3.getLocalSocketAddress() instanceof InetSocketAddress, "bind(loop,0,5) SA typed");
            InetSocketAddress lsa = (InetSocketAddress) b3.getLocalSocketAddress();
            check(lsa.getPort() == b3.getLocalPort(), "bind(loop,0,5) SA port");
            check(lsa.getAddress().isLoopbackAddress(), "bind(loop,0,5) SA loopback");
            check(b3.toString().contains("addr=") && b3.toString().contains("localport="),
                    "bind(loop,0,5) toString form");
            SocketAddress eq = new InetSocketAddress(lo, b3.getLocalPort());
            check(b3.getLocalSocketAddress().equals(eq), "bind(loop,0,5) SA equals equivalent");
        } finally { b3.close(); }

        // bind(null, backlog) overload also binds wildcard ephemeral.
        ServerSocket b4 = new ServerSocket();
        try {
            b4.bind(null, 10);
            check(b4.isBound(), "bind(null,10) bound");
            check(b4.getLocalPort() > 0, "bind(null,10) ephemeral port");
        } finally { b4.close(); }
    }

    static void testBindFailures() throws Exception {
        // Binding twice on the same ServerSocket.
        ServerSocket b1 = new ServerSocket();
        try {
            b1.bind(null);
            try {
                b1.bind(null);
                check(false, "double bind must fail");
            } catch (SocketException e) {
                check("Already bound".equals(e.getMessage()),
                        "double bind message, got: " + e.getMessage());
            }
        } finally { b1.close(); }

        // Bind to a port occupied by a live loopback listener.
        ServerSocket occ = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        ServerSocket b2 = new ServerSocket();
        try {
            try {
                b2.bind(new InetSocketAddress("127.0.0.1", occ.getLocalPort()));
                check(false, "bind occupied must fail");
            } catch (BindException e) {
                check(e.getMessage() != null && e.getMessage().contains("Address already in use"),
                        "occupied bind message, got: " + e.getMessage());
            }
            // Constructor form fails with the same precise type.
            try {
                new ServerSocket(occ.getLocalPort(), 5, InetAddress.getByName("127.0.0.1"));
                check(false, "ctor occupied must fail");
            } catch (BindException e) {
                check(e.getMessage() != null && e.getMessage().contains("Address already in use"),
                        "occupied ctor message, got: " + e.getMessage());
            }
        } finally {
            b2.close();
            occ.close();
        }

        // Port range validation happens before any OS call.
        ServerSocket b3 = new ServerSocket();
        try {
            try { b3.bind(new InetSocketAddress("127.0.0.1", 65536)); check(false, "bind 65536 must fail"); }
            catch (IllegalArgumentException e) {
                check("port out of range:65536".equals(e.getMessage()), "bind 65536 message");
            }
            try { b3.bind(new InetSocketAddress("127.0.0.1", -2)); check(false, "bind -2 must fail"); }
            catch (IllegalArgumentException e) {
                check("port out of range:-2".equals(e.getMessage()), "bind -2 message");
            }
        } finally { b3.close(); }
        try { new ServerSocket(-2); check(false, "ctor(-2) must fail"); }
        catch (IllegalArgumentException e) {
            check("Port value out of range: -2".equals(e.getMessage()), "ctor(-2) message");
        }
        try { new ServerSocket(65536); check(false, "ctor(65536) must fail"); }
        catch (IllegalArgumentException e) {
            check("Port value out of range: 65536".equals(e.getMessage()), "ctor(65536) message");
        }
    }

    static void testCloseSemantics() throws Exception {
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        int port = ss.getLocalPort();
        ss.close();
        check(ss.isClosed(), "isClosed after close");
        try {
            ss.accept();
            check(false, "closed accept must fail");
        } catch (SocketException e) {
            check("Socket is closed".equals(e.getMessage()),
                    "closed accept message, got: " + e.getMessage());
        }
        try {
            ss.bind(new InetSocketAddress(0));
            check(false, "closed bind must fail");
        } catch (SocketException e) {
            check("Socket is closed".equals(e.getMessage()),
                    "closed bind message, got: " + e.getMessage());
        }
        // close is idempotent; port view is retained as bound-but-closed.
        ss.close();
        check(ss.isClosed(), "close idempotent");
        check(ss.getLocalPort() == port, "closed keeps bound port view");
        check(ss.isBound(), "closed keeps isBound true");
        check(ss.getLocalSocketAddress() != null, "closed keeps local SA");
    }

    static void testAcceptTimeout() throws Exception {
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        try {
            ss.setSoTimeout(100);
            check(ss.getSoTimeout() == 100, "SoTimeout round trip");
            try {
                ss.accept();
                check(false, "accept with timeout must time out");
            } catch (SocketTimeoutException e) {
                check("Accept timed out".equals(e.getMessage()),
                        "accept timeout message, got: " + e.getMessage());
            }
            // Server remains usable after an accept timeout.
            ss.setSoTimeout(0);
            check(ss.getSoTimeout() == 0, "SoTimeout reset to blocking");
            try { ss.setSoTimeout(-1); check(false, "setSoTimeout(-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("timeout < 0".equals(e.getMessage()), "setSoTimeout(-1) message");
            }
        } finally { ss.close(); }
    }

    static void testAcceptPair() throws Exception {
        final ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(1);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try { q.put(ss.accept()); } catch (Exception ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        Socket client = new Socket();
        Socket accepted = null;
        try {
            client.connect(new InetSocketAddress("127.0.0.1", ss.getLocalPort()), 200);
            accepted = q.poll(2, TimeUnit.SECONDS);
            acceptor.join(2000);
            check(accepted != null, "accept returned a socket");
            check(accepted.isConnected(), "accepted isConnected");
            check(accepted.isBound(), "accepted isBound");
            check(!accepted.isClosed(), "accepted not closed");
            check(accepted.getPort() == client.getLocalPort(),
                    "accepted.getPort == client local port (reciprocal)");
            check(accepted.getLocalPort() == ss.getLocalPort(),
                    "accepted local port == server port");
            check(accepted.getInetAddress() != null
                    && accepted.getInetAddress().isLoopbackAddress(),
                    "accepted remote is the loopback client");
            check(accepted.getChannel() == null, "accepted channel null");
            // Byte round-trip in both directions.
            client.getOutputStream().write(0x5A);
            client.getOutputStream().flush();
            accepted.setSoTimeout(500);
            check(accepted.getInputStream().read() == 0x5A, "server reads client byte");
            accepted.getOutputStream().write(0xA5);
            accepted.getOutputStream().flush();
            client.setSoTimeout(500);
            check(client.getInputStream().read() == 0xA5, "client reads server byte");
            // Accepted socket options round-trip.
            accepted.setSoTimeout(150);
            check(accepted.getSoTimeout() == 150, "accepted SoTimeout round trip");
            accepted.setTcpNoDelay(true);
            check(accepted.getTcpNoDelay(), "accepted TCP_NODELAY round trip");
        } finally {
            try { client.close(); } catch (IOException ignore) { }
            try { if (accepted != null) accepted.close(); } catch (IOException ignore) { }
            ss.close();
        }
    }

    static void testServerOptions() throws Exception {
        ServerSocket ss = new ServerSocket();
        try {
            check(ss.getReceiveBufferSize() > 0, "default receive buffer positive");
            ss.setReceiveBufferSize(8192);
            check(ss.getReceiveBufferSize() >= 8192, "receive buffer floor");
            try { ss.setReceiveBufferSize(0); check(false, "setReceiveBufferSize(0) must fail"); }
            catch (IllegalArgumentException e) {
                check("negative receive size".equals(e.getMessage()), "setReceiveBufferSize(0) message");
            }
            try { ss.setReceiveBufferSize(-1); check(false, "setReceiveBufferSize(-1) must fail"); }
            catch (IllegalArgumentException e) {
                check("negative receive size".equals(e.getMessage()), "setReceiveBufferSize(-1) message");
            }
            // REUSEADDR before bind: Linux default reads true on both JDKs.
            check(ss.getReuseAddress(), "default REUSEADDR true before bind (Linux)");
            ss.setReuseAddress(true);
            check(ss.getReuseAddress(), "REUSEADDR round trip true");
            ss.setReuseAddress(false);
            check(!ss.getReuseAddress(), "REUSEADDR round trip false");
        } finally { ss.close(); }
    }

    static void testBacklogCtor() throws Exception {
        InetAddress lo = InetAddress.getByName("127.0.0.1");
        ServerSocket a = new ServerSocket(0, 1, lo);
        try {
            check(a.isBound(), "backlog ctor bound");
            check(a.getLocalPort() > 0, "backlog ctor port");
            check(a.getInetAddress() != null && a.getInetAddress().isLoopbackAddress(),
                    "backlog ctor loopback");
        } finally { a.close(); }
        ServerSocket b = new ServerSocket(0, 100, lo);
        try {
            check(b.isBound(), "large backlog ctor bound");
            check(b.getLocalPort() > 0, "large backlog ctor port");
        } finally { b.close(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.ServerSocket");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor(int)");
        check(c.getConstructor(int.class, int.class) != null, "ctor(int,int)");
        check(c.getConstructor(int.class, int.class, InetAddress.class) != null, "ctor(int,int,InetAddress)");
        check(c.getMethod("bind", SocketAddress.class) != null, "bind(SocketAddress)");
        check(c.getMethod("bind", SocketAddress.class, int.class) != null, "bind(SocketAddress,int)");
        check(c.getMethod("accept") != null, "accept");
        check(c.getMethod("close") != null, "close");
        check(c.getMethod("isBound") != null, "isBound");
        check(c.getMethod("isClosed") != null, "isClosed");
        check(c.getMethod("getLocalPort") != null, "getLocalPort");
        check(c.getMethod("getLocalSocketAddress") != null, "getLocalSocketAddress");
        check(c.getMethod("setSoTimeout", int.class) != null, "setSoTimeout");
        check(c.getMethod("setReuseAddress", boolean.class) != null, "setReuseAddress");
        check(c.getMethod("setReceiveBufferSize", int.class) != null, "setReceiveBufferSize");
        check(c.getMethod("setSocketFactory", java.net.SocketImplFactory.class) != null, "setSocketFactory");
        check(c.getMethod("getChannel") != null, "getChannel");
        // implAccept is protected final on JDK-8.
        java.lang.reflect.Method m = c.getDeclaredMethod("implAccept", Socket.class);
        check(java.lang.reflect.Modifier.isProtected(m.getModifiers()), "implAccept protected");
        check(java.lang.reflect.Modifier.isFinal(m.getModifiers()), "implAccept final");
    }

    static void testToStringForms() throws Exception {
        ServerSocket a = new ServerSocket();
        check("ServerSocket[unbound]".equals(a.toString()), "unbound toString exact");
        a.bind(null);
        check(a.toString().equals("ServerSocket[addr=0.0.0.0/0.0.0.0,localport=" + a.getLocalPort() + "]"),
                "wildcard toString exact, got: " + a.toString());
        a.close();
        ServerSocket b = new ServerSocket(0, 5, InetAddress.getByName("127.0.0.1"));
        check(b.toString().equals("ServerSocket[addr=/127.0.0.1,localport=" + b.getLocalPort() + "]"),
                "loopback toString exact, got: " + b.toString());
        b.close();
        // toString after close retains the bound form.
        check(b.toString().contains("addr="), "closed toString keeps addr form");
    }

    static void testMultipleAccepts() throws Exception {
        final ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(4);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try {
                    for (int i = 0; i < 3; i++) q.put(ss.accept());
                } catch (Exception ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        Socket[] clients = new Socket[3];
        Socket[] accepted = new Socket[3];
        try {
            for (int i = 0; i < 3; i++) {
                clients[i] = new Socket();
                clients[i].connect(new InetSocketAddress("127.0.0.1", ss.getLocalPort()), 200);
            }
            for (int i = 0; i < 3; i++) accepted[i] = q.poll(2, TimeUnit.SECONDS);
            acceptor.join(2000);
            check(accepted[0] != null && accepted[1] != null && accepted[2] != null, "all 3 accepts done");
            // Every accepted socket shares the server endpoint but sees a
            // distinct remote port.
            for (int i = 0; i < 3; i++) {
                check(accepted[i].getLocalPort() == ss.getLocalPort(), "accepted " + i + " local port");
                check(accepted[i].getPort() > 0, "accepted " + i + " remote port positive");
            }
            check(accepted[0].getPort() != accepted[1].getPort(), "distinct remote ports 0/1");
            check(accepted[1].getPort() != accepted[2].getPort(), "distinct remote ports 1/2");
            // Pairing by remote port: every client local port appears among
            // the accepted remote ports.
            java.util.Set<Integer> remotePorts = new java.util.HashSet<Integer>();
            for (int i = 0; i < 3; i++) remotePorts.add(accepted[i].getPort());
            for (int i = 0; i < 3; i++) {
                check(remotePorts.contains(clients[i].getLocalPort()),
                        "client " + i + " local port found among accepted remotes");
            }
        } finally {
            for (int i = 0; i < 3; i++) {
                try { if (clients[i] != null) clients[i].close(); } catch (IOException ignore) { }
                try { if (accepted[i] != null) accepted[i].close(); } catch (IOException ignore) { }
            }
            ss.close();
        }
    }

    static void testServerSideHalfClose() throws Exception {
        final ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        final ArrayBlockingQueue<Socket> q = new ArrayBlockingQueue<Socket>(1);
        Thread acceptor = new Thread(new Runnable() {
            public void run() {
                try { q.put(ss.accept()); } catch (Exception ignore) { }
            }
        });
        acceptor.setDaemon(true);
        acceptor.start();
        Socket client = new Socket();
        Socket accepted = null;
        try {
            client.connect(new InetSocketAddress("127.0.0.1", ss.getLocalPort()), 200);
            accepted = q.poll(2, TimeUnit.SECONDS);
            acceptor.join(2000);
            check(accepted != null, "accept for half-close test");
            // Server-side shutdownOutput is seen by the client as EOF.
            accepted.shutdownOutput();
            check(accepted.isOutputShutdown(), "server output shutdown flag");
            client.setSoTimeout(500);
            check(client.getInputStream().read() == -1, "client sees EOF after server shutdownOutput");
            // Client can still send; server input side remains open.
            client.getOutputStream().write(33);
            client.getOutputStream().flush();
            accepted.setSoTimeout(500);
            check(accepted.getInputStream().read() == 33, "server reads after its shutdownOutput");
        } finally {
            try { client.close(); } catch (IOException ignore) { }
            try { if (accepted != null) accepted.close(); } catch (IOException ignore) { }
            ss.close();
        }
    }

    static void testMiscInvariants() throws Exception {
        ServerSocket a = new ServerSocket(0);
        ServerSocket b = new ServerSocket(0);
        try {
            check(a.getLocalPort() != b.getLocalPort(), "distinct ephemeral ports");
            check(a.equals(a), "server equals itself");
            check(!a.equals(b), "distinct servers not equal");
            check(!a.equals(null), "not equal to null");
            check(a.hashCode() == a.hashCode(), "hashCode stable");
            check(a.getChannel() == null, "channel null");
            check(a.getLocalSocketAddress() != null, "local SA non-null");
            check(((InetSocketAddress) a.getLocalSocketAddress()).getPort() == a.getLocalPort(),
                    "SA port equals local port");
        } finally {
            a.close();
            b.close();
        }
    }

    static void testClosedOptionFailures() throws Exception {
        // Every mutating option on a closed server fails with "Socket is closed";
        // reads keep the bound view.
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        InetAddress lo = ss.getInetAddress();
        int port = ss.getLocalPort();
        ss.close();
        try { ss.setSoTimeout(100); check(false, "closed setSoTimeout"); }
        catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed setSoTimeout msg"); }
        try { ss.setReceiveBufferSize(1024); check(false, "closed setReceiveBufferSize"); }
        catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed setRcvBuf msg"); }
        try { ss.setReuseAddress(true); check(false, "closed setReuseAddress"); }
        catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed setReuseAddress msg"); }
        check(ss.getInetAddress() != null && ss.getInetAddress().isLoopbackAddress(),
                "closed keeps loopback inet address view");
        check(ss.getLocalPort() == port, "closed keeps port view");
        check(ss.getLocalSocketAddress() != null, "closed keeps local SA view");
        check(lo.isLoopbackAddress(), "pre-close address was loopback");
        // An unbound server that is then closed also reports "Socket is closed"
        // for accept (closed wins over not-bound).
        ServerSocket u = new ServerSocket();
        u.close();
        try { u.accept(); check(false, "closed-unbound accept"); }
        catch (SocketException e) { check("Socket is closed".equals(e.getMessage()), "closed-unbound accept msg"); }
    }

    static void testBacklogQueueing() throws Exception {
        // Three clients connect before any accept; the kernel backlog holds
        // them all, then accept drains them in main thread.
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        Socket[] clients = new Socket[3];
        Socket[] accepted = new Socket[3];
        try {
            for (int i = 0; i < 3; i++) {
                clients[i] = new Socket();
                clients[i].connect(new InetSocketAddress("127.0.0.1", ss.getLocalPort()), 200);
                check(clients[i].isConnected(), "client " + i + " connected before accept");
            }
            for (int i = 0; i < 3; i++) {
                ss.setSoTimeout(500);
                accepted[i] = ss.accept();
                check(accepted[i] != null, "accept " + i + " drains backlog");
            }
            for (int i = 0; i < 3; i++) {
                check(accepted[i].getLocalPort() == ss.getLocalPort(), "drained " + i + " local port");
            }
            // Each connection is independently usable.
            for (int i = 0; i < 3; i++) clients[i].getOutputStream().write(i + 10);
            for (int i = 0; i < 3; i++) clients[i].getOutputStream().flush();
            for (int i = 0; i < 3; i++) {
                accepted[i].setSoTimeout(500);
                check(accepted[i].getInputStream().read() == i + 10, "drained pair " + i + " byte round trip");
            }
        } finally {
            for (int i = 0; i < 3; i++) {
                try { if (clients[i] != null) clients[i].close(); } catch (IOException ignore) { }
                try { if (accepted[i] != null) accepted[i].close(); } catch (IOException ignore) { }
            }
            ss.close();
        }
    }

    static void testAcceptTimeoutTyping() throws Exception {
        // The accept timeout exception is an InterruptedIOException (not a
        // SocketException) with bytesTransferred 0.
        ServerSocket ss = new ServerSocket(0, 50, InetAddress.getByName("127.0.0.1"));
        try {
            check(ss.getSoTimeout() == 0, "default SoTimeout 0");
            ss.setSoTimeout(100);
            try {
                ss.accept();
                check(false, "must time out");
            } catch (SocketTimeoutException e) {
                check(e instanceof java.io.InterruptedIOException, "STE is InterruptedIOException");
                check(!SocketException.class.isInstance(e), "STE is not a SocketException");
                check(e.bytesTransferred == 0, "bytesTransferred 0");
                check(e.getMessage() != null, "timeout message non-null");
            }
            // The timeout does not reset the option.
            check(ss.getSoTimeout() == 100, "SoTimeout survives accept timeout");
        } finally { ss.close(); }
    }

    public static void main(String[] args) throws Exception {
        java.net.ServerSocket ss = new java.net.ServerSocket(0);
        check(ss.isBound(), "isBound");
        check(ss.getLocalPort() > 0, "getLocalPort > 0");
        ss.close();
        check(ss.isClosed(), "isClosed");
        testUnboundState();
        testBindVariants();
        testBindFailures();
        testCloseSemantics();
        testAcceptTimeout();
        testAcceptPair();
        testServerOptions();
        testBacklogCtor();
        testWhiteBox();
        testToStringForms();
        testMultipleAccepts();
        testServerSideHalfClose();
        testMiscInvariants();
        testClosedOptionFailures();
        testBacklogQueueing();
        testAcceptTimeoutTyping();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

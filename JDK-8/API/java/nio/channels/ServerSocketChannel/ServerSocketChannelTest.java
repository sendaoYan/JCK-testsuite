/*
 * @test
 * @summary java.nio.channels.ServerSocketChannel conformance: open/bind/address.
 * @run main ServerSocketChannelTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.StandardSocketOptions;
import java.nio.channels.AlreadyBoundException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.NotYetBoundException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class ServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    /** Throwing action so lambdas may call methods that declare checked IOException. */
    interface Op { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static void testServerSocketChannel() throws Exception {
        ServerSocketChannel ch = ServerSocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == SelectionKey.OP_ACCEPT, "validOps OP_ACCEPT");
        ch.bind(new InetSocketAddress(0));
        check(ch.getLocalAddress() != null, "getLocalAddress");
        check(ch.isBlocking(), "initially blocking");
        ch.configureBlocking(false);
        check(!ch.isBlocking(), "non-blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    // ===================== A6 deepening: bind/accept lifecycle, selector flow, options, close =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.ServerSocketChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "ServerSocketChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c), "implements NetworkChannel");
        boolean prot = true;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            prot &= java.lang.reflect.Modifier.isProtected(ct.getModifiers());
        check(prot, "declared constructors are protected");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "bind", "socket", "accept", "setOption",
                "getOption", "supportedOptions", "getLocalAddress", "validOps"})
            check(names.contains(n), "has method " + n);
    }

    static void testOpenState() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.isOpen(), "open");
            check(ssc.getLocalAddress() == null, "unbound: getLocalAddress is null");
            check(ssc.socket().getLocalPort() == -1, "unbound: adaptor port is -1");
            check(!ssc.socket().isBound(), "unbound: adaptor not bound");
            check(ssc.isBlocking(), "default blocking");
            check(ssc.validOps() == SelectionKey.OP_ACCEPT, "validOps only OP_ACCEPT");
            check(ssc.supportedOptions() != null && !ssc.supportedOptions().isEmpty(),
                  "supportedOptions non-empty");
            check(ssc.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF),
                  "SO_RCVBUF supported");
            check(ssc.supportedOptions().contains(StandardSocketOptions.SO_REUSEADDR),
                  "SO_REUSEADDR supported");
            check(ssc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
        }
    }

    static void testAcceptUnbound() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            checkThrows(NotYetBoundException.class, () -> ssc.accept(),
                        "blocking accept before bind -> NotYetBoundException");
            ssc.configureBlocking(false);
            checkThrows(NotYetBoundException.class, () -> ssc.accept(),
                        "non-blocking accept before bind -> NotYetBoundException");
        }
    }

    static void testBindSemantics() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.bind(null) == ssc, "bind(null) returns this");
            InetSocketAddress wild = (InetSocketAddress) ssc.getLocalAddress();
            check(wild != null && wild.getPort() > 0, "wildcard bind has an ephemeral port");
            check(wild.getAddress().isAnyLocalAddress(), "bind(null) binds the wildcard address");
            check(ssc.socket().isBound(), "adaptor bound");
            check(ssc.socket().getLocalPort() == wild.getPort(), "adaptor port matches");
            checkThrows(AlreadyBoundException.class, () -> ssc.bind(null),
                        "second bind -> AlreadyBoundException");
            checkThrows(AlreadyBoundException.class,
                        () -> ssc.bind(new InetSocketAddress(LO, 0)),
                        "bind to another address while bound -> AlreadyBoundException");
        }
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            InetSocketAddress a = (InetSocketAddress) ssc.bind(new InetSocketAddress(LO, 0)).getLocalAddress();
            check(a.getAddress().equals(LO), "bound to the loopback address");
            check(a.getPort() > 0, "ephemeral port on loopback");
        }
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0), 1); // backlog overload
            check(((InetSocketAddress) ssc.getLocalAddress()).getPort() > 0,
                  "bind(addr, backlog) binds");
        }
    }

    static void testNonBlockingAcceptNull() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            ssc.configureBlocking(false);
            check(ssc.accept() == null, "non-blocking accept with no pending connection is null");
            check(ssc.accept() == null, "still null on second call");
        }
    }

    static void testSelectorAcceptFlow() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            ssc.configureBlocking(false);
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (Selector sel = Selector.open()) {
                SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(sel.selectNow() == 0, "not ready before client connects");
                try (SocketChannel client = SocketChannel.open(addr)) {
                    check(client.isConnected(), "client connected (blocking open-connect)");
                    check(sel.select(5_000) == 1, "exactly one ready channel");
                check(sel.selectedKeys().size() == 1, "exactly one selected key");
                check(key.readyOps() == SelectionKey.OP_ACCEPT, "readyOps == OP_ACCEPT");
                SocketChannel accepted = ssc.accept();
                check(accepted != null, "accept returns the pending connection");
                check(accepted.isConnected(), "accepted channel is connected");
                check(accepted.getRemoteAddress() != null, "accepted has a remote address");
                InetSocketAddress remote = (InetSocketAddress) accepted.getRemoteAddress();
                InetSocketAddress clientLocal = (InetSocketAddress) client.getLocalAddress();
                check(remote.getPort() == clientLocal.getPort(),
                      "accepted remote port equals client local port");
                check(accepted.isBlocking(), "accepted channel defaults to blocking");
                check(sel.selectedKeys().remove(key), "remove processed key");
                check(sel.selectNow() == 0, "no further readiness");
                check(ssc.accept() == null, "accept drained the pending connection");
                accepted.close();
                }
            }
        }
    }

    static void testBlockingAcceptWithDaemonClient() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            final InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            final SocketChannel[] client = new SocketChannel[1];
            Thread t = new Thread(() -> {
                try { client[0] = SocketChannel.open(addr); }
                catch (Exception ignore) { }
            }, "ssc-client");
            t.setDaemon(true);
            t.start();
            SocketChannel accepted = ssc.accept(); // blocking until the client arrives
            check(accepted != null && accepted.isConnected(), "blocking accept returned a connection");
            t.join(5_000);
            check(!t.isAlive(), "client daemon finished");
            check(client[0] != null && client[0].isConnected(), "client side connected");
            accepted.close();
            client[0].close();
        }
    }

    static void testSocketAdaptor() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            ServerSocket sock = ssc.socket();
            check(sock.getChannel() == ssc, "adaptor getChannel identity");
            check(sock.isBound(), "adaptor isBound");
            check(sock.getLocalPort() == ((InetSocketAddress) ssc.getLocalAddress()).getPort(),
                  "adaptor local port equals channel port");
            check(sock.isClosed() == false, "adaptor open while channel open");
            // socket().accept() blocks until the daemon client connects
            final InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            final Socket[] client = new Socket[1];
            Thread t = new Thread(() -> {
                try { client[0] = new Socket(addr.getAddress(), addr.getPort()); }
                catch (Exception ignore) { }
            }, "ssc-sock-client");
            t.setDaemon(true);
            t.start();
            Socket accepted = sock.accept();
            check(accepted != null && accepted.isConnected(), "adaptor accept returned a socket");
            t.join(5_000);
            check(!t.isAlive(), "adaptor client daemon finished");
            accepted.close();
            if (client[0] != null) client[0].close();
        }
    }

    static void testOptions() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024) == ssc,
                  "setOption returns this");
            int rcv = ssc.getOption(StandardSocketOptions.SO_RCVBUF);
            check(rcv >= 64 * 1024, "SO_RCVBUF readback >= requested, got " + rcv);
            check(ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true) == ssc,
                  "SO_REUSEADDR settable");
            check(ssc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback true");
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, false);
            check(!ssc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback false");
            // options outside supportedOptions()
            checkThrows(UnsupportedOperationException.class,
                        () -> ssc.setOption(StandardSocketOptions.SO_SNDBUF, 1024),
                        "setOption(SO_SNDBUF) on SSC -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                        () -> ssc.getOption(StandardSocketOptions.SO_SNDBUF),
                        "getOption(SO_SNDBUF) on SSC -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                        () -> ssc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1),
                        "setOption(IP_MULTICAST_TTL) on SSC -> UnsupportedOperationException");
        }
    }

    static void testCloseSemantics() throws Exception {
        ServerSocketChannel ssc = ServerSocketChannel.open();
        ssc.bind(new InetSocketAddress(LO, 0));
        ssc.close();
        check(!ssc.isOpen(), "closed");
        checkThrows(ClosedChannelException.class, () -> ssc.accept(),
                    "accept after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class, () -> ssc.bind(null),
                    "bind after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class,
                    () -> ssc.setOption(StandardSocketOptions.SO_RCVBUF, 1024),
                    "setOption after close -> ClosedChannelException");
        checkThrows(ClosedChannelException.class,
                    () -> ssc.getOption(StandardSocketOptions.SO_RCVBUF),
                    "getOption after close -> ClosedChannelException");
        ssc.close(); // idempotent
        check(!ssc.isOpen(), "still closed after second close");
        check(ssc.validOps() == SelectionKey.OP_ACCEPT, "validOps still queryable after close");
    }

    static void testRegisterWhileBound() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open(); Selector sel = Selector.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            checkThrows(IllegalBlockingModeException.class,
                        () -> ssc.register(sel, SelectionKey.OP_ACCEPT),
                        "register while blocking -> IllegalBlockingModeException");
            ssc.configureBlocking(false);
            SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
            check(key.isValid(), "registered non-blocking bound channel");
            checkThrows(IllegalBlockingModeException.class, () -> ssc.configureBlocking(true),
                        "configureBlocking(true) while registered -> IllegalBlockingModeException");
        }
    }

    public static void main(String[] args) throws Exception {
        testServerSocketChannel();
        testWhiteBoxStructure();
        testOpenState();
        testAcceptUnbound();
        testBindSemantics();
        testNonBlockingAcceptNull();
        testSelectorAcceptFlow();
        testBlockingAcceptWithDaemonClient();
        testSocketAdaptor();
        testOptions();
        testCloseSemantics();
        testRegisterWhileBound();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

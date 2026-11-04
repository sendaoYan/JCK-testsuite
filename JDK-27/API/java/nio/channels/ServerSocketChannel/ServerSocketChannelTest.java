/*
 * @test
 * @summary java.nio.channels.ServerSocketChannel conformance: white-box structure + black-box behavior.
 * @run main ServerSocketChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.StandardSocketOptions;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class ServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.ServerSocketChannel");
        check(Modifier.isAbstract(c.getModifiers()), "ServerSocketChannel is abstract");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.NetworkChannel.class.isAssignableFrom(c), "implements NetworkChannel");
        boolean protCtor = true;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            protCtor &= Modifier.isProtected(ct.getModifiers());
        check(protCtor, "declared constructors are protected");
        Set<String> names = new java.util.HashSet<>();
        for (Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "bind", "socket", "accept", "setOption",
                "getLocalAddress", "validOps"})
            check(names.contains(n), "has method " + n);
    }
    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.isOpen(), "open");
            check(!ssc.isRegistered(), "not registered");
            check(ssc.isBlocking(), "default blocking");

            // accept before bind -> NotYetBoundException
            boolean threw = false;
            try { ssc.accept(); }
            catch (java.nio.channels.NotYetBoundException e) { threw = true; }
            check(threw, "accept before bind -> NotYetBoundException");

            // bind(null) to ephemeral port on loopback
            InetSocketAddress bound = (InetSocketAddress) ssc.bind(null).getLocalAddress();
            check(bound.getPort() > 0, "bound to ephemeral port");

            // double bind -> AlreadyBoundException
            threw = false;
            try { ssc.bind(null); }
            catch (java.nio.channels.AlreadyBoundException e) { threw = true; }
            check(threw, "second bind -> AlreadyBoundException");

            // backlog overload + option
            ssc.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
            check(ssc.getOption(StandardSocketOptions.SO_RCVBUF) >= 16 * 1024, "SO_RCVBUF set");
            check(!ssc.supportedOptions().isEmpty(), "supportedOptions non-empty");

            // socket() adaptor mirrors binding
            check(ssc.socket().isBound(), "adaptor bound");
            check(ssc.socket().getLocalPort() == bound.getPort(), "adaptor port");

            // accept in blocking mode
            try (SocketChannel client = SocketChannel.open(
                    new InetSocketAddress(InetAddress.getLoopbackAddress(), bound.getPort()))) {
                SocketChannel accepted = ssc.accept();
                check(accepted != null, "accepted connection");
                check(accepted.isConnected(), "accepted connected");
                accepted.close();
            }

            // accept in non-blocking mode returns null when no pending connection
            ssc.configureBlocking(false);
            check(!ssc.isBlocking(), "non-blocking");
            check(ssc.accept() == null, "accept returns null when no connection pending");
            ssc.configureBlocking(true);

            // validOps: only OP_ACCEPT
            check(ssc.validOps() == SelectionKey.OP_ACCEPT, "validOps == OP_ACCEPT");

            // setOption returns the channel for chaining; bind(SocketAddress,int) overload
            try (ServerSocketChannel ssc2 = ServerSocketChannel.open()) {
                check(ssc2.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0), 10) != null,
                      "bind(addr, backlog) returns channel");
            }
        }

        // open(ProtocolFamily) variant
        try (ServerSocketChannel ssc = ServerSocketChannel.open(java.net.StandardProtocolFamily.INET)) {
            check(ssc.isOpen(), "INET family open");
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            check(((InetSocketAddress) ssc.getLocalAddress()).getPort() > 0, "INET family bind");
        }
        try (ServerSocketChannel ssc = ServerSocketChannel.open(java.net.StandardProtocolFamily.UNIX)) {
            check(ssc.isOpen(), "UNIX family open");
        }
    }

    // ===================== A6 deepening: bind/accept lifecycle, selector flow, options, close =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

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

    static void testUnboundState() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.getLocalAddress() == null, "unbound: getLocalAddress is null");
            check(ssc.socket().getLocalPort() == -1, "unbound: adaptor port is -1");
            check(!ssc.socket().isBound(), "unbound: adaptor not bound");
            check(ssc.validOps() == SelectionKey.OP_ACCEPT, "validOps only OP_ACCEPT");
            check(ssc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            checkThrows(java.nio.channels.NotYetBoundException.class, () -> ssc.accept(),
                        "blocking accept before bind -> NotYetBoundException");
            ssc.configureBlocking(false);
            checkThrows(java.nio.channels.NotYetBoundException.class, () -> ssc.accept(),
                        "non-blocking accept before bind -> NotYetBoundException");
        }
    }

    static void testBindSemantics() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.bind(null) == ssc, "bind(null) returns this");
            InetSocketAddress wild = (InetSocketAddress) ssc.getLocalAddress();
            check(wild != null && wild.getPort() > 0, "wildcard bind has an ephemeral port");
            check(wild.getAddress().isAnyLocalAddress(), "bind(null) binds the wildcard address");
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> ssc.bind(null),
                        "second bind -> AlreadyBoundException");
            checkThrows(java.nio.channels.AlreadyBoundException.class,
                        () -> ssc.bind(new InetSocketAddress(LO, 0)),
                        "bind to another address while bound -> AlreadyBoundException");
        }
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            InetSocketAddress a = (InetSocketAddress)
                    ssc.bind(new InetSocketAddress(LO, 0), 5).getLocalAddress();
            check(a.getAddress().equals(LO), "bind(loopback, backlog) binds the loopback address");
            check(a.getPort() > 0, "ephemeral port with backlog overload");
            check(ssc.socket().getLocalPort() == a.getPort(), "adaptor port matches");
        }
    }

    static void testSelectorAcceptFlow() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            ssc.configureBlocking(false);
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
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
                    InetSocketAddress remote = (InetSocketAddress) accepted.getRemoteAddress();
                    check(remote.getPort()
                          == ((InetSocketAddress) client.getLocalAddress()).getPort(),
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
            java.net.ServerSocket sock = ssc.socket();
            check(sock.getChannel() == ssc, "adaptor getChannel identity");
            check(sock.getLocalPort() == ((InetSocketAddress) ssc.getLocalAddress()).getPort(),
                  "adaptor local port equals channel port");
            final InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            final java.net.Socket[] client = new java.net.Socket[1];
            Thread t = new Thread(() -> {
                try { client[0] = new java.net.Socket(addr.getAddress(), addr.getPort()); }
                catch (Exception ignore) { }
            }, "ssc-sock-client");
            t.setDaemon(true);
            t.start();
            java.net.Socket accepted = sock.accept();
            check(accepted != null && accepted.isConnected(), "adaptor accept returned a socket");
            t.join(5_000);
            check(!t.isAlive(), "adaptor client daemon finished");
            accepted.close();
            if (client[0] != null) client[0].close();
        }
    }

    static void testOptionDetails() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            check(ssc.setOption(StandardSocketOptions.SO_REUSEADDR, true) == ssc,
                  "setOption returns this");
            check(ssc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback true");
            ssc.setOption(StandardSocketOptions.SO_REUSEADDR, false);
            check(!ssc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback false");
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
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> ssc.accept(),
                    "accept after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class, () -> ssc.bind(null),
                    "bind after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ssc.setOption(StandardSocketOptions.SO_RCVBUF, 1024),
                    "setOption after close -> ClosedChannelException");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> ssc.getOption(StandardSocketOptions.SO_RCVBUF),
                    "getOption after close -> ClosedChannelException");
        ssc.close(); // idempotent
        check(!ssc.isOpen(), "still closed after second close");
        check(ssc.validOps() == SelectionKey.OP_ACCEPT, "validOps still queryable after close");
    }

    static void testRegisterWhileBound() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            ssc.bind(new InetSocketAddress(LO, 0));
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> ssc.register(sel, SelectionKey.OP_ACCEPT),
                        "register while blocking -> IllegalBlockingModeException");
            ssc.configureBlocking(false);
            SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
            check(key.isValid(), "registered non-blocking bound channel");
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> ssc.configureBlocking(true),
                        "configureBlocking(true) while registered -> IllegalBlockingModeException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnboundState();
        testBindSemantics();
        testSelectorAcceptFlow();
        testBlockingAcceptWithDaemonClient();
        testSocketAdaptor();
        testOptionDetails();
        testCloseSemantics();
        testRegisterWhileBound();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

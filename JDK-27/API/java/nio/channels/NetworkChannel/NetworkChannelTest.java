/*
 * @test
 * @summary java.nio.channels.NetworkChannel conformance: white-box structure + black-box behavior.
 * @run main NetworkChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.NetworkChannel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class NetworkChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.NetworkChannel");
        check(c.isInterface(), "NetworkChannel is an interface");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "extends Channel");
        for (String n : new String[] {"bind", "getLocalAddress", "setOption", "getOption",
                "supportedOptions"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();

        // exercise the contract through SocketChannel
        try (SocketChannel sc = SocketChannel.open()) {
            NetworkChannel nc = sc;
            InetSocketAddress local = (InetSocketAddress) nc.bind(new InetSocketAddress(lo, 0)).getLocalAddress();
            check(local.getPort() > 0, "bound to ephemeral port");
            check(sc.socket().getLocalPort() == local.getPort(), "adaptor sees the binding");

            // setOption returns the channel for chaining; getOption round trip
            NetworkChannel chained = nc.setOption(StandardSocketOptions.SO_REUSEADDR, true);
            check(chained == nc, "setOption returns this");
            check(nc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR round trip");
            nc.setOption(StandardSocketOptions.SO_SNDBUF, 64 * 1024);
            check(nc.getOption(StandardSocketOptions.SO_SNDBUF) >= 64 * 1024, "SO_SNDBUF round trip");

            Set<SocketOption<?>> options = nc.supportedOptions();
            check(options != null && !options.isEmpty(), "supportedOptions non-empty");
            check(options.contains(StandardSocketOptions.SO_SNDBUF), "SO_SNDBUF supported");

            boolean threw = false;
            try { nc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true); }
            catch (UnsupportedOperationException | IllegalArgumentException e) { threw = true; }
            check(threw, "unsupported socket option rejected");
        }

        // same contract through ServerSocketChannel
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            NetworkChannel nc = ssc;
            nc.bind(new InetSocketAddress(lo, 0));
            check(((InetSocketAddress) nc.getLocalAddress()).getPort() > 0, "server bound");
            nc.setOption(StandardSocketOptions.SO_RCVBUF, 16 * 1024);
            check(nc.getOption(StandardSocketOptions.SO_RCVBUF) >= 16 * 1024, "SO_RCVBUF");
            check(nc.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF), "rcvbuf supported");
        }
    }

    // ===================== A6 deepening: unbound addresses / datagram contract / closed rules =====================

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

    static void testUnboundAndUnconnectedAddresses() throws Exception {
        InetAddress lo = InetAddress.getLoopbackAddress();
        try (SocketChannel sc = SocketChannel.open();
             ServerSocketChannel ssc = ServerSocketChannel.open();
             java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            check(sc.getLocalAddress() == null, "unbound SocketChannel: local address null");
            check(sc.getRemoteAddress() == null, "unconnected SocketChannel: remote null");
            check(ssc.getLocalAddress() == null, "unbound ServerSocketChannel: local null");
            check(dc.getLocalAddress() == null, "unbound DatagramChannel: local null");
            check(dc.getRemoteAddress() == null, "unconnected DatagramChannel: remote null");
            // bind(null) wildcard vs bind(loopback) exact
            NetworkChannel w = ssc;
            check(w.bind(null) == w, "bind(null) returns this");
            InetSocketAddress wild = (InetSocketAddress) w.getLocalAddress();
            check(wild != null && wild.getPort() > 0, "wildcard ephemeral port");
            check(wild.getAddress().isAnyLocalAddress(), "bind(null) uses the wildcard address");
            try (ServerSocketChannel s2 = ServerSocketChannel.open()) {
                InetSocketAddress a = (InetSocketAddress)
                        s2.bind(new InetSocketAddress(lo, 0)).getLocalAddress();
                check(a.getAddress().equals(lo), "bind(loopback) binds the loopback address");
                checkThrows(java.nio.channels.AlreadyBoundException.class,
                            () -> s2.bind(new InetSocketAddress(lo, 0)),
                            "second bind -> AlreadyBoundException");
            }
        }
    }

    static void testContractThroughDatagramChannel() throws Exception {
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            NetworkChannel nc = dc;
            nc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            check(nc.getLocalAddress() != null, "bound address available");
            check(nc.getOption(StandardSocketOptions.SO_RCVBUF) > 0, "SO_RCVBUF positive");
            nc.setOption(StandardSocketOptions.SO_BROADCAST, true);
            check(nc.getOption(StandardSocketOptions.SO_BROADCAST), "SO_BROADCAST readback true");
            checkThrows(UnsupportedOperationException.class,
                        () -> nc.setOption(StandardSocketOptions.SO_KEEPALIVE, true),
                        "stream-only option on datagram -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                        () -> nc.getOption(StandardSocketOptions.SO_KEEPALIVE),
                        "getOption unsupported -> UnsupportedOperationException");
        }
    }

    static void testClosedChannelRules() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.close();
            NetworkChannel nc = sc;
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> nc.bind(null),
                        "bind after close -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> nc.getOption(StandardSocketOptions.SO_SNDBUF),
                        "getOption after close -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> nc.setOption(StandardSocketOptions.SO_SNDBUF, 1024),
                        "setOption after close -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> nc.getLocalAddress(),
                        "getLocalAddress after close -> ClosedChannelException (per javadoc)");
            check(!nc.supportedOptions().isEmpty(),
                  "supportedOptions still queryable after close");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnboundAndUnconnectedAddresses();
        testContractThroughDatagramChannel();
        testClosedChannelRules();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

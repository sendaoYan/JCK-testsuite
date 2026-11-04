/*
 * @test
 * @summary java.nio.channels.NetworkChannel conformance: interface hierarchy and methods.
 * @run main NetworkChannelTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketOption;
import java.net.StandardSocketOptions;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.NetworkChannel;
import java.nio.channels.Channel;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

public class NetworkChannelTest {
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

    static void testNetworkChannel() {
        check(NetworkChannel.class.isInterface(), "is interface");
        check(Channel.class.isAssignableFrom(NetworkChannel.class), "extends Channel");
        Set<String> names = new HashSet<String>();
        for (Method m : NetworkChannel.class.getMethods()) names.add(m.getName());
        String[] required = { "bind", "getLocalAddress", "setOption", "getOption", "supportedOptions" };
        for (String n : required) check(names.contains(n), "has method " + n);
        // every concrete network channel type implements the interface
        check(NetworkChannel.class.isAssignableFrom(SocketChannel.class),
              "SocketChannel implements NetworkChannel");
        check(NetworkChannel.class.isAssignableFrom(ServerSocketChannel.class),
              "ServerSocketChannel implements NetworkChannel");
        check(NetworkChannel.class.isAssignableFrom(DatagramChannel.class),
              "DatagramChannel implements NetworkChannel");
    }

    // ===================== A6 deepening: bind/addresses/options through the interface type =====================

    static final InetAddress LO = InetAddress.getLoopbackAddress();

    static void testContractThroughSocketChannel() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            NetworkChannel nc = sc;
            check(nc.getLocalAddress() == null, "unbound SocketChannel: local address null");
            check(sc.getRemoteAddress() == null, "unconnected: remote address null");
            check(nc.bind(null) == nc, "bind(null) returns this");
            InetSocketAddress local = (InetSocketAddress) nc.getLocalAddress();
            check(local != null && local.getPort() > 0, "bound to an ephemeral port");
            check(local.getAddress().isAnyLocalAddress(), "bind(null) uses the wildcard address");
            checkThrows(java.nio.channels.AlreadyBoundException.class, () -> nc.bind(null),
                        "second bind -> AlreadyBoundException");
            // option chaining and round trips through the interface type
            check(nc.setOption(StandardSocketOptions.SO_REUSEADDR, true) == nc,
                  "setOption returns this");
            check(nc.getOption(StandardSocketOptions.SO_REUSEADDR), "SO_REUSEADDR readback true");
            nc.setOption(StandardSocketOptions.SO_SNDBUF, 32 * 1024);
            check(nc.getOption(StandardSocketOptions.SO_SNDBUF) >= 32 * 1024,
                  "SO_SNDBUF readback >= requested");
            Set<SocketOption<?>> options = nc.supportedOptions();
            check(options != null && !options.isEmpty(), "supportedOptions non-empty");
            check(options.contains(StandardSocketOptions.SO_SNDBUF), "SO_SNDBUF supported");
            check(options.contains(StandardSocketOptions.SO_RCVBUF), "SO_RCVBUF supported");
            checkThrows(UnsupportedOperationException.class,
                        () -> nc.setOption(StandardSocketOptions.IP_MULTICAST_LOOP, true),
                        "unsupported option -> UnsupportedOperationException");
        }
    }

    static void testContractThroughServerSocketChannel() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            NetworkChannel nc = ssc;
            check(nc.getLocalAddress() == null, "unbound ServerSocketChannel: local address null");
            check(nc.bind(new InetSocketAddress(LO, 0)) == nc, "bind(addr) returns this");
            InetSocketAddress local = (InetSocketAddress) nc.getLocalAddress();
            check(local.getAddress().equals(LO), "bound to the loopback address");
            check(local.getPort() > 0, "ephemeral port on loopback");
            checkThrows(java.nio.channels.AlreadyBoundException.class,
                        () -> nc.bind(new InetSocketAddress(LO, 0)),
                        "second bind -> AlreadyBoundException");
            nc.setOption(StandardSocketOptions.SO_RCVBUF, 64 * 1024);
            check(nc.getOption(StandardSocketOptions.SO_RCVBUF) >= 64 * 1024,
                  "SO_RCVBUF readback >= requested");
            check(nc.supportedOptions().contains(StandardSocketOptions.SO_RCVBUF),
                  "SO_RCVBUF supported");
            checkThrows(UnsupportedOperationException.class,
                        () -> nc.setOption(StandardSocketOptions.IP_MULTICAST_TTL, 1),
                        "unsupported multicast option on stream listener -> UnsupportedOperationException");
        }
    }

    static void testContractThroughDatagramChannel() throws Exception {
        try (DatagramChannel dc = DatagramChannel.open()) {
            NetworkChannel nc = dc;
            check(nc.getLocalAddress() == null, "unbound DatagramChannel: local address null");
            check(dc.getRemoteAddress() == null, "unconnected: remote address null");
            nc.bind(new InetSocketAddress(LO, 0));
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
            checkThrows(ClosedChannelException.class, () -> nc.bind(null),
                        "bind after close -> ClosedChannelException");
            checkThrows(ClosedChannelException.class,
                        () -> nc.getOption(StandardSocketOptions.SO_SNDBUF),
                        "getOption after close -> ClosedChannelException");
            checkThrows(ClosedChannelException.class,
                        () -> nc.setOption(StandardSocketOptions.SO_SNDBUF, 1024),
                        "setOption after close -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class, () -> nc.getLocalAddress(),
                        "getLocalAddress after close -> ClosedChannelException (per javadoc)");
            check(!nc.supportedOptions().isEmpty(), "supportedOptions still queryable after close");
        }
    }

    public static void main(String[] args) throws Exception {
        testNetworkChannel();
        testContractThroughSocketChannel();
        testContractThroughServerSocketChannel();
        testContractThroughDatagramChannel();
        testClosedChannelRules();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

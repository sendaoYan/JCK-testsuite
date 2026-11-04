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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

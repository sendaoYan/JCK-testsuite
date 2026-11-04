/*
 * @test
 * @summary java.nio.channels.spi.SelectorProvider conformance: white-box structure + black-box behavior.
 * @run main SelectorProviderTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.Selector;
import java.nio.channels.DatagramChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class SelectorProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.spi.SelectorProvider");
        check(Modifier.isAbstract(c.getModifiers()), "SelectorProvider is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"provider", "openDatagramChannel", "openPipe",
                "openSelector", "openServerSocketChannel", "openSocketChannel", "inheritedChannel"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        SelectorProvider sp = SelectorProvider.provider();
        check(sp != null, "provider() returns the system provider");
        // repeated calls return the same instance (per-invocation cached)
        check(SelectorProvider.provider() == sp, "provider() is stable");

        // factory methods produce working channels
        try (SocketChannel sc = sp.openSocketChannel()) {
            check(sc.isOpen(), "socket channel from provider");
            check(sc.provider() == sp, "channel wired to provider");
        }
        try (ServerSocketChannel ssc = sp.openServerSocketChannel()) {
            check(ssc.isOpen(), "server channel from provider");
            check(ssc.provider() == sp, "server wired to provider");
        }
        try (DatagramChannel dc = sp.openDatagramChannel()) {
            check(dc.isOpen(), "datagram channel from provider");
            check(dc.provider() == sp, "datagram wired to provider");
        }
        try (Selector sel = sp.openSelector()) {
            check(sel.isOpen(), "selector from provider");
            check(sel.provider() == sp, "selector wired to provider");
        }
        Pipe p = sp.openPipe();
        try {
            check(p.sink().isOpen() && p.source().isOpen(), "pipe from provider");
            check(p.sink().provider() == sp, "pipe sink wired to provider");
        } finally {
            p.sink().close();
            p.source().close();
        }
        // ProtocolFamily overloads (JDK 15+)
        try (SocketChannel sc = sp.openSocketChannel(java.net.StandardProtocolFamily.UNIX)) {
            check(sc.isOpen(), "unix socket channel from provider");
        }
        try (ServerSocketChannel ssc = sp.openServerSocketChannel(java.net.StandardProtocolFamily.INET)) {
            check(ssc.isOpen(), "INET server channel from provider");
        }
        try (DatagramChannel dc = sp.openDatagramChannel(java.net.StandardProtocolFamily.INET)) {
            check(dc.isOpen(), "INET datagram channel from provider");
        }
        // inheritedChannel: null when no channel was inherited
        check(sp.inheritedChannel() == null, "inheritedChannel null in ordinary JVM");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

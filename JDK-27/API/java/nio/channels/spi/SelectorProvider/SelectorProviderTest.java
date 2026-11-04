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
        testProviderSurfaceContract();
        testOpenContract();
        testProtocolFamilyOverloadsContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static boolean spHasMethod(String name, Class<?>... params) {
        try {
            SelectorProvider.class.getMethod(name, params);
            return true;
        } catch (NoSuchMethodException e) {
            return false;
        }
    }

    static void testProviderSurfaceContract() {
        check(java.lang.reflect.Modifier.isAbstract(SelectorProvider.class.getModifiers()),
              "class is abstract");
        SelectorProvider p = SelectorProvider.provider();
        check(p != null, "provider() non-null");
        check(SelectorProvider.provider() == p, "provider() returns the same instance");
        check(SelectorProvider.provider() == p, "provider() cached across calls");
        check(spHasMethod("openSelector"), "has openSelector");
        check(spHasMethod("openPipe"), "has openPipe");
        check(spHasMethod("openServerSocketChannel"), "has openServerSocketChannel");
        check(spHasMethod("openSocketChannel"), "has openSocketChannel");
        check(spHasMethod("openDatagramChannel"), "has openDatagramChannel");
        check(spHasMethod("openDatagramChannel", java.net.ProtocolFamily.class),
              "has openDatagramChannel(ProtocolFamily)");
        check(spHasMethod("inheritedChannel"), "has inheritedChannel");
    }

    static void testOpenContract() throws Exception {
        java.nio.channels.Selector a = SelectorProvider.provider().openSelector();
        java.nio.channels.Selector b = SelectorProvider.provider().openSelector();
        check(a != b, "two selectors are distinct");
        check(a.isOpen() && b.isOpen(), "both selectors open");
        a.close();
        b.close();
        check(!a.isOpen() && !b.isOpen(), "both selectors closed");
        java.nio.channels.Pipe p1 = SelectorProvider.provider().openPipe();
        java.nio.channels.Pipe p2 = SelectorProvider.provider().openPipe();
        check(p1 != p2, "two pipes are distinct");
        check(p1.sink() != null && p1.source() != null, "pipe endpoints non-null");
        p1.sink().close(); p1.source().close();
        p2.sink().close(); p2.source().close();
        java.nio.channels.ServerSocketChannel ssc =
                SelectorProvider.provider().openServerSocketChannel();
        check(ssc.isOpen(), "server socket channel open");
        ssc.close();
        check(!ssc.isOpen(), "server socket channel closed");
        java.nio.channels.SocketChannel sc = SelectorProvider.provider().openSocketChannel();
        check(sc.isOpen(), "socket channel open");
        sc.close();
        java.nio.channels.DatagramChannel dc = SelectorProvider.provider().openDatagramChannel();
        check(dc.isOpen(), "datagram channel open");
        dc.close();
        boolean npe = false;
        try {
            SelectorProvider.provider().openDatagramChannel(null);
        } catch (NullPointerException e) {
            npe = true;
        }
        check(npe, "openDatagramChannel(null) -> NullPointerException");
        java.nio.channels.DatagramChannel inet =
                SelectorProvider.provider().openDatagramChannel(java.net.StandardProtocolFamily.INET);
        check(inet.isOpen(), "openDatagramChannel(INET) opens");
        inet.close();
        java.nio.channels.Channel inherited = SelectorProvider.provider().inheritedChannel();
        check(inherited == null || inherited.isOpen(),
              "inheritedChannel is null or an open channel (environment-dependent)");
    }

    static void testProtocolFamilyOverloadsContract() throws Exception {
        // JDK-16+: stream channels can be opened for a specific protocol family
        check(spHasMethod("openSocketChannel", java.net.ProtocolFamily.class),
              "openSocketChannel(ProtocolFamily) present");
        check(spHasMethod("openServerSocketChannel", java.net.ProtocolFamily.class),
              "openServerSocketChannel(ProtocolFamily) present");
        check(java.net.StandardProtocolFamily.UNIX != null, "UNIX protocol family available");
        boolean refused = false;
        try {
            SelectorProvider.provider().openDatagramChannel(java.net.StandardProtocolFamily.UNIX);
        } catch (UnsupportedOperationException e) {
            refused = true;
        }
        check(refused, "openDatagramChannel(UNIX) -> UnsupportedOperationException");
        // full UNIX-domain round trip through the provider
        java.nio.file.Path dir = java.nio.file.Files.createTempDirectory("jck-sp-unix");
        java.net.UnixDomainSocketAddress addr =
                java.net.UnixDomainSocketAddress.of(dir.resolve("s.sock").toString());
        java.nio.channels.ServerSocketChannel ssc = SelectorProvider.provider()
                .openServerSocketChannel(java.net.StandardProtocolFamily.UNIX);
        java.nio.channels.SocketChannel cli = SelectorProvider.provider()
                .openSocketChannel(java.net.StandardProtocolFamily.UNIX);
        try {
            ssc.bind(addr);
            check(ssc.isOpen(), "unix server channel open");
            check(cli.isOpen(), "unix client channel open");
            check(cli.connect(ssc.getLocalAddress()), "unix connect succeeds");
            java.nio.channels.SocketChannel srv = ssc.accept();
            check(srv != null, "unix accept returns the peer");
            check(cli.write(java.nio.ByteBuffer.wrap(new byte[] {7})) == 1, "unix write one byte");
            java.nio.ByteBuffer buf = java.nio.ByteBuffer.allocate(1);
            while (srv.read(buf) == 0) { /* blocking read until one byte */ }
            check(buf.get(0) == 7, "unix read round-trips the byte");
            srv.close();
        } finally {
            cli.close();
            ssc.close();
            java.nio.file.Files.deleteIfExists(dir.resolve("s.sock"));
            java.nio.file.Files.deleteIfExists(dir);
        }
    }

}

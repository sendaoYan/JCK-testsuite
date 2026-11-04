/*
 * @test
 * @summary java.nio.channels.spi.SelectorProvider conformance: provider/open*.
 * @run main SelectorProviderTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.DatagramChannel;
import java.nio.channels.Pipe;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;

public class SelectorProviderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSelectorProvider() throws Exception {
        check(Modifier.isAbstract(SelectorProvider.class.getModifiers()), "abstract");
        SelectorProvider provider = SelectorProvider.provider();
        check(provider != null, "provider() non-null");

        AbstractSelector sel = provider.openSelector();
        check(sel.isOpen(), "openSelector");
        sel.close();

        ServerSocketChannel ssc = provider.openServerSocketChannel();
        check(ssc.isOpen(), "openServerSocketChannel");
        ssc.close();

        SocketChannel sc = provider.openSocketChannel();
        check(sc.isOpen(), "openSocketChannel");
        sc.close();

        DatagramChannel dc = provider.openDatagramChannel();
        check(dc.isOpen(), "openDatagramChannel");
        dc.close();

        Pipe pipe = provider.openPipe();
        check(pipe.sink() != null && pipe.source() != null, "openPipe");
        pipe.sink().close();
        pipe.source().close();
    }

    public static void main(String[] args) throws Exception {
        testSelectorProvider();
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

    static void testProtocolFamilyOverloadsContract() {
        // JDK-8: the ProtocolFamily overloads for stream channels do not exist
        check(!spHasMethod("openSocketChannel", java.net.ProtocolFamily.class),
              "JDK-8 has no openSocketChannel(ProtocolFamily)");
        check(!spHasMethod("openServerSocketChannel", java.net.ProtocolFamily.class),
              "JDK-8 has no openServerSocketChannel(ProtocolFamily)");
        boolean unixField = false;
        try {
            java.net.StandardProtocolFamily.class.getField("UNIX");
            unixField = true;
        } catch (NoSuchFieldException e) {
            // expected on JDK-8
        }
        check(!unixField, "JDK-8 StandardProtocolFamily has no UNIX constant");
        check(java.net.StandardProtocolFamily.INET != null, "INET constant available");
        check(java.net.StandardProtocolFamily.INET6 != null, "INET6 constant available");
    }

}

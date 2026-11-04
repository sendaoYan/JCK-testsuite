/*
 * @test
 * @summary java.nio.channels.SelectableChannel conformance: white-box structure + black-box behavior.
 * @run main SelectableChannelTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

public class SelectableChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.SelectableChannel");
        check(Modifier.isAbstract(c.getModifiers()), "SelectableChannel is abstract");
        check(java.nio.channels.spi.AbstractInterruptibleChannel.class.isAssignableFrom(c),
              "extends AbstractInterruptibleChannel");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "is a Channel");
        for (String n : new String[] {"provider", "validOps", "isRegistered", "keyFor",
                "register", "configureBlocking", "isBlocking", "blockingLock"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (ServerSocketChannel ch = ServerSocketChannel.open(); Selector sel = Selector.open()) {
            SelectableChannel sc = ch; // exercise through the abstract type
            check(sc.provider() != null, "provider non-null");
            check(sc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            check(!sc.isRegistered(), "not registered initially");
            check(sc.keyFor(sel) == null, "keyFor null before register");
            check(sc.isBlocking(), "default blocking mode");

            Object lock = sc.blockingLock();
            check(lock != null, "blockingLock non-null");

            // a channel in blocking mode cannot be registered with a selector
            boolean threw = false;
            try { sc.register(sel, SelectionKey.OP_ACCEPT); }
            catch (java.nio.channels.IllegalBlockingModeException e) { threw = true; }
            check(threw, "register while blocking -> IllegalBlockingModeException");

            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_ACCEPT);
            check(sc.isRegistered(), "registered");
            check(sc.keyFor(sel) == key, "keyFor returns the registration key");
            threw = false;
            try { sc.configureBlocking(true); }
            catch (java.nio.channels.IllegalBlockingModeException e) { threw = true; }
            check(threw, "configureBlocking while registered -> IllegalBlockingModeException");

            // after cancel + select, mode change allowed again
            key.cancel();
            sel.selectNow();
            sc.configureBlocking(false);
            check(!sc.isBlocking(), "non-blocking after cancel");
            sc.configureBlocking(true);
            check(sc.isBlocking(), "blocking restored");
            sc.configureBlocking(false);

            // register after cancel creates a fresh key for the same channel
            SelectionKey k2 = sc.register(sel, SelectionKey.OP_ACCEPT);
            check(sc.keyFor(sel) == k2, "re-registration returns current key");
            check(sc.keyFor(sel).interestOps() == SelectionKey.OP_ACCEPT, "interest ops");
            check(sel.keys().size() == 1, "single key for channel");

            // validOps enforced: OP_READ invalid for a server socket channel
            threw = false;
            try { sc.register(sel, SelectionKey.OP_READ); }
            catch (IllegalArgumentException e) { threw = true; }
            check(threw, "invalid ops -> IllegalArgumentException");

            // same constraint for a client socket channel
            try (SocketChannel s = SocketChannel.open(); Selector sel2 = Selector.open()) {
                threw = false;
                try { s.register(sel2, SelectionKey.OP_CONNECT); }
                catch (java.nio.channels.IllegalBlockingModeException e) { threw = true; }
                check(threw, "socket register while blocking -> IllegalBlockingModeException");
                s.configureBlocking(false);
                check(s.register(sel2, SelectionKey.OP_CONNECT) != null, "non-blocking register ok");
                check(s.keyFor(sel2) != null, "keyFor after register");
            }
        }
    }

    // ===================== A6 deepening: validOps matrix / blocking-lock / closed-channel rules =====================

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

    static void testValidOpsMatrix() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            check(ssc.validOps() == SelectionKey.OP_ACCEPT,
                  "ServerSocketChannel.validOps == OP_ACCEPT");
            check(sc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT),
                  "SocketChannel.validOps == READ|WRITE|CONNECT");
            check(dc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE),
                  "DatagramChannel.validOps == READ|WRITE");
            check((ssc.validOps() & SelectionKey.OP_READ) == 0, "SSC cannot read");
            check((dc.validOps() & SelectionKey.OP_CONNECT) == 0, "DC cannot connect-select");
        }
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try {
            check(pipe.source().validOps() == SelectionKey.OP_READ, "Pipe.source.validOps == OP_READ");
            check(pipe.sink().validOps() == SelectionKey.OP_WRITE, "Pipe.sink.validOps == OP_WRITE");
            check((pipe.source().validOps() & SelectionKey.OP_WRITE) == 0, "source cannot write");
            check((pipe.sink().validOps() & SelectionKey.OP_READ) == 0, "sink cannot read");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testRegisterWhileBlocking() throws Exception {
        // Pipe itself is not AutoCloseable (only its sink/source ends are)
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open();
             Selector sel = Selector.open()) {
            check(dc.isBlocking(), "DC defaults to blocking");
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> dc.register(sel, SelectionKey.OP_READ),
                        "DC register while blocking -> IllegalBlockingModeException");
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> pipe.source().register(sel, SelectionKey.OP_READ),
                        "pipe source register while blocking -> IllegalBlockingModeException");
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> pipe.sink().register(sel, SelectionKey.OP_WRITE),
                        "pipe sink register while blocking -> IllegalBlockingModeException");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testConfigureBlockingReturnsThis() throws Exception {
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            check(dc.configureBlocking(false) == dc, "DC configureBlocking returns this");
            check(!dc.isBlocking(), "DC now non-blocking");
            check(dc.configureBlocking(true) == dc, "DC configureBlocking(true) returns this");
        }
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try {
            check(pipe.source().configureBlocking(false) == pipe.source(),
                  "source configureBlocking returns this");
            check(pipe.sink().configureBlocking(false) == pipe.sink(),
                  "sink configureBlocking returns this");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testBlockingLockContract() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            Object lock = sc.blockingLock();
            check(lock != null, "blockingLock non-null");
            check(sc.blockingLock() == lock, "blockingLock stable identity");
            synchronized (lock) {
                check(sc.configureBlocking(false) == sc,
                      "owner of the lock may change the mode (same thread)");
            }
            check(sc.blockingLock() == lock, "lock identity unchanged by mode flips");
        }
    }

    static void testRegisterInvalidOps() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open();
             Selector sel = Selector.open()) {
            ssc.configureBlocking(false);
            sc.configureBlocking(false);
            dc.configureBlocking(false);
            checkThrows(IllegalArgumentException.class,
                        () -> ssc.register(sel, SelectionKey.OP_READ),
                        "SSC register OP_READ -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class,
                        () -> ssc.register(sel, SelectionKey.OP_READ | SelectionKey.OP_ACCEPT),
                        "SSC register mixed ops -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class,
                        () -> sc.register(sel, SelectionKey.OP_ACCEPT),
                        "SC register OP_ACCEPT -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class,
                        () -> dc.register(sel, SelectionKey.OP_CONNECT),
                        "DC register OP_CONNECT -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class,
                        () -> sc.register(sel, 1 << 5),
                        "register unknown bit 5 -> IllegalArgumentException");
            // ops 0 (no interest) is legal on every channel type
            check(ssc.register(sel, 0).interestOps() == 0, "SSC register with ops 0 legal");
            check(sc.register(sel, 0).interestOps() == 0, "SC register with ops 0 legal");
            check(dc.register(sel, 0).interestOps() == 0, "DC register with ops 0 legal");
        }
    }

    static void testClosedChannelRules() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            Selector sel = Selector.open();
            sc.close();
            checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sc.register(sel, SelectionKey.OP_READ),
                        "register after close -> ClosedChannelException");
            checkThrows(java.nio.channels.ClosedChannelException.class,
                        () -> sc.configureBlocking(false),
                        "configureBlocking after close -> ClosedChannelException");
            sel.close();
        }
    }

    static void testProviderContract() throws Exception {
        java.nio.channels.spi.SelectorProvider def =
                java.nio.channels.spi.SelectorProvider.provider();
        try (java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open()) {
            check(dc.provider() == def, "DC provider is the default provider");
        }
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try {
            check(pipe.source().provider() == def, "pipe source provider is default");
            check(pipe.sink().provider() == def, "pipe sink provider is default");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testKeyForSemantics() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            try (Selector s1 = Selector.open(); Selector s2 = Selector.open()) {
                check(!sc.isRegistered(), "not registered initially");
                check(sc.keyFor(s1) == null && sc.keyFor(s2) == null, "keyFor null before register");
                SelectionKey k1 = sc.register(s1, SelectionKey.OP_READ);
                check(sc.isRegistered(), "isRegistered after register");
                check(sc.keyFor(s1) == k1, "keyFor(s1) returns the key");
                check(sc.keyFor(s2) == null, "keyFor(s2) still null");
                check(sc.keyFor(null) == null, "keyFor(null) is null");
                SelectionKey k2 = sc.register(s2, SelectionKey.OP_READ);
                check(k1 != k2, "second selector gets a distinct key");
                check(sc.keyFor(s1) == k1 && sc.keyFor(s2) == k2, "both keys tracked");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testValidOpsMatrix();
        testRegisterWhileBlocking();
        testConfigureBlockingReturnsThis();
        testBlockingLockContract();
        testRegisterInvalidOps();
        testClosedChannelRules();
        testProviderContract();
        testKeyForSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

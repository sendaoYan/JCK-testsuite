/*
 * @test
 * @summary java.nio.channels.SelectableChannel conformance: blocking/register.
 * @run main SelectableChannelTest
 */

import java.nio.channels.ClosedChannelException;
import java.nio.channels.DatagramChannel;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SelectableChannelTest {
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

    static void testSelectableChannel() throws Exception {
        SelectableChannel sc = SocketChannel.open();
        check(sc.isOpen(), "isOpen");
        check(sc.isBlocking(), "initially blocking");
        sc.configureBlocking(false);
        check(!sc.isBlocking(), "non-blocking");
        check(sc.blockingLock() != null, "blockingLock");
        check(sc.validOps() != 0, "validOps");
        check(sc.provider() != null, "provider");

        Selector sel = Selector.open();
        SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
        check(sc.isRegistered(), "isRegistered");
        check(key.isValid(), "key valid");
        check(key.interestOps() == SelectionKey.OP_READ, "interestOps");
        sc.close();
        sel.close();
    }

    // ===================== A6 deepening: validOps matrix / blocking mode / register rules =====================

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.SelectableChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SelectableChannel is abstract");
        check(java.nio.channels.spi.AbstractInterruptibleChannel.class.isAssignableFrom(c),
              "extends AbstractInterruptibleChannel");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "is a Channel");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"provider", "validOps", "isRegistered", "keyFor",
                "register", "configureBlocking", "isBlocking", "blockingLock"})
            check(names.contains(n), "declares method " + n);
    }

    static void testValidOpsMatrix() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             DatagramChannel dc = DatagramChannel.open()) {
            check(ssc.validOps() == SelectionKey.OP_ACCEPT,
                  "ServerSocketChannel.validOps == OP_ACCEPT");
            check(sc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT),
                  "SocketChannel.validOps == READ|WRITE|CONNECT");
            check(dc.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE),
                  "DatagramChannel.validOps == READ|WRITE");
            check((ssc.validOps() & SelectionKey.OP_READ) == 0, "SSC cannot read");
            check((dc.validOps() & SelectionKey.OP_CONNECT) == 0, "DC cannot connect-select");
        }
        Pipe pipe = Pipe.open();
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
        // every selectable channel refuses registration while in blocking mode
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             DatagramChannel dc = DatagramChannel.open();
             Selector sel = Selector.open()) {
            check(ssc.isBlocking() && sc.isBlocking() && dc.isBlocking(),
                  "all channels default to blocking mode");
            checkThrows(IllegalBlockingModeException.class,
                        () -> ssc.register(sel, SelectionKey.OP_ACCEPT),
                        "SSC register while blocking -> IllegalBlockingModeException");
            checkThrows(IllegalBlockingModeException.class,
                        () -> sc.register(sel, SelectionKey.OP_READ),
                        "SC register while blocking -> IllegalBlockingModeException");
            checkThrows(IllegalBlockingModeException.class,
                        () -> dc.register(sel, SelectionKey.OP_READ),
                        "DC register while blocking -> IllegalBlockingModeException");
        }
        Pipe pipe = Pipe.open();
        try (Selector sel = Selector.open()) {
            checkThrows(IllegalBlockingModeException.class,
                        () -> pipe.source().register(sel, SelectionKey.OP_READ),
                        "pipe source register while blocking -> IllegalBlockingModeException");
            checkThrows(IllegalBlockingModeException.class,
                        () -> pipe.sink().register(sel, SelectionKey.OP_WRITE),
                        "pipe sink register while blocking -> IllegalBlockingModeException");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testConfigureBlockingReturnsThis() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             DatagramChannel dc = DatagramChannel.open()) {
            check(ssc.configureBlocking(false) == ssc, "SSC configureBlocking returns this");
            check(!ssc.isBlocking(), "SSC now non-blocking");
            check(ssc.configureBlocking(true) == ssc, "SSC configureBlocking(true) returns this");
            check(sc.configureBlocking(false) == sc, "SC configureBlocking returns this");
            check(dc.configureBlocking(false) == dc, "DC configureBlocking returns this");
        }
        Pipe pipe = Pipe.open();
        try {
            check(pipe.source().configureBlocking(false) == pipe.source(),
                  "source configureBlocking returns this");
            check(pipe.sink().configureBlocking(false) == pipe.sink(),
                  "sink configureBlocking returns this");
            check(!pipe.source().isBlocking() && !pipe.sink().isBlocking(),
                  "pipe ends non-blocking");
        } finally {
            pipe.source().close();
            pipe.sink().close();
        }
    }

    static void testRegisteredBlockingConflict() throws Exception {
        try (SocketChannel sc = SocketChannel.open(); Selector sel = Selector.open()) {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            checkThrows(IllegalBlockingModeException.class, () -> sc.configureBlocking(true),
                        "configureBlocking(true) while registered -> IllegalBlockingModeException");
            check(!sc.isBlocking(), "mode unchanged after rejected switch");
            // cancelling and running a select frees the channel again
            key.cancel();
            sel.selectNow();
            check(sc.configureBlocking(true) == sc, "mode change allowed after cancel+select");
            check(sc.isBlocking(), "blocking restored");
            check(sc.keyFor(sel) == null, "no key left for the selector");
        }
    }

    static void testBlockingLockContract() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
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
             DatagramChannel dc = DatagramChannel.open();
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
            checkThrows(ClosedChannelException.class,
                        () -> sc.register(sel, SelectionKey.OP_READ),
                        "register after close -> ClosedChannelException");
            checkThrows(ClosedChannelException.class, () -> sc.configureBlocking(false),
                        "configureBlocking after close -> ClosedChannelException");
            sel.close();
        }
    }

    static void testProviderContract() throws Exception {
        java.nio.channels.spi.SelectorProvider def =
                java.nio.channels.spi.SelectorProvider.provider();
        try (ServerSocketChannel ssc = ServerSocketChannel.open();
             SocketChannel sc = SocketChannel.open();
             DatagramChannel dc = DatagramChannel.open()) {
            check(ssc.provider() == def, "SSC provider is the default provider");
            check(sc.provider() == def, "SC provider is the default provider");
            check(dc.provider() == def, "DC provider is the default provider");
        }
        Pipe pipe = Pipe.open();
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
        testSelectableChannel();
        testWhiteBoxStructure();
        testValidOpsMatrix();
        testRegisterWhileBlocking();
        testConfigureBlockingReturnsThis();
        testRegisteredBlockingConflict();
        testBlockingLockContract();
        testRegisterInvalidOps();
        testClosedChannelRules();
        testProviderContract();
        testKeyForSemantics();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

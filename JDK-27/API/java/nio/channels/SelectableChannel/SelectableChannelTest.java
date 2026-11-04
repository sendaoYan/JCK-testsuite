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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

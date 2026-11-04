/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelectableChannel conformance: white-box structure + black-box behavior.
 * @run main AbstractSelectableChannelTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectableChannel;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class AbstractSelectableChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal concrete subclass: one valid op bit, no real transport. */
    static final class StubSelectable extends AbstractSelectableChannel {
        boolean blocking = true;
        boolean closed;
        StubSelectable() {
            super(SelectorProvider.provider());
        }
        protected void implCloseSelectableChannel() { closed = true; }
        protected void implConfigureBlocking(boolean block) { blocking = block; }
        public int validOps() { return SelectionKey.OP_READ; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.spi.AbstractSelectableChannel");
        check(Modifier.isAbstract(c.getModifiers()), "AbstractSelectableChannel is abstract");
        check(SelectableChannel.class.isAssignableFrom(c), "extends SelectableChannel");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"provider", "isRegistered", "keyFor", "register",
                "isBlocking", "blockingLock", "configureBlocking"})
            check(Modifier.isFinal(java.util.Arrays.stream(c.getMethods())
                  .filter(m -> m.getName().equals(n)).findFirst().orElseThrow().getModifiers()),
                  n + " is final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StubSelectable ch = new StubSelectable();
        check(ch.isOpen(), "stub open");
        check(ch.provider() == SelectorProvider.provider(), "provider wired");
        check(ch.validOps() == SelectionKey.OP_READ, "validOps from subclass");
        check(ch.isBlocking(), "starts blocking");
        check(ch.blockingLock() != null, "blockingLock non-null");

        // configureBlocking delegates to implConfigureBlocking
        ch.configureBlocking(false);
        check(!ch.isBlocking() && !ch.blocking, "implConfigureBlocking(false) ran");
        ch.configureBlocking(true);
        check(ch.isBlocking() && ch.blocking, "implConfigureBlocking(true) ran");

        // a real selector only accepts channels from its own provider implementation,
        // so a foreign stub is rejected with IllegalSelectorException
        try (Selector sel = Selector.open()) {
            ch.configureBlocking(false); // registration requires non-blocking mode
            boolean rejected = false;
            try { ch.register(sel, SelectionKey.OP_READ); }
            catch (java.nio.channels.IllegalSelectorException e) { rejected = true; }
            check(rejected, "foreign channel registration -> IllegalSelectorException");
        }

        // registration lifecycle through the abstract type, using a real channel
        try (Selector sel = Selector.open();
             java.nio.channels.SocketChannel real = java.nio.channels.SocketChannel.open()) {
            AbstractSelectableChannel ch2 = real;
            ch2.configureBlocking(false);
            SelectionKey key = ch2.register(sel, SelectionKey.OP_READ);
            check(ch2.isRegistered(), "registered");
            check(ch2.keyFor(sel) == key, "keyFor returns registration");
            check(key.channel() == real, "key bound to channel");
            check(sel.keys().size() == 1, "selector tracks one key");

            // cannot change blocking while registered
            boolean threw = false;
            try { ch2.configureBlocking(true); }
            catch (java.nio.channels.IllegalBlockingModeException e) { threw = true; }
            check(threw, "configureBlocking while registered -> IllegalBlockingModeException");

            // cancel then select releases registration
            key.cancel();
            sel.selectNow();
            check(!ch2.isRegistered(), "registration gone after cancel+select");
            check(sel.keys().isEmpty(), "selector key set empty again");

            // close on the abstract type: register afterwards fails
            ch2.close();
            check(!ch2.isOpen(), "closed");
            boolean closedReg = false;
            try { ch2.register(sel, SelectionKey.OP_READ); }
            catch (java.nio.channels.ClosedChannelException e) { closedReg = true; }
            check(closedReg, "register after close -> ClosedChannelException");
        }

        // close delegates to implCloseSelectableChannel on the stub
        ch.close();
        check(ch.closed, "implCloseSelectableChannel ran");
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

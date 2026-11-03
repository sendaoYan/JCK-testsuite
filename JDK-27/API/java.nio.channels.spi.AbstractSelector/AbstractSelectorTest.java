/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelector conformance: white-box structure + black-box behavior.
 * @run main AbstractSelectorTest
 */

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelector;
import java.nio.channels.spi.SelectorProvider;
import java.util.Set;

public class AbstractSelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal selector built on the abstract base, mirroring JDK patterns. */
    static final class StubSelector extends AbstractSelector {
        boolean closed;
        StubSelector() {
            super(SelectorProvider.provider());
        }
        protected SelectionKey register(java.nio.channels.spi.AbstractSelectableChannel ch,
                                        int ops, Object att) {
            return new StubKey(ch, this, ops, att);
        }
        protected void implCloseSelector() { closed = true; }
        public Set<SelectionKey> keys() { return Set.of(); }
        public Set<SelectionKey> selectedKeys() { return Set.of(); }
        public int selectNow() { return 0; }
        public int select(long timeout) { return 0; }
        public int select() { return 0; }
        public Selector wakeup() { return this; }
    }

    static final class StubKey extends SelectionKey {
        final java.nio.channels.SelectableChannel ch;
        final Selector sel;
        final int ops;
        StubKey(java.nio.channels.SelectableChannel ch, Selector sel, int ops, Object att) {
            this.ch = ch; this.sel = sel; this.ops = ops; attach(att);
        }
        public java.nio.channels.SelectableChannel channel() { return ch; }
        public Selector selector() { return sel; }
        public boolean isValid() { return true; }
        public void cancel() { }
        public int interestOps() { return ops; }
        public SelectionKey interestOps(int o) { return this; }
        public int readyOps() { return 0; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.spi.AbstractSelector");
        check(Modifier.isAbstract(c.getModifiers()), "AbstractSelector is abstract");
        check(Selector.class.isAssignableFrom(c), "extends Selector");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(Modifier.isFinal(c.getMethod("close").getModifiers()), "close final");
        check(Modifier.isFinal(c.getMethod("isOpen").getModifiers()), "isOpen final");
        check(Modifier.isFinal(c.getMethod("provider").getModifiers()), "provider final");
        check(Modifier.isProtected(c.getDeclaredMethod("register",
                java.nio.channels.spi.AbstractSelectableChannel.class, int.class, Object.class)
                .getModifiers()), "register protected");
        check(Modifier.isProtected(c.getDeclaredMethod("implCloseSelector").getModifiers()),
              "implCloseSelector protected");
        check(Modifier.isProtected(c.getDeclaredMethod("cancelledKeys").getModifiers()),
              "cancelledKeys protected");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        StubSelector sel = new StubSelector();
        check(sel.isOpen(), "stub open");
        check(sel.provider() == SelectorProvider.provider(), "provider wired");
        check(sel.keys().isEmpty(), "no keys");
        check(sel.selectNow() == 0, "selectNow 0");

        // register produces a key bound to this selector
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try {
            pipe.source().configureBlocking(false);
            SelectionKey k = sel.register((java.nio.channels.spi.AbstractSelectableChannel)
                    (Object) pipe.source(), java.nio.channels.SelectionKey.OP_READ, "att");
            check(k.selector() == sel, "key selector");
            check("att".equals(k.attachment()), "attachment passed through");

            // close delegates to implCloseSelector exactly once
            sel.close();
            check(sel.closed, "implCloseSelector ran");
            check(!sel.isOpen(), "closed");
            sel.close();
            check(sel.closed, "idempotent close");

            // JDK's real selector obeys the same contract
            try (Selector real = Selector.open()) {
                check(real instanceof AbstractSelector, "default selector extends AbstractSelector");
                check(((AbstractSelector) real).provider() == SelectorProvider.provider(),
                      "real provider wired");
            }
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

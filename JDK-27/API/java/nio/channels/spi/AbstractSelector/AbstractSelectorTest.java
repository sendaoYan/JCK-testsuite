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
        testStructureContract();
        testLifecycleContract();
        testRegisterDeregisterContract();
        testSelectSurfaceContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static final class ProbeSelector extends AbstractSelector {
        final java.util.List<java.nio.channels.SelectionKey> regs =
                new java.util.concurrent.CopyOnWriteArrayList<java.nio.channels.SelectionKey>();
        int registerCalls;
        int closeCalls;
        ProbeSelector(java.nio.channels.spi.SelectorProvider sp) { super(sp); }
        protected java.nio.channels.SelectionKey register(java.nio.channels.spi.AbstractSelectableChannel ch, int ops, Object att) {
            registerCalls++;
            final java.nio.channels.spi.AbstractSelectableChannel cc = ch;
            final ProbeSelector self = this;
            final int captured = ops;
            class K extends java.nio.channels.spi.AbstractSelectionKey {
                public java.nio.channels.SelectableChannel channel() { return cc; }
                public java.nio.channels.Selector selector() { return self; }
                public int interestOps() { return captured; }
                public java.nio.channels.SelectionKey interestOps(int o) { return this; }
                public int readyOps() { return 0; }
            }
            K k = new K();
            k.attach(att);
            regs.add(k);
            return k;
        }
        protected void implCloseSelector() {
            closeCalls++;
            for (java.nio.channels.SelectionKey k : regs)
                deregister((java.nio.channels.spi.AbstractSelectionKey) k);
        }
        public java.util.Set<java.nio.channels.SelectionKey> keys() {
            return new java.util.HashSet<java.nio.channels.SelectionKey>(regs);
        }
        public java.util.Set<java.nio.channels.SelectionKey> selectedKeys() {
            return new java.util.HashSet<java.nio.channels.SelectionKey>();
        }
        public int selectNow() { return 0; }
        public int select(long t) { return 0; }
        public int select() { return 0; }
        public java.nio.channels.Selector wakeup() { return this; }
    }

    static void testStructureContract() {
        check(java.lang.reflect.Modifier.isAbstract(AbstractSelector.class.getModifiers()),
              "class is abstract");
        check(java.nio.channels.Selector.class.isAssignableFrom(AbstractSelector.class),
              "extends Selector");
        boolean aselProviderCtor = false;
        for (java.lang.reflect.Constructor<?> c : AbstractSelector.class.getDeclaredConstructors()) {
            check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "no public constructor");
            if (java.lang.reflect.Modifier.isProtected(c.getModifiers())
                    && c.getParameterTypes().length == 1
                    && c.getParameterTypes()[0] == java.nio.channels.spi.SelectorProvider.class)
                aselProviderCtor = true;
        }
        check(aselProviderCtor, "protected constructor taking a SelectorProvider");
        try {
            check(AbstractSelector.class.isAssignableFrom(java.nio.channels.Selector.open().getClass()),
                  "Selector.open() yields an AbstractSelector");
        } catch (java.io.IOException e) {
            check(false, "Selector.open failed: " + e);
        }
    }

    static void testLifecycleContract() throws Exception {
        ProbeSelector sel = new ProbeSelector(java.nio.channels.spi.SelectorProvider.provider());
        check(sel.isOpen(), "fresh selector open");
        check(sel.provider() == java.nio.channels.spi.SelectorProvider.provider(),
              "provider() returns the constructor argument");
        sel.close();
        check(!sel.isOpen(), "closed after close()");
        check(sel.closeCalls == 1, "implCloseSelector ran exactly once");
        sel.close();
        check(!sel.isOpen() && sel.closeCalls == 1, "second close is a no-op");
        check(sel.provider() != null, "provider() still works after close");
        java.nio.channels.Selector a = java.nio.channels.Selector.open();
        java.nio.channels.Selector b = java.nio.channels.Selector.open();
        check(a != b, "two opens are distinct selectors");
        check(a.isOpen() && b.isOpen(), "both open");
        a.close();
        b.close();
        check(!a.isOpen() && !b.isOpen(), "both closed");
        check(a instanceof AbstractSelector && b instanceof AbstractSelector,
              "default selectors are AbstractSelectors");
    }

    static void testRegisterDeregisterContract() throws Exception {
        ProbeSelector sel = new ProbeSelector(java.nio.channels.spi.SelectorProvider.provider());
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        try {
            ch.configureBlocking(false);
            check(sel.keys().isEmpty(), "no keys initially");
            check(sel.selectedKeys().isEmpty(), "no selected keys initially");
            java.nio.channels.SelectionKey k =
                    ch.register(sel, java.nio.channels.SelectionKey.OP_READ, "z");
            check(sel.registerCalls == 1, "register hook invoked");
            check(sel.keys().size() == 1 && sel.keys().contains(k), "key visible through keys()");
            check("z".equals(k.attachment()), "attachment through the SPI path");
            java.util.Set<java.nio.channels.SelectionKey> sk = sel.selectedKeys();
            sk.add(k);
            check(sk.contains(k), "selectedKeys set is modifiable");
            sel.close();
            check(!k.isValid(), "deregister at close invalidates the key");
            check(!ch.isRegistered(), "channel no longer registered after selector close");
        } finally {
            ch.close();
            sel.close();
        }
    }

    static void testSelectSurfaceContract() throws Exception {
        ProbeSelector sel = new ProbeSelector(java.nio.channels.spi.SelectorProvider.provider());
        try {
            check(sel.selectNow() == 0, "selectNow returns 0 with no keys");
            check(sel.select() == 0, "select() returns 0 with no keys");
            check(sel.select(100) == 0, "select(timeout) returns 0 with no keys");
            check(sel.wakeup() == sel, "wakeup returns this selector");
            check(sel.selectNow() == 0, "selectNow after wakeup");
        } finally {
            sel.close();
        }
        // a real selector processes real keys
        java.nio.channels.Selector real = java.nio.channels.Selector.open();
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        try {
            ch.configureBlocking(false);
            java.nio.channels.SelectionKey k =
                    ch.register(real, java.nio.channels.SelectionKey.OP_READ, null);
            check(real.selectNow() >= 0, "selectNow runs against a registered key");
            k.interestOps(0);
            check(real.selectNow() == 0, "zero interest selects nothing");
            check(real.isOpen(), "real selector still open");
        } finally {
            ch.close();
            real.close();
        }
    }

}

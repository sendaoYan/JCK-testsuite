/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelectableChannel conformance: hierarchy.
 * @run main AbstractSelectableChannelTest
 */

import java.lang.reflect.Modifier;
import java.nio.channels.SelectableChannel;
import java.nio.channels.spi.AbstractSelectableChannel;

public class AbstractSelectableChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAbstractSelectableChannel() {
        check(Modifier.isAbstract(AbstractSelectableChannel.class.getModifiers()),
              "abstract");
        check(SelectableChannel.class.isAssignableFrom(AbstractSelectableChannel.class),
              "extends SelectableChannel");
    }

    public static void main(String[] args) throws Exception {
        testAbstractSelectableChannel();
        testStructureContract();
        testProviderAndBlockingContract();
        testRegisterContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static final class ProbeSelectable extends AbstractSelectableChannel {
        boolean hookCalled;
        boolean blocked;
        boolean closed;
        ProbeSelectable() { super(java.nio.channels.spi.SelectorProvider.provider()); }
        protected void implCloseSelectableChannel() { closed = true; }
        protected void implConfigureBlocking(boolean block) {
            hookCalled = true;
            blocked = block;
        }
        public int validOps() {
            return java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE;
        }
    }

    static final class ProbeSelector extends java.nio.channels.spi.AbstractSelector {
        final java.util.List<java.nio.channels.SelectionKey> regs =
                new java.util.concurrent.CopyOnWriteArrayList<java.nio.channels.SelectionKey>();
        int registerCalls;
        int closeCalls;
        ProbeSelector() { super(java.nio.channels.spi.SelectorProvider.provider()); }
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
        check(java.lang.reflect.Modifier.isAbstract(AbstractSelectableChannel.class.getModifiers()),
              "class is abstract");
        check(java.nio.channels.SelectableChannel.class.isAssignableFrom(AbstractSelectableChannel.class),
              "extends SelectableChannel");
        check(java.nio.channels.spi.AbstractInterruptibleChannel.class
                      .isAssignableFrom(AbstractSelectableChannel.class),
              "extends AbstractInterruptibleChannel");
        boolean ascProtectedCtor = false;
        for (java.lang.reflect.Constructor<?> c : AbstractSelectableChannel.class.getDeclaredConstructors()) {
            check(!java.lang.reflect.Modifier.isPublic(c.getModifiers()), "no public constructor");
            if (java.lang.reflect.Modifier.isProtected(c.getModifiers())) ascProtectedCtor = true;
        }
        check(ascProtectedCtor, "protected constructor present");
        check(AbstractSelectableChannel.class.isAssignableFrom(java.nio.channels.SocketChannel.class),
              "SocketChannel builds on AbstractSelectableChannel");
        check(AbstractSelectableChannel.class.isAssignableFrom(java.nio.channels.ServerSocketChannel.class),
              "ServerSocketChannel builds on AbstractSelectableChannel");
        check(AbstractSelectableChannel.class.isAssignableFrom(java.nio.channels.DatagramChannel.class),
              "DatagramChannel builds on AbstractSelectableChannel");
        check(AbstractSelectableChannel.class.isAssignableFrom(java.nio.channels.Pipe.SinkChannel.class),
              "Pipe.SinkChannel builds on AbstractSelectableChannel");
    }

    static void testProviderAndBlockingContract() throws Exception {
        ProbeSelectable p = new ProbeSelectable();
        check(p.provider() == java.nio.channels.spi.SelectorProvider.provider(),
              "provider() returns the constructor argument");
        check(p.isBlocking(), "channels start in blocking mode");
        check(p.blockingLock() != null, "blocking lock is non-null");
        check(p.blockingLock() == p.blockingLock(), "blocking lock identity is stable");
        p.configureBlocking(false);
        check(!p.isBlocking(), "switched to non-blocking");
        check(p.hookCalled, "implConfigureBlocking hook invoked");
        check(!p.blocked, "hook observed the requested mode");
        p.configureBlocking(true);
        check(p.isBlocking() && p.blocked, "back to blocking");
        java.nio.channels.SocketChannel sc = java.nio.channels.SocketChannel.open();
        try {
            check(sc.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "socket channel wired to the default provider");
            check(sc.validOps() == (java.nio.channels.SelectionKey.OP_READ
                    | java.nio.channels.SelectionKey.OP_WRITE
                    | java.nio.channels.SelectionKey.OP_CONNECT), "SocketChannel validOps");
            check(sc.blockingLock() != null && sc.blockingLock() == sc.blockingLock(),
                  "real channel lock identity stable");
        } finally {
            sc.close();
        }
        java.nio.channels.ServerSocketChannel ssc = java.nio.channels.ServerSocketChannel.open();
        try {
            check(ssc.validOps() == java.nio.channels.SelectionKey.OP_ACCEPT,
                  "ServerSocketChannel validOps is OP_ACCEPT only");
        } finally {
            ssc.close();
        }
        java.nio.channels.DatagramChannel dc = java.nio.channels.DatagramChannel.open();
        try {
            check(dc.validOps() == (java.nio.channels.SelectionKey.OP_READ
                    | java.nio.channels.SelectionKey.OP_WRITE), "DatagramChannel validOps");
        } finally {
            dc.close();
        }
        check(new ProbeSelectable().validOps() == (java.nio.channels.SelectionKey.OP_READ
                | java.nio.channels.SelectionKey.OP_WRITE), "stub validOps served by the subclass");
    }

    static void testRegisterContract() throws Exception {
        ProbeSelector sel = new ProbeSelector();
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        java.nio.channels.SocketChannel dead = java.nio.channels.SocketChannel.open();
        try {
            try {
                ch.register(sel, java.nio.channels.SelectionKey.OP_READ, null);
                check(false, "register while blocking must be refused");
            } catch (java.nio.channels.IllegalBlockingModeException e) {
                check(true, "register while blocking -> IllegalBlockingModeException");
            }
            ch.configureBlocking(false);
            java.nio.channels.SelectionKey k =
                    ch.register(sel, java.nio.channels.SelectionKey.OP_READ, "att");
            check(sel.registerCalls == 1, "selector register hook invoked once");
            check(k != null, "a key is returned");
            check(k.channel() == ch, "key bound to the channel");
            check(k.selector() == sel, "key bound to the selector");
            check("att".equals(k.attachment()), "attachment stored");
            check(ch.isRegistered(), "channel reports itself registered");
            check(ch.keyFor(sel) == k, "keyFor returns the same key");
            try {
                ch.register(sel, java.nio.channels.SelectionKey.OP_ACCEPT, null);
                check(false, "ops outside validOps must be refused");
            } catch (IllegalArgumentException e) {
                check(true, "ops outside validOps -> IllegalArgumentException");
            }
            java.nio.channels.SelectionKey k2 =
                    ch.register(sel, java.nio.channels.SelectionKey.OP_WRITE, "att2");
            check(k2 == k, "re-register on the same selector returns the same key");
            check(sel.registerCalls == 1, "no second register call for the same selector");
            check("att2".equals(k.attachment()), "attachment replaced on re-register");
            try {
                ch.configureBlocking(true);
                check(false, "configureBlocking while registered must be refused");
            } catch (java.nio.channels.IllegalBlockingModeException e) {
                check(true, "configureBlocking while registered -> IllegalBlockingModeException");
            }
            ProbeSelector sel2 = new ProbeSelector();
            java.nio.channels.SelectionKey kk =
                    ch.register(sel2, java.nio.channels.SelectionKey.OP_READ, null);
            check(kk != k, "a second selector gets its own key");
            check(ch.keyFor(sel) == k && ch.keyFor(sel2) == kk, "keyFor per selector");
            sel2.close();
            check(!kk.isValid(), "closing a selector invalidates its keys");
            check(sel2.closeCalls == 1, "implCloseSelector ran once");
            ch.close();
            check(!k.isValid(), "closing the channel invalidates its key");
            // registering a closed channel fails precisely
            dead.configureBlocking(false);
            dead.close();
            try {
                dead.register(sel, java.nio.channels.SelectionKey.OP_READ, null);
                check(false, "register on a closed channel must fail");
            } catch (java.nio.channels.ClosedChannelException e) {
                check(true, "register on closed channel -> ClosedChannelException");
            }
        } finally {
            ch.close();
            dead.close();
            sel.close();
        }
        // a stub channel flows through the real register() template method
        ProbeSelectable ps = new ProbeSelectable();
        ProbeSelector psel = new ProbeSelector();
        try {
            try {
                ps.register(psel, java.nio.channels.SelectionKey.OP_READ, "s");
                check(false, "stub register while blocking must be refused");
            } catch (java.nio.channels.IllegalBlockingModeException e) {
                check(true, "stub register while blocking -> IllegalBlockingModeException");
            }
            ps.configureBlocking(false);
            java.nio.channels.SelectionKey pk =
                    ps.register(psel, java.nio.channels.SelectionKey.OP_READ, "s");
            check(psel.registerCalls == 1, "stub channel wired through the real register path");
            check(pk.channel() == ps && pk.selector() == psel, "stub key wiring");
            check("s".equals(pk.attachment()), "stub key attachment");
            check(ps.isRegistered(), "stub channel registered");
        } finally {
            ps.close();
            psel.close();
        }
        check(true, "cleanup done");
    }

}

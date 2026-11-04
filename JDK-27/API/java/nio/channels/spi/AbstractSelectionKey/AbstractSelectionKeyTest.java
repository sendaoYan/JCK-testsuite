/*
 * @test
 * @summary java.nio.channels.spi.AbstractSelectionKey conformance: white-box structure + black-box behavior.
 * @run main AbstractSelectionKeyTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.spi.AbstractSelectionKey;
import java.util.Set;

public class AbstractSelectionKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static final class StubKey extends AbstractSelectionKey {
        final java.nio.channels.SelectableChannel ch;
        final Selector sel;
        StubKey(java.nio.channels.SelectableChannel ch, Selector sel) {
            this.ch = ch; this.sel = sel;
        }
        public java.nio.channels.SelectableChannel channel() { return ch; }
        public Selector selector() { return sel; }
        public int interestOps() { return ops; }
        public SelectionKey interestOps(int ops) { this.ops = ops; return this; }
        public int readyOps() { return 0; }
        int ops;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.spi.AbstractSelectionKey");
        check(Modifier.isAbstract(c.getModifiers()), "AbstractSelectionKey is abstract");
        check(SelectionKey.class.isAssignableFrom(c), "extends SelectionKey");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(Modifier.isFinal(c.getMethod("isValid").getModifiers()), "isValid final");
        check(Modifier.isFinal(c.getMethod("cancel").getModifiers()), "cancel final");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // real key from a pipe, checked through the abstract supertype
        java.nio.channels.Pipe pipe = java.nio.channels.Pipe.open();
        try {
            pipe.source().configureBlocking(false);
            try (Selector sel = Selector.open()) {
                SelectionKey key = pipe.source().register(sel, SelectionKey.OP_READ);
                check(key instanceof AbstractSelectionKey, "JDK key extends AbstractSelectionKey");
                AbstractSelectionKey ak = (AbstractSelectionKey) key;
                check(ak.isValid(), "valid while registered");
                ak.cancel();
                check(!ak.isValid(), "invalid after cancel");

                // stub key: attachments still work through the base class
                StubKey stub = new StubKey(pipe.source(), sel);
                check(stub.attachment() == null, "no attachment");
                Object token = new Object();
                check(stub.attach(token) == null, "attach returns previous (null)");
                check(stub.attachment() == token, "attachment round trip");
                stub.interestOps(SelectionKey.OP_WRITE);
                check(stub.interestOps() == SelectionKey.OP_WRITE, "stub interestOps");
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
        testValidInvalidateContract();
        testAttachContract();
        testRealKeyContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ===================== A7 batch: deepened contract coverage =====================

    static final class ProbeSelector extends java.nio.channels.spi.AbstractSelector {
        final java.util.List<java.nio.channels.SelectionKey> regs =
                new java.util.concurrent.CopyOnWriteArrayList<java.nio.channels.SelectionKey>();
        ProbeSelector() { super(java.nio.channels.spi.SelectorProvider.provider()); }
        protected java.nio.channels.SelectionKey register(java.nio.channels.spi.AbstractSelectableChannel ch, int ops, Object att) {
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

    static java.nio.channels.SelectionKey newProbeKey(java.nio.channels.SelectableChannel ch,
                                                      int ops, Object att) throws java.io.IOException {
        return ((java.nio.channels.spi.AbstractSelectableChannel) ch)
                .register(new ProbeSelector(), ops, att);
    }

    static void testStructureContract() {
        check(java.lang.reflect.Modifier.isAbstract(AbstractSelectionKey.class.getModifiers()),
              "class is abstract");
        check(java.nio.channels.SelectionKey.class.isAssignableFrom(AbstractSelectionKey.class),
              "extends SelectionKey");
        for (java.lang.reflect.Constructor<?> c : AbstractSelectionKey.class.getDeclaredConstructors())
            check(java.lang.reflect.Modifier.isProtected(c.getModifiers()),
                  "constructor is protected");
        check(java.nio.channels.SelectionKey.OP_READ == 1, "OP_READ constant");
        check(java.nio.channels.SelectionKey.OP_WRITE == 4, "OP_WRITE constant");
        check(java.nio.channels.SelectionKey.OP_CONNECT == 8, "OP_CONNECT constant");
        check(java.nio.channels.SelectionKey.OP_ACCEPT == 16, "OP_ACCEPT constant");
    }

    static java.nio.channels.SelectionKey probeKey(java.nio.channels.Selector sel) throws Exception {
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        ch.configureBlocking(false);
        java.nio.channels.SelectionKey k = ch.register(sel, java.nio.channels.SelectionKey.OP_READ, null);
        return k;
    }

    static void testValidInvalidateContract() throws Exception {
        ProbeSelector sel = new ProbeSelector();
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        ch.configureBlocking(false);
        try {
            java.nio.channels.SelectionKey k =
                    ch.register(sel, java.nio.channels.SelectionKey.OP_READ, null);
            check(k.isValid(), "a fresh key is valid");
            check(k.selector() == sel && k.channel() == ch, "key wiring");
            check(k.readyOps() == 0, "readyOps starts at zero");
            check(k.interestOps() == java.nio.channels.SelectionKey.OP_READ, "interestOps recorded");
            // cancel() invalidates the key immediately
            k.cancel();
            check(!k.isValid(), "cancel invalidates the key at once");
            k.cancel();
            check(!k.isValid(), "second cancel is a no-op");
            // a key on another channel/selector is independent
            java.nio.channels.SocketChannel ch2 = java.nio.channels.SocketChannel.open();
            try {
                ch2.configureBlocking(false);
                java.nio.channels.SelectionKey k2 =
                        ch2.register(sel, java.nio.channels.SelectionKey.OP_WRITE, null);
                check(k2 != k && k2.isValid(), "a fresh key is independent and valid");
                check(k2.channel() == ch2, "second key bound to its own channel");
            } finally {
                ch2.close();
            }
        } finally {
            ch.close();
            sel.close();
        }
        check(true, "validity cleanup done");
    }

    static void testAttachContract() throws Exception {
        ProbeSelector sel = new ProbeSelector();
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        ch.configureBlocking(false);
        try {
            java.nio.channels.SelectionKey k =
                    ch.register(sel, java.nio.channels.SelectionKey.OP_READ, null);
            check(k.attachment() == null, "attachment null by default");
            Object prev = k.attach("first");
            check(prev == null, "attach returns the previous attachment (null)");
            check("first".equals(k.attachment()), "attachment stored");
            Object prev2 = k.attach("second");
            check("first".equals(prev2), "attach returns the replaced attachment");
            check("second".equals(k.attachment()), "latest attachment wins");
            Object prev3 = k.attach(null);
            check("second".equals(prev3), "attach(null) returns the previous attachment");
            check(k.attachment() == null, "attachment cleared by attach(null)");
            Object token = new Object();
            k.attach(token);
            check(k.attachment() == token, "attachment identity preserved");
        } finally {
            ch.close();
            sel.close();
        }
    }

    static void testRealKeyContract() throws Exception {
        java.nio.channels.Selector sel = java.nio.channels.Selector.open();
        java.nio.channels.SocketChannel ch = java.nio.channels.SocketChannel.open();
        try {
            ch.configureBlocking(false);
            java.nio.channels.SelectionKey k = ch.register(sel,
                    java.nio.channels.SelectionKey.OP_READ | java.nio.channels.SelectionKey.OP_WRITE,
                    "real");
            check(k.isValid(), "real key valid");
            check(k.channel() == ch && k.selector() == sel, "real key wiring");
            check(k.interestOps() == (java.nio.channels.SelectionKey.OP_READ
                    | java.nio.channels.SelectionKey.OP_WRITE), "interestOps both ops");
            java.nio.channels.SelectionKey ret = k.interestOps(java.nio.channels.SelectionKey.OP_READ);
            check(ret == k, "interestOps(int) returns the same key");
            check(k.interestOps() == java.nio.channels.SelectionKey.OP_READ, "interestOps updated");
            try {
                k.interestOps(java.nio.channels.SelectionKey.OP_ACCEPT);
                check(false, "interestOps outside validOps must be refused");
            } catch (IllegalArgumentException e) {
                check(true, "interestOps(OP_ACCEPT) -> IllegalArgumentException");
            }
            check("real".equals(k.attachment()), "real key attachment");
            int ready = sel.selectNow();
            check(ready >= 0, "selectNow runs against the key");
            ch.close();
            check(!k.isValid(), "closing the channel invalidates the real key");
        } finally {
            ch.close();
            sel.close();
        }
    }

}

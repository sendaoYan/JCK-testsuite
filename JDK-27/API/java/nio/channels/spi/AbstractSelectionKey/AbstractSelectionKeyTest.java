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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.nio.channels.SelectionKey conformance: white-box structure + black-box behavior.
 * @run main SelectionKeyTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;

public class SelectionKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.SelectionKey");
        check(Modifier.isAbstract(c.getModifiers()), "SelectionKey is abstract");
        for (String f : new String[] {"OP_READ", "OP_WRITE", "OP_CONNECT", "OP_ACCEPT"}) {
            Field fl = c.getField(f);
            check(Modifier.isPublic(fl.getModifiers()) && Modifier.isStatic(fl.getModifiers())
                  && Modifier.isFinal(fl.getModifiers()), f + " public static final int");
            check(fl.getType() == int.class, f + " is int");
        }
        for (String n : new String[] {"channel", "selector", "isValid", "cancel", "interestOps",
                "readyOps", "isReadable", "isWritable", "isConnectable", "isAcceptable",
                "attach", "attachment"})
            check(hasMethod(c, n), "has method " + n);
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (Method m : c.getMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            ssc.configureBlocking(false);

            try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
                SelectionKey serverKey = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(serverKey.isValid(), "server key valid");
                check(serverKey.channel().isRegistered(), "channel reports registered");

                // constant values per specification
                check(SelectionKey.OP_READ == 1 << 0, "OP_READ == 1");
                check(SelectionKey.OP_WRITE == 1 << 2, "OP_WRITE == 4");
                check(SelectionKey.OP_CONNECT == 1 << 3, "OP_CONNECT == 8");
                check(SelectionKey.OP_ACCEPT == 1 << 4, "OP_ACCEPT == 16");

                // attachment round trip
                check(serverKey.attachment() == null, "no attachment initially");
                Object token = new Object();
                check(serverKey.attach(token) == null, "attach returns old attachment");
                check(serverKey.attachment() == token, "attachment retrieved");
                check(serverKey.attach(null) == token, "clearing returns previous");

                // interestOps manipulation incl. JDK 11 interestOpsOr/And
                sc.configureBlocking(false);
                sc.connect(addr);
                SelectionKey clientKey = sc.register(sel, SelectionKey.OP_CONNECT);
                check(clientKey.interestOps() == SelectionKey.OP_CONNECT, "client interest OP_CONNECT");
                clientKey.interestOps(clientKey.interestOps() | SelectionKey.OP_READ);
                check(clientKey.interestOps() == (SelectionKey.OP_CONNECT | SelectionKey.OP_READ),
                      "interestOps OR READ");
                int prev = clientKey.interestOpsOr(SelectionKey.OP_WRITE);
                check(prev == (SelectionKey.OP_CONNECT | SelectionKey.OP_READ), "interestOpsOr returns previous");
                check(clientKey.interestOps() == (SelectionKey.OP_CONNECT | SelectionKey.OP_READ | SelectionKey.OP_WRITE),
                      "interestOpsOr added WRITE");
                prev = clientKey.interestOpsAnd(~SelectionKey.OP_WRITE);
                check((prev & SelectionKey.OP_WRITE) != 0, "interestOpsAnd returns previous with WRITE");
                check((clientKey.interestOps() & SelectionKey.OP_WRITE) == 0, "WRITE cleared by And");
                clientKey.interestOps(SelectionKey.OP_READ);

                // readyOps / convenience predicates once events arrive
                while (!sc.finishConnect()) { sel.selectNow(); }
                int n = sel.select(5000);
                check(n >= 1, "some key ready after connect");
                check(clientKey.isConnectable() || clientKey.isReadable() || serverKey.isAcceptable(),
                      "at least one predicate true after select");
                if (serverKey.isAcceptable()) {
                    SocketChannel accepted = ssc.accept();
                    check(accepted != null, "accept on acceptable key");
                    accepted.close();
                }
                check((clientKey.readyOps() & ~clientKey.interestOps()) == 0
                      || clientKey.readyOps() == 0,
                      "readyOps subset of interestOps");

                // cancel invalidates
                clientKey.cancel();
                check(!clientKey.isValid(), "client key invalid after cancel");
                sel.selectNow();
                boolean threw = false;
                try { clientKey.interestOps(SelectionKey.OP_READ); }
                catch (java.nio.channels.CancelledKeyException e) { threw = true; }
                check(threw, "interestOps on cancelled key -> CancelledKeyException");
            }
        }
    }

    // ===================== A6 deepening: constant matrix / interestOps / lifecycle / cancel =====================

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

    static void testConstantMatrix() {
        check(SelectionKey.OP_READ == 1, "OP_READ == 1");
        check(SelectionKey.OP_WRITE == 4, "OP_WRITE == 4");
        check(SelectionKey.OP_CONNECT == 8, "OP_CONNECT == 8");
        check(SelectionKey.OP_ACCEPT == 16, "OP_ACCEPT == 16");
        check((SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT
               | SelectionKey.OP_ACCEPT) == 29, "union of all ops == 29");
        check(Integer.bitCount(SelectionKey.OP_READ) == 1
              && Integer.bitCount(SelectionKey.OP_WRITE) == 1
              && Integer.bitCount(SelectionKey.OP_CONNECT) == 1
              && Integer.bitCount(SelectionKey.OP_ACCEPT) == 1, "each op is a single bit");
        check((SelectionKey.OP_READ & SelectionKey.OP_WRITE) == 0
              && (SelectionKey.OP_CONNECT & SelectionKey.OP_ACCEPT) == 0, "bits are disjoint");
    }

    static void testInterestOpsContract() throws Exception {
        try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.interestOps() == SelectionKey.OP_READ, "interestOps roundtrip READ");
            check(key.interestOps(SelectionKey.OP_WRITE) == key, "interestOps setter returns this key");
            check(key.interestOps() == SelectionKey.OP_WRITE, "interestOps now WRITE");
            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT);
            check(key.interestOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE
                                        | SelectionKey.OP_CONNECT), "interestOps full mask");
            // interest 0 is explicitly legal ("no interest")
            key.interestOps(0);
            check(key.interestOps() == 0, "interestOps(0) is allowed");
            checkThrows(IllegalArgumentException.class, () -> key.interestOps(1 << 5),
                        "interestOps bit 5 -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class,
                        () -> key.interestOps(SelectionKey.OP_ACCEPT),
                        "interestOps OP_ACCEPT on SocketChannel key -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class, () -> key.interestOps(-1),
                        "interestOps(-1) -> IllegalArgumentException");
        }
    }

    static void testRegisterWithAttachment() throws Exception {
        try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            Object token = new Object();
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ, token);
            check(key.attachment() == token, "register(sel, ops, att) stores the attachment");
            SelectionKey same = sc.register(sel, SelectionKey.OP_WRITE);
            check(same == key, "re-register on same selector returns the same key");
            check(key.interestOps() == SelectionKey.OP_WRITE, "re-register updates interestOps");
            // RI: the 2-arg register is register(sel, ops, null) and attach(null) is
            // applied unconditionally, so the previous attachment is cleared
            check(key.attachment() == null, "re-register without attachment clears it (RI attaches null)");
            sc.register(sel, SelectionKey.OP_READ, null);
            check(key.attachment() == null, "explicit null attachment overwrites");
        }
    }

    static void testIsValidLifecycle() throws Exception {
        try (Selector sel = Selector.open()) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.isValid(), "valid while channel open");
            sc.close();
            check(!key.isValid(), "invalid after channel close");
        }
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            Selector sel = Selector.open();
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.isValid(), "valid while selector open");
            sel.close();
            check(!key.isValid(), "invalid after selector close");
        }
        try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            key.cancel();
            check(!key.isValid(), "invalid after cancel");
        }
    }

    static void testCancelledKeyExceptions() throws Exception {
        try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            key.cancel();
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.interestOps(),
                        "interestOps() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class,
                        () -> key.interestOps(SelectionKey.OP_READ),
                        "interestOps(int) on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.readyOps(),
                        "readyOps() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isReadable(),
                        "isReadable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isWritable(),
                        "isWritable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isConnectable(),
                        "isConnectable() on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class, () -> key.isAcceptable(),
                        "isAcceptable() on cancelled key -> CancelledKeyException");
            // attach/attachment/channel/selector stay usable on a cancelled key
            key.attach("x");
            check("x".equals(key.attachment()), "attach still works on cancelled key");
            check(key.channel() == sc, "channel() still works on cancelled key");
            check(key.selector() == sel, "selector() still works on cancelled key");
            // interestOpsOr/And (JDK-11+) also reject cancelled keys
            checkThrows(java.nio.channels.CancelledKeyException.class,
                        () -> key.interestOpsOr(SelectionKey.OP_READ),
                        "interestOpsOr on cancelled key -> CancelledKeyException");
            checkThrows(java.nio.channels.CancelledKeyException.class,
                        () -> key.interestOpsAnd(SelectionKey.OP_READ),
                        "interestOpsAnd on cancelled key -> CancelledKeyException");
        }
    }

    static void testReadinessPredicates() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            ssc.configureBlocking(false);
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (Selector sel = Selector.open(); SocketChannel client = SocketChannel.open(addr)) {
                SelectionKey serverKey = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(sel.selectNow() == 1, "server key ready after client connect");
                check(serverKey.isAcceptable(), "isAcceptable after accept readiness");
                check(serverKey.readyOps() == SelectionKey.OP_ACCEPT, "server readyOps == OP_ACCEPT");
                check(!serverKey.isReadable(), "server key not readable");
                check(!serverKey.isWritable(), "server key not writable");
                check(!serverKey.isConnectable(), "server key not connectable");
                try (SocketChannel server = ssc.accept()) {
                    client.configureBlocking(false);
                    SelectionKey clientKey = client.register(sel, SelectionKey.OP_READ);
                    check(clientKey.readyOps() == 0, "client readyOps 0 before select");
                    java.nio.ByteBuffer w = java.nio.ByteBuffer.wrap(new byte[] {(byte) 0xAB});
                    while (w.hasRemaining()) server.write(w);
                    check(sel.select(5_000) == 1, "client readable after server write");
                    check(clientKey.readyOps() == SelectionKey.OP_READ, "client readyOps == OP_READ");
                    check(clientKey.isReadable(), "isReadable true");
                    java.nio.ByteBuffer r = java.nio.ByteBuffer.allocate(1);
                    check(client.read(r) == 1 && r.get(0) == (byte) 0xAB, "drain the byte");
                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    check(sel.select(5_000) == 1, "client writable");
                    check(clientKey.isWritable(), "isWritable true");
                    // version difference: JDK-8 clears the previously selected OP_READ bit and
                    // reports readyOps == OP_WRITE exactly; JDK-27 keeps the OP_READ bit, so
                    // only containment is asserted here (probed deterministic: JDK-27 -> 5)
                    check((clientKey.readyOps() & SelectionKey.OP_WRITE) == SelectionKey.OP_WRITE,
                          "readyOps contains OP_WRITE");
                }
            }
        }
    }

    static void testConnectReadiness() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (SocketChannel nb = SocketChannel.open(); Selector sel = Selector.open()) {
                nb.configureBlocking(false);
                SelectionKey key = nb.register(sel, SelectionKey.OP_CONNECT);
                check(!nb.isConnected() && !nb.isConnectionPending(), "clean state before connect");
                boolean completed = nb.connect(addr);
                check(sel.select(5_000) == 1, "OP_CONNECT readiness");
                check(key.isConnectable(), "isConnectable true");
                check(key.readyOps() == SelectionKey.OP_CONNECT, "readyOps == OP_CONNECT");
                check(!key.isReadable() && !key.isWritable() && !key.isAcceptable(),
                      "only the connect bit is set");
                check(nb.finishConnect(), "finishConnect after readiness");
                check(nb.isConnected(), "connected after finishConnect (connect returned " + completed + ")");
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstantMatrix();
        testInterestOpsContract();
        testRegisterWithAttachment();
        testIsValidLifecycle();
        testCancelledKeyExceptions();
        testReadinessPredicates();
        testConnectReadiness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

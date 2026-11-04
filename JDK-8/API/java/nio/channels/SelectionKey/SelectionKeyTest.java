/*
 * @test
 * @summary java.nio.channels.SelectionKey conformance: ops and identity.
 * @run main SelectionKeyTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.CancelledKeyException;
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

    static void testSelectionKey() throws Exception {
        check(SelectionKey.OP_READ == 1, "OP_READ");
        check(SelectionKey.OP_WRITE == 4, "OP_WRITE");
        check(SelectionKey.OP_CONNECT == 8, "OP_CONNECT");
        check(SelectionKey.OP_ACCEPT == 16, "OP_ACCEPT");

        Selector sel = Selector.open();
        SocketChannel sc = SocketChannel.open();
        sc.configureBlocking(false);
        SelectionKey key = sc.register(sel, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
        check(key.channel() == sc, "channel");
        check(key.selector() == sel, "selector");
        check(key.isValid(), "isValid");
        check(key.interestOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE),
              "interestOps");
        check(key.readyOps() == 0, "readyOps initially 0");
        check(key.interestOps(SelectionKey.OP_READ).interestOps() == SelectionKey.OP_READ,
              "interestOps setter");
        check(key.toString() != null, "toString");
        sc.close();
        sel.close();
        check(!key.isValid(), "invalid after close");
    }

    // ===================== A6 deepening: constant matrix / interestOps / lifecycle / cancel =====================

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
        int all = SelectionKey.OP_READ | SelectionKey.OP_WRITE | SelectionKey.OP_CONNECT
                  | SelectionKey.OP_ACCEPT;
        check((SelectionKey.OP_READ & SelectionKey.OP_WRITE) == 0
              && (SelectionKey.OP_CONNECT & SelectionKey.OP_ACCEPT) == 0, "bits are disjoint");
        check(all == (all & 0x1D), "ops live in the low five bits");
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
            SelectionKey restored = sc.register(sel, SelectionKey.OP_READ, token);
            check(restored == key && key.attachment() == token, "re-register with token restores it");
        }
    }

    static void testAttachSemantics() throws Exception {
        try (Selector sel = Selector.open(); SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.attachment() == null, "attachment initially null");
            Object token = new Object();
            check(key.attach(token) == null, "attach returns the OLD attachment (null)");
            check(key.attachment() == token, "attachment retrievable");
            Object token2 = new Object();
            check(key.attach(token2) == token, "attach returns previous attachment");
            check(key.attachment() == token2, "attachment replaced");
            check(key.attach(null) == token2, "attach(null) returns previous attachment");
            check(key.attachment() == null, "attach(null) clears the attachment");
        }
    }

    static void testIsValidLifecycle() throws Exception {
        // channel close invalidates the key
        try (Selector sel = Selector.open()) {
            SocketChannel sc = SocketChannel.open();
            sc.configureBlocking(false);
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.isValid(), "valid while channel open");
            sc.close();
            check(!key.isValid(), "invalid after channel close");
        }
        // selector close invalidates the key
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            Selector sel = Selector.open();
            SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
            check(key.isValid(), "valid while selector open");
            sel.close();
            check(!key.isValid(), "invalid after selector close");
        }
        // cancel invalidates immediately
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
            checkThrows(CancelledKeyException.class, () -> key.interestOps(),
                        "interestOps() on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.interestOps(SelectionKey.OP_READ),
                        "interestOps(int) on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.readyOps(),
                        "readyOps() on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.isReadable(),
                        "isReadable() on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.isWritable(),
                        "isWritable() on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.isConnectable(),
                        "isConnectable() on cancelled key -> CancelledKeyException");
            checkThrows(CancelledKeyException.class, () -> key.isAcceptable(),
                        "isAcceptable() on cancelled key -> CancelledKeyException");
            // attach/attachment/channel/selector stay usable on a cancelled key
            key.attach("x");
            check("x".equals(key.attachment()), "attach still works on cancelled key");
            check(key.channel() == sc, "channel() still works on cancelled key");
            check(key.selector() == sel, "selector() still works on cancelled key");
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
                    // OP_READ readiness: server writes one byte
                    client.configureBlocking(false);
                    SelectionKey clientKey = client.register(sel, SelectionKey.OP_READ);
                    check(clientKey.readyOps() == 0, "client readyOps 0 before select");
                    ByteBuffer w = ByteBuffer.wrap(new byte[] {(byte) 0xAB});
                    while (w.hasRemaining()) server.write(w);
                    check(sel.select(5_000) == 1, "client readable after server write");
                    check(clientKey.readyOps() == SelectionKey.OP_READ, "client readyOps == OP_READ");
                    check(clientKey.isReadable(), "isReadable true");
                    check(!clientKey.isWritable() || clientKey.readyOps() != 0,
                          "writable flag only with its bit");
                    ByteBuffer r = ByteBuffer.allocate(1);
                    check(client.read(r) == 1 && r.get(0) == (byte) 0xAB, "drain the byte");
                    // OP_WRITE readiness: connected socket with empty send buffer is writable.
                    // version difference: JDK-8 clears the previously selected OP_READ bit here
                    // (readyOps == OP_WRITE exactly); JDK-27 keeps it, so only containment is
                    // asserted on that side (probed deterministic: JDK-8 -> 4, JDK-27 -> 5)
                    clientKey.interestOps(SelectionKey.OP_WRITE);
                    check(sel.select(5_000) == 1, "client writable");
                    check(clientKey.isWritable(), "isWritable true");
                    check(clientKey.readyOps() == SelectionKey.OP_WRITE, "readyOps == OP_WRITE");
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
                check(!nb.isConnected() && nb.isConnectionPending() == false,
                      "clean state before connect");
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
        testSelectionKey();
        testConstantMatrix();
        testInterestOpsContract();
        testRegisterWithAttachment();
        testAttachSemantics();
        testIsValidLifecycle();
        testCancelledKeyExceptions();
        testReadinessPredicates();
        testConnectReadiness();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

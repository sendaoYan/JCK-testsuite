/*
 * @test
 * @summary java.nio.channels.Selector conformance: open/select/keys.
 * @run main SelectorTest
 */

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedSelectorException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.locks.LockSupport;

public class SelectorTest {
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

    /** Bounded settle (poll-with-timeout, < 0.5 s) so a daemon thread reliably enters a blocking op. */
    static void settle() {
        LockSupport.parkNanos(300_000_000L);
    }

    static void testSelector() throws Exception {
        Selector sel = Selector.open();
        check(sel.isOpen(), "isOpen");
        check(sel.provider() != null, "provider");
        check(sel.keys().isEmpty(), "keys empty");
        check(sel.selectedKeys().isEmpty(), "selectedKeys empty");
        check(sel.selectNow() >= 0, "selectNow");
        check(sel.toString() != null, "toString");
        sel.close();
        check(!sel.isOpen(), "closed");
    }

    // ===================== A6 deepening: lifecycle / sets / wakeup / close semantics =====================

    static void testLifecycleAndProvider() throws Exception {
        Selector sel = Selector.open();
        check(sel.isOpen(), "fresh selector open");
        check(sel.provider() != null, "provider non-null");
        check(sel.provider() == java.nio.channels.spi.SelectorProvider.provider(),
              "provider is the default SelectorProvider.provider()");
        sel.close();
        check(!sel.isOpen(), "closed after close");
        sel.close(); // idempotent
        check(!sel.isOpen(), "second close keeps it closed, no exception");
        check(sel.provider() != null, "provider still queryable after close");
        check(sel.toString() != null, "toString non-null after close");
    }

    static void testSetSemantics() throws Exception {
        try (Selector sel = Selector.open()) {
            check(sel.keys() != null && sel.keys().isEmpty(), "keys() empty set");
            check(sel.selectedKeys() != null && sel.selectedKeys().isEmpty(),
                  "selectedKeys() empty set");
            // keys() is immutable from the outside
            checkThrows(UnsupportedOperationException.class,
                        () -> sel.keys().add(null), "keys().add -> UnsupportedOperationException");
            checkThrows(UnsupportedOperationException.class,
                        () -> sel.selectedKeys().add(null),
                        "selectedKeys().add -> UnsupportedOperationException");
            // selectedKeys() is mutable for the remove/clear protocol
            check(!sel.selectedKeys().remove(new Object()),
                  "selectedKeys().remove of absent element returns false");
            sel.selectedKeys().clear(); // must not throw on empty set
            check(sel.selectedKeys().isEmpty(), "selectedKeys empty after clear");
        }
    }

    static void testSelectTimeouts() throws Exception {
        try (Selector sel = Selector.open()) {
            check(sel.selectNow() == 0, "selectNow with no registration is 0");
            check(sel.selectNow() == 0, "selectNow idempotent when nothing ready");
            long t0 = System.nanoTime();
            int n = sel.select(20); // 20 ms bound, nothing registered
            long ms = (System.nanoTime() - t0) / 1_000_000;
            check(n == 0, "select(20) with nothing ready is 0");
            check(ms >= 15, "select(20) honored the timeout, waited " + ms + "ms");
            check(ms < 5_000, "select(20) returned promptly, took " + ms + "ms");
        }
        try (Selector sel = Selector.open()) {
            checkThrows(IllegalArgumentException.class, () -> sel.select(-1),
                        "select(-1) -> IllegalArgumentException");
            checkThrows(IllegalArgumentException.class, () -> sel.select(-100),
                        "select(negative) -> IllegalArgumentException");
        }
    }

    static void testWakeupStickiness() throws Exception {
        try (Selector sel = Selector.open()) {
            sel.wakeup();
            long t0 = System.nanoTime();
            int n = sel.select(60_000); // must return immediately: wakeup is sticky
            long ms = (System.nanoTime() - t0) / 1_000_000;
            check(n == 0, "select after wakeup returns 0");
            check(ms < 5_000, "sticky wakeup returned promptly, took " + ms + "ms");
            check(sel.selectNow() == 0, "wakeup consumed: selectNow back to 0");
        }
    }

    static void testWakeupFromDaemonThread() throws Exception {
        try (Selector sel = Selector.open()) {
            final CountDownLatch started = new CountDownLatch(1);
            Thread waker = new Thread(() -> {
                started.countDown();
                sel.wakeup();
            }, "selector-waker");
            waker.setDaemon(true);
            waker.start();
            started.await();
            long t0 = System.nanoTime();
            int n = sel.select(60_000); // deterministic 0 whether wakeup lands before or during
            long ms = (System.nanoTime() - t0) / 1_000_000;
            check(n == 0, "select woken by daemon wakeup returns 0");
            check(ms < 5_000, "daemon wakeup returned promptly, took " + ms + "ms");
            waker.join(5_000);
            check(!waker.isAlive(), "waker daemon finished");
        }
    }

    static void testSelectZeroBlockingSemantics() throws Exception {
        // select(0) means "block indefinitely": a pre-fired wakeup makes it return 0 deterministically.
        try (Selector sel = Selector.open()) {
            sel.wakeup();
            long t0 = System.nanoTime();
            int n = sel.select(0);
            long ms = (System.nanoTime() - t0) / 1_000_000;
            check(n == 0, "select(0) after wakeup returns 0");
            check(ms < 5_000, "select(0) not blocked forever, took " + ms + "ms");
        }
    }

    static void testRegisterLifecycle() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            ssc.configureBlocking(false);
            try (Selector sel = Selector.open()) {
                SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(key != null, "register returns a key");
                check(sel.keys().size() == 1, "keys() contains the registration");
                check(sel.keys().contains(key), "keys() contains the exact key");
                check(ssc.keyFor(sel) == key, "keyFor returns the same key");
                check(sel.selectNow() == 0, "registered but not ready: selectNow 0");
                // registering with a closed selector
                Selector dead = Selector.open();
                dead.close();
                checkThrows(ClosedSelectorException.class,
                            () -> ssc.register(dead, SelectionKey.OP_ACCEPT),
                            "register on closed selector -> ClosedSelectorException");
                // null selector
                checkThrows(NullPointerException.class,
                            () -> ssc.register(null, SelectionKey.OP_ACCEPT),
                            "register(null selector) -> NullPointerException");
            }
        }
    }

    static void testSelectedKeysFlow() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            ssc.configureBlocking(false);
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();
            try (Selector sel = Selector.open(); SocketChannel client = SocketChannel.open(addr)) {
                SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(sel.selectNow() == 1, "one ready channel after connect");
                check(sel.selectedKeys().size() == 1, "selectedKeys size 1");
                check(sel.selectedKeys().contains(key), "selectedKeys contains the accept key");
                check(key.readyOps() == SelectionKey.OP_ACCEPT, "readyOps == OP_ACCEPT");
                check(key.isAcceptable(), "isAcceptable true");
                check(!key.isReadable() && !key.isWritable() && !key.isConnectable(),
                      "no other readiness flags");
                SocketChannel accepted = ssc.accept();
                check(accepted != null, "accept succeeds after readiness");
                accepted.close();
                // the documented protocol: remove the processed key from selectedKeys
                check(sel.selectedKeys().remove(key), "selectedKeys().remove(key) works");
                check(sel.selectedKeys().isEmpty(), "selectedKeys empty after remove");
                check(sel.selectNow() == 0, "nothing more ready (event consumed by accept)");
                // manual clear() is the bulk form of the same protocol
                client.close();
                ssc.register(sel, 0);
                sel.selectedKeys().clear();
                check(sel.selectedKeys().isEmpty(), "selectedKeys empty after clear");
            }
        }
    }

    static void testCancelSemantics() throws Exception {
        try (SocketChannel sc = SocketChannel.open()) {
            sc.configureBlocking(false);
            try (Selector sel = Selector.open()) {
                SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
                check(sel.keys().size() == 1, "key present before cancel");
                key.cancel();
                check(sel.keys().contains(key), "cancelled key still listed until next select");
                check(!key.isValid(), "cancelled key invalid immediately");
                sel.selectNow(); // forces removal of cancelled keys
                check(sel.keys().isEmpty(), "keys() drops cancelled key after selectNow");
                key.cancel(); // second cancel documented no-op
                check(!key.isValid(), "still invalid after double cancel");
            }
        }
    }

    static void testClosedSelectorOperations() throws Exception {
        Selector sel = Selector.open();
        sel.close();
        check(!sel.isOpen(), "isOpen false after close");
        checkThrows(ClosedSelectorException.class, () -> sel.keys(),
                    "keys() after close -> ClosedSelectorException");
        checkThrows(ClosedSelectorException.class, () -> sel.selectedKeys(),
                    "selectedKeys() after close -> ClosedSelectorException");
        checkThrows(ClosedSelectorException.class, () -> sel.selectNow(),
                    "selectNow() after close -> ClosedSelectorException");
        checkThrows(ClosedSelectorException.class, () -> sel.select(1),
                    "select(long) after close -> ClosedSelectorException");
        checkThrows(ClosedSelectorException.class, () -> sel.select(),
                    "select() after close -> ClosedSelectorException");
        check(sel.wakeup() == sel, "wakeup() after close is a documented no-op returning this");
    }

    static void testTwoSelectorsOneChannel() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            try (SocketChannel sc = SocketChannel.open(
                        (InetSocketAddress) ssc.getLocalAddress());
                 SocketChannel peer = ssc.accept();
                 Selector s1 = Selector.open();
                 Selector s2 = Selector.open()) {
                sc.configureBlocking(false);
                SelectionKey k1 = sc.register(s1, SelectionKey.OP_READ);
                SelectionKey k2 = sc.register(s2, SelectionKey.OP_READ);
                check(k1 != k2, "distinct keys per selector");
                check(k1.selector() == s1 && k2.selector() == s2, "each key bound to its selector");
                check(sc.keyFor(s1) == k1 && sc.keyFor(s2) == k2, "keyFor identity");
                check(sc.keyFor(null) == null, "keyFor(null) is null");
                check(s1.keys().size() == 1 && s2.keys().size() == 1, "one key in each selector");
                // connected + quiescent: nothing ready in either selector
                // (an unconnected socket reports ready for both OP_READ and OP_WRITE, so a
                //  connected loopback pair is required for the not-ready assertion)
                check(s1.selectNow() == 0 && s2.selectNow() == 0, "neither selector reports ready");
                k1.cancel();
                s1.selectNow();
                check(s1.keys().isEmpty(), "cancel on s1 only affects s1");
                check(s2.keys().size() == 1, "s2 registration unaffected");
            }
        }
    }

    static void testCloseWakesBlockedSelect() throws Exception {
        final Selector sel = Selector.open();
        try {
            final int[] got = {Integer.MIN_VALUE};
            final CountDownLatch started = new CountDownLatch(1);
            Thread reader = new Thread(() -> {
                try {
                    started.countDown();
                    got[0] = sel.select(60_000);
                } catch (Exception e) {
                    got[0] = -2;
                }
            }, "select-blocked");
            reader.setDaemon(true);
            reader.start();
            started.await();
            settle(); // let the daemon enter the blocking select (bounded)
            sel.close();
            reader.join(10_000);
            check(!reader.isAlive(), "blocked select thread finished after close (bounded)");
            check(got[0] == 0, "close() wakes a blocked select which returns 0, got " + got[0]);
        } finally {
            sel.close();
        }
    }

    public static void main(String[] args) throws Exception {
        testSelector();
        testLifecycleAndProvider();
        testSetSemantics();
        testSelectTimeouts();
        testWakeupStickiness();
        testWakeupFromDaemonThread();
        testSelectZeroBlockingSemantics();
        testRegisterLifecycle();
        testSelectedKeysFlow();
        testCancelSemantics();
        testClosedSelectorOperations();
        testTwoSelectorsOneChannel();
        testCloseWakesBlockedSelect();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.nio.channels.Selector conformance: white-box structure + black-box behavior.
 * @run main SelectorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class SelectorTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Selector");
        check(Modifier.isAbstract(c.getModifiers()), "Selector is abstract");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        Set<String> names = new java.util.HashSet<>();
        for (Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"open", "isOpen", "provider", "keys", "selectedKeys",
                "selectNow", "select", "wakeup", "close"})
            check(names.contains(n), "has method " + n);
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        try (ServerSocketChannel ssc = ServerSocketChannel.open()) {
            ssc.bind(new InetSocketAddress(InetAddress.getLoopbackAddress(), 0));
            ssc.configureBlocking(false);
            InetSocketAddress addr = (InetSocketAddress) ssc.getLocalAddress();

            try (Selector sel = Selector.open()) {
                check(sel.isOpen(), "selector open");
                check(sel.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                      "default provider");
                check(sel.keys().isEmpty(), "no keys initially");
                check(sel.selectNow() == 0, "selectNow 0 with no registration");

                SelectionKey key = ssc.register(sel, SelectionKey.OP_ACCEPT);
                check(key != null, "registered");
                check(sel.keys().size() == 1, "one key");
                check(key.selector() == sel, "key bound to selector");
                check(key.channel() == ssc, "key bound to channel");
                check(key.interestOps() == SelectionKey.OP_ACCEPT, "interest ops");

                // no pending connection: selectNow()==0, select(100)==0
                check(sel.selectNow() == 0, "selectNow 0, nothing pending");
                check(sel.select(100) == 0, "timed select 0, nothing pending");

                // wakeup a blocked select
                Thread waker = new Thread(() -> sel.wakeup(), "waker");
                waker.setDaemon(true);
                waker.start();
                int n = sel.select(60_000); // returns promptly via wakeup
                check(n == 0, "wakeup produced no events (n=" + n + ")");
                waker.join(10_000);

                // real event: client connects -> OP_ACCEPT selected
                try (SocketChannel client = SocketChannel.open(addr)) {
                    n = sel.select(5000);
                    check(n == 1, "one channel ready");
                    check(key.isAcceptable(), "isAcceptable after select");
                    Set<SelectionKey> selected = sel.selectedKeys();
                    check(selected.contains(key), "selected set contains the accept key");
                    // consume the event: accept
                    SocketChannel accepted = ssc.accept();
                    check(accepted != null, "accepted after select");
                    accepted.close();
                    selected.remove(key);
                    check(sel.selectNow() == 0, "no more ready after accepting");
                }

                // select(Consumer) variant
                AtomicInteger visited = new AtomicInteger();
                int cn = sel.select(k -> { check(k == key, "consumer sees accept key"); visited.incrementAndGet(); }, 5000);
                check(cn >= 0, "consumer select returns");
                try (SocketChannel c2 = SocketChannel.open(addr)) {
                    cn = sel.select(k -> visited.incrementAndGet(), 5000);
                    check(cn == 1, "consumer select found accept");
                    SocketChannel a2 = ssc.accept();
                    check(a2 != null, "accepted second");
                    a2.close();
                }
                check(visited.get() >= 1, "consumer invoked");
                sel.selectedKeys().clear();

                // cancel removes the key
                key.cancel();
                check(sel.selectNow() == 0, "selectNow after cancel");
                check(!key.isValid(), "cancelled key invalid");
                check(!sel.keys().contains(key) || sel.keys().isEmpty(), "key removed after cancel+select");
            }

            // closed selector rejects operations
            Selector cl = Selector.open();
            cl.close();
            check(!cl.isOpen(), "closed");
            boolean threw = false;
            try { cl.keys(); }
            catch (java.nio.channels.ClosedSelectorException e) { threw = true; }
            check(threw, "keys() after close -> ClosedSelectorException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

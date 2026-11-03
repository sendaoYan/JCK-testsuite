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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

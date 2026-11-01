/*
 * @test
 * @summary java.nio.channels.SelectionKey conformance: ops and identity.
 * @run main SelectionKeyTest
 */

import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SelectionKeyTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
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

    public static void main(String[] args) throws Exception {
        testSelectionKey();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

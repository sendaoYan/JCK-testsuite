/*
 * @test
 * @summary java.nio.channels.SelectableChannel conformance: blocking/register.
 * @run main SelectableChannelTest
 */

import java.nio.channels.SelectableChannel;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;

public class SelectableChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSelectableChannel() throws Exception {
        SelectableChannel sc = SocketChannel.open();
        check(sc.isOpen(), "isOpen");
        check(sc.isBlocking(), "initially blocking");
        sc.configureBlocking(false);
        check(!sc.isBlocking(), "non-blocking");
        check(sc.blockingLock() != null, "blockingLock");
        check(sc.validOps() != 0, "validOps");
        check(sc.provider() != null, "provider");

        Selector sel = Selector.open();
        SelectionKey key = sc.register(sel, SelectionKey.OP_READ);
        check(sc.isRegistered(), "isRegistered");
        check(key.isValid(), "key valid");
        check(key.interestOps() == SelectionKey.OP_READ, "interestOps");
        sc.close();
        sel.close();
    }

    public static void main(String[] args) throws Exception {
        testSelectableChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

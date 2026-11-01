/*
 * @test
 * @summary java.nio.channels.SocketChannel conformance: open/validOps/state.
 * @run main SocketChannelTest
 */

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class SocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSocketChannel() throws Exception {
        SocketChannel ch = SocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE |
                SelectionKey.OP_CONNECT), "validOps");
        check(!ch.isConnected(), "not connected before connect");
        check(ch.isBlocking(), "initially blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testSocketChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

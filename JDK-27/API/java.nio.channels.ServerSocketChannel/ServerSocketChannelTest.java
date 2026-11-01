/*
 * @test
 * @summary java.nio.channels.ServerSocketChannel conformance: open/bind/address.
 * @run main ServerSocketChannelTest
 */

import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.ServerSocketChannel;

public class ServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testServerSocketChannel() throws Exception {
        ServerSocketChannel ch = ServerSocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == SelectionKey.OP_ACCEPT, "validOps OP_ACCEPT");
        ch.bind(new InetSocketAddress(0));
        check(ch.getLocalAddress() != null, "getLocalAddress");
        check(ch.isBlocking(), "initially blocking");
        ch.configureBlocking(false);
        check(!ch.isBlocking(), "non-blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testServerSocketChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

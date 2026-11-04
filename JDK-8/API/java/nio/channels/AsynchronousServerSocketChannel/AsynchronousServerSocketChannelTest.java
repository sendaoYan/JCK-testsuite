/*
 * @test
 * @summary java.nio.channels.AsynchronousServerSocketChannel conformance: open/bind.
 * @run main AsynchronousServerSocketChannelTest
 */

import java.net.InetSocketAddress;
import java.nio.channels.AsynchronousServerSocketChannel;

public class AsynchronousServerSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousServerSocketChannel() throws Exception {
        AsynchronousServerSocketChannel ch = AsynchronousServerSocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.provider() != null, "provider");
        ch.bind(new InetSocketAddress(0));
        check(ch.getLocalAddress() != null, "getLocalAddress");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousServerSocketChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

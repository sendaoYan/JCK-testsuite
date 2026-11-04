/*
 * @test
 * @summary java.nio.channels.AsynchronousSocketChannel conformance: open/provider.
 * @run main AsynchronousSocketChannelTest
 */

import java.nio.channels.AsynchronousSocketChannel;

public class AsynchronousSocketChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testAsynchronousSocketChannel() throws Exception {
        AsynchronousSocketChannel ch = AsynchronousSocketChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.provider() != null, "provider");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testAsynchronousSocketChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

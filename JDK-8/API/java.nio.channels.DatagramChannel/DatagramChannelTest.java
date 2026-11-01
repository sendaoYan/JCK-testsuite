/*
 * @test
 * @summary java.nio.channels.DatagramChannel conformance: open/bind/state.
 * @run main DatagramChannelTest
 */

import java.net.InetSocketAddress;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;

public class DatagramChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testDatagramChannel() throws Exception {
        DatagramChannel ch = DatagramChannel.open();
        check(ch.isOpen(), "isOpen");
        check(ch.validOps() == (SelectionKey.OP_READ | SelectionKey.OP_WRITE), "validOps");
        check(!ch.isConnected(), "not connected");
        ch.bind(new InetSocketAddress(0));
        check(ch.getLocalAddress() != null, "getLocalAddress");
        check(ch.isBlocking(), "initially blocking");
        ch.close();
        check(!ch.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testDatagramChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

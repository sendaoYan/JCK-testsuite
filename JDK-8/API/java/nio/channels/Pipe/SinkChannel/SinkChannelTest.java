/*
 * @test
 * @summary java.nio.channels.Pipe.SinkChannel conformance: open/write/validOps.
 * @run main SinkChannelTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;

public class SinkChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSinkChannel() throws Exception {
        Pipe.SinkChannel sink = Pipe.open().sink();
        check(sink.isOpen(), "isOpen");
        check(sink.validOps() == SelectionKey.OP_WRITE, "validOps OP_WRITE");
        int n = sink.write(ByteBuffer.wrap(new byte[] {1, 2, 3}));
        check(n == 3, "write 3 bytes");
        check(sink.isBlocking(), "initially blocking");
        sink.close();
        check(!sink.isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testSinkChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

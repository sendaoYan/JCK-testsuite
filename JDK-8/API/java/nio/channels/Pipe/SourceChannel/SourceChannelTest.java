/*
 * @test
 * @summary java.nio.channels.Pipe.SourceChannel conformance: open/read/validOps.
 * @run main SourceChannelTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;

public class SourceChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testSourceChannel() throws Exception {
        Pipe pipe = Pipe.open();
        Pipe.SourceChannel source = pipe.source();
        check(source.isOpen(), "isOpen");
        check(source.validOps() == SelectionKey.OP_READ, "validOps OP_READ");
        pipe.sink().write(ByteBuffer.wrap(new byte[] {9}));
        ByteBuffer in = ByteBuffer.allocate(1);
        check(source.read(in) == 1, "read 1 byte");
        check(source.isBlocking(), "initially blocking");
        source.close();
        check(!source.isOpen(), "closed");
        pipe.sink().close();
    }

    public static void main(String[] args) throws Exception {
        testSourceChannel();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

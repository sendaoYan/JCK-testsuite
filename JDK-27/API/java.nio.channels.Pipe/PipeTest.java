/*
 * @test
 * @summary java.nio.channels.Pipe conformance: open/sink/source round-trip.
 * @run main PipeTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.Pipe;

public class PipeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testPipe() throws Exception {
        Pipe pipe = Pipe.open();
        check(pipe.sink() != null, "sink");
        check(pipe.source() != null, "source");
        ByteBuffer out = ByteBuffer.wrap(new byte[] {42});
        pipe.sink().write(out);
        ByteBuffer in = ByteBuffer.allocate(1);
        pipe.source().read(in);
        in.flip();
        check(in.get() == 42, "round-trip");
        pipe.sink().close();
        pipe.source().close();
        check(!pipe.sink().isOpen() && !pipe.source().isOpen(), "closed");
    }

    public static void main(String[] args) throws Exception {
        testPipe();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.nio.channels.Pipe$SourceChannel conformance: white-box structure + black-box behavior.
 * @run main SourceChannelTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;

public class SourceChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe$SourceChannel");
        check(Modifier.isAbstract(c.getModifiers()), "SourceChannel is abstract");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(c),
              "implements ReadableByteChannel");
        check(java.nio.channels.ScatteringByteChannel.class.isAssignableFrom(c),
              "implements ScatteringByteChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.sink().write(ByteBuffer.wrap(new byte[] {1, 2, 3, 4}));
            Pipe.SourceChannel source = pipe.source();
            // read through the abstract SourceChannel type
            ByteBuffer r = ByteBuffer.allocate(4);
            while (r.hasRemaining()) source.read(r);
            check(r.get(0) == 1 && r.get(3) == 4, "read 4 via SourceChannel");
            check(source.validOps() == SelectionKey.OP_READ, "validOps OP_READ only");
            // scattering read
            pipe.sink().write(ByteBuffer.wrap(new byte[] {9, 8}));
            ByteBuffer x = ByteBuffer.allocate(1);
            ByteBuffer y = ByteBuffer.allocate(1);
            long n = source.read(new ByteBuffer[] {x, y});
            check(n == 2, "scattered 2 bytes");
            check(x.get(0) == 9 && y.get(0) == 8, "scatter order");
            // non-blocking + EOF semantics
            source.configureBlocking(false);
            check(source.read(ByteBuffer.allocate(1)) == 0, "empty non-blocking read 0");
            pipe.sink().close();
            check(source.read(ByteBuffer.allocate(1)) == -1, "EOF after sink close");
            check(source.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
        } finally {
            try { pipe.sink().close(); } catch (Exception ignore) { }
            try { pipe.source().close(); } catch (Exception ignore) { }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

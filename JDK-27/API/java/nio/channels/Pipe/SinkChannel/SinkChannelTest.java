/*
 * @test
 * @summary java.nio.channels.Pipe$SinkChannel conformance: white-box structure + black-box behavior.
 * @run main SinkChannelTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;

public class SinkChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe$SinkChannel");
        check(Modifier.isAbstract(c.getModifiers()), "SinkChannel is abstract");
        check(c.getDeclaredConstructors().length > 0, "has declared ctors");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(c),
              "implements WritableByteChannel");
        check(java.nio.channels.GatheringByteChannel.class.isAssignableFrom(c),
              "implements GatheringByteChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SinkChannel sink = pipe.sink();
            // write through the abstract SinkChannel type
            ByteBuffer w = ByteBuffer.wrap(new byte[] {7, 8, 9});
            int total = 0;
            while (w.hasRemaining()) total += sink.write(w);
            check(total == 3, "wrote 3 bytes via SinkChannel");
            check(sink.validOps() == SelectionKey.OP_WRITE, "validOps OP_WRITE only");
            check(!sink.isRegistered(), "not registered");
            // scattering write via the GatheringByteChannel face
            ByteBuffer a = ByteBuffer.allocate(1);
            ByteBuffer b = ByteBuffer.allocate(1);
            a.put((byte) 10).flip();
            b.put((byte) 20).flip();
            long n = sink.write(new ByteBuffer[] {a, b});
            check(n == 2, "gathered 2 bytes");
            // drain from source to verify order
            ByteBuffer all = ByteBuffer.allocate(5);
            while (all.hasRemaining()) pipe.source().read(all);
            check(all.get(0) == 7 && all.get(2) == 9 && all.get(3) == 10 && all.get(4) == 20,
                  "FIFO order across writes");
            // provider wiring
            check(sink.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            // blocking mode flips
            sink.configureBlocking(false);
            check(!sink.isBlocking(), "non-blocking sink");
            sink.configureBlocking(true);
            check(sink.isBlocking(), "blocking sink");
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

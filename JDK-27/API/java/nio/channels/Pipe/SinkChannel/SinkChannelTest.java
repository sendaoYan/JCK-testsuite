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

    // ===================== A6 deepening: write contracts / register / close =====================

    /** Negative-path helper: asserts the EXACT exception class (no subclasses). */
    /** Throwing action so lambdas may call methods that declare checked IOException. */
    interface Op { void run() throws Exception; }

    static void checkThrows(Class<? extends Throwable> ex, Op r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t.getClass() != ex)
                throw new AssertionError(msg + ": expected " + ex.getName() + " got " + t);
            passed++;
            return;
        }
        throw new AssertionError(msg + ": expected " + ex.getName() + " but nothing thrown");
    }

    static void testWriteContracts() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SinkChannel sink = pipe.sink();
            check(sink.isOpen(), "open");
            check(sink.validOps() == SelectionKey.OP_WRITE, "validOps is exactly OP_WRITE");
            check((sink.validOps() & SelectionKey.OP_READ) == 0, "sink can never be selected for read");
            check(sink.write(ByteBuffer.wrap(new byte[] {0x41, 0x42})) == 2, "write 2 exact");
            check(sink.write(ByteBuffer.wrap(new byte[] {0x43})) == 1, "second write 1");
            ByteBuffer all = ByteBuffer.allocate(3);
            while (all.hasRemaining()) check(pipe.source().read(all) > 0, "source drains");
            check(all.get(0) == 0x41 && all.get(1) == 0x42 && all.get(2) == 0x43, "FIFO order");
            sink.configureBlocking(false);
            check(!sink.isBlocking(), "non-blocking");
            ByteBuffer w = ByteBuffer.wrap(new byte[] {9, 9, 9});
            while (w.hasRemaining()) check(sink.write(w) >= 0, "non-blocking write progresses");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testRegisterAndClose() throws Exception {
        Pipe pipe = Pipe.open();
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            Pipe.SinkChannel sink = pipe.sink();
            checkThrows(java.nio.channels.IllegalBlockingModeException.class,
                        () -> sink.register(sel, SelectionKey.OP_WRITE),
                        "register while blocking -> IllegalBlockingModeException");
            sink.configureBlocking(false);
            SelectionKey key = sink.register(sel, SelectionKey.OP_WRITE);
            check(key.isValid(), "registered non-blocking sink");
            check(sel.select(5_000) == 1, "sink writable once registered");
            check(key.isWritable(), "key isWritable");
            checkThrows(IllegalArgumentException.class,
                        () -> sink.register(sel, SelectionKey.OP_READ),
                        "sink register OP_READ -> IllegalArgumentException");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
        Pipe p2 = Pipe.open();
        Pipe.SinkChannel closed = p2.sink();
        closed.close();
        check(!closed.isOpen(), "closed sink not open");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> closed.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        check(closed.validOps() == SelectionKey.OP_WRITE, "validOps still queryable after close");
        p2.source().close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testWriteContracts();
        testRegisterAndClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

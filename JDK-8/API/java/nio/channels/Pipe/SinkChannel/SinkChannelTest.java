/*
 * @test
 * @summary java.nio.channels.Pipe.SinkChannel conformance: open/write/validOps.
 * @run main SinkChannelTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class SinkChannelTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

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

    // ===================== A6 deepening: type hierarchy / write contracts / close =====================

    static void testTypeHierarchy() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe$SinkChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SinkChannel is abstract");
        boolean prot = true;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            prot &= java.lang.reflect.Modifier.isProtected(ct.getModifiers());
        check(prot, "SinkChannel constructors are protected");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.SelectableChannel.class.isAssignableFrom(c),
              "is a SelectableChannel");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(c),
              "implements WritableByteChannel");
        check(java.nio.channels.GatheringByteChannel.class.isAssignableFrom(c),
              "implements GatheringByteChannel");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "is a Channel");
        check(Pipe.SinkChannel.class == c, "nested class is Pipe.SinkChannel itself");
    }

    static void testWriteContracts() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SinkChannel sink = pipe.sink();
            check(sink.isOpen(), "open");
            check(sink.isBlocking(), "default blocking");
            check(sink.validOps() == SelectionKey.OP_WRITE, "validOps is exactly OP_WRITE");
            check((sink.validOps() & SelectionKey.OP_READ) == 0, "sink can never be selected for read");
            check(sink.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            // exact byte count, single write
            check(sink.write(ByteBuffer.wrap(new byte[] {0x21, 0x22})) == 2, "write 2 exact");
            // multiple writes keep FIFO order; verify through the source side
            check(sink.write(ByteBuffer.wrap(new byte[] {0x23})) == 1, "second write 1");
            ByteBuffer all = ByteBuffer.allocate(3);
            while (all.hasRemaining()) check(pipe.source().read(all) > 0, "source drains");
            check(all.get(0) == 0x21 && all.get(1) == 0x22 && all.get(2) == 0x23, "FIFO order");
            // gathering write through the GatheringByteChannel face
            ByteBuffer a = ByteBuffer.allocate(1);
            a.put((byte) 0x31).flip(); // Buffer.flip() returns Buffer before JDK-9: chain via statement
            ByteBuffer b = ByteBuffer.allocate(1);
            b.put((byte) 0x32).flip();
            check(sink.write(new ByteBuffer[] {a, b}) == 2, "gathering write 2");
            ByteBuffer two = ByteBuffer.allocate(2);
            while (two.hasRemaining()) check(pipe.source().read(two) > 0, "source drains gather");
            check(two.get(0) == 0x31 && two.get(1) == 0x32, "gather order preserved");
            // non-blocking small write completes without blocking
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
        try (Selector sel = Selector.open()) {
            Pipe.SinkChannel sink = pipe.sink();
            checkThrows(IllegalBlockingModeException.class,
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
        Pipe.SinkChannel closed = Pipe.open().sink();
        closed.close();
        check(!closed.isOpen(), "closed sink not open");
        checkThrows(ClosedChannelException.class,
                    () -> closed.write(ByteBuffer.wrap(new byte[] {1})),
                    "write after close -> ClosedChannelException");
        check(closed.validOps() == SelectionKey.OP_WRITE, "validOps still queryable after close");
    }

    public static void main(String[] args) throws Exception {
        testSinkChannel();
        testTypeHierarchy();
        testWriteContracts();
        testRegisterAndClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

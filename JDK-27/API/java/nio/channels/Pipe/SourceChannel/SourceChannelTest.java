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

    // ===================== A6 deepening: read contracts / register / close / EOF =====================

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

    static void testReadContracts() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SourceChannel source = pipe.source();
            check(source.isOpen(), "open");
            check(source.validOps() == SelectionKey.OP_READ, "validOps is exactly OP_READ");
            check((source.validOps() & SelectionKey.OP_WRITE) == 0,
                  "source can never be selected for write");
            pipe.sink().write(ByteBuffer.wrap(new byte[] {0x71, 0x72, 0x73}));
            ByteBuffer r = ByteBuffer.allocate(3);
            while (r.hasRemaining()) check(source.read(r) > 0, "read delivers bytes");
            check(r.get(0) == 0x71 && r.get(1) == 0x72 && r.get(2) == 0x73, "content exact");
            // scattering read keeps FIFO order across buffers
            pipe.sink().write(ByteBuffer.wrap(new byte[] {(byte) 0x81, (byte) 0x82,
                                                           (byte) 0x83, (byte) 0x84}));
            ByteBuffer x = ByteBuffer.allocate(3);
            ByteBuffer y = ByteBuffer.allocate(1);
            while (x.hasRemaining() || y.hasRemaining())
                source.read(new ByteBuffer[] {x, y});
            check(x.get(0) == (byte) 0x81 && x.get(1) == (byte) 0x82
                      && x.get(2) == (byte) 0x83 && y.get(0) == (byte) 0x84,
                  "scatter order preserved");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testRegisterAndClose() throws Exception {
        Pipe pipe = Pipe.open();
        try (java.nio.channels.Selector sel = java.nio.channels.Selector.open()) {
            Pipe.SourceChannel source = pipe.source();
            source.configureBlocking(false);
            SelectionKey key = source.register(sel, SelectionKey.OP_READ);
            check(key.isValid(), "registered non-blocking source");
            check(sel.selectNow() == 0, "not ready before any write");
            pipe.sink().write(ByteBuffer.wrap(new byte[] {7}));
            check(sel.select(5_000) == 1, "source readable after write");
            check(key.isReadable(), "key isReadable");
            checkThrows(IllegalArgumentException.class,
                        () -> source.register(sel, SelectionKey.OP_WRITE),
                        "source register OP_WRITE -> IllegalArgumentException");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
        Pipe p2 = Pipe.open();
        Pipe.SourceChannel closed = p2.source();
        closed.close();
        check(!closed.isOpen(), "closed source not open");
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> closed.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        check(closed.validOps() == SelectionKey.OP_READ, "validOps still queryable after close");
        p2.sink().close();
    }

    static void testEofAfterSinkClose() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.sink().write(ByteBuffer.wrap(new byte[] {3, 4}));
            ByteBuffer r = ByteBuffer.allocate(2);
            while (r.hasRemaining()) check(pipe.source().read(r) > 0, "drain buffered bytes");
            pipe.sink().close();
            check(pipe.source().read(ByteBuffer.allocate(1)) == -1,
                  "EOF after sink closed and drained");
            check(pipe.source().read(ByteBuffer.allocate(1)) == -1, "EOF sticky");
        } finally {
            pipe.source().close();
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReadContracts();
        testRegisterAndClose();
        testEofAfterSinkClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

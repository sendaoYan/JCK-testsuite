/*
 * @test
 * @summary java.nio.channels.Pipe.SourceChannel conformance: open/read/validOps.
 * @run main SourceChannelTest
 */

import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.IllegalBlockingModeException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class SourceChannelTest {
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

    // ===================== A6 deepening: type hierarchy / read contracts / close / EOF =====================

    static void testTypeHierarchy() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe$SourceChannel");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "SourceChannel is abstract");
        boolean prot = true;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            prot &= java.lang.reflect.Modifier.isProtected(ct.getModifiers());
        check(prot, "SourceChannel constructors are protected");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(c),
              "extends AbstractSelectableChannel");
        check(java.nio.channels.SelectableChannel.class.isAssignableFrom(c),
              "is a SelectableChannel");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(c),
              "implements ReadableByteChannel");
        check(java.nio.channels.ScatteringByteChannel.class.isAssignableFrom(c),
              "implements ScatteringByteChannel");
        check(java.nio.channels.Channel.class.isAssignableFrom(c), "is a Channel");
        check(Pipe.SourceChannel.class == c, "nested class is Pipe.SourceChannel itself");
    }

    static void testReadContracts() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SourceChannel source = pipe.source();
            check(source.isOpen(), "open");
            check(source.isBlocking(), "default blocking");
            check(source.validOps() == SelectionKey.OP_READ, "validOps is exactly OP_READ");
            check((source.validOps() & SelectionKey.OP_WRITE) == 0,
                  "source can never be selected for write");
            check(source.provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "default provider");
            // exact byte count and content, single read
            pipe.sink().write(ByteBuffer.wrap(new byte[] {0x51, 0x52, 0x53}));
            ByteBuffer r = ByteBuffer.allocate(3);
            while (r.hasRemaining()) check(source.read(r) > 0, "read delivers bytes");
            check(r.get(0) == 0x51 && r.get(1) == 0x52 && r.get(2) == 0x53, "content exact");
            // scattering read keeps FIFO order across buffers
            pipe.sink().write(ByteBuffer.wrap(new byte[] {0x61, 0x62, 0x63, 0x64}));
            ByteBuffer x = ByteBuffer.allocate(3);
            ByteBuffer y = ByteBuffer.allocate(1);
            while (x.hasRemaining() || y.hasRemaining())
                source.read(new ByteBuffer[] {x, y});
            check(x.get(0) == 0x61 && x.get(1) == 0x62 && x.get(2) == 0x63 && y.get(0) == 0x64,
                  "scatter order preserved");
            // non-blocking read with no data returns 0 (never blocks)
            source.configureBlocking(false);
            check(!source.isBlocking(), "non-blocking");
            check(source.read(ByteBuffer.allocate(2)) == 0, "empty non-blocking read is 0");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testRegisterAndClose() throws Exception {
        Pipe pipe = Pipe.open();
        try (Selector sel = Selector.open()) {
            Pipe.SourceChannel source = pipe.source();
            checkThrows(IllegalBlockingModeException.class,
                        () -> source.register(sel, SelectionKey.OP_READ),
                        "register while blocking -> IllegalBlockingModeException");
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
        Pipe pipe2 = Pipe.open();
        Pipe.SourceChannel closed = pipe2.source();
        closed.close();
        check(!closed.isOpen(), "closed source not open");
        checkThrows(ClosedChannelException.class, () -> closed.read(ByteBuffer.allocate(1)),
                    "read after close -> ClosedChannelException");
        check(closed.validOps() == SelectionKey.OP_READ, "validOps still queryable after close");
        pipe2.sink().close();
    }

    static void testEofAfterSinkClose() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.sink().write(ByteBuffer.wrap(new byte[] {1, 2}));
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
        testSourceChannel();
        testTypeHierarchy();
        testReadContracts();
        testRegisterAndClose();
        testEofAfterSinkClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

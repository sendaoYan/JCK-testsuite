/*
 * @test
 * @summary java.nio.channels.Pipe conformance: open/sink/source round-trip.
 * @run main PipeTest
 */

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class PipeTest {
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

    // ===================== A6 deepening: FIFO semantics, EOF/broken pipe, selector integration =====================

    static void testWhiteBoxStructure() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "Pipe is abstract");
        boolean prot = true;
        for (java.lang.reflect.Constructor<?> ct : c.getDeclaredConstructors())
            prot &= java.lang.reflect.Modifier.isProtected(ct.getModifiers());
        check(prot, "Pipe constructors are protected");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        for (String n : new String[] {"source", "sink", "open"})
            check(names.contains(n), "has method " + n);
        Class<?> src = Class.forName("java.nio.channels.Pipe$SourceChannel");
        Class<?> snk = Class.forName("java.nio.channels.Pipe$SinkChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(src),
              "SourceChannel extends AbstractSelectableChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(snk),
              "SinkChannel extends AbstractSelectableChannel");
        check(java.nio.channels.ReadableByteChannel.class.isAssignableFrom(src),
              "SourceChannel implements ReadableByteChannel");
        check(java.nio.channels.WritableByteChannel.class.isAssignableFrom(snk),
              "SinkChannel implements WritableByteChannel");
    }

    static void testEndsBasics() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            check(pipe.sink() != null && pipe.source() != null, "both ends non-null");
            check(pipe.sink() == pipe.sink(), "sink() returns the same object each call");
            check(pipe.source() == pipe.source(), "source() returns the same object each call");
            check(pipe.sink().isOpen() && pipe.source().isOpen(), "both ends open");
            check(pipe.sink().validOps() == SelectionKey.OP_WRITE, "sink validOps == OP_WRITE");
            check(pipe.source().validOps() == SelectionKey.OP_READ, "source validOps == OP_READ");
            check(pipe.sink().provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "sink default provider");
            check(pipe.source().provider() == java.nio.channels.spi.SelectorProvider.provider(),
                  "source default provider");
            check(pipe.sink().isBlocking() && pipe.source().isBlocking(),
                  "both ends default to blocking");
            check(pipe.sink().configureBlocking(false) == pipe.sink(),
                  "sink configureBlocking returns this");
            check(pipe.source().configureBlocking(false) == pipe.source(),
                  "source configureBlocking returns this");
            check(!pipe.sink().isBlocking() && !pipe.source().isBlocking(),
                  "both ends non-blocking now");
            check(pipe.sink().toString() != null && pipe.source().toString() != null,
                  "toString non-null on both ends");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testFifoRoundTripExact() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            byte[] payload = new byte[16];
            for (int i = 0; i < payload.length; i++) payload[i] = (byte) (0xF0 + i);
            ByteBuffer w = ByteBuffer.wrap(payload);
            long total = 0;
            while (w.hasRemaining()) total += pipe.sink().write(w);
            check(total == 16, "wrote all 16 bytes");
            ByteBuffer r = ByteBuffer.allocate(16);
            while (r.hasRemaining()) {
                int n = pipe.source().read(r);
                check(n > 0, "source delivered bytes");
            }
            boolean same = true;
            for (int i = 0; i < 16; i++)
                if (r.get(i) != payload[i]) { same = false; break; }
            check(same, "FIFO content byte-exact");
            // gathering write / scattering read keep the order
            ByteBuffer a = ByteBuffer.allocate(2);
            ByteBuffer b = ByteBuffer.allocate(2);
            a.put(new byte[] {0x11, 0x12}).flip();
            b.put(new byte[] {0x13, 0x14}).flip();
            check(pipe.sink().write(new ByteBuffer[] {a, b}) == 4, "gathering write 4");
            ByteBuffer x = ByteBuffer.allocate(2);
            ByteBuffer y = ByteBuffer.allocate(2);
            while (x.hasRemaining() || y.hasRemaining())
                pipe.source().read(new ByteBuffer[] {x, y});
            check(x.get(0) == 0x11 && x.get(1) == 0x12 && y.get(0) == 0x13 && y.get(1) == 0x14,
                  "scattering read order preserved");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testNonBlockingSemantics() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.source().configureBlocking(false);
            check(pipe.source().read(ByteBuffer.allocate(4)) == 0,
                  "non-blocking source read with no data returns 0");
            pipe.sink().configureBlocking(false);
            ByteBuffer w = ByteBuffer.wrap(new byte[] {1, 2, 3});
            int n = pipe.sink().write(w);
            check(n == 3 && !w.hasRemaining(), "non-blocking sink write of a small buffer completes");
            ByteBuffer r = ByteBuffer.allocate(3);
            while (r.hasRemaining())
                check(pipe.source().read(r) >= 0, "non-blocking source drains the data");
            check(r.get(0) == 1 && r.get(2) == 3, "non-blocking round trip exact");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testSelectorIntegration() throws Exception {
        Pipe pipe = Pipe.open();
        try (Selector sel = Selector.open()) {
            pipe.source().configureBlocking(false);
            pipe.sink().configureBlocking(false);
            SelectionKey srcKey = pipe.source().register(sel, SelectionKey.OP_READ);
            check(sel.selectNow() == 0, "source not ready before any write");
            pipe.sink().write(ByteBuffer.wrap(new byte[] {(byte) 0x77}));
            check(sel.select(5_000) == 1, "source readable after write");
            check(srcKey.readyOps() == SelectionKey.OP_READ, "source readyOps == OP_READ");
            check(srcKey.isReadable(), "source key isReadable");
            ByteBuffer r = ByteBuffer.allocate(1);
            check(pipe.source().read(r) == 1 && r.get(0) == (byte) 0x77, "drain via selected source");
            sel.selectedKeys().clear();
            check(sel.selectNow() == 0, "no more readiness after draining");
            // a sink with room is immediately writable
            SelectionKey snkKey = pipe.sink().register(sel, SelectionKey.OP_WRITE);
            check(sel.select(5_000) == 1, "sink writable");
            check(snkKey.readyOps() == SelectionKey.OP_WRITE, "sink readyOps == OP_WRITE");
            check(snkKey.isWritable(), "sink key isWritable");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testSinkCloseEof() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.sink().write(ByteBuffer.wrap(new byte[] {5, 6, 7}));
            ByteBuffer r = ByteBuffer.allocate(3);
            while (r.hasRemaining()) check(pipe.source().read(r) > 0, "buffered bytes readable");
            pipe.sink().close();
            check(!pipe.sink().isOpen(), "sink closed");
            check(pipe.source().isOpen(), "source still open");
            check(pipe.source().read(ByteBuffer.allocate(1)) == -1,
                  "source reads EOF after sink closed and drained");
            check(pipe.source().read(ByteBuffer.allocate(1)) == -1, "EOF is sticky");
        } finally {
            pipe.source().close();
        }
    }

    static void testSourceCloseBrokenPipe() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.source().close();
            check(!pipe.source().isOpen(), "source closed");
            check(pipe.sink().isOpen(), "sink still open");
            // Linux contract: writing into a pipe whose read end is gone fails with
            // a plain IOException ("Broken pipe"); first small writes may sit in the
            // pipe buffer, so keep writing until the error surfaces (bounded).
            int writes = 0;
            Exception broken = null;
            try {
                while (writes < 4096) {
                    pipe.sink().write(ByteBuffer.allocate(64 * 1024));
                    writes++;
                }
            } catch (IOException e) {
                broken = e;
            }
            check(broken != null, "writing after source close eventually fails");
            check(broken.getClass() == IOException.class,
                  "broken pipe is a plain IOException, got " + broken);
        } finally {
            pipe.sink().close();
        }
    }

    static void testClosedEndOperations() throws Exception {
        Pipe pipe = Pipe.open();
        pipe.sink().close();
        checkThrows(ClosedChannelException.class,
                    () -> pipe.sink().write(ByteBuffer.wrap(new byte[] {1})),
                    "write after sink close -> ClosedChannelException");
        pipe.source().close();
        checkThrows(ClosedChannelException.class, () -> pipe.source().read(ByteBuffer.allocate(1)),
                    "read after source close -> ClosedChannelException");
        Pipe p2 = Pipe.open();
        p2.source().close();
        p2.sink().close();
        check(!p2.sink().isOpen() && !p2.source().isOpen(), "double close idempotent");
    }

    public static void main(String[] args) throws Exception {
        testPipe();
        testWhiteBoxStructure();
        testEndsBasics();
        testFifoRoundTripExact();
        testNonBlockingSemantics();
        testSelectorIntegration();
        testSinkCloseEof();
        testSourceCloseBrokenPipe();
        testClosedEndOperations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

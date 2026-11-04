/*
 * @test
 * @summary java.nio.channels.Pipe conformance: white-box structure + black-box behavior.
 * @run main PipeTest
 */

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.channels.Pipe;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;

public class PipeTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.channels.Pipe");
        check(Modifier.isAbstract(c.getModifiers()), "Pipe is abstract");
        for (Constructor<?> ct : c.getDeclaredConstructors())
            check(Modifier.isProtected(ct.getModifiers()), "ctor protected");
        for (String n : new String[] {"source", "sink", "open"})
            check(java.util.Arrays.stream(c.getMethods()).anyMatch(m -> m.getName().equals(n)),
                  "has method " + n);
        Class<?> src = Class.forName("java.nio.channels.Pipe$SourceChannel");
        Class<?> snk = Class.forName("java.nio.channels.Pipe$SinkChannel");
        check(src.getDeclaredClasses().length == 0, "no nested in SourceChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(src),
              "SourceChannel extends AbstractSelectableChannel");
        check(java.nio.channels.spi.AbstractSelectableChannel.class.isAssignableFrom(snk),
              "SinkChannel extends AbstractSelectableChannel");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            Pipe.SinkChannel sink = pipe.sink();
            Pipe.SourceChannel source = pipe.source();
            check(sink != null && source != null, "ends obtained");
            check(sink.isOpen() && source.isOpen(), "both ends open");

            // validOps: sink is writable only, source readable only
            check(sink.validOps() == SelectionKey.OP_WRITE, "sink validOps OP_WRITE");
            check(source.validOps() == SelectionKey.OP_READ, "source validOps OP_READ");

            // write then read round trip
            byte[] payload = {11, 22, 33, 44};
            ByteBuffer w = ByteBuffer.wrap(payload);
            while (w.hasRemaining()) sink.write(w);
            ByteBuffer r = ByteBuffer.allocate(payload.length);
            while (r.hasRemaining()) {
                int n = source.read(r);
                check(n > 0, "pipe delivered bytes");
            }
            check(java.util.Arrays.equals(r.array(), payload), "pipe round trip content");

            // gathering write / scattering read
            ByteBuffer g1 = ByteBuffer.allocate(2);
            ByteBuffer g2 = ByteBuffer.allocate(2);
            g1.put(new byte[] {55, 56}).flip();
            g2.put(new byte[] {66, 67}).flip();
            long gw = sink.write(new ByteBuffer[] {g1, g2}, 0, 2);
            check(gw == 4, "gathering write 4 into sink");
            ByteBuffer s1 = ByteBuffer.allocate(2);
            ByteBuffer s2 = ByteBuffer.allocate(2);
            long sr = source.read(new ByteBuffer[] {s1, s2}, 0, 2);
            check(sr == 4, "scattering read 4 from source");
            check(s1.get(0) == 55 && s1.get(1) == 56, "scatter first half");
            check(s2.get(0) == 66 && s2.get(1) == 67, "scatter second half");

            // non-blocking read with no data returns 0
            source.configureBlocking(false);
            check(source.read(ByteBuffer.allocate(4)) == 0, "non-blocking empty read is 0");

            // selector integration: OP_READ ready after a write
            try (Selector sel = Selector.open()) {
                source.register(sel, SelectionKey.OP_READ);
                check(sel.selectNow() == 0, "no readiness before write");
                ByteBuffer m = ByteBuffer.wrap(new byte[] {1});
                while (m.hasRemaining()) sink.write(m);
                check(sel.select(5000) == 1, "OP_READ ready after write");
                ByteBuffer got = ByteBuffer.allocate(1);
                check(source.read(got) == 1 && got.get(0) == 1, "drain via selected channel");
            }

            // closing the sink makes the source read EOF
            sink.close();
            check(!sink.isOpen(), "sink closed");
            ByteBuffer eof = ByteBuffer.allocate(1);
            int n = source.read(eof); // source back to blocking mode
            check(n == -1, "source reads EOF after sink closed");
        } finally {
            try { pipe.sink().close(); } catch (Exception ignore) { }
            try { pipe.source().close(); } catch (Exception ignore) { }
        }
    }

    // ===================== A6 deepening: end identity / EOF-broken pipe / closed ends =====================

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

    static void testEndsIdentity() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            check(pipe.sink() == pipe.sink(), "sink() returns the same object each call");
            check(pipe.source() == pipe.source(), "source() returns the same object each call");
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

    static void testNonBlockingSemantics() throws Exception {
        Pipe pipe = Pipe.open();
        try {
            pipe.source().configureBlocking(false);
            check(pipe.source().read(ByteBuffer.allocate(4)) == 0,
                  "non-blocking source read with no data returns 0");
            pipe.sink().configureBlocking(false);
            ByteBuffer w = ByteBuffer.wrap(new byte[] {4, 5, 6});
            int n = pipe.sink().write(w);
            check(n == 3 && !w.hasRemaining(), "non-blocking sink write of a small buffer completes");
            ByteBuffer r = ByteBuffer.allocate(3);
            while (r.hasRemaining())
                check(pipe.source().read(r) >= 0, "non-blocking source drains the data");
            check(r.get(0) == 4 && r.get(2) == 6, "non-blocking round trip exact");
        } finally {
            pipe.sink().close();
            pipe.source().close();
        }
    }

    static void testSinkWritableReadiness() throws Exception {
        Pipe pipe = Pipe.open();
        try (Selector sel = Selector.open()) {
            pipe.sink().configureBlocking(false);
            SelectionKey snkKey = pipe.sink().register(sel, SelectionKey.OP_WRITE);
            check(sel.select(5_000) == 1, "sink with room is writable");
            check(snkKey.readyOps() == SelectionKey.OP_WRITE, "sink readyOps == OP_WRITE");
            check(snkKey.isWritable(), "sink key isWritable");
        } finally {
            pipe.sink().close();
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
            // a plain IOException ("Broken pipe"); bounded in case bytes land in buffers.
            int writes = 0;
            Exception broken = null;
            try {
                while (writes < 4096) {
                    pipe.sink().write(ByteBuffer.allocate(64 * 1024));
                    writes++;
                }
            } catch (java.io.IOException e) {
                broken = e;
            }
            check(broken != null, "writing after source close eventually fails");
            check(broken.getClass() == java.io.IOException.class,
                  "broken pipe is a plain IOException, got " + broken);
        } finally {
            pipe.sink().close();
        }
    }

    static void testClosedEndOperations() throws Exception {
        Pipe pipe = Pipe.open();
        pipe.sink().close();
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> pipe.sink().write(ByteBuffer.wrap(new byte[] {1})),
                    "write after sink close -> ClosedChannelException");
        pipe.source().close();
        checkThrows(java.nio.channels.ClosedChannelException.class,
                    () -> pipe.source().read(ByteBuffer.allocate(1)),
                    "read after source close -> ClosedChannelException");
        pipe.sink().close();
        pipe.source().close();
        check(!pipe.sink().isOpen() && !pipe.source().isOpen(), "double close idempotent");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testEndsIdentity();
        testNonBlockingSemantics();
        testSinkWritableReadiness();
        testSourceCloseBrokenPipe();
        testClosedEndOperations();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

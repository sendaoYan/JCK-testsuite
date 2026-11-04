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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

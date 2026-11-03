/*
 * @test
 * @summary java.io.PipedInputStream conformance: white-box structure + black-box
 *          behavior (both constructors and connect(), round-trip with a single
 *          deterministic writer thread, available() reporting, buffer wraparound past
 *          the pipe capacity, reader-closed write failure, writer-closed EOF,
 *          unconnected read failure).
 * @run main PipedInputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class PipedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PipedInputStream");
        check(c.getSuperclass().getName().equals("java.io.InputStream"), "extends InputStream");
        check(c.getConstructor().getModifiers() == Modifier.PUBLIC, "no-arg ctor");
        check(c.getConstructor(int.class) != null, "ctor (int pipeSize)");
        check(c.getConstructor(PipedOutputStream.class) != null, "ctor (PipedOutputStream)");
        check(c.getConstructor(PipedOutputStream.class, int.class) != null,
              "ctor (PipedOutputStream, int)");
        check(c.getMethod("connect", PipedOutputStream.class) != null, "connect method");
        check(Modifier.isProtected(c.getDeclaredField("buffer").getModifiers()), "protected buffer");
        check(Modifier.isProtected(c.getDeclaredField("in").getModifiers())
              && Modifier.isProtected(c.getDeclaredField("out").getModifiers()),
              "protected in/out cursors");
        check(Modifier.isStatic(c.getDeclaredField("PIPE_SIZE").getModifiers())
              && c.getDeclaredField("PIPE_SIZE").getType() == int.class,
              "protected static final int PIPE_SIZE");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. reading before connecting fails
        try {
            new PipedInputStream().read();
            check(false, "read before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. constructor wiring + deterministic round-trip: one daemon writer,
        //    the main thread drains it fully (writer closes to signal EOF)
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        Thread writer = new Thread(null, () -> {
            try {
                byte[] payload = new byte[600];
                for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
                out.write(payload);
                out.write('Z');
                out.flush();
                out.close();                          // signals EOF to the reader
            } catch (IOException ignored) { }
        }, "pis-writer", 0);
        writer.setDaemon(true);
        writer.start();
        int total = 0, last = -1, v;
        while ((v = in.read()) != -1) { total++; last = v; }
        writer.join(5000);
        check(total == 601, "all bytes delivered: " + total);
        check(last == 'Z', "final byte is the sentinel");
        check(!writer.isAlive(), "writer finished");

        // 3. available() reflects buffered bytes after a write
        PipedInputStream in3 = new PipedInputStream(1024);
        PipedOutputStream out3 = new PipedOutputStream();
        out3.connect(in3);
        out3.write(new byte[]{1, 2, 3});
        out3.flush();
        check(in3.available() == 3, "available counts 3");
        check(in3.read() == 1, "consume one");
        check(in3.available() == 2, "available now 2");

        // 4. the circular buffer wraps: write 5000 bytes (> default capacity) with
        //    a concurrent reader; no data lost or reordered
        PipedInputStream in4 = new PipedInputStream();
        PipedOutputStream out4 = new PipedOutputStream(in4);
        Thread w4 = new Thread(null, () -> {
            try {
                for (int i = 0; i < 5000; i++) out4.write(i & 0x7F);
                out4.close();
            } catch (IOException ignored) { }
        }, "pis-wrap", 0);
        w4.setDaemon(true);
        w4.start();
        boolean ordered = true;
        for (int i = 0; i < 5000; i++) {
            int b = in4.read();
            if (b != (i & 0x7F)) { ordered = false; break; }
        }
        check(ordered, "5000 bytes in exact order across buffer wraparound");
        check(in4.read() == -1, "EOF after writer closed");
        w4.join(5000);

        // 5. write after the reader closed fails with "Pipe closed"
        PipedInputStream in5 = new PipedInputStream();
        PipedOutputStream out5 = new PipedOutputStream(in5);
        in5.close();
        try {
            out5.write(1);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "failure message: " + ioe.getMessage());
        }

        // 6. read after the reader itself closed throws
        try {
            in5.read();
            check(false, "read after own close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. double connect is refused
        PipedInputStream in7 = new PipedInputStream();
        PipedOutputStream first = new PipedOutputStream(in7);
        try {
            in7.connect(new PipedOutputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }
        first.close();

        // 8. bulk read drains the pipe
        PipedInputStream in8 = new PipedInputStream();
        PipedOutputStream out8 = new PipedOutputStream(in8);
        Thread w8 = new Thread(null, () -> {
            try {
                out8.write(new byte[]{10, 20, 30, 40});
                out8.close();
            } catch (IOException ignored) { }
        }, "pis-bulk", 0);
        w8.setDaemon(true);
        w8.start();
        byte[] buf = new byte[4];
        int n = 0;
        while (n < 4) {
            int got = in8.read(buf, n, 4 - n);
            if (got < 0) break;
            n += got;
        }
        check(n == 4 && buf[0] == 10 && buf[3] == 40, "bulk read drained in order");
        check(in8.read() == -1, "EOF follows");
        w8.join(5000);
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

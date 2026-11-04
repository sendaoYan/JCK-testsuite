/*
 * @test
 * @summary java.io.PipedInputStream conformance: full contract coverage.
 *          connect(), round-trip with a single deterministic writer thread, available(),
 *          buffer wraparound past capacity, reader-closed write failure, writer-closed EOF,
 *          unconnected read failure, double connect refusal.
 * @run main PipedInputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        // reading before connecting fails
        try {
            new PipedInputStream().read();
            check(false, "read before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // constructor wiring + deterministic round-trip
        PipedInputStream in = new PipedInputStream();
        PipedOutputStream out = new PipedOutputStream(in);
        Thread writer = new Thread(null, () -> {
            try {
                byte[] payload = new byte[600];
                for (int i = 0; i < payload.length; i++) payload[i] = (byte) i;
                out.write(payload);
                out.write('Z');
                out.flush();
                out.close();
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

        // available() reflects buffered bytes
        PipedInputStream in3 = new PipedInputStream(1024);
        PipedOutputStream out3 = new PipedOutputStream();
        out3.connect(in3);
        out3.write(new byte[]{1, 2, 3});
        out3.flush();
        check(in3.available() == 3, "available counts 3");
        check(in3.read() == 1, "consume one");
        check(in3.available() == 2, "available now 2");

        // circular buffer wraps past capacity
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

        // write after the reader closed fails
        PipedInputStream in5 = new PipedInputStream();
        PipedOutputStream out5 = new PipedOutputStream(in5);
        in5.close();
        try {
            out5.write(1);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "failure reported");
        }

        // read after the reader itself closed throws
        try {
            in5.read();
            check(false, "read after own close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // double connect is refused
        PipedInputStream in7 = new PipedInputStream();
        PipedOutputStream first = new PipedOutputStream(in7);
        try {
            in7.connect(new PipedOutputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }
        first.close();

        // bulk read drains the pipe
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
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

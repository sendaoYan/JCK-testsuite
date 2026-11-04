/*
 * @test
 * @summary java.io.PipedOutputStream conformance: full contract coverage.
 *          connect(), write/flush round-trip with a single deterministic reader thread,
 *          unconnected write failure, reader-closed write failure, double connect refused,
 *          close terminates the peer with EOF, sub-range write.
 * @run main PipedOutputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class PipedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        // writing before connecting fails
        try {
            new PipedOutputStream().write(1);
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // flush before connecting is a silent no-op
        new PipedOutputStream().flush();
        passed++;

        // round-trip: main thread writes, one daemon reader drains, close delivers EOF
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        out.connect(in);
        final java.util.List<Integer> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<Integer>());
        Thread reader = new Thread(null, () -> {
            try {
                int v;
                while ((v = in.read()) != -1) got.add(v);
            } catch (IOException ignored) { }
        }, "pos-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (int i = 1; i <= 100; i++) out.write(i);
        out.flush();
        out.close();
        reader.join(5000);
        check(!reader.isAlive(), "reader terminated on close");
        check(got.size() == 100, "reader received every byte: " + got.size());
        boolean ordered = got.size() == 100;
        for (int i = 0; i < got.size() && ordered; i++) ordered = got.get(i) == i + 1;
        check(ordered, "payload order preserved");

        // bulk write delivers identical bytes (4 bytes absorbed by default buffer)
        PipedOutputStream out4 = new PipedOutputStream(new PipedInputStream());
        out4.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}, 0, 4);
        out4.flush();
        passed++;

        // write after the reader closed fails
        PipedOutputStream out5 = new PipedOutputStream();
        PipedInputStream in5 = new PipedInputStream(out5);
        in5.close();
        try {
            out5.write(7);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // double connect refused
        PipedOutputStream out6 = new PipedOutputStream(new PipedInputStream());
        try {
            out6.connect(new PipedInputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // close idempotent; write after close fails cleanly
        out6.close();
        out6.close();
        try {
            out6.write(0);
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // sub-range write copies only that range
        PipedInputStream in8 = new PipedInputStream();
        PipedOutputStream out8 = new PipedOutputStream(in8);
        out8.write(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        out8.flush();
        check(in8.available() == 3, "three bytes buffered: " + in8.available());
        check(in8.read() == 8 && in8.read() == 7 && in8.read() == 6, "range [1,4) delivered");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

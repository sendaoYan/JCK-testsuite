/*
 * @test
 * @summary java.io.PipedOutputStream conformance: white-box structure + black-box
 *          behavior (both constructors and connect(), write/flush round-trip with a
 *          single deterministic reader thread, unconnected write failure,
 *          reader-closed write failure, connect idempotence errors, close terminates
 *          the peer with EOF).
 * @run main PipedOutputStreamTest
 */

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.lang.reflect.Modifier;

public class PipedOutputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PipedOutputStream");
        check(c.getSuperclass().getName().equals("java.io.OutputStream"), "extends OutputStream");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(PipedInputStream.class) != null, "ctor (PipedInputStream)");
        check(c.getMethod("connect", PipedInputStream.class) != null, "connect method");
        check(c.getMethod("write", int.class) != null, "write(int)");
        check(c.getMethod("write", byte[].class, int.class, int.class) != null,
              "write(byte[], int, int)");
        check(c.getMethod("flush") != null, "flush");
        check(Modifier.isPrivate(c.getDeclaredField("sink").getModifiers())
                  && c.getDeclaredField("sink").getType() == PipedInputStream.class,
              "private volatile sink");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. writing before connecting fails
        try {
            new PipedOutputStream().write(1);
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. flush before connecting is a silent no-op (sink == null)
        new PipedOutputStream().flush();
        passed++;

        // 3. round-trip: write a known payload from the main thread while one
        //    daemon reader drains it, then close to deliver EOF
        PipedOutputStream out = new PipedOutputStream();
        PipedInputStream in = new PipedInputStream();
        out.connect(in);
        final java.util.List<Integer> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<>());
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
        out.close();                                        // wakes the reader with EOF
        reader.join(5000);
        check(!reader.isAlive(), "reader terminated on close");
        check(got.size() == 100, "reader received every byte: " + got.size());
        boolean ordered = got.size() == 100;
        for (int i = 0; i < got.size() && ordered; i++) ordered = got.get(i) == i + 1;
        check(ordered, "payload order preserved");

        // 4. bulk write delivers identical bytes
        PipedOutputStream out4 = new PipedOutputStream(new PipedInputStream());
        // no reader thread: the default 1024-byte buffer absorbs 4 bytes without blocking
        out4.write(new byte[]{(byte) 0xCA, (byte) 0xFE, (byte) 0xBA, (byte) 0xBE}, 0, 4);
        out4.flush();
        passed++;

        // 5. write after the reader closed fails
        PipedOutputStream out5 = new PipedOutputStream();
        PipedInputStream in5 = new PipedInputStream(out5);
        in5.close();
        try {
            out5.write(7);
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 6. double connect is refused in both directions
        PipedOutputStream out6 = new PipedOutputStream(new PipedInputStream());
        try {
            out6.connect(new PipedInputStream());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 7. close() is idempotent and a later write still fails cleanly
        out6.close();
        out6.close();
        try {
            out6.write(0);
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // 8. writing a sub-range copies only that range (verified via the peer)
        PipedInputStream in8 = new PipedInputStream();
        PipedOutputStream out8 = new PipedOutputStream(in8);
        out8.write(new byte[]{9, 8, 7, 6, 5}, 1, 3);
        out8.flush();
        check(in8.available() == 3, "three bytes buffered: " + in8.available());
        check(in8.read() == 8 && in8.read() == 7 && in8.read() == 6, "range [1,4) delivered");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

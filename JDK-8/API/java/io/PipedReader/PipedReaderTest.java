/*
 * @test
 * @summary java.io.PipedReader conformance: full contract coverage.
 *          connect(), round-trip with a single deterministic writer thread, ready()
 *          semantics, unconnected read failure, reader-closed read/write failures,
 *          writer-closed EOF, skip, small-buffer constructor, double connect refusal.
 * @run main PipedReaderTest
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class PipedReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        // reading before connecting fails
        try {
            new PipedReader().read();
            check(false, "read before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // deterministic round-trip
        PipedReader in = new PipedReader();
        PipedWriter out = new PipedWriter(in);
        Thread writer = new Thread(null, () -> {
            try {
                for (char ch = 'a'; ch <= 'z'; ch++) out.write(ch);
                out.flush();
                out.close();
            } catch (IOException ignored) { }
        }, "pr-writer", 0);
        writer.setDaemon(true);
        writer.start();
        StringBuilder sb = new StringBuilder();
        int v;
        while ((v = in.read()) != -1) sb.append((char) v);
        writer.join(5000);
        check("abcdefghijklmnopqrstuvwxyz".contentEquals(sb), "payload");
        check(!writer.isAlive(), "writer finished");

        // bulk read into a char[] region
        PipedReader in3 = new PipedReader(1024);
        PipedWriter out3 = new PipedWriter();
        out3.connect(in3);
        out3.write("hello".toCharArray(), 1, 3);          // "ell"
        out3.flush();
        char[] buf = new char[5];
        int n = in3.read(buf, 1, 3);
        check(n == 3, "three chars read: " + n);
        check(buf[1] == 'e' && buf[2] == 'l' && buf[3] == 'l' && buf[0] == 0 && buf[4] == 0,
              "region filled, untouched slots intact");

        // ready() is true exactly when data is buffered
        PipedReader in4 = new PipedReader();
        PipedWriter out4 = new PipedWriter(in4);
        check(!in4.ready(), "empty pipe is not ready");
        out4.write('x');
        out4.flush();
        check(in4.ready(), "buffered data makes it ready");
        check(in4.read() == 'x', "consume the char");
        check(!in4.ready(), "drained again");

        // writer closing delivers EOF
        PipedReader in5 = new PipedReader();
        PipedWriter out5 = new PipedWriter(in5);
        out5.write('Q');
        out5.close();
        check(in5.read() == 'Q', "byte before close");
        check(in5.read() == -1, "EOF after writer close");

        // closing the reader makes subsequent reads fail
        PipedReader in6 = new PipedReader();
        PipedWriter out6 = new PipedWriter(in6);
        in6.close();
        try {
            in6.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // writing into a closed reader fails from the writer side
        try {
            out6.write('z');
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "failure reported");
        }

        // double connect refused
        PipedReader in8 = new PipedReader();
        PipedWriter first = new PipedWriter(in8);
        try {
            in8.connect(new PipedWriter());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }
        first.close();

        // skip() forwards to the character stream
        PipedReader in9 = new PipedReader();
        PipedWriter out9 = new PipedWriter(in9);
        out9.write("abcdef");
        out9.close();
        check(in9.skip(2) == 2, "skip two chars");
        check(in9.read() == 'c', "read resumes after skip");

        // tiny pipe (size 1) round-trips once a reader is draining it
        PipedReader in10 = new PipedReader(1);
        PipedWriter out10 = new PipedWriter(in10);
        Thread w10 = new Thread(null, () -> {
            try {
                out10.write("AB");
                out10.close();
            } catch (IOException ignored) { }
        }, "pr-small", 0);
        w10.setDaemon(true);
        w10.start();
        check(in10.read() == 'A' && in10.read() == 'B' && in10.read() == -1,
              "size-1 pipe delivers both chars");
        w10.join(5000);
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

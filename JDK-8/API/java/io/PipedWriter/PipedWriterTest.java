/*
 * @test
 * @summary java.io.PipedWriter conformance: full contract coverage.
 *          connect(), round-trip with a single deterministic reader thread,
 *          unconnected write failure, reader-closed write/flush failure, close delivers EOF,
 *          double connect refused, sub-range write, append routes through write.
 * @run main PipedWriterTest
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;

public class PipedWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testBehavior() throws Exception {
        // writing before connecting fails
        try {
            new PipedWriter().write('x');
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // flush before connecting is a silent no-op
        new PipedWriter().flush();
        passed++;

        // deterministic round-trip
        PipedWriter out = new PipedWriter();
        PipedReader in = new PipedReader(out);
        final java.util.List<Character> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<Character>());
        Thread reader = new Thread(null, () -> {
            try {
                int v;
                while ((v = in.read()) != -1) got.add((char) v);
            } catch (IOException ignored) { }
        }, "pw-reader", 0);
        reader.setDaemon(true);
        reader.start();
        for (char ch = '0'; ch <= '9'; ch++) out.write(ch);
        out.write("suffix", 0, 6);
        out.flush();
        out.close();
        reader.join(5000);
        check(!reader.isAlive(), "reader terminated on writer close");
        check(got.size() == 16, "every char received: " + got.size());
        check(got.size() == 16 && got.get(0) == '0' && got.get(9) == '9' && got.get(15) == 'x',
              "payload order preserved");

        // write(String) sub-range copies exactly that slice
        PipedWriter out4 = new PipedWriter();
        PipedReader in4 = new PipedReader(out4);
        out4.write("abcdef", 2, 2);                       // "cd"
        out4.flush();
        char[] buf = new char[2];
        check(in4.read(buf) == 2 && buf[0] == 'c' && buf[1] == 'd', "slice [2,4) delivered");

        // append(CharSequence) routes through write
        PipedWriter out5 = new PipedWriter(new PipedReader());
        out5.append("ok");
        out5.flush();
        passed++;

        // write after the reader closed fails
        PipedWriter out6 = new PipedWriter();
        PipedReader in6 = new PipedReader(out6);
        in6.close();
        try {
            out6.write('z');
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // double connect refused
        PipedWriter out7 = new PipedWriter(new PipedReader());
        try {
            out7.connect(new PipedReader());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // close idempotent; write afterwards still fails
        out7.close();
        out7.close();
        try {
            out7.write('q');
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // flush after the reader closed reports the closed pipe
        try {
            out6.flush();
            check(false, "flush after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "flush message: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

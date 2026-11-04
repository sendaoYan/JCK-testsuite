/*
 * @test
 * @summary java.io.PipedWriter conformance: white-box structure + black-box behavior
 *          (both constructors and connect(), round-trip with a single deterministic
 *          reader thread, unconnected write failure, reader-closed write failure,
 *          close delivers EOF, double connect refused, append/flush inherited API).
 * @run main PipedWriterTest
 */

import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.lang.reflect.Modifier;

public class PipedWriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PipedWriter");
        check(c.getSuperclass().getName().equals("java.io.Writer"), "extends Writer");
        check(c.getConstructor() != null, "no-arg ctor");
        check(c.getConstructor(PipedReader.class) != null, "ctor (PipedReader)");
        check(c.getMethod("connect", PipedReader.class) != null, "connect method");
        check(c.getMethod("write", int.class) != null, "write(int)");
        check(c.getMethod("write", char[].class, int.class, int.class) != null,
              "write(char[], int, int)");
        check(c.getMethod("write", String.class, int.class, int.class) != null,
              "write(String, int, int)");
        check(c.getMethod("flush") != null, "flush");
        check(Modifier.isPrivate(c.getDeclaredField("sink").getModifiers())
                  && c.getDeclaredField("sink").getType() == PipedReader.class,
              "private sink");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. writing before connecting fails
        try {
            new PipedWriter().write('x');
            check(false, "write before connect must throw");
        } catch (IOException ioe) {
            check("Pipe not connected".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 2. flush before connecting is a silent no-op (sink == null)
        new PipedWriter().flush();
        passed++;

        // 3. deterministic round-trip: one daemon reader drains while the main
        //    thread writes; closing the writer ends the reader with EOF
        PipedWriter out = new PipedWriter();
        PipedReader in = new PipedReader(out);
        final java.util.List<Character> got = java.util.Collections.synchronizedList(
                new java.util.ArrayList<>());
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

        // 4. write(String) sub-range copies exactly that slice
        PipedWriter out4 = new PipedWriter();
        PipedReader in4 = new PipedReader(out4);
        out4.write("abcdef", 2, 2);                       // "cd"
        out4.flush();
        char[] buf = new char[2];
        check(in4.read(buf) == 2 && buf[0] == 'c' && buf[1] == 'd', "slice [2,4) delivered");

        // 5. append(CharSequence) routes through write
        PipedWriter out5 = new PipedWriter(new PipedReader());
        out5.append("ok");                                 // buffered, no reader needed
        out5.flush();
        passed++;

        // 6. write after the reader closed fails
        PipedWriter out6 = new PipedWriter();
        PipedReader in6 = new PipedReader(out6);
        in6.close();
        try {
            out6.write('z');
            check(false, "write after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. double connect refused
        PipedWriter out7 = new PipedWriter(new PipedReader());
        try {
            out7.connect(new PipedReader());
            check(false, "second connect must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 8. close is idempotent; writing afterwards still fails
        out7.close();
        out7.close();
        try {
            out7.write('q');
            check(false, "write after close must throw");
        } catch (IOException ioe) {
            passed++;
        }

        // 9. flush after the reader closed reports the closed pipe (JDK 27 raises
        //    "Pipe closed" from flush; an unconnected flush stays a silent no-op)
        try {
            out6.flush();
            check(false, "flush after reader close must throw");
        } catch (IOException ioe) {
            check("Pipe closed".equals(ioe.getMessage()), "flush message: " + ioe.getMessage());
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

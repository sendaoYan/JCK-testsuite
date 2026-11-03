/*
 * @test
 * @summary java.io.LineNumberInputStream conformance: white-box structure (deprecated
 *          byte-side line counting FilterInputStream) + black-box behavior (line
 *          counting over \n, \r, \r\n with read() collapsing the pair, bulk read,
 *          available adjustment for \r\n, setLineNumber/getLineNumber, mark/reset,
 *          skip).
 * @run main LineNumberInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.LineNumberInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class LineNumberInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static LineNumberInputStream ln(String s) {
        return new LineNumberInputStream(new ByteArrayInputStream(s.getBytes()));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.LineNumberInputStream");
        check(c.getSuperclass().getName().equals("java.io.FilterInputStream"),
              "extends FilterInputStream");
        check(c.getConstructor(java.io.InputStream.class) != null, "ctor (InputStream)");
        for (String m : new String[]{"read", "skip", "setLineNumber",
                                     "getLineNumber", "available", "mark", "reset"})
            check(has(c, m), "has method " + m);
        check(c.isAnnotationPresent(Deprecated.class), "@Deprecated");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. single-byte reads: \n, \r, \r\n each count one line; \r is delivered
        //    as '\n' and the \n of a \r\n pair is consumed silently
        LineNumberInputStream in = ln("a\nb\rc\r\nd");
        check(in.read() == 'a' && in.getLineNumber() == 0, "first char, line 0");
        check(in.read() == '\n' && in.getLineNumber() == 1, "\\n counted");
        check(in.read() == 'b' && in.getLineNumber() == 1, "still line 1");
        check(in.read() == '\n' && in.getLineNumber() == 2, "\\r translated to \\n and counted");
        check(in.read() == 'c' && in.getLineNumber() == 2, "c on line 2");
        check(in.read() == '\n' && in.getLineNumber() == 3, "\\r\\n collapsed and counted once");
        check(in.read() == 'd' && in.getLineNumber() == 3, "d on line 3");
        check(in.read() == -1, "EOF");

        // 2. bulk read counts lines, translates \r to \n and collapses \r\n to one byte
        in = ln("x\r\ny\n");
        byte[] buf = new byte[16];
        int n = in.read(buf, 0, 16);
        check(n == 4, "four bytes delivered (\\r\\n collapsed): " + n);
        check(buf[0] == 'x' && buf[1] == '\n' && buf[2] == 'y' && buf[3] == '\n',
              "both terminators delivered as \\n");
        check(in.getLineNumber() == 2, "two lines counted");

        // 3. available() hides the second byte of a pending \r\n pair
        in = ln("ab\r\n");
        check(in.available() == 2, "available counts 2 before the pair: " + in.available());
        in.read();
        in.read();
        check(in.available() == 1, "available reports 1: only the \\r (its \\n stays hidden)");
        in.read();                                      // consumes \r AND the paired \n
        check(in.available() == 0, "pair fully consumed");

        // 4. setLineNumber shifts the counter
        in = ln("q\n");
        in.setLineNumber(41);
        check(in.getLineNumber() == 41, "setLineNumber");
        in.read();
        in.read();
        check(in.getLineNumber() == 42, "count continues");

        // 5. mark/reset restores the line number
        in = ln("m\nn\no");
        in.read(); in.read();                           // m,\n -> line 1
        in.mark(64);
        in.read(); in.read();                           // n,\n -> line 2
        check(in.getLineNumber() == 2, "past the mark");
        in.reset();
        check(in.getLineNumber() == 1, "reset restored the line number");
        check(in.read() == 'n', "reset restored the position");

        // 6. skip crosses terminators and counts them
        in = ln("123\n45\n");
        in.skip(4);                                     // consumes "123\n"
        check(in.getLineNumber() == 1, "skip crossed one terminator");
        check(in.read() == '4', "position after skip");

        // 7. close is a pass-through: a ByteArrayInputStream tolerates close, so
        //    reads follow the wrapped stream's semantics (EOF, no exception)
        in = ln("q");
        in.close();                                     // pass-through close; BAIS ignores it
        in.close();
        check(in.read() == 'q', "unread data still served after close (wrapped BAIS is close-tolerant)");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

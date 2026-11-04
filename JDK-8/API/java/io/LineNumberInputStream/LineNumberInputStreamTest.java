/*
 * @test
 * @summary java.io.LineNumberInputStream conformance: full contract coverage.
 *          Line counting over \n, \r, \r\n with read() collapsing the pair, bulk read,
 *          available adjustment for \r\n, setLineNumber/getLineNumber, mark/reset, skip.
 * @run main LineNumberInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.LineNumberInputStream;

public class LineNumberInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static LineNumberInputStream ln(String s) {
        return new LineNumberInputStream(new ByteArrayInputStream(s.getBytes()));
    }

    static void testBehavior() throws Exception {
        LineNumberInputStream in = ln("a\nb\rc\r\nd");
        check(in.read() == 'a' && in.getLineNumber() == 0, "first char, line 0");
        check(in.read() == '\n' && in.getLineNumber() == 1, "\\n counted");
        check(in.read() == 'b' && in.getLineNumber() == 1, "still line 1");
        check(in.read() == '\n' && in.getLineNumber() == 2, "\\r translated to \\n and counted");
        check(in.read() == 'c' && in.getLineNumber() == 2, "c on line 2");
        check(in.read() == '\n' && in.getLineNumber() == 3, "\\r\\n collapsed and counted once");
        check(in.read() == 'd' && in.getLineNumber() == 3, "d on line 3");
        check(in.read() == -1, "EOF");

        in = ln("x\r\ny\n");
        byte[] buf = new byte[16];
        int n = in.read(buf, 0, 16);
        check(n == 4, "four bytes delivered (\\r\\n collapsed)");
        check(buf[0] == 'x' && buf[1] == '\n' && buf[2] == 'y' && buf[3] == '\n',
              "both terminators delivered as \\n");
        check(in.getLineNumber() == 2, "two lines counted");

        in = ln("ab\r\n");
        check(in.available() == 2, "available counts 2 before the pair");
        in.read();
        in.read();
        check(in.available() == 1, "available reports 1: only the \\r (its \\n stays hidden)");
        in.read();                                      // consumes \r AND the paired \n
        check(in.available() == 0, "pair fully consumed");

        in = ln("q\n");
        in.setLineNumber(41);
        check(in.getLineNumber() == 41, "setLineNumber");
        in.read();
        in.read();
        check(in.getLineNumber() == 42, "count continues");

        in = ln("m\nn\no");
        in.read(); in.read();                           // m,\n -> line 1
        in.mark(64);
        in.read(); in.read();                           // n,\n -> line 2
        check(in.getLineNumber() == 2, "past the mark");
        in.reset();
        check(in.getLineNumber() == 1, "reset restored the line number");
        check(in.read() == 'n', "reset restored the position");

        in = ln("123\n45\n");
        in.skip(4);                                     // consumes "123\n"
        check(in.getLineNumber() == 1, "skip crossed one terminator");
        check(in.read() == '4', "position after skip");

        in = ln("q");
        in.close();                                     // pass-through close; BAIS ignores it
        in.close();
        check(in.read() == 'q', "unread data still served after close (wrapped BAIS is close-tolerant)");
    }

    public static void main(String[] args) throws Exception {
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

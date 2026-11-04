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

    // ---- 深化追加（契约级），行为全部经 JDK-27 实测锁定 ----

    /** 底层第一次 read 正常、第二次抛 IOException 的源。 */
    static class OneThenBoom extends java.io.InputStream {
        boolean boomed;
        public int read() throws IOException {
            if (boomed) throw new IOException("mid-boom");
            boomed = true;
            return 'A';
        }
    }

    /** 构造 null 不抛（FilterInputStream 纯赋值），NPE 延迟到首次使用底层。 */
    static void testConstructorDeep() throws Exception {
        LineNumberInputStream nullIn = new LineNumberInputStream(null);
        boolean npe = false;
        try { nullIn.read(); } catch (NullPointerException expected) { npe = true; }
        check(npe, "read on null underlying -> NPE");
        LineNumberInputStream nullClose = new LineNumberInputStream(null);
        boolean npe2 = false;
        try { nullClose.close(); } catch (NullPointerException expected) { npe2 = true; }
        check(npe2, "close on null underlying -> NPE");
        check(ln("ab").markSupported(), "markSupported delegates to underlying BAIS (true)");
    }

    /** available 公式：(pushBack==-1) ? avail/2 : avail/2+1（javadoc 的 k/2 契约）。 */
    static void testAvailableFormula() throws Exception {
        LineNumberInputStream in = ln("a\rb");
        check(in.available() == 1, "initial available == 3/2 == 1");
        in.read();
        check(in.available() == 1, "after 'a': 2/2 == 1");
        in.read();                                       // '\r' → '\n'，pushBack='b'
        check(in.available() == 1, "pushback path: 0/2+1 == 1");
        check(in.read() == 'b', "pushed-back byte served next");
        check(in.available() == 0, "empty at end");

        LineNumberInputStream pair = ln("ab\r\n");
        check(pair.available() == 2, "4/2 == 2");
        pair.read();
        pair.read();
        check(pair.available() == 1, "remaining \\r\\n estimated as 1");
        pair.read();
        check(pair.available() == 0, "pair consumed, available 0");
    }

    /** bulk read 吞掉中途 IOException 返回部分计数；单字节 read() 透传异常。 */
    static void testIOExceptionSemantics() throws Exception {
        LineNumberInputStream bulk = new LineNumberInputStream(new OneThenBoom());
        byte[] buf = new byte[4];
        int n = bulk.read(buf, 0, 4);
        check(n == 1 && buf[0] == 'A',
              "bulk read swallows mid-stream IOException and returns partial count");
        LineNumberInputStream single = new LineNumberInputStream(new OneThenBoom());
        check(single.read() == 'A', "first read served");
        try {
            single.read();
            check(false, "single read() must propagate IOException");
        } catch (IOException expected) {
            passed++;
        }
    }

    /** read(b,off,len) 参数矩阵。 */
    static void testReadBounds() throws Exception {
        LineNumberInputStream in = ln("abcdef");
        byte[] buf = new byte[3];
        try { in.read(buf, 1, 3); check(false, "must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { in.read(buf, -1, 0); check(false, "must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { in.read(buf, 0, -1); check(false, "must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { in.read(null, 0, 1); check(false, "must throw"); }
        catch (NullPointerException expected) { passed++; }
        check(in.read(buf, 0, 0) == 0, "read(b,0,0) == 0");
        check(in.read(buf, 0, 3) == 3 && buf[0] == 'a' && buf[2] == 'c', "region read content");
    }

    /** skip：负数 0、跨 \r\n 计一行、纯 \r 计一行、越过 EOF 夹紧。 */
    static void testSkipDeep() throws Exception {
        LineNumberInputStream in = ln("1\r\n2\n");
        check(in.skip(-3) == 0 && in.skip(0) == 0, "negative/zero skip 0");
        check(in.skip(2) == 2, "skip(2) consumes merged \\r\\n");
        check(in.getLineNumber() == 1, "line 1 after crossing \\r\\n");
        check(in.read() == '2', "position after skip");
        check(in.skip(99) == 1, "skip past EOF clamps to remaining merged byte");
        check(in.getLineNumber() == 2, "second terminator counted by skip");

        LineNumberInputStream cr = ln("1\r2");
        check(cr.skip(2) == 2 && cr.getLineNumber() == 1, "lone \\r counted by skip");
        LineNumberInputStream empty = ln("");
        check(empty.skip(5) == 0, "empty source skip 0");
    }

    /** 行号计数深分支：负 setLineNumber、EOF 不加行、单独 \r 计行。 */
    static void testLineNumberDeep() throws Exception {
        LineNumberInputStream in = ln("ab");
        in.setLineNumber(-7);
        check(in.getLineNumber() == -7, "negative line number kept");
        in.read();
        in.read();
        check(in.getLineNumber() == -7, "no terminator read: line number unchanged");
        check(in.read() == -1, "EOF");
        check(in.getLineNumber() == -7, "EOF itself never increments");

        LineNumberInputStream cr = ln("a\r");
        check(cr.read() == 'a', "lead-in byte");
        check(cr.read() == '\n', "lone \\r delivered as \\n");
        check(cr.getLineNumber() == 1, "\\r at EOF counts a line");
        check(cr.read() == -1, "EOF after lone \\r");
    }

    /** mark/reset 恢复行号与 pushBack 状态。 */
    static void testMarkResetDeep() throws Exception {
        LineNumberInputStream in = ln("x\r\ny");
        check(in.read() == 'x', "x");
        check(in.read() == '\n' && in.getLineNumber() == 1, "merged pair consumed");
        in.mark(4);
        check(in.read() == 'y', "read past mark");
        in.reset();
        check(in.getLineNumber() == 1, "reset restores line number");
        check(in.read() == 'y', "reset restores position after the pair");

        LineNumberInputStream mid = ln("p\rq");
        mid.read();                                      // 'p'
        check(mid.read() == '\n', "\\r consumed");
        mid.mark(4);
        mid.read();                                      // 'q'
        mid.reset();
        check(mid.read() == 'q', "reset restores pending pushback byte");
    }

    /** 逐字节读 \r\n 的合并语义与空流矩阵。 */
    static void testByteByByteAndEmpty() throws Exception {
        LineNumberInputStream in = ln("1\r\n2");
        StringBuilder sb = new StringBuilder();
        int c;
        while ((c = in.read()) != -1) sb.append((char) c);
        check(sb.toString().equals("1\n2"), "byte-by-byte collapses pair to one \\n");
        check(in.getLineNumber() == 1, "one line counted");
        check(in.read() == -1, "repeated EOF reads stay -1");

        LineNumberInputStream empty = ln("");
        check(empty.read() == -1 && empty.available() == 0 && empty.skip(2) == 0,
              "empty source matrix");
        check(empty.getLineNumber() == 0, "empty source line 0");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testConstructorDeep();
        testAvailableFormula();
        testIOExceptionSemantics();
        testReadBounds();
        testSkipDeep();
        testLineNumberDeep();
        testMarkResetDeep();
        testByteByByteAndEmpty();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

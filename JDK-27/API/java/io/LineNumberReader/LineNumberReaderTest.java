/*
 * @test
 * @summary java.io.LineNumberReader conformance: white-box structure + black-box
 *          behavior (line counting across \n, \r and \r\n, read/read(char[])/readLine
 *          paths, setLineNumber/getLineNumber, mark/reset restores the line number,
 *          skip counts lines, terminator semantics on readLine, no terminator at EOF).
 *          Deepened (contract level): terminator matrix via read()/readLine(), bulk read
 *          delivers raw \r\n while still counting once, CRLF straddling a size-4 buffer
 *          counts once, EOF line-number semantics (JDK-27: read()/skip() to EOF DO count
 *          the final unterminated line - behavioral difference vs JDK-8), closed-state
 *          matrix, skip message ("skip() value is negative"), array bounds + ctor size.
 * @run main LineNumberReaderTest
 */

import java.io.IOException;
import java.io.LineNumberReader;
import java.io.StringReader;
import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;

public class LineNumberReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static LineNumberReader lnr(String s) {
        return new LineNumberReader(new StringReader(s));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.LineNumberReader");
        check(c.getSuperclass().getName().equals("java.io.BufferedReader"),
              "extends BufferedReader");
        check(c.getConstructor(java.io.Reader.class) != null, "ctor (Reader)");
        check(c.getConstructor(java.io.Reader.class, int.class) != null, "ctor (Reader, int)");
        for (String m : new String[]{"setLineNumber", "getLineNumber", "read",
                                     "readLine", "skip", "mark", "reset"})
            check(has(c, m), "has method " + m);
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. counting with read() over all three terminator forms; \r and \r\n
        //    are both delivered as a single '\n' read
        LineNumberReader r = lnr("a\nb\rc\r\nd");
        check(r.read() == 'a', "first char");
        check(r.getLineNumber() == 0, "line 0 before any terminator");
        check(r.read() == '\n', "\n delivered");
        check(r.getLineNumber() == 1, "\\n increments");
        check(r.read() == 'b', "b on line 1");
        check(r.read() == '\n', "\\r delivered as \n");
        check(r.getLineNumber() == 2, "\\r increments");
        check(r.read() == 'c', "c on line 2");
        check(r.read() == '\n', "\\r\\n collapsed to one \n");
        check(r.getLineNumber() == 3, "\\r\\n counts once");
        check(r.read() == 'd', "d on line 3");
        check(r.getLineNumber() == 3, "reading the char itself does not increment");
        check(r.read() == -1, "EOF");
        check(r.getLineNumber() == 4, "EOF terminates the unterminated last line");

        // 2. readLine: strips terminators, returns null at EOF
        r = lnr("one\r\ntwo\nthree");
        check("one".equals(r.readLine()), "first line");
        check(r.getLineNumber() == 1, "line after first readLine");
        check("two".equals(r.readLine()) && "three".equals(r.readLine()),
              "remaining lines");
        check(r.getLineNumber() == 3, "final line counted");
        check(r.readLine() == null, "null at EOF");

        // 3. bulk read counts lines too
        r = lnr("1\n2\n3\n");
        char[] buf = new char[64];
        r.read(buf);
        check(r.getLineNumber() == 3, "bulk read counted 3 lines");

        // 4. setLineNumber moves the counter arbitrarily (no repositioning)
        r = lnr("x\ny");
        r.setLineNumber(100);
        check(r.getLineNumber() == 100, "setLineNumber");
        r.readLine();
        check(r.getLineNumber() == 101, "counting continues from the set value");
        r.setLineNumber(-5);
        check(r.getLineNumber() == -5, "negative line numbers allowed");

        // 5. mark/reset restores the line number along with the position
        r = lnr("p\nq\nr");
        r.readLine();
        r.mark(100);
        r.readLine();
        check(r.getLineNumber() == 2, "advanced past the mark");
        r.reset();
        check(r.getLineNumber() == 1, "reset restored the line number");
        check("q".equals(r.readLine()), "reset restored the position");

        // 6. skip counts lines it crosses
        r = lnr("ab\ncd\nef");
        r.skip(3);                                       // consumes "ab\n"
        check(r.getLineNumber() == 1, "skip crossed one terminator");
        check(r.read() == 'c', "position after skip");

        // 7. empty input
        r = lnr("");
        check(r.readLine() == null && r.getLineNumber() == 0, "empty input");

        // 8. read() at EOF and close idempotence
        r = lnr("z");
        r.readLine();
        check(r.read() == -1, "EOF via read");
        r.close();
        r.close();
        try {
            r.readLine();
            check(false, "readLine after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }

        // 9. long lines exceeding the default buffer still count correctly
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 500; i++) sb.append('x');
        String longLine = sb.toString();
        r = lnr(longLine + "\n" + longLine + "\n");
        check(longLine.equals(r.readLine()), "long line 1");
        check(longLine.equals(r.readLine()), "long line 2");
        check(r.getLineNumber() == 2, "long lines counted");
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void checkMsg(Class<? extends Throwable> expected, String sub,
                         ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(sub)) {
                passed++;
                return;
            }
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " containing \"" + sub + "\" but got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepened: readLine terminator matrix ----
    static void testReadLineTerminators() throws Exception {
        LineNumberReader r = lnr("L1\nL2\rL3\r\nL4\r\rL5");
        check("L1".equals(r.readLine()), "readLine LF");
        check(r.getLineNumber() == 1, "LF counted");
        check("L2".equals(r.readLine()), "readLine CR");
        check(r.getLineNumber() == 2, "CR counted");
        check("L3".equals(r.readLine()), "readLine CRLF");
        check(r.getLineNumber() == 3, "CRLF counted once");
        check("L4".equals(r.readLine()), "line before CR CR");
        check("".equals(r.readLine()), "empty line from CR CR");
        check("L5".equals(r.readLine()), "line after CR CR");
        check(r.getLineNumber() == 6, "two CRs count two lines + tail counted by readLine");
        check(r.readLine() == null, "null at EOF");
        check(r.getLineNumber() == 6, "readLine null does not increment");

        // \r immediately at EOF
        LineNumberReader t = lnr("x\r");
        check("x".equals(t.readLine()), "CR at EOF terminates line");
        check(t.getLineNumber() == 1, "CR at EOF counted");
        check(t.readLine() == null, "EOF after CR");

        // CRLF only
        LineNumberReader c = lnr("\r\n\r\n");
        check("".equals(c.readLine()) && "".equals(c.readLine()), "CRLF-only empty lines");
        check(c.getLineNumber() == 2, "two CRLFs count two");
    }

    // ---- deepened: bulk read delivers RAW terminators but counts lines ----
    static void testBulkReadRawTerminators() throws Exception {
        LineNumberReader r = lnr("a\r\nb");
        char[] buf = new char[10];
        int n = r.read(buf);
        check(n == 4, "bulk read length 4 (raw \\r\\n kept)");
        check(buf[0] == 'a' && buf[1] == '\r' && buf[2] == '\n' && buf[3] == 'b',
              "bulk read passes \\r\\n through untranslated");
        check(r.getLineNumber() == 1, "but CRLF counted exactly once");

        LineNumberReader r2 = lnr("x\r\ny\r\n");
        char[] buf2 = new char[10];
        int n2 = r2.read(buf2);
        check(n2 == 6, "second bulk length");
        check(r2.getLineNumber() == 2, "two CRLFs counted in bulk read");

        // offset bulk read
        LineNumberReader r3 = lnr("x\ny\nz");
        char[] buf3 = new char[8];
        int n3 = r3.read(buf3, 2, 5);
        check(n3 == 5 && buf3[2] == 'x' && buf3[6] == 'z', "bulk read with offset placement");
        check(r3.getLineNumber() == 2, "offset bulk counted 2 lines");

        // bulk read shorter than buffer
        LineNumberReader r4 = lnr("p\nq");
        char[] buf4 = new char[100];
        int n4 = r4.read(buf4);
        check(n4 == 3, "partial fill length");
        check(r4.getLineNumber() == 1, "partial fill counted");

        // read(char[],off,len) len==0
        check(lnr("x").read(new char[1], 0, 0) == 0, "len==0 bulk returns 0");
    }

    // ---- deepened: CRLF straddling the buffer boundary counts once ----
    static void testStraddleBufferBoundary() throws Exception {
        // buffer 4: first fill "ab\r", second fill "\ncd..." -> CRLF split across refills
        LineNumberReader r = new LineNumberReader(new StringReader("ab\r\ncdefgh"), 4);
        check(r.read() == 'a' && r.read() == 'b', "prefix chars");
        check(r.read() == '\n', "\\r half of split CRLF delivered as \\n");
        check(r.getLineNumber() == 1, "split CRLF counted once after \\r");
        check(r.read() == 'c', "char right after split CRLF (LF consumed)");
        check(r.getLineNumber() == 1, "\\n half does not count a second line");
        check(r.read() == 'd', "next char");

        LineNumberReader r2 = new LineNumberReader(new StringReader("ab\r\ncd"), 4);
        check("ab".equals(r2.readLine()), "readLine across split CRLF");
        check(r2.getLineNumber() == 1, "one line for split CRLF");
        check("cd".equals(r2.readLine()), "second line after split CRLF");
        check(r2.getLineNumber() == 2, "second line counted");

        LineNumberReader r3 = new LineNumberReader(new StringReader("abc\r\ndef"), 4);
        check("abc".equals(r3.readLine()) && "def".equals(r3.readLine()),
              "readLine with \\r as last char of first fill");
        check(r3.getLineNumber() == 2, "both lines counted");

        LineNumberReader r4 = new LineNumberReader(new StringReader("aa\r\nbb\r\ncc"), 4);
        check("aa".equals(r4.readLine()) && "bb".equals(r4.readLine()) && "cc".equals(r4.readLine()),
              "all lines intact with split CRLFs");
        check(r4.getLineNumber() == 3, "three lines total");
    }

    // ---- deepened: EOF line-number semantics (JDK-27: final unterminated line IS counted) ----
    static void testEofLineCounting() throws Exception {
        LineNumberReader r = lnr("abc");          // no terminator at all
        while (r.read() != -1) { }
        check(r.getLineNumber() == 1, "JDK-27: read() to EOF counts unterminated tail");

        LineNumberReader r2 = lnr("abc\n");
        while (r2.read() != -1) { }
        check(r2.getLineNumber() == 1, "terminated single line counted once");

        LineNumberReader r3 = lnr("a\nb\rc\r\nd");
        while (r3.read() != -1) { }
        check(r3.getLineNumber() == 4, "three terminators + tail counted (JDK-27)");

        LineNumberReader r4 = lnr("a\nb\rc\r\nd");
        r4.skip(100);                              // skip drains to EOF
        check(r4.getLineNumber() == 4, "skip to EOF also counts the tail (JDK-27)");

        LineNumberReader r5 = lnr("abc");
        r5.readLine();                             // readLine counts the tail on both JDKs
        check(r5.getLineNumber() == 1, "readLine counts unterminated final line");
    }

    // ---- deepened: closed-state semantics ----
    static void testClosedSemantics() throws Exception {
        final LineNumberReader r = lnr("x");
        r.close();
        checkMsg(IOException.class, "Stream closed", () -> r.read(), "closed read()");
        checkMsg(IOException.class, "Stream closed", () -> r.readLine(), "closed readLine()");
        checkMsg(IOException.class, "Stream closed", () -> r.mark(1), "closed mark()");
        checkMsg(IOException.class, "Stream closed", () -> r.reset(), "closed reset()");
        checkMsg(IOException.class, "Stream closed", () -> r.skip(1), "closed skip()");
        // line number accessors remain functional after close
        r.setLineNumber(9);
        check(r.getLineNumber() == 9, "set/getLineNumber still work after close");
    }

    // ---- deepened: skip semantics ----
    static void testSkipSemantics() throws Exception {
        checkMsg(IllegalArgumentException.class, "skip() value is negative",
                 () -> lnr("x").skip(-1), "LNR-specific negative skip message");
        check(lnr("abc").skip(0) == 0, "skip(0) returns 0");

        LineNumberReader r = lnr("a\r\nb\r\nc");
        long skipped = r.skip(3);                  // consumes "a\r\n"
        check(skipped == 3, "skip over CRLF returns 3 chars");
        check(r.getLineNumber() == 1, "skip over CRLF counts one line");
        check(r.read() == 'b', "position after skip over CRLF");

        LineNumberReader r2 = lnr("abcdef");
        check(r2.skip(2) == 2 && r2.read() == 'c', "plain skip within line keeps count at 0");
        check(r2.getLineNumber() == 0, "no terminator crossed, no increment");
    }

    // ---- deepened: array bounds + constructor validation ----
    static void testArrayBoundsAndCtor() throws Exception {
        final LineNumberReader r = lnr("ab");
        checkThrows(NullPointerException.class, () -> r.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 1, Integer.MAX_VALUE),
                    "read(cbuf,1,MAX) overflow -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 0, 3),
                    "read(cbuf,0,3) -> IndexOutOfBoundsException");
        check(r.read() == 'a', "position intact after failed bound reads");

        checkThrows(IllegalArgumentException.class, () -> new LineNumberReader(new StringReader("x"), 0),
                    "size 0 -> IAE");
        checkMsg(IllegalArgumentException.class, "Buffer size <= 0",
                 () -> new LineNumberReader(new StringReader("x"), -3),
                 "negative size message");
        checkThrows(NullPointerException.class, () -> new LineNumberReader(null),
                    "LineNumberReader(null) -> NPE");
        check(lnr("x").markSupported(), "markSupported true (inherits BufferedReader)");

        final LineNumberReader un = lnr("x");
        checkMsg(IOException.class, "Stream not marked", () -> un.reset(), "reset unmarked");

        final LineNumberReader over = new LineNumberReader(new StringReader("abcdef"), 2);
        over.mark(1);
        while (over.read() != -1) { }
        checkMsg(IOException.class, "Mark invalid", () -> over.reset(),
                "reset after exceeding read-ahead limit");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReadLineTerminators();
        testBulkReadRawTerminators();
        testStraddleBufferBoundary();
        testEofLineCounting();
        testClosedSemantics();
        testSkipSemantics();
        testArrayBoundsAndCtor();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

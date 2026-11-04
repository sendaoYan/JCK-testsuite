/*
 * @test
 * @summary java.io.BufferedReader API conformance: full contract coverage.
 *          Constructors (null->NPE, size<=0->IAE), read()/EOF, read(char[],off,len)
 *          validation (NPE/IOBE) + len==0 + exact offset, readLine (LF/CRLF/trailing/EOF),
 *          skip (negative->IAE, past-end), ready, mark/reset (replay, unmarked->IOException,
 *          mark(-1)->IAE), close idempotent + closed->IOException, lines() stream.
 *          Deepened (contract level): readLine terminator matrix (LF/CR/CRLF/no trailing
 *          terminator/empty lines/empty source/mixed with read), cross-buffer long lines,
 *          mark-limit boundary (exact "Mark invalid" message), closed-state matrix
 *          ("Stream closed" on every op), lines() counting + UncheckedIOException when
 *          closed, read(char[],off,len) int-overflow, skip(0)/mark(0) edge semantics.
 * @run main BufferedReaderTest
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.List;
import java.util.stream.Collectors;

public class BufferedReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        checkThrows(NullPointerException.class, () -> new BufferedReader(null),
                    "BufferedReader(null) -> NPE");
        checkThrows(IllegalArgumentException.class, () -> new BufferedReader(new StringReader("x"), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> new BufferedReader(new StringReader("x"), -1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. read() single char ----
    static void testReadSingleChar() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("AB"));
        check(r.read() == 'A', "read first");
        check(r.read() == 'B', "read second");
        check(r.read() == -1, "read EOF");
        check(r.read() == -1, "read repeatedly EOF");
    }

    // ---- 3. read(char[],off,len) validation ----
    static void testReadArrayValidation() throws Exception {
        final BufferedReader r = new BufferedReader(new StringReader("hello"));
        checkThrows(NullPointerException.class, () -> r.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], -1, 1),
                    "read(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], 0, -1),
                    "read(c,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], 0, 3),
                    "read(c,0,3) -> IndexOutOfBoundsException");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0");

        char[] buf = new char[4];
        int n = r.read(buf, 1, 2);
        check(n == 2, "read count");
        check(buf[0] == 0 && buf[1] == 'h' && buf[2] == 'e' && buf[3] == 0, "exact offset placement");
    }

    // ---- 4. readLine ----
    static void testReadLine() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("one\ntwo\r\nthree\n"));
        check("one".equals(r.readLine()), "readLine LF");
        check("two".equals(r.readLine()), "readLine CRLF");
        check("three".equals(r.readLine()), "readLine trailing LF");
        check(r.readLine() == null, "readLine EOF null");
    }

    // ---- 5. skip ----
    static void testSkip() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("abcdef"));
        check(r.skip(2) == 2, "skip(2)");
        check(r.read() == 'c', "read after skip");
        check(r.skip(100) == 3, "skip past end returns remaining");
        check(r.read() == -1, "EOF after skip");

        final BufferedReader neg = new BufferedReader(new StringReader("abc"));
        checkThrows(IllegalArgumentException.class, () -> neg.skip(-1),
                    "negative skip -> IllegalArgumentException");
    }

    // ---- 6. ready ----
    static void testReady() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("abc"));
        check(r.ready(), "ready with data");
    }

    // ---- 7. mark/reset ----
    static void testMarkReset() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("abcd"));
        check(r.markSupported(), "markSupported");

        final BufferedReader unmarked = new BufferedReader(new StringReader("x"));
        checkThrows(IOException.class, () -> unmarked.reset(), "reset unmarked -> IOException");

        r.read();
        r.mark(100);
        r.read();
        r.read();
        r.reset();
        check(r.read() == 'b' && r.read() == 'c', "mark/reset replay");

        final BufferedReader neg = new BufferedReader(new StringReader("x"));
        checkThrows(IllegalArgumentException.class, () -> neg.mark(-1),
                    "mark(-1) -> IllegalArgumentException");
    }

    // ---- 8. close ----
    static void testClose() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("abc"));
        r.close();
        r.close();                      // idempotent
        checkThrows(IOException.class, () -> r.read(), "read after close -> IOException");
    }

    // ---- 9. lines() stream (Java 8) ----
    static void testLines() {
        BufferedReader r = new BufferedReader(new StringReader("a\nb\nc"));
        List<String> lines = r.lines().collect(Collectors.toList());
        check(lines.size() == 3 && lines.get(0).equals("a") && lines.get(1).equals("b")
              && lines.get(2).equals("c"), "lines() stream");
    }

    // ---- 10. readLine terminator matrix (deepened) ----
    static void testReadLineTerminatorMatrix() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("a\rb"));
        check("a".equals(r.readLine()), "readLine lone CR terminates");
        check("b".equals(r.readLine()), "readLine continues after lone CR");
        check(r.readLine() == null, "EOF null after lone CR");

        BufferedReader e = new BufferedReader(new StringReader("\n\na\n"));
        check("".equals(e.readLine()), "empty first line");
        check("".equals(e.readLine()), "consecutive empty lines");
        check("a".equals(e.readLine()), "content line after empties");
        check(e.readLine() == null, "null after trailing LF");

        BufferedReader z = new BufferedReader(new StringReader(""));
        check(z.readLine() == null, "empty source -> null");
        check(z.readLine() == null, "repeated readLine on empty stays null");

        BufferedReader t = new BufferedReader(new StringReader("tail"));
        check("tail".equals(t.readLine()), "no terminator at EOF still yields line");
        check(t.readLine() == null, "null at EOF");
        check(t.readLine() == null, "readLine repeatedly null at EOF");

        BufferedReader c = new BufferedReader(new StringReader("a\r\rb"));
        check("a".equals(c.readLine()), "CR CR: first line");
        check("".equals(c.readLine()), "CR CR: empty middle line");
        check("b".equals(c.readLine()), "CR CR: last line");

        BufferedReader n = new BufferedReader(new StringReader("\r\n\r\n"));
        check("".equals(n.readLine()), "CRLF only: first empty line");
        check("".equals(n.readLine()), "CRLF only: second empty line");
        check(n.readLine() == null, "CRLF only: EOF");
    }

    // ---- 11. readLine mixed with read(): position continuity ----
    static void testReadLineMixedWithRead() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("ab\ncde\nf"));
        check(r.read() == 'a', "single read advances one char");
        check("b".equals(r.readLine()), "readLine resumes from read position");
        check(r.read() == 'c', "read resumes after readLine's terminator");
        check("de".equals(r.readLine()), "second readLine after single read");
        check(r.read() == 'f', "final char after readLine");
        check(r.read() == -1, "EOF after mixed usage");
    }

    // ---- 12. lines longer than the internal buffer ----
    static void testCrossBufferLines() throws Exception {
        StringBuilder longLine = new StringBuilder();
        for (int i = 0; i < 23; i++) longLine.append((char) ('a' + i % 26));
        BufferedReader r = new BufferedReader(
            new StringReader(longLine + "\n" + "xy" + "\r" + "z"), 5);
        check(longLine.toString().equals(r.readLine()), "line longer than buffer (size 5)");
        check("xy".equals(r.readLine()), "short line after long line");
        check("z".equals(r.readLine()), "lone-CR terminated line after refill");
        check(r.readLine() == null, "EOF after cross-buffer lines");

        // terminator itself straddling the buffer boundary: buffer 4, "\r\n" split
        BufferedReader s = new BufferedReader(new StringReader("ab\r\ncd"), 4);
        check("ab".equals(s.readLine()), "CRLF split across buffer: line content");
        check("cd".equals(s.readLine()), "CRLF split across buffer: next line");
    }

    // ---- 13. mark limit boundary (exact messages) ----
    static void checkThrowsMsg(Class<? extends Throwable> expected, String sub,
                               ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(sub)) {
                passed++;
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " containing \"" + sub + "\" but got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " containing \"" + sub + "\" but nothing was thrown");
    }

    static void testMarkLimitBoundary() throws Exception {
        // read beyond readAheadLimit with a small buffer -> mark invalidated
        final BufferedReader over = new BufferedReader(new StringReader("abcdef"), 3);
        over.mark(2);
        while (over.read() != -1) { /* drain to force refill */ }
        checkThrowsMsg(IOException.class, "Mark invalid", () -> over.reset(),
                    "reset after exceeding read-ahead limit");

        // within the limit, reset still works even across refills
        BufferedReader in = new BufferedReader(new StringReader("abcdef"), 3);
        in.mark(5);
        check(in.read() == 'a' && in.read() == 'b' && in.read() == 'c' && in.read() == 'd',
              "read within limit across refill");
        in.reset();
        check(in.read() == 'a', "reset within limit replays");

        // mark(0) valid until any char is read past it
        BufferedReader zero = new BufferedReader(new StringReader("abcdef"), 3);
        zero.mark(0);
        zero.reset();
        check(zero.read() == 'a', "mark(0)+immediate reset replays");
        final BufferedReader zero2 = new BufferedReader(new StringReader("abcdef"), 3);
        zero2.mark(0);
        zero2.read();
        zero2.read();
        checkThrowsMsg(IOException.class, "Mark invalid", () -> zero2.reset(),
                    "reset after reading past mark(0)");

        // reset without any mark
        final BufferedReader un = new BufferedReader(new StringReader("x"));
        checkThrowsMsg(IOException.class, "Stream not marked", () -> un.reset(),
                    "reset without mark");

        checkThrowsMsg(IllegalArgumentException.class, "Read-ahead limit",
                    () -> new BufferedReader(new StringReader("x")).mark(-1),
                    "mark(-1) message");

        // mark/reset interplay with readLine
        BufferedReader lr = new BufferedReader(new StringReader("l1\nl2\nl3"));
        lr.readLine();
        lr.mark(100);
        lr.readLine();
        lr.reset();
        check("l2".equals(lr.readLine()), "reset replays a whole line");
        check("l3".equals(lr.readLine()), "line after replayed line");
    }

    // ---- 14. closed-state matrix (exact messages) ----
    static void testClosedStateMatrix() throws Exception {
        final BufferedReader r = new BufferedReader(new StringReader("abc"));
        r.close();
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.read(), "closed read()");
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.readLine(), "closed readLine()");
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.skip(1), "closed skip");
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.ready(), "closed ready()");
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.mark(1), "closed mark()");
        checkThrowsMsg(IOException.class, "Stream closed", () -> r.reset(), "closed reset()");
        checkThrowsMsg(IOException.class, "Stream closed",
                    () -> r.read(new char[2], 0, 1), "closed read(char[])");

        // lines() on a closed reader: terminal operation surfaces UncheckedIOException
        try {
            r.lines().count();
            check(false, "lines() on closed reader must throw");
        } catch (java.io.UncheckedIOException u) {
            Throwable cause = u.getCause();
            check(cause instanceof IOException && cause.getMessage() != null
                  && cause.getMessage().contains("Stream closed"),
                  "UncheckedIOException cause is Stream closed");
        }

        // skip(0) is legal while open, and negative skip has exact message
        BufferedReader open = new BufferedReader(new StringReader("abc"));
        check(open.skip(0) == 0, "skip(0) returns 0");
        checkThrowsMsg(IllegalArgumentException.class, "skip value is negative",
                    () -> open.skip(-1), "negative skip message");
        check(open.read() == 'a', "failed skip did not move position");
    }

    // ---- 15. read(char[],off,len) int overflow ----
    static void testReadArrayOverflow() throws Exception {
        final BufferedReader r = new BufferedReader(new StringReader("ab"));
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], 1, Integer.MAX_VALUE),
                    "read(cbuf,1,MAX) off+len overflow -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> r.read(new char[2], Integer.MAX_VALUE, 0),
                    "read(cbuf,MAX,0) off alone out of bounds");
        check(r.read() == 'a', "position intact after failed overflow reads");
    }

    // ---- 16. lines() counting semantics ----
    static void testLinesCounting() throws Exception {
        BufferedReader r = new BufferedReader(new StringReader("a\nb\r\nc\rd"));
        long n = r.lines().count();
        check(n == 4, "lines() counts LF/CRLF/CR/unterminated equally");
        check(r.read() == -1, "stream exhausted by lines()");

        BufferedReader e = new BufferedReader(new StringReader(""));
        check(e.lines().count() == 0, "lines() on empty source is empty stream");

        BufferedReader t = new BufferedReader(new StringReader("\n\n"));
        check(t.lines().count() == 2, "lines() counts empty lines");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testReadSingleChar();
        testReadArrayValidation();
        testReadLine();
        testSkip();
        testReady();
        testMarkReset();
        testClose();
        testLines();
        testReadLineTerminatorMatrix();
        testReadLineMixedWithRead();
        testCrossBufferLines();
        testMarkLimitBoundary();
        testClosedStateMatrix();
        testReadArrayOverflow();
        testLinesCounting();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.BufferedReader API conformance: full contract coverage.
 *          Constructors (null->NPE, size<=0->IAE), read()/EOF, read(char[],off,len)
 *          validation (NPE/IOBE) + len==0 + exact offset, readLine (LF/CRLF/trailing/EOF),
 *          skip (negative->IAE, past-end), ready, mark/reset (replay, unmarked->IOException,
 *          mark(-1)->IAE), close idempotent + closed->IOException, lines() stream.
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

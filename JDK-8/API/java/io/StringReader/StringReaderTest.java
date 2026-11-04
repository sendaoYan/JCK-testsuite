/*
 * @test
 * @summary java.io.StringReader API conformance: full contract coverage.
 *          Constructor null->NPE, read()/EOF, read(char[],off,len) validation
 *          (NPE/IOBE) + len==0 + exact offset, skip (negative->0, past-end),
 *          ready, mark/reset (mark(-1)->IAE, replay, unmarked->0), close idempotent
 *          + closed->IOException.
 * @run main StringReaderTest
 */

import java.io.IOException;
import java.io.StringReader;

public class StringReaderTest {
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

    // ---- 1. constructor ----
    static void testConstructor() {
        checkThrows(NullPointerException.class, () -> new StringReader(null),
                    "StringReader(null) -> NPE");
    }

    // ---- 2. read() ----
    static void testReadSingle() throws Exception {
        StringReader r = new StringReader("AB");
        check(r.read() == 'A', "read first");
        check(r.read() == 'B', "read second");
        check(r.read() == -1, "read EOF");
        check(r.read() == -1, "read repeatedly EOF");
    }

    // ---- 3. read(char[],off,len) ----
    static void testReadArray() throws Exception {
        final StringReader r = new StringReader("hello");
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

        StringReader eof = new StringReader("x");
        check(eof.read() == 'x' && eof.read(new char[2], 0, 2) == -1, "read(byte[]) at EOF -1");
    }

    // ---- 4. skip ----
    static void testSkip() throws Exception {
        StringReader r = new StringReader("abcdef");
        check(r.skip(2) == 2, "skip(2)");
        check(r.read() == 'c', "read after skip");
        check(r.skip(100) == 3, "skip past end returns remaining");
        check(r.read() == -1, "EOF after skip");

        StringReader neg = new StringReader("abc");
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 'a', "negative skip does not move");
    }

    // ---- 5. ready ----
    static void testReady() throws Exception {
        StringReader r = new StringReader("abc");
        check(r.ready(), "ready with data");
    }

    // ---- 6. mark/reset ----
    static void testMarkReset() throws Exception {
        StringReader r = new StringReader("abcd");
        check(r.markSupported(), "markSupported");

        // unmarked reset returns to position 0 (mark is 0 by default)
        r.read();
        r.read();
        r.reset();
        check(r.read() == 'a', "unmarked reset returns to 0");

        StringReader m = new StringReader("abcd");
        m.read();
        m.mark(100);
        m.read();
        m.read();
        m.reset();
        check(m.read() == 'b' && m.read() == 'c', "mark/reset replay");

        final StringReader neg = new StringReader("x");
        checkThrows(IllegalArgumentException.class, () -> neg.mark(-1),
                    "mark(-1) -> IllegalArgumentException");
    }

    // ---- 7. close ----
    static void testClose() throws Exception {
        StringReader r = new StringReader("abc");
        r.close();
        r.close();                      // idempotent
        checkThrows(IOException.class, () -> r.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> r.reset(), "reset after close -> IOException");
    }

    public static void main(String[] args) throws Exception {
        testConstructor();
        testReadSingle();
        testReadArray();
        testSkip();
        testReady();
        testMarkReset();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.StringReader conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructor null->NPE, read/EOF, read(char[],off,len)
 *          validation, skip (negative->0), ready, mark/reset, close.
 *          Deepening: surrogate pairs, exact "Stream closed" messages for every
 *          closed operation (closed check precedes bounds check), ready() at EOF,
 *          mark/readAheadLimit not enforced, skip extremes (0/LONG_MAX/LONG_MIN),
 *          inherited read(char[]) null NPE, inherited read(CharBuffer) contract.
 * @run main StringReaderTest
 */

import java.io.IOException;
import java.io.StringReader;
import java.lang.reflect.Modifier;

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

    /** Runs r and returns the thrown Throwable, or null if nothing was thrown. */
    static Throwable capture(ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) { return t; }
        return null;
    }

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.StringReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Reader.class, "extends Reader");
        hasMethod(c, "read");
        hasMethod(c, "skip", long.class);
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
        hasMethod(c, "markSupported");
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
        r.close();
        checkThrows(IOException.class, () -> r.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> r.reset(), "reset after close -> IOException");
    }

    // ---- 8. supplementary characters (char-based API sees both halves) ----
    static void testSupplementary() throws Exception {
        String sup = new String(Character.toChars(0x1F600));   // one surrogate pair
        StringReader r = new StringReader(sup);
        check(r.read() == 0xD83D, "high surrogate delivered first");
        check(r.read() == 0xDE00, "low surrogate delivered second");
        check(r.read() == -1, "EOF after the pair");
        StringReader bulk = new StringReader(sup + "x");
        char[] buf = new char[3];
        check(bulk.read(buf, 0, 3) == 3, "bulk read covers the pair");
        check(buf[0] == 0xD83D && buf[1] == 0xDE00 && buf[2] == 'x',
              "pair delivered verbatim by read(char[],int,int)");
        // BMP boundary code points survive
        StringReader bmp = new StringReader("\u0000\uD7FF\uE000\uFFFF");
        check(bmp.read() == 0, "U+0000 read as int 0");
        check(bmp.read() == 0xD7FF, "U+D7FF (last code point before surrogates)");
        check(bmp.read() == 0xE000, "U+E000 (first private use)");
        check(bmp.read() == 0xFFFF, "U+FFFF (largest BMP code point)");
    }

    // ---- 9. bulk read edges ----
    static void testReadBulkEdge() throws Exception {
        StringReader r = new StringReader("abc");
        char[] buf = new char[5];
        check(r.read(buf, 0, 5) == 3, "asking for more than available returns the rest");
        check(buf[3] == 0 && buf[4] == 0, "untouched tail stays zero");
        check(r.read(buf, 0, 5) == -1, "next bulk read at EOF");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0 even at EOF");
        check(new StringReader("a").read(new char[2], 2, 0) == 0, "off==length with len==0 legal");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new StringReader("a").read(new char[4], 3, Integer.MAX_VALUE),
                    "off+len int overflow -> IndexOutOfBoundsException");
        // base Reader.read(char[]) evaluates cbuf.length first -> NPE before subclass sees it
        checkThrows(NullPointerException.class, () -> new StringReader("a").read((char[]) null),
                    "read((char[])null) -> NPE");
        // exact-fit read
        StringReader ex = new StringReader("abc");
        char[] three = new char[3];
        check(ex.read(three) == 3 && "abc".equals(new String(three)), "exact-fit bulk read");
        check(ex.read() == -1, "EOF right after exact-fit read");
    }

    // ---- 10. skip extremes ----
    static void testSkipEdge() throws Exception {
        StringReader r = new StringReader("abc");
        check(r.skip(0) == 0 && r.read() == 'a', "skip(0) does not move");
        check(r.skip(Long.MAX_VALUE) == 2, "skip(Long.MAX_VALUE) returns the remainder");
        check(r.skip(Long.MAX_VALUE) == 0, "skip at EOF returns 0");
        check(r.skip(0) == 0, "skip(0) at EOF returns 0");
        StringReader n = new StringReader("xy");
        check(n.skip(Long.MIN_VALUE) == 0, "skip(Long.MIN_VALUE) returns 0");
        check(n.read() == 'x', "position unchanged by the most negative skip");
        // skip lands between the two halves of a surrogate pair
        String sup = new String(Character.toChars(0x1F600));
        StringReader s = new StringReader("a" + sup);
        s.skip(2);
        check(s.read() == 0xDE00, "skip can land on the low surrogate half");
    }

    // ---- 11. ready() is true while the stream is open (even at EOF) ----
    static void testReadyEof() throws Exception {
        check(new StringReader("").ready(), "ready() true for the empty string");
        StringReader eof = new StringReader("");
        check(eof.read() == -1 && eof.ready(), "ready() still true at EOF");
        StringReader r = new StringReader("ab");
        r.read(); r.read();
        check(r.ready(), "ready() true after consuming everything");
    }

    // ---- 12. mark/reset deep contract ----
    static void testMarkResetDeep() throws Exception {
        // readAheadLimit is not enforced: StringReader can always reset to the mark
        StringReader r = new StringReader("abcdef");
        r.read();
        r.mark(0);                                   // limit 0 is accepted
        check(r.read() == 'b' && r.read() == 'c' && r.read() == 'd', "read past limit 0");
        r.reset();
        check(r.read() == 'b', "reset after reading far past readAheadLimit 0");
        // mark at EOF, reset replays the tail
        StringReader e = new StringReader("ab");
        e.read(); e.mark(1); e.read(); e.read();
        e.reset();
        check(e.read() == 'b', "reset at EOF replays from the mark");
        // mark/reset preserves surrogate halves
        String sup = new String(Character.toChars(0x1F600));
        StringReader s = new StringReader(sup + "q");
        s.read();                                    // high surrogate
        s.mark(2);
        s.read();                                    // low surrogate
        s.reset();
        check(s.read() == 0xDE00, "reset lands between the surrogate halves");
        // the most recent mark wins
        StringReader m = new StringReader("xyz");
        m.read(); m.mark(1); m.read(); m.mark(1); m.read();
        m.reset();
        check(m.read() == 'z', "the most recent mark wins");
    }

    // ---- 13. closed-stream matrix: every op reports exactly "Stream closed" ----
    static void testClosedOperations() throws Exception {
        final StringReader r = new StringReader("abc");
        r.close();
        // message verified byte-for-byte on JDK 8 and JDK 27
        String closed = "Stream closed";
        Throwable t1 = capture(() -> r.read());
        check(t1 instanceof IOException && closed.equals(t1.getMessage()),
              "closed read(): IOException(\"Stream closed\"), got " + t1);
        Throwable t2 = capture(() -> r.read(new char[2], 0, 1));
        check(t2 instanceof IOException && closed.equals(t2.getMessage()), "closed read(cbuf,off,len)");
        Throwable t3 = capture(() -> r.skip(1));
        check(t3 instanceof IOException && closed.equals(t3.getMessage()), "closed skip(1)");
        Throwable t4 = capture(() -> r.skip(-1));
        check(t4 instanceof IOException && closed.equals(t4.getMessage()),
              "closed skip(-1): the closed check wins over the negative arg");
        Throwable t5 = capture(() -> r.ready());
        check(t5 instanceof IOException && closed.equals(t5.getMessage()), "closed ready()");
        Throwable t6 = capture(() -> r.mark(1));
        check(t6 instanceof IOException && closed.equals(t6.getMessage()), "closed mark(1)");
        Throwable t7 = capture(() -> r.reset());
        check(t7 instanceof IOException && closed.equals(t7.getMessage()), "closed reset()");
        Throwable t8 = capture(() -> r.read(java.nio.CharBuffer.allocate(1)));
        check(t8 instanceof IOException && closed.equals(t8.getMessage()), "closed read(CharBuffer)");
        // the closed check precedes the bounds check
        Throwable t9 = capture(() -> r.read(new char[2], 5, 1));
        check(t9 instanceof IOException && closed.equals(t9.getMessage()),
              "bad bounds on a closed stream report the closed state first");
        // but the base read(char[]) null check happens before delegation
        checkThrows(NullPointerException.class, () -> r.read((char[]) null),
                    "closed read((char[])null) -> NPE from the base length check");
    }

    // ---- 14. inherited read(CharBuffer) ----
    static void testReadCharBuffer() throws Exception {
        StringReader r = new StringReader("wxyz");
        java.nio.CharBuffer b = java.nio.CharBuffer.allocate(3);
        check(r.read(b) == 3, "fills up to remaining()");
        b.flip();
        check("wxy".contentEquals(b), "content placed at the buffer position");
        check(r.read() == 'z', "position is shared with read()");
        check(r.read(java.nio.CharBuffer.allocate(3)) == -1, "read(CharBuffer) at EOF -> -1");
        // zero remaining -> len==0 read -> 0 (bounds/len==0 path, not EOF)
        java.nio.CharBuffer full = java.nio.CharBuffer.allocate(1);
        full.put('q'); full.flip(); full.position(1);
        check(new StringReader("a").read(full) == 0, "zero remaining -> 0");
        checkThrows(java.nio.ReadOnlyBufferException.class,
                    () -> new StringReader("a").read(java.nio.CharBuffer.wrap("x").asReadOnlyBuffer()),
                    "read-only buffer -> ReadOnlyBufferException");
        checkThrows(NullPointerException.class,
                    () -> new StringReader("a").read((java.nio.CharBuffer) null),
                    "read(null CharBuffer) -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructor();
        testReadSingle();
        testReadArray();
        testSkip();
        testReady();
        testMarkReset();
        testClose();
        testSupplementary();
        testReadBulkEdge();
        testSkipEdge();
        testReadyEof();
        testMarkResetDeep();
        testClosedOperations();
        testReadCharBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

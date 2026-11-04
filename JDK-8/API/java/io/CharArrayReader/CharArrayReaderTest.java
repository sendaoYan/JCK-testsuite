/*
 * @test
 * @summary java.io.CharArrayReader API conformance: full contract coverage.
 *          Constructors (null->NPE, offset/length clamping, negative/overflow->IAE),
 *          read()/EOF, read(char[],off,len) validation (NPE/IOBE), skip (negative->0),
 *          ready (EOF false), mark/reset (replay, unmarked->offset), close.
 *          Deepening: source-array aliasing (no copy), ready false at EOF (vs
 *          StringReader true), mark ignores readAheadLimit, skip extremes and
 *          window bounds, exact "Stream closed" matrix (closed precedes bounds),
 *          inherited read(char[]) null NPE, read(CharBuffer) contract.
 * @run main CharArrayReaderTest
 */

import java.io.CharArrayReader;
import java.io.IOException;

public class CharArrayReaderTest {
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

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        CharArrayReader in = new CharArrayReader(new char[]{'a', 'b', 'c'});
        check(in.read() == 'a', "default constructor reads from 0");

        CharArrayReader sub = new CharArrayReader(new char[]{'x', 'a', 'b', 'c'}, 1, 2);
        check(sub.read() == 'a' && sub.read() == 'b' && sub.read() == -1, "offset/length reads");

        // off+len > buf.length clamps
        CharArrayReader clamped = new CharArrayReader(new char[]{'a', 'b', 'c'}, 1, 100);
        check(clamped.read() == 'b' && clamped.read() == 'c' && clamped.read() == -1, "clamped");

        // length 0 -> empty; offset == buf.length -> empty
        check(new CharArrayReader(new char[]{'a', 'b'}, 1, 0).read() == -1, "length 0 empty");
        check(new CharArrayReader(new char[]{'a', 'b'}, 2, 3).read() == -1, "offset==len empty");

        checkThrows(NullPointerException.class, () -> new CharArrayReader(null),
                    "CharArrayReader(null) -> NPE");
        // CharArrayReader(char[],off,len) validates: IAE (not IndexOutOfBoundsException)
        checkThrows(IllegalArgumentException.class, () -> new CharArrayReader(new char[4], -1, 1),
                    "negative offset -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> new CharArrayReader(new char[4], 5, 0),
                    "offset > length -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> new CharArrayReader(new char[4], 0, -1),
                    "negative length -> IllegalArgumentException");
    }

    // ---- 2. read() ----
    static void testReadSingle() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'A', 'B'});
        check(r.read() == 'A', "read first");
        check(r.read() == 'B', "read second");
        check(r.read() == -1, "read EOF");
    }

    // ---- 3. read(char[],off,len) ----
    static void testReadArray() throws Exception {
        final CharArrayReader r = new CharArrayReader(new char[]{'h', 'e', 'l', 'l', 'o'});
        checkThrows(NullPointerException.class, () -> r.read(null, 0, 1), "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], -1, 1),
                    "read(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], 0, -1),
                    "read(c,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> r.read(new char[2], 0, 3),
                    "read(c,0,3) -> IndexOutOfBoundsException");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0");

        char[] buf = new char[4];
        int n = r.read(buf, 1, 2);
        check(n == 2 && buf[0] == 0 && buf[1] == 'h' && buf[2] == 'e' && buf[3] == 0,
              "exact offset placement");

        CharArrayReader eof = new CharArrayReader(new char[]{'x'});
        check(eof.read() == 'x' && eof.read(new char[2], 0, 2) == -1, "read at EOF -1");
    }

    // ---- 4. skip ----
    static void testSkip() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c', 'd', 'e', 'f'});
        check(r.skip(2) == 2, "skip(2)");
        check(r.read() == 'c', "read after skip");
        check(r.skip(100) == 3, "skip past end returns remaining");

        CharArrayReader neg = new CharArrayReader(new char[]{'a', 'b', 'c'});
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 'a', "negative skip does not move");
    }

    // ---- 5. ready ----
    static void testReady() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a'});
        check(r.ready(), "ready with data");
        r.read();
        check(!r.ready(), "not ready at EOF");
    }

    // ---- 6. mark/reset ----
    static void testMarkReset() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c', 'd'});
        check(r.markSupported(), "markSupported");

        // unmarked reset returns to offset (0)
        r.read();
        r.read();
        r.reset();
        check(r.read() == 'a', "unmarked reset returns to offset");

        CharArrayReader m = new CharArrayReader(new char[]{'a', 'b', 'c', 'd'});
        m.read();
        m.mark(100);
        m.read();
        m.read();
        m.reset();
        check(m.read() == 'b' && m.read() == 'c', "mark/reset replay");

        // offset constructor: initial mark == offset
        CharArrayReader off = new CharArrayReader(new char[]{'0', 'a', 'b', 'c'}, 1, 3);
        off.read();
        off.reset();
        check(off.read() == 'a', "offset constructor initial mark == offset");
    }

    // ---- 7. close ----
    static void testClose() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a'});
        r.close();
        r.close();
        checkThrows(IOException.class, () -> r.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> r.reset(), "reset after close -> IOException");
    }

    // ---- 8. the source array is aliased, not copied ----
    static void testSourceAliasing() throws Exception {
        char[] src = {'a', 'b', 'c'};
        CharArrayReader r = new CharArrayReader(src);
        src[1] = 'Z';                                    // documented: buf is not copied
        check(r.read() == 'a', "first char unaffected");
        check(r.read() == 'Z', "later mutations of the source array are visible");
        char[] src2 = {'x', 'y', 'z'};
        CharArrayReader w = new CharArrayReader(src2, 1, 2);
        src2[2] = 'Q';
        check(w.read() == 'y' && w.read() == 'Q', "the window ctor also aliases the source");
    }

    // ---- 9. ready is false at EOF (StringReader returns true instead) ----
    static void testReadyDeep() throws Exception {
        check(!new CharArrayReader(new char[0]).ready(), "empty reader is never ready");
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b'});
        check(r.ready(), "ready while data remains");
        r.read(); r.read();
        check(!r.ready(), "not ready at EOF");
        CharArrayReader s = new CharArrayReader(new char[]{'a'});
        s.skip(10);                                      // jump past the end
        check(!s.ready(), "skipping past the end leaves the reader not ready");
    }

    // ---- 10. bulk read edges ----
    static void testReadBulkEdge() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c'});
        char[] buf = new char[5];
        check(r.read(buf, 0, 5) == 3, "asking for more than available returns the rest");
        check(buf[3] == 0 && buf[4] == 0, "untouched tail stays zero");
        check(r.read(buf, 0, 5) == -1, "next bulk read at EOF");
        check(r.read(new char[1], 0, 0) == 0, "len==0 returns 0 even at EOF");
        check(new CharArrayReader(new char[]{'a'}).read(new char[2], 2, 0) == 0,
              "off==length with len==0 legal");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new CharArrayReader(new char[1]).read(new char[4], 3, Integer.MAX_VALUE),
                    "off+len int overflow -> IndexOutOfBoundsException");
        checkThrows(NullPointerException.class,
                    () -> new CharArrayReader(new char[]{'a'}).read((char[]) null),
                    "read((char[])null) -> NPE (base length check first)");
        // exact-fit read
        CharArrayReader ex = new CharArrayReader(new char[]{'a', 'b', 'c'});
        char[] three = new char[3];
        check(ex.read(three) == 3 && "abc".equals(new String(three)), "exact-fit bulk read");
        check(ex.read() == -1, "EOF right after exact-fit read");
    }

    // ---- 11. skip extremes and window bounds ----
    static void testSkipEdge() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c'});
        check(r.skip(0) == 0 && r.read() == 'a', "skip(0) does not move");
        check(r.skip(Long.MAX_VALUE) == 2, "skip(Long.MAX_VALUE) returns the remainder");
        check(r.skip(5) == 0, "skip at EOF returns 0");
        CharArrayReader n = new CharArrayReader(new char[]{'x'});
        check(n.skip(Long.MIN_VALUE) == 0, "skip(Long.MIN_VALUE) returns 0");
        check(n.read() == 'x', "position unchanged by the most negative skip");
        // the window ctor bounds skipping to [off, off+len)
        CharArrayReader w = new CharArrayReader(new char[]{'0', 'a', 'b', 'c'}, 1, 2);
        check(w.skip(99) == 2, "skip is bounded by the window end");
        check(w.read() == -1, "EOF at the window end");
    }

    // ---- 12. mark/reset deep contract ----
    static void testMarkResetDeep() throws Exception {
        // readAheadLimit is ignored: reset works after reading far past it
        CharArrayReader r = new CharArrayReader(new char[]{'a', 'b', 'c', 'd', 'e'});
        r.read();
        r.mark(0);                                       // limit 0 accepted
        r.read(); r.read(); r.read();
        r.reset();
        check(r.read() == 'b', "reset after reading past readAheadLimit 0");
        // skip over the mark, then reset back
        CharArrayReader s = new CharArrayReader(new char[]{'a', 'b', 'c', 'd'});
        s.read(); s.mark(1); s.skip(2);
        s.reset();
        check(s.read() == 'b', "reset after skipping past the mark");
        // mark at EOF, reset replays
        CharArrayReader e = new CharArrayReader(new char[]{'a', 'b'});
        e.read(); e.mark(1); e.read(); e.read();
        e.reset();
        check(e.read() == 'b', "reset at EOF replays from the mark");
        // window ctor: unmarked reset goes to the window start even after EOF
        CharArrayReader w = new CharArrayReader(new char[]{'w', 'x', 'y', 'z'}, 1, 2);
        w.read(); w.read(); w.read();
        w.reset();
        check(w.read() == 'x', "unmarked reset returns to the window offset");
        // mark before any read replays everything
        CharArrayReader f = new CharArrayReader(new char[]{'p', 'q'});
        f.mark(5); f.read();
        f.reset();
        check(f.read() == 'p', "mark before the first read replays everything");
    }

    // ---- 13. closed-stream matrix: every op reports exactly "Stream closed" ----
    static void testClosedOperations() throws Exception {
        final CharArrayReader r = new CharArrayReader(new char[]{'a'});
        r.close();
        r.close();                                       // idempotent
        String closed = "Stream closed";                 // verified on JDK 8 and JDK 27
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
        // the closed check precedes the bounds check
        Throwable t8 = capture(() -> r.read(new char[2], 5, 1));
        check(t8 instanceof IOException && closed.equals(t8.getMessage()),
              "bad bounds on a closed stream report the closed state first");
        // but the base read(char[]) null check happens before delegation
        checkThrows(NullPointerException.class, () -> r.read((char[]) null),
                    "closed read((char[])null) -> NPE from the base length check");
    }

    // ---- 14. read(CharBuffer) ----
    static void testReadCharBuffer() throws Exception {
        CharArrayReader r = new CharArrayReader(new char[]{'x', 'y', 'z'});
        java.nio.CharBuffer b = java.nio.CharBuffer.allocate(2);
        check(r.read(b) == 2, "fills up to remaining()");
        b.flip();
        check("xy".contentEquals(b), "content placed at the buffer position");
        check(r.read(java.nio.CharBuffer.allocate(1)) == 1, "next read gets 'z'");
        check(r.read(java.nio.CharBuffer.allocate(1)) == -1, "read(CharBuffer) at EOF -> -1");
        check(new CharArrayReader(new char[0]).read(java.nio.CharBuffer.allocate(2)) == -1,
              "empty reader -> -1");
        // zero remaining -> len==0 read -> 0
        java.nio.CharBuffer full = java.nio.CharBuffer.allocate(1);
        full.put('q'); full.flip(); full.position(1);
        check(new CharArrayReader(new char[]{'a'}).read(full) == 0, "zero remaining -> 0");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testReadSingle();
        testReadArray();
        testSkip();
        testReady();
        testMarkReset();
        testClose();
        testSourceAliasing();
        testReadyDeep();
        testReadBulkEdge();
        testSkipEdge();
        testMarkResetDeep();
        testClosedOperations();
        testReadCharBuffer();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

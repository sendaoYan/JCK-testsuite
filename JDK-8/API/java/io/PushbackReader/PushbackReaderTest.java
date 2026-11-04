/*
 * @test
 * @summary java.io.PushbackReader API conformance: full contract coverage.
 *          Constructors (size<=0->IAE, default buffer 1), unread(int) LIFO,
 *          unread(char[]) / unread(char[],off,len) order, buffer overflow -> IOException,
 *          read(char[],off,len) validation, ready, skip consumes pushback first /
 *          negative->0, markSupported false / mark & reset -> IOException, close.
 *          Deepened (contract level): exact "Pushback buffer overflow" message on all
 *          unread forms, "mark/reset not supported" exact message, markSupported never
 *          delegates, closed-state matrix, skip consumes pushback first, unread at EOF,
 *          mixed unread(int)/unread(char[]) LIFO order, ready derived from pushback
 *          (stub Reader with ready()==false), null ctor, empty-array unread.
 * @run main PushbackReaderTest
 */

import java.io.IOException;
import java.io.PushbackReader;
import java.io.StringReader;

public class PushbackReaderTest {
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
        PushbackReader p = new PushbackReader(new StringReader("a"));
        check(p.read() == 'a', "default constructor reads from wrapped reader");

        checkThrows(IllegalArgumentException.class,
                    () -> new PushbackReader(new StringReader("a"), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
                    () -> new PushbackReader(new StringReader("a"), -1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. unread(int) LIFO ----
    static void testUnreadInt() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("bc"));
        p.unread('a');
        check(p.read() == 'a', "unread char delivered first");
        check(p.read() == 'b' && p.read() == 'c', "wrapped reader follows");
        check(p.read() == -1, "EOF");

        PushbackReader lifo = new PushbackReader(new StringReader("z"), 2);
        lifo.unread('y');
        lifo.unread('x');
        check(lifo.read() == 'x' && lifo.read() == 'y' && lifo.read() == 'z',
              "multiple unread unwind LIFO");
    }

    // ---- 3. unread(char[]) / unread(char[],off,len) ----
    static void testUnreadArray() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("def"), 3);
        p.unread(new char[]{'0', 'a', 'b', 'c', '0'}, 1, 3);
        char[] out = new char[6];
        int n = p.read(out, 0, 6);
        check(n == 6 && out[0] == 'a' && out[1] == 'b' && out[2] == 'c'
                  && out[3] == 'd' && out[4] == 'e' && out[5] == 'f',
              "region unread then bulk read");

        PushbackReader whole = new PushbackReader(new StringReader("z"), 3);
        whole.unread(new char[]{'x', 'y'});
        check(whole.read() == 'x' && whole.read() == 'y' && whole.read() == 'z',
              "unread(char[]) whole array");
    }

    // ---- 4. buffer overflow -> IOException ----
    static void testBufferOverflow() throws Exception {
        // default buffer is exactly one char
        PushbackReader p = new PushbackReader(new StringReader("q"));
        p.unread('1');
        checkThrows(IOException.class, () -> p.unread('2'),
                    "second unread on size-1 buffer -> IOException");

        // explicit capacity; overflowing leaves buffer intact
        PushbackReader p2 = new PushbackReader(new StringReader("9"), 2);
        p2.unread(new char[]{'x', 'y'});
        checkThrows(IOException.class, () -> p2.unread('z'),
                    "unread into full buffer -> IOException");
        check(p2.read() == 'x' && p2.read() == 'y' && p2.read() == '9',
              "buffer intact after failed push");

        // unread(char[],off,len) validates only via System.arraycopy; use a fresh,
        // non-full buffer so the bounds check is reached before the overflow check.
        final PushbackReader fresh = new PushbackReader(new StringReader("a"), 8);
        checkThrows(NullPointerException.class, () -> fresh.unread((char[]) null),
                    "unread((char[])null) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> fresh.unread(new char[4], 1, 5), "unread bad off/len -> IOBE");
    }

    // ---- 5. read(char[],off,len) validation ----
    static void testReadArrayValidation() throws Exception {
        final PushbackReader p = new PushbackReader(new StringReader("hi"));
        checkThrows(NullPointerException.class, () -> p.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new char[2], -1, 1),
                    "read(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new char[2], 0, -1),
                    "read(c,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new char[2], 0, 3),
                    "read(c,0,3) -> IndexOutOfBoundsException");
        check(p.read(new char[1], 0, 0) == 0, "len==0 returns 0");
    }

    // ---- 6. ready / skip ----
    static void testReadySkip() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader(""), 1);
        check(p.ready(), "ready true while wrapped StringReader is always ready");
        p.unread('k');
        check(p.ready(), "ready true with pushback present");
        check(p.read() == 'k', "pushed char readable");

        PushbackReader s = new PushbackReader(new StringReader("wxy"));
        s.unread('v');
        check(s.skip(1) == 1, "skip returns 1");
        check(s.read() == 'w', "skip consumed the pushed-back char");

        PushbackReader neg = new PushbackReader(new StringReader("a"));
        neg.unread('z');
        checkThrows(IllegalArgumentException.class, () -> neg.skip(-5),
                    "negative skip -> IllegalArgumentException");
        check(neg.skip(0) == 0, "zero skip returns 0");
        check(neg.read() == 'z', "failed negative skip did not move");
    }

    // ---- 7. mark/reset unsupported ----
    static void testMarkReset() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("a"));
        check(!p.markSupported(), "markSupported false");
        checkThrows(IOException.class, () -> p.mark(10), "mark -> IOException");
        checkThrows(IOException.class, () -> p.reset(), "reset -> IOException");
    }

    // ---- 8. close ----
    static void testClose() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("m"));
        p.close();
        p.close();
        checkThrows(IOException.class, () -> p.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> p.unread('u'), "unread after close -> IOException");
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
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " containing \"" + sub + "\" but got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " containing \"" + sub + "\" but nothing was thrown");
    }

    // Stub whose ready() is deliberately false, to prove PushbackReader.ready()
    // reports pushback-buffer state independent of the wrapped reader.
    static class NotReadyReader extends java.io.Reader {
        String data;
        int pos;
        NotReadyReader(String d) { data = d; }
        public int read(char[] c, int off, int len) {
            if (pos >= data.length()) return -1;
            int n = Math.min(len, data.length() - pos);
            data.getChars(pos, pos + n, c, off);
            pos += n;
            return n;
        }
        public void close() { }
        public boolean ready() { return false; }
    }

    // ---- 9. exact overflow message on every unread form ----
    static void testOverflowMessages() throws Exception {
        final PushbackReader one = new PushbackReader(new StringReader("q"));
        one.unread('1');
        checkMsg(IOException.class, "Pushback buffer overflow", () -> one.unread('2'),
                 "unread(int) overflow message");

        final PushbackReader two = new PushbackReader(new StringReader("9"), 2);
        two.unread(new char[]{'x', 'y'});
        checkMsg(IOException.class, "Pushback buffer overflow", () -> two.unread('z'),
                 "unread(int) into full size-2 buffer");
        checkMsg(IOException.class, "Pushback buffer overflow", () -> two.unread(new char[]{'z'}),
                 "unread(char[]) into full size-2 buffer");
        // len exceeding capacity manifests as the same overflow IOException
        final PushbackReader big = new PushbackReader(new StringReader("a"), 8);
        checkMsg(IOException.class, "Pushback buffer overflow",
                 () -> big.unread(new char[3], 1, Integer.MAX_VALUE),
                 "unread(cbuf,off,len) with len>capacity -> overflow");

        // buffer intact after all failed pushes
        check(two.read() == 'x' && two.read() == 'y' && two.read() == '9',
              "size-2 buffer intact after failed pushes");
    }

    // ---- 10. mark/reset: exact message, never delegated ----
    static void testMarkResetMessages() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("a"));   // StringReader supports marks
        check(!p.markSupported(), "markSupported is false regardless of wrapped reader");
        checkMsg(IOException.class, "mark/reset not supported", () -> p.mark(10),
                 "mark message");
        checkMsg(IOException.class, "mark/reset not supported", () -> p.reset(),
                 "reset message");
        check(p.read() == 'a', "reader still usable after failed mark/reset");
    }

    // ---- 11. closed-state matrix ----
    static void testClosedMatrix() throws Exception {
        final PushbackReader p = new PushbackReader(new StringReader("m"));
        p.close();
        checkMsg(IOException.class, "Stream closed", () -> p.read(), "closed read()");
        checkMsg(IOException.class, "Stream closed", () -> p.unread('u'), "closed unread(int)");
        checkMsg(IOException.class, "Stream closed", () -> p.ready(), "closed ready()");
        checkMsg(IOException.class, "Stream closed", () -> p.skip(1), "closed skip()");
        checkMsg(IOException.class, "Stream closed",
                 () -> p.read(new char[1], 0, 1), "closed read(char[])");
        checkMsg(IOException.class, "Stream closed",
                 () -> p.unread(new char[]{'u'}), "closed unread(char[])");
    }

    // ---- 12. skip ordering and validation ----
    static void testSkipOrdering() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("wxy"));
        p.unread('v');
        check(p.skip(1) == 1, "skip returns 1");
        check(p.read() == 'w', "skip consumed the pushback char first");

        PushbackReader p2 = new PushbackReader(new StringReader("wx"));
        p2.unread('v');
        check(p2.skip(100) == 3, "skip past everything returns pushback+source");
        check(p2.read() == -1, "EOF after skip past end");

        PushbackReader p3 = new PushbackReader(new StringReader("abc"));
        check(p3.skip(0) == 0, "skip(0) returns 0");
        check(p3.read() == 'a', "position unchanged after skip(0)");
        checkMsg(IllegalArgumentException.class, "skip value is negative",
                 () -> p3.skip(-5), "negative skip message");
        check(p3.read() == 'b', "failed negative skip did not move");
    }

    // ---- 13. unread at EOF ----
    static void testUnreadAtEof() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader(""));
        p.unread('z');
        check(p.read() == 'z', "unread works with exhausted source");
        check(p.read() == -1, "EOF again after reading pushed char");
        p.unread('y');
        check(p.read() == 'y', "second unread cycle at EOF");
    }

    // ---- 14. mixed unread(int)/unread(char[]) LIFO order ----
    static void testMixedUnreadOrder() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("S"), 3);
        p.unread('a');
        p.unread(new char[]{'x', 'y'});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append((char) p.read());
        check("xyaS".equals(sb.toString()),
              "each unread call re-inserts at the front (last push reads first)");
    }

    // ---- 15. ready() reflects pushback state, not the wrapped reader ----
    static void testReadyFromPushback() throws Exception {
        PushbackReader p = new PushbackReader(new NotReadyReader("S"), 2);
        check(!p.ready(), "ready false when pushback empty and wrapped not ready");
        p.unread('k');
        check(p.ready(), "ready true while pushback holds a char (wrapped still not ready)");
        check(p.read() == 'k', "pushed char readable");
        check(!p.ready(), "ready false again once pushback drained");
        check(p.read() == 'S', "wrapped source still readable despite ready()==false");
        check(!p.ready(), "ready false at EOF of wrapped source");
    }

    // ---- 16. null constructor and empty-array unread ----
    static void testNullCtorAndEmptyArray() throws Exception {
        checkThrows(NullPointerException.class, () -> new PushbackReader(null),
                    "PushbackReader(null) -> NPE at construction");
        checkThrows(NullPointerException.class, () -> new PushbackReader(null, 2),
                    "PushbackReader(null,2) -> NPE at construction");

        PushbackReader full = new PushbackReader(new StringReader("q"));   // size 1
        full.unread('1');
        full.unread(new char[0]);              // empty push is a no-op even on a full buffer
        check(full.read() == '1', "empty-array unread did not disturb the buffer");
        full.unread(new char[0], 0, 0);
        check(full.read() == 'q', "empty-region unread also a no-op");
    }

    // ---- 17. bulk read pulls pushback region then source, with offset ----
    static void testBulkReadWithPushbackRegion() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("def"), 3);
        p.unread(new char[]{'0', 'a', 'b', 'c', '0'}, 1, 3);
        char[] out = new char[8];
        int n = p.read(out, 1, 6);
        check(n == 6, "bulk read length with offset");
        check(out[0] == 0 && out[1] == 'a' && out[2] == 'b' && out[3] == 'c'
              && out[4] == 'd' && out[5] == 'e' && out[6] == 'f' && out[7] == 0,
              "exact placement: pushback region first, then source");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testUnreadInt();
        testUnreadArray();
        testBufferOverflow();
        testReadArrayValidation();
        testReadySkip();
        testMarkReset();
        testClose();
        testOverflowMessages();
        testMarkResetMessages();
        testClosedMatrix();
        testSkipOrdering();
        testUnreadAtEof();
        testMixedUnreadOrder();
        testReadyFromPushback();
        testNullCtorAndEmptyArray();
        testBulkReadWithPushbackRegion();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

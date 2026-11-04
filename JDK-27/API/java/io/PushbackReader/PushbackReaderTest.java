/*
 * @test
 * @summary java.io.PushbackReader conformance: white-box structure + black-box
 *          behavior (unread of char/char[]/regions, LIFO order, default size 1,
 *          buffer exhaustion IOException, mark unsupported, ready interplay, skip
 *          consumes pushback first, close behavior).
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PushbackReaderTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static PushbackReader pr(String s) {
        return new PushbackReader(new StringReader(s));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PushbackReader");
        check(c.getSuperclass().getName().equals("java.io.FilterReader"),
              "extends FilterReader");
        check(c.getConstructor(java.io.Reader.class) != null, "ctor (Reader)");
        check(c.getConstructor(java.io.Reader.class, int.class) != null, "ctor (Reader, int)");
        for (String m : new String[]{"read", "unread", "ready", "mark",
                                     "reset", "markSupported", "close", "skip"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. unread(int): pushed char comes out first
        PushbackReader p = pr("bc");
        p.unread('a');
        check(p.read() == 'a', "unread char delivered first");
        check(p.read() == 'b' && p.read() == 'c', "source content follows");
        check(p.read() == -1, "EOF");

        // 2. LIFO across multiple unread calls
        p = new PushbackReader(new StringReader("z"), 2);
        p.unread('y');
        p.unread('x');
        check(p.read() == 'x' && p.read() == 'y' && p.read() == 'z', "LIFO order");

        // 3. default buffer is one char
        p = pr("q");
        p.unread('1');
        try {
            p.unread('2');
            check(false, "second unread on size-1 buffer must throw");
        } catch (IOException ioe) {
            check("Pushback buffer overflow".equals(ioe.getMessage()),
                  "overflow message: " + ioe.getMessage());
        }

        // 4. larger buffer, region unread
        p = new PushbackReader(new StringReader("def"), 3);
        p.unread(new char[]{'0', 'a', 'b', 'c', '0'}, 1, 3);
        char[] out = new char[6];
        check(p.read(out) == 6 && out[0] == 'a' && out[5] == 'f', "region unread + bulk read");

        // 5. exact-fit unread then oversized unread
        p = new PushbackReader(new StringReader("9"), 2);
        p.unread(new char[]{'x', 'y'});
        try {
            p.unread('z');
            check(false, "overflowing a full buffer must throw");
        } catch (IOException expected) {
            passed++;
        }
        check(p.read() == 'x' && p.read() == 'y' && p.read() == '9', "buffer intact after failed push");

        // 6. ready() true while pushback buffer holds data
        p = new PushbackReader(new StringReader(""), 1);
        check(p.ready(), "ready follows the wrapped StringReader (always ready)");
        p.unread('k');
        check(p.read() == 'k', "pushed char readable");

        // 7. mark/reset unsupported
        p = pr("a");
        check(!p.markSupported(), "mark unsupported");
        try {
            p.reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "reset message: " + ioe.getMessage());
        }

        // 8. skip consumes the pushback buffer first
        p = pr("wxy");
        p.unread('v');
        p.skip(1);
        check(p.read() == 'w', "skip ate the pushed-back char");

        // 9. negative/invalid buffer size
        try {
            new PushbackReader(new StringReader("a"), -1);
            check(false, "negative size must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 10. close: reads after close throw
        p = pr("m");
        p.close();
        try {
            p.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }
        try {
            p.unread('u');
            check(false, "unread after close must throw");
        } catch (IOException expected) {
            passed++;
        }
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

    // ---- deepened: exact overflow message on every unread form ----
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

    // ---- deepened: mark/reset: exact message, never delegated ----
    static void testMarkResetMessages() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("a"));   // StringReader supports marks
        check(!p.markSupported(), "markSupported is false regardless of wrapped reader");
        checkMsg(IOException.class, "mark/reset not supported", () -> p.mark(10),
                 "mark message");
        checkMsg(IOException.class, "mark/reset not supported", () -> p.reset(),
                 "reset message");
        check(p.read() == 'a', "reader still usable after failed mark/reset");
    }

    // ---- deepened: closed-state matrix ----
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

    // ---- deepened: skip ordering and validation ----
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

    // ---- deepened: unread at EOF ----
    static void testUnreadAtEof() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader(""));
        p.unread('z');
        check(p.read() == 'z', "unread works with exhausted source");
        check(p.read() == -1, "EOF again after reading pushed char");
        p.unread('y');
        check(p.read() == 'y', "second unread cycle at EOF");
    }

    // ---- deepened: mixed unread(int)/unread(char[]) LIFO order ----
    static void testMixedUnreadOrder() throws Exception {
        PushbackReader p = new PushbackReader(new StringReader("S"), 3);
        p.unread('a');
        p.unread(new char[]{'x', 'y'});
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++) sb.append((char) p.read());
        check("xyaS".equals(sb.toString()),
              "each unread call re-inserts at the front (last push reads first)");
    }

    // ---- deepened: ready() reflects pushback state, not the wrapped reader ----
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

    // ---- deepened: null constructor and empty-array unread ----
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

    // ---- deepened: bulk read pulls pushback region then source, with offset ----
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
        testWhiteBox();
        testBlackBox();
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

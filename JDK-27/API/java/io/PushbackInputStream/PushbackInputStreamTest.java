/*
 * @test
 * @summary java.io.PushbackInputStream conformance: white-box structure + black-box
 *          behavior (unread of single bytes/arrays/regions, pushback buffer order
 *          LIFO, buffer exhaustion IOException, default buffer size 1, mark/reset
 *          unsupported, available counts pushed-back bytes, skip/transferTo interplay,
 *          null/bounds checks, close).
 *          Deepening: exact overflow/closed messages, unread(int) low-byte
 *          truncation, skip spanning pushback+stream with clamping, available
 *          arithmetic, LIFO ordering inside a single bulk read, lookahead
 *          re-read pattern, unread len==0 no-ops, and the JDK-27 transferTo
 *          override draining pushback mid-stream.
 * @run main PushbackInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PushbackInputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class PushbackInputStreamTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static PushbackInputStream pb(String s) {
        return new PushbackInputStream(new ByteArrayInputStream(s.getBytes()));
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.PushbackInputStream");
        check(c.getSuperclass().getName().equals("java.io.FilterInputStream"),
              "extends FilterInputStream");
        check(c.getConstructor(java.io.InputStream.class) != null, "ctor (InputStream)");
        check(c.getConstructor(java.io.InputStream.class, int.class) != null,
              "ctor (InputStream, int)");
        for (String m : new String[]{"read", "unread", "available", "skip",
                                     "markSupported", "mark", "reset", "close", "transferTo"})
            check(has(c, m), "has method " + m);
        check(Modifier.isProtected(c.getDeclaredField("buf").getModifiers()), "protected buf");
        check(Modifier.isProtected(c.getDeclaredField("pos").getModifiers()), "protected pos");
    }

    static boolean has(Class<?> c, String m) {
        for (java.lang.reflect.Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. unread(int): the byte is delivered before stream content (LIFO order)
        PushbackInputStream p = pb("bc");
        p.unread('a');
        check(p.read() == 'a', "unread byte delivered first");
        check(p.read() == 'b' && p.read() == 'c', "stream content follows");
        check(p.read() == -1, "EOF after all consumed");

        // 2. multiple unread calls deliver in reverse order
        p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'z'}), 2);
        p.unread('y');
        p.unread('x');
        check(p.read() == 'x' && p.read() == 'y' && p.read() == 'z',
              "pushbacks unwind LIFO");

        // 3. default buffer size is exactly one byte
        p = pb("q");
        p.unread('1');
        try {
            p.unread('2');
            check(false, "second unread on a size-1 buffer must throw");
        } catch (IOException ioe) {
            check("Push back buffer is full".equals(ioe.getMessage()),
                  "overflow message: " + ioe.getMessage());
        }

        // 4. explicit buffer capacity; unread region of exact size fits
        PushbackInputStream p4 = new PushbackInputStream(
            new ByteArrayInputStream(new byte[]{9}), 2);
        p4.unread(new byte[]{1, 2}, 0, 2);
        check(p4.read() == 1 && p4.read() == 2 && p4.read() == 9, "region unread order");
        try {
            p4.unread(new byte[3], 0, 3);
            check(false, "oversized unread must throw");
        } catch (IOException expected) {
            passed++;
        }

        // 5. unread(byte[]) region semantics
        p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'d', 'e', 'f'}), 3);
        p.unread(new byte[]{0, 'a', 'b', 'c', 0}, 1, 3);
        byte[] out = new byte[6];
        int n = p.read(out, 0, 6);
        check(n == 6 && out[0] == 'a' && out[2] == 'c' && out[5] == 'f',
              "region unread then bulk read");

        // 6. available() counts the pushback buffer plus the stream
        p = pb("12");
        check(p.available() == 2, "available from stream only");
        p.unread('0');
        check(p.available() == 3, "available includes pushed-back byte");

        // 7. mark/reset unsupported even when the wrapped stream supports them
        java.io.ByteArrayInputStream marks = new ByteArrayInputStream(new byte[1]);
        PushbackInputStream p7 = new PushbackInputStream(marks, 4);
        check(!p7.markSupported(), "mark unsupported");
        try {
            p7.reset();
            check(false, "reset must throw");
        } catch (IOException ioe) {
            check(ioe.getMessage() != null, "reset message: " + ioe.getMessage());
        }

        // 8. skip consumes pushed-back bytes first
        p = pb("wxy");
        p.unread('v');
        p.skip(1);
        check(p.read() == 'w', "skip consumed the pushed-back byte");

        // 9. transferTo drains pushback + stream
        p = pb("rs");
        p.unread('t');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        check(p.transferTo(bos) == 3 && "trs".equals(bos.toString()),
              "transferTo includes pushback buffer");

        // 10. argument checking
        p = pb("a");
        try {
            p.unread((byte[]) null);
            check(false, "unread(null) must throw");
        } catch (NullPointerException | IOException expected) {
            passed++;
        }
        try {
            new PushbackInputStream(new ByteArrayInputStream(new byte[1]), -1);
            check(false, "negative buffer size must throw");
        } catch (IllegalArgumentException expected) {
            passed++;
        }

        // 11. close releases both layers; reads after close throw
        p = pb("m");
        p.close();
        try {
            p.read();
            check(false, "read after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "close message: " + ioe.getMessage());
        }
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, ThrowingRunnable r,
                               String msg, String substr) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) {
                check(t.getMessage() != null && t.getMessage().contains(substr),
                      msg + ": message must contain \"" + substr + "\", got: " + t.getMessage());
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- deepening: exact overflow message on every unread form ----
    static void testUnreadExactMessages() throws Exception {
        final PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'q'}));
        p.unread('1');
        checkThrowsMsg(IOException.class, () -> p.unread('2'),
              "unread(int) on a full 1-byte buffer", "Push back buffer is full");

        final PushbackInputStream p2 = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{9}), 2);
        p2.unread(new byte[]{1, 2});
        checkThrowsMsg(IOException.class, () -> p2.unread(new byte[3], 0, 3),
              "unread(byte[],off,len) longer than capacity", "Push back buffer is full");
        checkThrowsMsg(IOException.class, () -> p2.unread('x'),
              "unread(int) into a full 2-byte buffer", "Push back buffer is full");
        checkThrowsMsg(IOException.class, () -> p2.unread(new byte[]{5, 6, 7}),
              "unread(byte[]) longer than capacity", "Push back buffer is full");
        check(p2.read() == 1 && p2.read() == 2 && p2.read() == 9,
              "failed pushes left the buffer content untouched");
    }

    // ---- deepening: every operation after close says "Stream closed" ----
    static void testClosedMessages() throws Exception {
        final PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'m'}));
        p.close();
        p.close();
        check(true, "close is idempotent");
        checkThrowsMsg(IOException.class, () -> p.read(), "read() after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.read(new byte[1], 0, 1),
              "read(byte[],int,int) after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.unread('u'),
              "unread(int) after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.unread(new byte[]{1}, 0, 1),
              "unread(byte[],int,int) after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.available(),
              "available() after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.skip(1), "skip() after close", "Stream closed");
        checkThrowsMsg(IOException.class, () -> p.skip(-1),
              "skip(negative) after close", "Stream closed");
    }

    // ---- deepening: unread(int) stores the low 8 bits ----
    static void testUnreadHighBits() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'z'}));
        p.unread(0x141);
        check(p.read() == 0x41, "unread(0x141) delivers the low byte 0x41");

        PushbackInputStream z = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'z'}));
        z.unread(0);
        check(z.read() == 0, "unread(0) delivers a genuine 0 byte (distinct from EOF -1)");

        PushbackInputStream m = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'z'}));
        m.unread(-1);
        check(m.read() == 255, "unread(-1) delivers 0xFF, read() stays unsigned");
    }

    // ---- deepening: skip interacts with the pushback buffer first ----
    static void testSkipPushbackInteraction() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a', 'b'}), 3);
        p.unread(new byte[]{'x', 'y'}, 0, 2);
        check(p.skip(1) == 1, "skip(1) reports 1");
        check(p.read() == 'y', "skip consumed the front pushed-back byte");
        check(p.read() == 'a', "then the wrapped stream resumes");

        PushbackInputStream b = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a'}), 2);
        b.unread('q');
        b.unread('p');
        check(b.skip(2) == 2, "skip exactly drains a full pushback buffer");
        check(b.read() == 'a', "stream byte follows the drained pushback");

        PushbackInputStream span = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a', 'b', 'c'}), 3);
        span.unread(new byte[]{'x', 'y', 'z'}, 0, 3);
        check(span.skip(10) == 6, "skip beyond availability returns pushback(3) + stream(3)");
        check(span.read() == -1, "everything is gone after the spanning skip");

        PushbackInputStream only = new PushbackInputStream(
                new ByteArrayInputStream(new byte[0]), 4);
        only.unread(new byte[]{'1', '2', '3'}, 0, 3);
        check(only.skip(2) == 2 && only.read() == '3' && only.available() == 0,
              "skip on a pushback-only stream consumes buffer bytes only");
    }

    // ---- deepening: available() arithmetic includes the pushback buffer ----
    static void testAvailableCounting() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'1', '2'}), 2);
        check(p.available() == 2, "available from the stream alone");
        p.unread('0');
        check(p.available() == 3, "available = stream + pushback");
        check(p.read() == '0' && p.available() == 2, "draining one pushed byte drops the count");
        p.unread(new byte[]{'a', 'b'});
        check(p.available() == 4, "bulk unread adds its length");
        check(new PushbackInputStream(new ByteArrayInputStream(new byte[0])).available() == 0,
              "available 0 on an empty stream with empty buffer");
    }

    // ---- deepening: lookahead pattern: read, unread, re-read ----
    static void testRereadPattern() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a', 'b'}), 1);
        int c1 = p.read();
        p.unread(c1);
        int c2 = p.read();
        check(c1 == 'a' && c2 == 'a', "a byte can be re-read after being pushed back");
        check(p.read() == 'b', "the wrapped stream position was not disturbed");

        PushbackInputStream z = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a'}));
        z.unread(new byte[0], 0, 0);
        check(z.read() == 'a', "unread(b,off,0) is a no-op");
    }

    // ---- deepening: one bulk read spans pushback (LIFO) and stream bytes ----
    static void testBulkReadSpansPushbackAndStream() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'c', 'd', 'e'}), 4);
        p.unread('a');
        p.unread('b');
        byte[] buf = new byte[10];
        int n = p.read(buf, 0, 10);
        check(n == 5, "single bulk read returned pushback(2) + stream(3)");
        check(buf[0] == 'b' && buf[1] == 'a', "pushed bytes surface in LIFO order first");
        check(buf[2] == 'c' && buf[4] == 'e', "then the stream bytes in order");
        check(p.read(buf, 0, 10) == -1, "stream is exhausted afterwards");

        PushbackInputStream partial = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'x', 'y'}), 2);
        partial.unread('w');
        byte[] small = new byte[2];
        int m = partial.read(small, 1, 1);
        check(m == 1 && small[0] == 0 && small[1] == 'w',
              "read(b,off,len) with len < pushback count fills only the region");
        check(partial.read() == 'x', "remaining pushback/stream order intact");
    }

    // ---- deepening: interleaved read/unread sequence (parser walk) ----
    static void testInterleavedSequence() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'a', 'b', 'c'}), 2);
        check(p.read() == 'a', "step 1 reads 'a'");
        check(p.read() == 'b', "step 2 reads 'b'");
        p.unread('b');
        p.unread('a');
        check(p.read() == 'a' && p.read() == 'b', "steps replay in original order");
        check(p.read() == 'c', "stream continues");
        check(p.read() == -1, "EOF");
    }

    // ---- deepening (JDK-27): the transferTo override drains pushback mid-stream ----
    static void testTransferToContract() throws Exception {
        // mid-stream: partial read first, push one back, transferTo must include it
        PushbackInputStream p = pb("abcdef");
        check(p.read() == 'a', "consume one byte");
        p.unread('a');
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        long n = p.transferTo(bos);
        check(n == 6, "transferTo counts the pushed-back byte plus the remainder");
        check("abcdef".equals(bos.toString()), "transferTo preserves the original order");
        check(p.read() == -1, "EOF after transferTo");

        // pushback survives across transferTo: unread then transfer again
        PushbackInputStream q = pb("xy");
        q.read();
        ByteArrayOutputStream bos2 = new ByteArrayOutputStream();
        check(q.transferTo(bos2) == 1, "transferTo without pushback transfers the remainder only");
        check("y".equals(bos2.toString()), "transferred content is exactly the remainder");
        q.unread('z');
        check(q.read() == 'z', "unread still works after transferTo drained the stream");
        check(q.read() == -1, "EOF after the post-transferTo pushback is consumed");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testUnreadExactMessages();
        testClosedMessages();
        testUnreadHighBits();
        testSkipPushbackInteraction();
        testAvailableCounting();
        testRereadPattern();
        testBulkReadSpansPushbackAndStream();
        testInterleavedSequence();
        testTransferToContract();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

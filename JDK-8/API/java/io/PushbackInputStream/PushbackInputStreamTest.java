/*
 * @test
 * @summary java.io.PushbackInputStream API conformance: full contract coverage.
 *          Constructors (size<=0->IAE, default buffer 1), unread(int) LIFO,
 *          unread(byte[]) / unread(byte[],off,len) order, buffer overflow -> IOException,
 *          read(byte[],off,len) validation, available counts pushback, skip consumes
 *          pushback first / negative->0, markSupported false / reset->IOException,
 *          close -> read IOException.
 *          Deepening: exact overflow/closed messages, unread(int) low-byte
 *          truncation, skip spanning pushback+stream with clamping, available
 *          arithmetic, LIFO ordering inside a single bulk read, lookahead
 *          re-read pattern, unread len==0 no-ops.
 * @run main PushbackInputStreamTest
 */

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.PushbackInputStream;

public class PushbackInputStreamTest {
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
        PushbackInputStream p = new PushbackInputStream(new ByteArrayInputStream(new byte[1]));
        check(p.read() == 0, "default constructor reads from wrapped stream");

        checkThrows(IllegalArgumentException.class,
                    () -> new PushbackInputStream(new ByteArrayInputStream(new byte[1]), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class,
                    () -> new PushbackInputStream(new ByteArrayInputStream(new byte[1]), -1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. unread(int) LIFO ----
    static void testUnreadInt() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'b', 'c'}));
        p.unread('a');
        check(p.read() == 'a', "unread byte delivered first");
        check(p.read() == 'b' && p.read() == 'c', "wrapped stream follows");
        check(p.read() == -1, "EOF");

        PushbackInputStream lifo = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'z'}), 2);
        lifo.unread('y');
        lifo.unread('x');
        check(lifo.read() == 'x' && lifo.read() == 'y' && lifo.read() == 'z',
              "multiple unread unwind LIFO");
    }

    // ---- 3. unread(byte[]) / unread(byte[],off,len) ----
    static void testUnreadArray() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'d', 'e', 'f'}), 3);
        p.unread(new byte[]{0, 'a', 'b', 'c', 0}, 1, 3);
        byte[] out = new byte[6];
        int n = p.read(out, 0, 6);
        check(n == 6 && out[0] == 'a' && out[1] == 'b' && out[2] == 'c'
                  && out[3] == 'd' && out[4] == 'e' && out[5] == 'f',
              "region unread then bulk read");

        PushbackInputStream whole = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'z'}), 3);
        whole.unread(new byte[]{'x', 'y'});
        check(whole.read() == 'x' && whole.read() == 'y' && whole.read() == 'z',
              "unread(byte[]) whole array");
    }

    // ---- 4. buffer overflow -> IOException ----
    static void testBufferOverflow() throws Exception {
        // default buffer is exactly one byte
        PushbackInputStream p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'q'}));
        p.unread('1');
        checkThrows(IOException.class, () -> p.unread('2'),
                    "second unread on size-1 buffer -> IOException");

        // explicit capacity; overflowing leaves buffer intact
        PushbackInputStream p2 = new PushbackInputStream(new ByteArrayInputStream(new byte[]{9}), 2);
        p2.unread(new byte[]{1, 2});
        checkThrows(IOException.class, () -> p2.unread(new byte[3], 0, 3),
                    "oversized unread -> IOException");
        check(p2.read() == 1 && p2.read() == 2 && p2.read() == 9, "buffer intact after failed push");

        // unread(byte[],off,len) validates only via System.arraycopy; use a fresh,
        // non-full buffer so the bounds check is reached before the overflow check.
        final PushbackInputStream fresh = new PushbackInputStream(
                new ByteArrayInputStream(new byte[1]), 8);
        checkThrows(NullPointerException.class, () -> fresh.unread((byte[]) null),
                    "unread((byte[])null) -> NPE");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> fresh.unread(new byte[4], 1, 5), "unread bad off/len -> IOBE");
    }

    // ---- 5. read(byte[],off,len) validation ----
    static void testReadArrayValidation() throws Exception {
        final PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'h', 'i'}));
        checkThrows(NullPointerException.class, () -> p.read(null, 0, 1),
                    "read(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new byte[2], -1, 1),
                    "read(b,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new byte[2], 0, -1),
                    "read(b,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> p.read(new byte[2], 0, 3),
                    "read(b,0,3) -> IndexOutOfBoundsException");
        check(p.read(new byte[1], 0, 0) == 0, "len==0 returns 0");
    }

    // ---- 6. available / skip ----
    static void testAvailableSkip() throws Exception {
        PushbackInputStream p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'1', '2'}));
        check(p.available() == 2, "available from stream only");
        p.unread('0');
        check(p.available() == 3, "available includes pushed-back byte");

        PushbackInputStream s = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{'w', 'x', 'y'}));
        s.unread('v');
        check(s.skip(1) == 1, "skip returns 1");
        check(s.read() == 'w', "skip consumed the pushed-back byte");

        PushbackInputStream neg = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'a'}));
        neg.unread('z');
        check(neg.skip(-5) == 0, "negative skip returns 0");
        check(neg.read() == 'z', "negative skip did not move");
        check(neg.skip(0) == 0, "zero skip returns 0");
    }

    // ---- 7. mark/reset unsupported ----
    static void testMarkReset() throws Exception {
        PushbackInputStream p = new PushbackInputStream(
                new ByteArrayInputStream(new byte[]{1}), 4);
        check(!p.markSupported(), "markSupported false");
        p.mark(10);                       // no-op, does not throw
        checkThrows(IOException.class, () -> p.reset(), "reset -> IOException");
    }

    // ---- 8. close ----
    static void testClose() throws Exception {
        PushbackInputStream p = new PushbackInputStream(new ByteArrayInputStream(new byte[]{'m'}));
        p.close();
        p.close();
        checkThrows(IOException.class, () -> p.read(), "read after close -> IOException");
        checkThrows(IOException.class, () -> p.unread('u'), "unread after close -> IOException");
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

    // ---- 9. exact overflow message on every unread form ----
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

    // ---- 10. every operation after close says "Stream closed" ----
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

    // ---- 11. unread(int) stores the low 8 bits ----
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

    // ---- 12. skip interacts with the pushback buffer first ----
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

    // ---- 13. available() arithmetic includes the pushback buffer ----
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

    // ---- 14. lookahead pattern: read, unread, re-read ----
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

    // ---- 15. one bulk read spans pushback (LIFO) and stream bytes ----
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

    // ---- 16. interleaved read/unread sequence (parser walk) ----
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

    public static void main(String[] args) throws Exception {
        testConstructors();
        testUnreadInt();
        testUnreadArray();
        testBufferOverflow();
        testReadArrayValidation();
        testAvailableSkip();
        testMarkReset();
        testClose();
        testUnreadExactMessages();
        testClosedMessages();
        testUnreadHighBits();
        testSkipPushbackInteraction();
        testAvailableCounting();
        testRereadPattern();
        testBulkReadSpansPushbackAndStream();
        testInterleavedSequence();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

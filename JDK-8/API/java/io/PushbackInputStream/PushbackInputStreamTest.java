/*
 * @test
 * @summary java.io.PushbackInputStream API conformance: full contract coverage.
 *          Constructors (size<=0->IAE, default buffer 1), unread(int) LIFO,
 *          unread(byte[]) / unread(byte[],off,len) order, buffer overflow -> IOException,
 *          read(byte[],off,len) validation, available counts pushback, skip consumes
 *          pushback first / negative->0, markSupported false / reset->IOException,
 *          close -> read IOException.
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

    public static void main(String[] args) throws Exception {
        testConstructors();
        testUnreadInt();
        testUnreadArray();
        testBufferOverflow();
        testReadArrayValidation();
        testAvailableSkip();
        testMarkReset();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

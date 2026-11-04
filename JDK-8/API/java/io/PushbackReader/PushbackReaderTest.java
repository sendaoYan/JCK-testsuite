/*
 * @test
 * @summary java.io.PushbackReader API conformance: full contract coverage.
 *          Constructors (size<=0->IAE, default buffer 1), unread(int) LIFO,
 *          unread(char[]) / unread(char[],off,len) order, buffer overflow -> IOException,
 *          read(char[],off,len) validation, ready, skip consumes pushback first /
 *          negative->0, markSupported false / mark & reset -> IOException, close.
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

    public static void main(String[] args) throws Exception {
        testConstructors();
        testUnreadInt();
        testUnreadArray();
        testBufferOverflow();
        testReadArrayValidation();
        testReadySkip();
        testMarkReset();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

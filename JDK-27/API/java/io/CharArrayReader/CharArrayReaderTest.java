/*
 * @test
 * @summary java.io.CharArrayReader conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (null->NPE, offset/length clamping,
 *          negative->IAE), read/EOF, read(char[],off,len) validation, skip, ready, mark/reset,
 *          close.
 * @run main CharArrayReaderTest
 */

import java.io.CharArrayReader;
import java.io.IOException;
import java.lang.reflect.Modifier;

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

    static void hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); }
        catch (NoSuchMethodException e) { throw new AssertionError("missing method " + name); }
        passed++;
    }

    // ---- white-box: structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.CharArrayReader");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Reader.class, "extends Reader");
        hasMethod(c, "read");
        hasMethod(c, "skip", long.class);
        hasMethod(c, "mark", int.class);
        hasMethod(c, "reset");
        hasMethod(c, "markSupported");
    }

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        CharArrayReader in = new CharArrayReader(new char[]{'a', 'b', 'c'});
        check(in.read() == 'a', "default constructor reads from 0");

        CharArrayReader sub = new CharArrayReader(new char[]{'x', 'a', 'b', 'c'}, 1, 2);
        check(sub.read() == 'a' && sub.read() == 'b' && sub.read() == -1, "offset/length reads");

        CharArrayReader clamped = new CharArrayReader(new char[]{'a', 'b', 'c'}, 1, 100);
        check(clamped.read() == 'b' && clamped.read() == 'c' && clamped.read() == -1, "clamped");

        check(new CharArrayReader(new char[]{'a', 'b'}, 1, 0).read() == -1, "length 0 empty");
        check(new CharArrayReader(new char[]{'a', 'b'}, 2, 3).read() == -1, "offset==len empty");

        checkThrows(NullPointerException.class, () -> new CharArrayReader(null),
                    "CharArrayReader(null) -> NPE");
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testReadSingle();
        testReadArray();
        testSkip();
        testReady();
        testMarkReset();
        testClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

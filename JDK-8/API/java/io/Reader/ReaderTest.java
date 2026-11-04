/*
 * @test
 * @summary java.io.Reader API conformance: full contract coverage.
 *          Abstract character-input root. Default read()/read(char[]) derived from
 *          read(char[],int,int), read(CharBuffer), skip (negative->IAE), ready/mark/reset
 *          defaults, markSupported false, argument checking, polymorphic use.
 * @run main ReaderTest
 */

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.nio.CharBuffer;

public class ReaderTest {
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

    /** Minimal subclass implementing only read(char[],int,int) and close(). */
    static class ArrayReader extends Reader {
        final char[] data;
        int pos;
        ArrayReader(char[] data) { this.data = data; }
        @Override public int read(char[] cbuf, int off, int len) {
            if (pos >= data.length) return -1;
            int n = 0;
            while (n < len && pos < data.length) cbuf[off + n++] = data[pos++];
            return n;
        }
        @Override public void close() { }
    }

    // ---- 1. default single-char and bulk reads derive from read(char[],int,int) ----
    static void testReadDefaults() throws Exception {
        ArrayReader r = new ArrayReader(new char[]{'a', 'b', 'c', 'd'});
        check(r.read() == 'a', "read() single char");
        char[] two = new char[2];
        check(r.read(two) == 2 && two[0] == 'b' && two[1] == 'c', "read(char[])");
        check(r.read() == 'd', "position shared across forms");
        check(r.read() == -1, "EOF via read()");
        check(r.read(new char[2]) == -1, "EOF via read(char[])");
        check(new ArrayReader(new char[0]).read(new char[1]) == -1, "empty reader EOF");
        check(new ArrayReader(new char[]{'z'}).read(new char[0]) == 0, "zero-length read 0");
    }

    // ---- 2. base read(char[]) evaluates cbuf.length first -> null NPE ----
    static void testNullArray() throws Exception {
        final ArrayReader r = new ArrayReader(new char[3]);
        // Reader.read(char[]) is concrete and evaluates cbuf.length, so a null
        // array throws NPE before reaching the subclass's read(char[],int,int).
        checkThrows(NullPointerException.class, () -> r.read((char[]) null),
                    "read((char[])null) -> NPE");
    }

    // ---- 3. read(CharBuffer) via Readable ----
    static void testReadCharBuffer() throws Exception {
        ArrayReader r = new ArrayReader(new char[]{'x', 'y', 'z'});
        CharBuffer cb = CharBuffer.allocate(10);
        check(r.read(cb) == 3, "read(CharBuffer) count");
        cb.flip();
        check("xyz".contentEquals(cb), "read(CharBuffer) contents");
        check(r.read(CharBuffer.allocate(2)) == -1, "read(CharBuffer) at EOF");
    }

    // ---- 4. skip ----
    static void testSkip() throws Exception {
        ArrayReader r = new ArrayReader(new char[]{'a', 'b', 'c', 'd'});
        check(r.skip(2) == 2 && r.read() == 'c', "skip(2)");
        checkThrows(IllegalArgumentException.class, () -> r.skip(-1),
                    "skip(negative) -> IllegalArgumentException");
        check(r.skip(0) == 0, "skip(0) == 0");
        check(r.read() == 'd', "position unchanged by failed skip");
    }

    // ---- 5. defaults: markSupported/ready/mark/reset ----
    static void testDefaults() throws Exception {
        ArrayReader r = new ArrayReader(new char[]{'a', 'b'});
        check(!r.markSupported(), "base markSupported false");
        check(!r.ready(), "base ready false");
        checkThrows(IOException.class, () -> r.mark(5), "base mark() -> IOException");
        checkThrows(IOException.class, () -> r.reset(), "base reset() -> IOException");
        check(r.read() == 'a', "position unchanged by failed mark/reset");
    }

    // ---- 6. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        Reader sr = new StringReader("hi");
        check(sr.read() == 'h' && sr.read() == 'i', "StringReader via Reader ref");
        sr.close();
    }

    public static void main(String[] args) throws Exception {
        testReadDefaults();
        testNullArray();
        testReadCharBuffer();
        testSkip();
        testDefaults();
        testPolymorphicUse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

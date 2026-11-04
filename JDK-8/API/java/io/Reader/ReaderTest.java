/*
 * @test
 * @summary java.io.Reader API conformance: full contract coverage.
 *          Abstract character-input root. Default read()/read(char[]) derived from
 *          read(char[],int,int), read(CharBuffer), skip (negative->IAE), ready/mark/reset
 *          defaults, markSupported false, argument checking, polymorphic use.
 *          Deepening: exact delegation shapes (read()/read(char[]) derive from
 *          read(char[],int,int) with len 1 / len cbuf.length, no internal loop),
 *          default skip loop-until-EOF semantics with negative -> IAE "skip value
 *          is negative" (verified message), exact default mark()/reset() messages,
 *          read(CharBuffer) default (len == remaining, read-only buffer, null NPE),
 *          structure via reflection.
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

    /** Probe that records every read(char[],int,int) delegation. */
    static class Counting extends Reader {
        final char[] data;
        int pos;
        int calls;
        final StringBuilder lens = new StringBuilder();
        Counting(String s) { data = s.toCharArray(); }
        @Override public int read(char[] cbuf, int off, int len) {
            calls++;
            lens.append(len).append(',');
            if (len == 0) return 0;
            int n = 0;
            while (n < len && pos < data.length) cbuf[off + n++] = data[pos++];
            return n == 0 ? -1 : n;
        }
        @Override public void close() { }
    }

    /** Runs r and returns the thrown Throwable, or null if nothing was thrown. */
    static Throwable capture(ThrowingRunnable r) {
        try { r.run(); } catch (Throwable t) { return t; }
        return null;
    }

    // ---- 7. structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Reader");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.Readable.class.isAssignableFrom(c), "implements Readable");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("read", char[].class, int.class, int.class).getModifiers()),
              "read(char[],int,int) is abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("close").getModifiers()),
              "close() is abstract");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredField("lock").getModifiers()),
              "protected lock field for subclass synchronization");
    }

    // ---- 8. exact delegation shapes of the derived read forms ----
    static void testReadDelegationShapes() throws Exception {
        Counting r = new Counting("abcde");
        check(r.read() == 'a', "read() returns the first char");
        check(r.calls == 1 && "1,".equals(r.lens.toString()),
              "read() delegates exactly one call with len 1");
        char[] three = new char[3];
        check(r.read(three) == 3 && "bcd".equals(new String(three)), "read(char[]) content");
        check(r.calls == 2 && "1,3,".equals(r.lens.toString()),
              "read(char[]) delegates one call with len == cbuf.length");
        check(r.read() == 'e', "position shared across all forms");
        check(r.read() == -1, "read() at EOF delegates and returns -1");
        check(r.calls == 4, "EOF read() still delegates");
        // the default read(char[]) does not loop over short reads
        final int[] tCalls = {0};
        final char[] mark = {'A'};
        Reader throttled = new Reader() {
            int round;
            @Override public int read(char[] cbuf, int off, int len) {
                tCalls[0]++;
                cbuf[off] = (char) (mark[0] + round++);
                return Math.min(len, 2);        // always reports a short read
            }
            @Override public void close() { }
        };
        char[] buf = new char[5];
        check(throttled.read(buf) == 2 && tCalls[0] == 1,
              "read(char[]) delegates once and returns the short count as-is");
        check(buf[2] == 0 && buf[3] == 0 && buf[4] == 0, "tail beyond the short read untouched");
    }

    // ---- 9. default skip: loop reads until n satisfied or EOF ----
    static void testSkipDefaultLoop() throws Exception {
        Counting r = new Counting("abcdefghij");
        check(r.skip(7) == 7, "skip(7) skips 7");
        check(r.calls == 1 && "7,".equals(r.lens.toString()),
              "one delegate call satisfies the skip: no extra EOF probe");
        check(r.read() == 'h', "position after skip");
        Counting e = new Counting("abc");
        check(e.skip(10) == 3, "skip past EOF returns only what was there");
        check(e.calls == 2, "default skip loops and calls read again after a short read");
        check("10,7,".equals(e.lens.toString()),
              "loop asks min(n, buffer) first, then the remainder");
        Counting z = new Counting("abc");
        check(z.skip(0) == 0 && z.calls == 0, "skip(0) delegates nothing");
        // skip crosses a surrogate pair without special handling
        Counting s = new Counting(new String(Character.toChars(0x1F600)) + "x");
        check(s.skip(2) == 2 && s.read() == 'x', "skip counts UTF-16 code units");
    }

    // ---- 10. skip(negative): exact IAE message, no state change ----
    static void testSkipNegativeDeep() throws Exception {
        Counting r = new Counting("abc");
        Throwable t = capture(() -> r.skip(-1));
        check(t instanceof IllegalArgumentException, "skip(-1) -> IllegalArgumentException");
        check("skip value is negative".equals(t.getMessage()),
              "exact message \"skip value is negative\" (verified on JDK 8 and 27)");
        check(r.calls == 0, "a rejected skip delegates nothing");
        check(r.read() == 'a', "position unchanged by the rejected skip");
    }

    // ---- 11. default ready/markSupported/mark/reset: exact behavior ----
    static void testDefaultsDeep() throws Exception {
        Counting r = new Counting("ab");
        check(!r.markSupported(), "default markSupported is false");
        check(!r.ready(), "default ready is false");
        Throwable m = capture(() -> r.mark(5));
        check(m instanceof IOException, "default mark() -> IOException");
        check("mark() not supported".equals(m.getMessage()),
              "exact message \"mark() not supported\"");
        Throwable rs = capture(() -> r.reset());
        check(rs instanceof IOException, "default reset() -> IOException");
        check("reset() not supported".equals(rs.getMessage()),
              "exact message \"reset() not supported\"");
        check(r.calls == 0, "failed mark/reset read nothing");
        check(r.read() == 'a', "position unchanged after the failed calls");
        check(!r.ready(), "ready() stays false even with data available");
    }

    // ---- 12. read(CharBuffer) default implementation ----
    static void testReadCharBufferDeep() throws Exception {
        Counting r = new Counting("abcdef");
        CharBuffer cb = CharBuffer.allocate(4);
        check(r.read(cb) == 4, "reads exactly remaining() chars");
        cb.flip();
        check("abcd".contentEquals(cb), "content placed at the buffer position");
        check(r.calls == 1 && "4,".equals(r.lens.toString()),
              "one delegate call with len == remaining()");
        check(r.read(CharBuffer.allocate(4)) == 2, "short read near EOF passes through");
        check(r.read(CharBuffer.allocate(4)) == -1, "EOF -> -1");
        // zero remaining: still one delegation, with len 0, and the len-0 result returned
        Counting z = new Counting("q");
        CharBuffer full = CharBuffer.allocate(1);
        full.put('x'); full.flip(); full.position(1);
        check(z.read(full) == 0, "zero remaining returns the len==0 result (0)");
        check(z.calls == 1 && "0,".equals(z.lens.toString()), "delegates once with len 0");
        // read-only buffer: the failure comes from the buffer, as a RuntimeException
        checkThrows(java.nio.ReadOnlyBufferException.class,
                    () -> new Counting("a").read(CharBuffer.wrap("x").asReadOnlyBuffer()),
                    "read-only buffer -> ReadOnlyBufferException");
        checkThrows(NullPointerException.class,
                    () -> new Counting("a").read((CharBuffer) null),
                    "read(null CharBuffer) -> NPE");
    }

    public static void main(String[] args) throws Exception {
        testReadDefaults();
        testNullArray();
        testReadCharBuffer();
        testSkip();
        testDefaults();
        testPolymorphicUse();
        testWhiteBox();
        testReadDelegationShapes();
        testSkipDefaultLoop();
        testSkipNegativeDeep();
        testDefaultsDeep();
        testReadCharBufferDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

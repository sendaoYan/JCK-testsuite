/*
 * @test
 * @summary java.io.Writer API conformance: full contract coverage.
 *          Abstract character-output root. write(int)/write(String)/write(String,off,len)/
 *          append(...) derived from write(char[],int,int); append returns this;
 *          flush/close abstract; null argument and bounds checking; polymorphic use.
 * @run main WriterTest
 */

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;

public class WriterTest {
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

    /** Minimal subclass: only write(char[],int,int), flush, close implemented. */
    static class Recording extends Writer {
        final StringBuilder sb = new StringBuilder();
        int flushes, closes;
        @Override public void write(char[] cbuf, int off, int len) { sb.append(cbuf, off, len); }
        @Override public void flush() { flushes++; }
        @Override public void close() { closes++; }
    }

    // ---- 1. every default write form routes through write(char[],int,int) ----
    static void testWriteForms() throws Exception {
        Recording w = new Recording();
        w.write('h');                          // write(int) low 16 bits
        w.write("ij");                         // write(String)
        w.write("klm".toCharArray());          // write(char[])
        w.write("XYZ", 1, 1);                  // write(String,off,len)
        w.write("abc".toCharArray(), 2, 1);
        check("hijklmYc".contentEquals(w.sb), "all write forms captured: " + w.sb);

        w.write("q", 0, 0);
        w.write(new char[1], 0, 0);
        check("hijklmYc".contentEquals(w.sb), "zero-length writes are no-ops");
    }

    // ---- 2. append family returns this and writes content ----
    static void testAppend() throws Exception {
        Recording a = new Recording();
        check(a.append('A') == a, "append(char) returns this");
        check(a.append("BC") == a, "append(CharSequence) returns this");
        check(a.append("CDE", 1, 3) == a, "append(CharSequence,int,int) returns this");
        check("ABCDE".contentEquals(a.sb), "append forms captured: " + a.sb);
        check(a.append((CharSequence) null) == a, "append(null CharSequence) returns this");
        check("null".contentEquals(a.sb.subSequence(5, 9)), "null appended as string \"null\"");
    }

    // ---- 3. flush/close dispatch through the base reference ----
    static void testFlushCloseDispatch() throws Exception {
        Recording w = new Recording();
        Writer base = w;
        base.flush();
        base.flush();
        base.close();
        check(w.flushes == 2 && w.closes == 1, "flush/close invoked through base reference");
    }

    // ---- 4. null and bounds checking ----
    static void testArgumentChecking() throws Exception {
        final Recording w = new Recording();
        checkThrows(NullPointerException.class, () -> w.write((char[]) null),
                    "write((char[])null) -> NPE");
        checkThrows(NullPointerException.class, () -> w.write((String) null),
                    "write((String)null) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], 2, 1),
                    "write(char[], bad off/len) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 0, 5),
                    "write(String, bad off/len) -> IndexOutOfBoundsException");
    }

    // ---- 5. polymorphic use ----
    static void testPolymorphicUse() throws Exception {
        StringWriter sw = new StringWriter();
        Writer ref = sw;
        ref.write("one");
        ref.append('!');
        ref.flush();
        check("one!".contentEquals(sw.getBuffer()), "StringWriter via Writer ref");
    }

    public static void main(String[] args) throws Exception {
        testWriteForms();
        testAppend();
        testFlushCloseDispatch();
        testArgumentChecking();
        testPolymorphicUse();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

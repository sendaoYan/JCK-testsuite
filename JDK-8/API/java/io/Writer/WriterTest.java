/*
 * @test
 * @summary java.io.Writer API conformance: full contract coverage.
 *          Abstract character-output root. write(int)/write(String)/write(String,off,len)/
 *          append(...) derived from write(char[],int,int); append returns this;
 *          flush/close abstract; null argument and bounds checking; polymorphic use.
 *          Deepening: exact delegation shapes with a counting probe (write(int) ->
 *          one write(char[],0,1) call carrying the low 16 bits; write(String) one call
 *          with len == s.length(); zero-length writes still delegate), null checks
 *          happen before delegation, append(null) writes "null", bounds matrix,
 *          structure via reflection (flush/close abstract, protected lock).
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

    /** Probe that records every write(char[],int,int) delegation. */
    static class Counting extends Writer {
        final StringBuilder data = new StringBuilder();
        int calls;
        final StringBuilder lens = new StringBuilder();
        @Override public void write(char[] cbuf, int off, int len) {
            calls++;
            lens.append(len).append(',');
            data.append(cbuf, off, len);
        }
        @Override public void flush() { }
        @Override public void close() { }
    }

    // ---- 6. structure via reflection ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Writer");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.Appendable.class.isAssignableFrom(c), "implements Appendable");
        check(java.io.Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(java.io.Flushable.class.isAssignableFrom(c), "implements Flushable");
        check(java.lang.reflect.Modifier.isAbstract(
                  c.getMethod("write", char[].class, int.class, int.class).getModifiers()),
              "write(char[],int,int) is abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("flush").getModifiers()),
              "flush() is abstract");
        check(java.lang.reflect.Modifier.isAbstract(c.getMethod("close").getModifiers()),
              "close() is abstract");
        check(java.lang.reflect.Modifier.isProtected(c.getDeclaredField("lock").getModifiers()),
              "protected lock field for subclass synchronization");
    }

    // ---- 7. exact delegation shapes ----
    static void testDelegationShapes() throws Exception {
        Counting w = new Counting();
        w.write('h');
        check(w.calls == 1 && "1,".equals(w.lens.toString()),
              "write(int) delegates one call with len 1");
        check("h".contentEquals(w.data), "write(int) content");
        w.write(0x10041);                       // low 16 bits = 'A'
        check("hA".contentEquals(w.data), "write(int) truncates to the low 16 bits");
        w.write(0);                             // NUL is written, not skipped
        check(w.data.charAt(2) == 0, "write(0) writes the NUL char");
        w.write("ij");
        check(w.calls == 4 && "1,1,1,2,".equals(w.lens.toString()),
              "write(String) delegates one call with len == s.length()");
        w.write("XYZ", 1, 1);
        check(w.calls == 5 && "1,1,1,2,1,".equals(w.lens.toString()),
              "write(String,off,len) delegates the region as one call");
        check("hA\u0000ijY".contentEquals(w.data), "all content correct");
        // zero-length writes delegate with len 0 (and write nothing)
        w.write("q", 0, 0);
        w.write(new char[1], 0, 0);
        check(w.calls == 7 && w.data.length() == 6, "zero-length writes delegate with len 0");
    }

    // ---- 8. null checks happen before any delegation ----
    static void testNullBeforeDelegation() throws Exception {
        final Counting w = new Counting();
        checkThrows(NullPointerException.class, () -> w.write((char[]) null),
                    "write((char[])null) -> NPE");
        check(w.calls == 0, "the null char[] never reached the subclass");
        checkThrows(NullPointerException.class, () -> w.write((String) null),
                    "write((String)null) -> NPE");
        check(w.calls == 0, "the null String never reached the subclass");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], 2, 1),
                    "write(char[],off,len) off+len > length -> IndexOutOfBoundsException");
        // write(char[],off,len) is abstract: the base does NOT pre-check the region,
        // so the subclass sees the call and the exception comes from the delegate
        check(w.calls == 1, "the abstract form reaches the subclass unvalidated");
        // full bounds matrix for the String region form
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", -1, 1),
                    "write(String,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 0, -1),
                    "write(String,0,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 1, 3),
                    "write(String,1,3) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 4, 0),
                    "write(String,4,0) -> IndexOutOfBoundsException");
        check(w.data.length() == 0, "failed writes left nothing behind");
        // append(csq,start,end) bounds are checked by CharSequence.subSequence
        checkThrows(IndexOutOfBoundsException.class, () -> w.append("abc", 2, 5),
                    "append end > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.append("abc", 3, 2),
                    "append start > end -> IndexOutOfBoundsException");
    }

    // ---- 9. append null CharSequence / null region ----
    static void testAppendNullDeep() throws Exception {
        Counting w = new Counting();
        w.append((CharSequence) null);
        check("null".contentEquals(w.data), "append(null) writes the four-char \"null\"");
        Counting r = new Counting();
        r.append((CharSequence) null, 0, 4);
        check("null".contentEquals(r.data), "append(null,0,4) writes \"null\" (empty region of the null literal)");
        Counting p = new Counting();
        p.append("abc", 1, 3);
        check("bc".contentEquals(p.data), "append region semantics [start,end)");
        p.append("abc", 2, 2);
        check("bc".contentEquals(p.data), "append empty region writes nothing");
    }

    public static void main(String[] args) throws Exception {
        testWriteForms();
        testAppend();
        testFlushCloseDispatch();
        testArgumentChecking();
        testPolymorphicUse();
        testWhiteBox();
        testDelegationShapes();
        testNullBeforeDelegation();
        testAppendNullDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

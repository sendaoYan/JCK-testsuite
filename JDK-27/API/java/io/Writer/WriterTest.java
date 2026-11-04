/*
 * @test
 * @summary java.io.Writer conformance: white-box structure (abstract character output
 *          root: Appendable + Closeable + Flushable) + black-box behavior (default
 *          write(int)/write(String)/write(String,off,len)/append(...) derived from
 *          write(char[],int,int); append returns this; nullWriter; null checks).
 *          Deepening: exact delegation shapes with a counting probe (write(int) ->
 *          one write(char[],0,1) call carrying the low 16 bits; write(String) one call
 *          with len == s.length(); zero-length writes still delegate), null checks
 *          happen before delegation, append(null) writes "null", bounds matrix,
 *          nullWriter full contract (append identity, null/OOB/close matrix).
 * @run main WriterTest
 */

import java.io.IOException;
import java.io.Writer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class WriterTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    /** Minimal subclass: only write(char[],int,int), flush, close implemented. */
    static class Recording extends Writer {
        final StringBuilder sb = new StringBuilder();
        int flushes, closes;
        @Override public void write(char[] cbuf, int off, int len) { sb.append(cbuf, off, len); }
        @Override public void flush() { flushes++; }
        @Override public void close() { closes++; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.io.Writer");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(java.lang.Appendable.class.isAssignableFrom(c), "Appendable");
        check(java.io.Closeable.class.isAssignableFrom(c), "Closeable");
        check(java.io.Flushable.class.isAssignableFrom(c), "Flushable");
        check(Modifier.isAbstract(c.getMethod("write", char[].class, int.class, int.class)
                                     .getModifiers()), "write(char[],int,int) abstract");
        check(Modifier.isAbstract(c.getMethod("flush").getModifiers()), "flush abstract");
        check(Modifier.isAbstract(c.getMethod("close").getModifiers()), "close abstract");
        check(Modifier.isProtected(c.getDeclaredField("lock").getModifiers()),
              "protected lock field");
        check(Modifier.isStatic(c.getMethod("nullWriter").getModifiers()), "nullWriter factory");
        for (String m : new String[]{"write", "append"})
            check(has(c, m), "has method " + m);
    }

    static boolean has(Class<?> c, String m) {
        for (Method mm : c.getMethods()) if (mm.getName().equals(m)) return true;
        return false;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // 1. every default write form routes through write(char[],int,int)
        Recording w = new Recording();
        w.write('h');                                       // write(int)
        w.write("ij");                                      // write(String)
        w.write("klm".toCharArray());                       // write(char[])
        w.write("XYZ", 1, 1);                               // write(String,off,len)
        w.write("abc".toCharArray(), 2, 1);
        check("hijklmYc".contentEquals(w.sb), "all write forms captured: " + w.sb);

        // 2. zero-length writes write nothing
        w.write("q", 0, 0);
        w.write(new char[1], 0, 0);
        check("hijklmYc".contentEquals(w.sb), "zero-length writes are no-ops");

        // 3. append family: content identical, returns this
        Recording a = new Recording();
        check(a.append('A') == a, "append(char) returns this");
        check(a.append("BC") == a, "append(CharSequence) returns this");
        check(a.append("CDE", 1, 3) == a, "append(CharSequence,int,int) returns this");
        check("ABCDE".contentEquals(a.sb), "append forms captured: " + a.sb);
        check(a.append((CharSequence) null) == a, "append(null CharSequence) writes \"null\"");
        check("null".contentEquals(a.sb.subSequence(5, 9)), "null appended as the string null");

        // 4. flush/close are the subclass's; called through the base reference
        Writer base = w;
        base.flush();
        base.flush();
        base.close();
        check(w.flushes == 2 && w.closes == 1, "flush/close invoked through base reference");

        // 5. null checks for array and String forms
        try {
            new Recording().write((char[]) null);
            check(false, "write(null char[]) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new Recording().write((String) null);
            check(false, "write(null String) must throw");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new Recording().write(new char[2], 2, 1);
            check(false, "out-of-bounds write must throw");
        } catch (IndexOutOfBoundsException expected) {
            passed++;
        }

        // 6. nullWriter: discards everything until closed
        Writer nw = Writer.nullWriter();
        nw.write('x');
        nw.write("str");
        nw.write(new char[]{'c'});
        nw.append('A');
        nw.append("BC");
        nw.flush();
        nw.close();
        try {
            nw.write('y');
            check(false, "nullWriter after close must throw");
        } catch (IOException ioe) {
            check("Stream closed".equals(ioe.getMessage()), "message: " + ioe.getMessage());
        }

        // 7. polymorphic use: JDK writer through the base reference
        java.io.StringWriter sw = new java.io.StringWriter();
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

    // ---- exact delegation shapes ----
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

    // ---- null checks happen before any delegation; bounds matrix ----
    static void testNullBeforeDelegation() throws Exception {
        final Counting w = new Counting();
        try { w.write((char[]) null); check(false, "write(null char[]) must throw"); }
        catch (NullPointerException expected) { passed++; }
        check(w.calls == 0, "the null char[] never reached the subclass");
        try { w.write((String) null); check(false, "write(null String) must throw"); }
        catch (NullPointerException expected) { passed++; }
        check(w.calls == 0, "the null String never reached the subclass");
        try { w.write(new char[2], 2, 1); check(false, "out-of-bounds write must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        // write(char[],off,len) is abstract: the base does NOT pre-check the region,
        // so the subclass sees the call and the exception comes from the delegate
        check(w.calls == 1, "the abstract form reaches the subclass unvalidated");
        try { w.write("abc", -1, 1); check(false, "write(String,-1,1) must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { w.write("abc", 0, -1); check(false, "write(String,0,-1) must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { w.write("abc", 1, 3); check(false, "write(String,1,3) must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { w.write("abc", 4, 0); check(false, "write(String,4,0) must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        check(w.data.length() == 0, "failed writes left nothing behind");
        try { w.append("abc", 2, 5); check(false, "append end > length must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
        try { w.append("abc", 3, 2); check(false, "append start > end must throw"); }
        catch (IndexOutOfBoundsException expected) { passed++; }
    }

    // ---- append null CharSequence / null region ----
    static void testAppendNullDeep() throws Exception {
        Counting w = new Counting();
        w.append((CharSequence) null);
        check("null".contentEquals(w.data), "append(null) writes the four-char \"null\"");
        Counting r = new Counting();
        r.append((CharSequence) null, 0, 4);
        check("null".contentEquals(r.data), "append(null,0,4) writes \"null\"");
        Counting p = new Counting();
        p.append("abc", 1, 3);
        check("bc".contentEquals(p.data), "append region semantics [start,end)");
        p.append("abc", 2, 2);
        check("bc".contentEquals(p.data), "append empty region writes nothing");
    }

    // ---- nullWriter full contract ----
    static void testNullWriterDeep() throws Exception {
        Writer nw = Writer.nullWriter();
        // every write/append form is discarded without complaint
        nw.write('x');
        nw.write("str");
        nw.write(new char[]{'c'});
        nw.write("abc", 1, 1);
        nw.write(new char[0]);
        nw.append('A');
        nw.append("BC");
        nw.append((CharSequence) null);          // default append converts to "null"
        nw.append("DEF", 1, 2);
        nw.flush();
        check(nw.append('z') == nw, "append returns the nullWriter itself");
        check(nw.append("xy") == nw, "append(CharSequence) returns the nullWriter itself");
        // null arguments are still rejected
        Throwable n1 = tryCatch(() -> nw.write((String) null));
        check(n1 instanceof NullPointerException, "nullWriter write((String)null) -> NPE");
        Throwable n2 = tryCatch(() -> nw.write((char[]) null));
        check(n2 instanceof NullPointerException, "nullWriter write((char[])null) -> NPE");
        // bounds are still validated
        Throwable b1 = tryCatch(() -> nw.write("abc", 0, 9));
        check(b1 instanceof IndexOutOfBoundsException, "nullWriter write OOB -> IndexOutOfBoundsException");
        Throwable b2 = tryCatch(() -> nw.write(new char[2], 0, 9));
        check(b2 instanceof IndexOutOfBoundsException, "nullWriter write(char[],OOB) -> IndexOutOfBoundsException");
        // close: writes/flushes fail afterwards with the standard message, close idempotent
        Writer closed = Writer.nullWriter();
        closed.close();
        closed.close();
        String msg = "Stream closed";
        Throwable c1 = tryCatch(() -> closed.write('y'));
        check(c1 instanceof IOException && msg.equals(c1.getMessage()), "closed write(int): \"Stream closed\"");
        Throwable c2 = tryCatch(() -> closed.write("s"));
        check(c2 instanceof IOException && msg.equals(c2.getMessage()), "closed write(String): \"Stream closed\"");
        Throwable c3 = tryCatch(() -> closed.write(new char[1]));
        check(c3 instanceof IOException && msg.equals(c3.getMessage()), "closed write(char[]): \"Stream closed\"");
        Throwable c4 = tryCatch(() -> closed.append('a'));
        check(c4 instanceof IOException && msg.equals(c4.getMessage()), "closed append(char): \"Stream closed\"");
        Throwable c5 = tryCatch(() -> closed.flush());
        check(c5 instanceof IOException && msg.equals(c5.getMessage()), "closed flush(): \"Stream closed\"");
    }

    interface Op { void run() throws Exception; }

    static Throwable tryCatch(Op r) {
        try { r.run(); } catch (Throwable t) { return t; }
        return null;
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDelegationShapes();
        testNullBeforeDelegation();
        testAppendNullDeep();
        testNullWriterDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

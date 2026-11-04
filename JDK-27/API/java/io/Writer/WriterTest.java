/*
 * @test
 * @summary java.io.Writer conformance: white-box structure (abstract character output
 *          root: Appendable + Closeable + Flushable) + black-box behavior (default
 *          write(int)/write(String)/write(String,off,len)/append(...) derived from
 *          write(char[],int,int); append returns this; nullWriter; null checks).
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.io.BufferedWriter conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (null->NPE, size<=0->IAE),
 *          write(int) low 16 bits, write(char[],off,len) validation, write(String,off,len),
 *          newLine, flush/close semantics.
 *          Deepened (contract level): exact newLine char sequence via probe sink, buffer
 *          auto-flush boundary (size 4 -> 5th write sinks the first 4), long writes pass
 *          through, write(String,char[]) edge quirks (null len-0 no-throw, negative len),
 *          append delegation, flush/close propagation counts, closed-write matrix.
 * @run main BufferedWriterTest
 */

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

public class BufferedWriterTest {
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
        Class<?> c = Class.forName("java.io.BufferedWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "write", int.class);
        hasMethod(c, "newLine");
        hasMethod(c, "flush");
        hasMethod(c, "close");
    }

    // ---- 1. constructors ----
    static void testConstructors() throws Exception {
        checkThrows(NullPointerException.class, () -> new BufferedWriter(null),
                    "BufferedWriter(null) -> NPE");
        checkThrows(IllegalArgumentException.class, () -> new BufferedWriter(new StringWriter(), 0),
                    "size 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> new BufferedWriter(new StringWriter(), -1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. write(int) low 16 bits ----
    static void testWriteInt() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        w.write('A');
        w.write(0x10041);
        w.flush();
        check("AA".equals(sw.toString()), "write(int) low 16 bits");
    }

    // ---- 3. write(char[],off,len) validation ----
    static void testWriteArrayValidation() throws Exception {
        final BufferedWriter w = new BufferedWriter(new StringWriter());
        checkThrows(NullPointerException.class, () -> w.write((char[]) null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], -1, 1),
                    "write(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], 0, 3),
                    "write(c,0,3) -> IndexOutOfBoundsException");
        w.write(new char[1], 0, 0);
        check(true, "len==0 is a no-op");
    }

    // ---- 4. write(String,off,len) ----
    static void testWriteString() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        w.write("hello world", 0, 5);
        w.flush();
        check("hello".equals(sw.toString()), "write(String,off,len) content");

        final BufferedWriter n = new BufferedWriter(new StringWriter());
        checkThrows(NullPointerException.class, () -> n.write((String) null, 0, 1),
                    "write((String)null,0,1) -> NPE");
    }

    // ---- 5. newLine ----
    static void testNewLine() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        w.write("a");
        w.newLine();
        w.write("b");
        w.flush();
        check(("a" + System.lineSeparator() + "b").equals(sw.toString()), "newLine uses line separator");
    }

    // ---- 6. flush/close ----
    static void testFlushClose() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        w.write("buffered");
        check(sw.toString().length() == 0, "buffered before flush");
        w.flush();
        check("buffered".equals(sw.toString()), "flush writes buffer");
        w.write(" more");
        w.close();
        check("buffered more".equals(sw.toString()), "close flushes remaining");
    }

    // Probe sink recording every char, flush and close for exact delegation checks.
    static class ProbeWriter extends java.io.Writer {
        StringBuilder sb = new StringBuilder();
        int flushes, closes;
        public void write(char[] c, int off, int len) { sb.append(c, off, len); }
        public void flush() { flushes++; }
        public void close() { closes++; }
        public String sunk() { return sb.toString(); }
    }

    static void checkMsg(Class<? extends Throwable> expected, String sub,
                         ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && t.getMessage() != null && t.getMessage().contains(sub)) {
                passed++;
                return;
            }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " containing \"" + sub + "\" but got " + t, t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " containing \"" + sub + "\" but nothing was thrown");
    }

    // ---- 7. constructors with size, null reader ----
    static void testConstructorsDeep() throws Exception {
        checkThrows(NullPointerException.class, () -> new BufferedWriter(null, 5),
                    "BufferedWriter(null,5) -> NPE");
        // size 1 is legal: each write sinks the previous buffered char
        ProbeWriter pw = new ProbeWriter();
        BufferedWriter w = new BufferedWriter(pw, 1);
        w.write('a');
        check(pw.sunk().isEmpty(), "first char buffered in size-1 buffer");
        w.write('b');
        check("a".equals(pw.sunk()), "second write sinks the first char");
        w.write('c');
        check("ab".equals(pw.sunk()), "third write sinks the second char");
        w.flush();
        check("abc".equals(pw.sunk()), "flush sinks the last char");
        w.close();
    }

    // ---- 8. newLine exact char sequence ----
    static void testNewLineExactChars() throws Exception {
        ProbeWriter pw = new ProbeWriter();
        BufferedWriter w = new BufferedWriter(pw, 16);
        w.write('a');
        w.newLine();
        w.write('b');
        w.flush();
        String sunk = pw.sunk();
        String sep = System.lineSeparator();
        check(sunk.length() == 1 + sep.length() + 1, "newLine emits exactly lineSeparator length");
        check(sunk.charAt(0) == 'a' && sunk.charAt(sunk.length() - 1) == 'b',
              "chars around newLine preserved");
        check(sep.equals(sunk.substring(1, sunk.length() - 1)),
              "newLine chars are exactly System.lineSeparator()");
    }

    // ---- 9. buffer auto-flush boundary (size 4) ----
    static void testBufferBoundaryAutoFlush() throws Exception {
        ProbeWriter pw = new ProbeWriter();
        BufferedWriter w = new BufferedWriter(pw, 4);
        w.write('a');
        w.write('b');
        w.write('c');
        w.write('d');
        check(pw.sunk().isEmpty(), "4 chars buffered in size-4 buffer (nothing sunk)");
        check(pw.flushes == 0, "no underlying flush while only buffering");
        w.write('e');                        // buffer full -> flushBuffer sinks first 4
        check("abcd".equals(pw.sunk()), "5th write sinks the buffered 4 chars");
        check(pw.flushes == 0, "auto sink does not flush the underlying writer");
        w.flush();
        check("abcde".equals(pw.sunk()), "explicit flush sinks remaining char");
        check(pw.flushes == 1, "explicit flush flushes underlying exactly once");
        w.close();
        check(pw.closes == 1, "close closes underlying exactly once");
        check("abcde".equals(pw.sunk()), "content preserved after close");
    }

    // ---- 10. writes longer than the buffer pass through ----
    static void testLongWritePassesThrough() throws Exception {
        ProbeWriter pw = new ProbeWriter();
        BufferedWriter w = new BufferedWriter(pw, 4);
        w.write("abcdefgh");
        check("abcdefgh".equals(pw.sunk()), "write(String) longer than buffer sinks directly");
        w.flush();
        check("abcdefgh".equals(pw.sunk()), "no duplication on subsequent flush");
        check(pw.flushes == 1, "flush propagated");

        ProbeWriter pw2 = new ProbeWriter();
        BufferedWriter w2 = new BufferedWriter(pw2, 4);
        w2.write(new char[]{'1', '2', '3', '4', '5'}, 0, 5);
        check("12345".equals(pw2.sunk()), "write(char[],0,5) longer than buffer sinks directly");
    }

    // ---- 11. write(String,...) edge quirks (probed contract) ----
    static void testWriteStringEdgeCases() throws Exception {
        // (String)null with len==0 does NOT throw on either JDK (loop body never runs)
        BufferedWriter n0 = new BufferedWriter(new StringWriter());
        n0.write((String) null, 0, 0);
        check(true, "write((String)null,0,0) is a no-op (no NPE)");

        // off+len-1 < off with negative len: also a silent no-op
        StringWriter sw = new StringWriter();
        BufferedWriter neg = new BufferedWriter(sw);
        neg.write("abc", 2, -1);
        neg.flush();
        check(sw.toString().isEmpty(), "write(s,2,-1) writes nothing");

        checkThrows(StringIndexOutOfBoundsException.class,
                    () -> new BufferedWriter(new StringWriter()).write("abc", 1, 5),
                    "write(String,1,5) beyond length -> StringIndexOutOfBoundsException");

        StringWriter reg = new StringWriter();
        BufferedWriter rw = new BufferedWriter(reg);
        rw.write("0123456789", 2, 3);
        rw.flush();
        check("234".equals(reg.toString()), "write(String,off,len) exact region");

        StringWriter edge = new StringWriter();
        BufferedWriter ew = new BufferedWriter(edge);
        ew.write("abc", 3, 0);
        ew.flush();
        check(edge.toString().isEmpty(), "write(s,len==0 at end) no-op");
    }

    // ---- 12. write(char[],...) edge cases ----
    static void testWriteArrayEdgeCases() throws Exception {
        checkThrows(NullPointerException.class,
                    () -> new BufferedWriter(new StringWriter()).write((char[]) null, 0, 0),
                    "write((char[])null,0,0) -> NPE (bounds use cbuf.length)");
        checkThrows(IndexOutOfBoundsException.class,
                    () -> new BufferedWriter(new StringWriter()).write(new char[2], 1, Integer.MAX_VALUE),
                    "write(cbuf,1,MAX) overflow -> IndexOutOfBoundsException");
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        w.write(new char[]{'w', 'x', 'y', 'z'}, 1, 2);
        w.flush();
        check("xy".equals(sw.toString()), "write(char[],off,len) exact region");
        w.write(new char[1], 0, 0);
        w.flush();
        check("xy".equals(sw.toString()), "len==0 array write is a no-op");
    }

    // ---- 13. append delegation to write ----
    static void testAppendDelegation() throws Exception {
        StringWriter sw = new StringWriter();
        BufferedWriter w = new BufferedWriter(sw);
        java.io.Writer ret = w.append('x');
        check(ret == w, "append(char) returns this");
        w.append("AB");
        w.append("cdef", 1, 3);
        w.flush();
        check("xABde".equals(sw.toString()), "append(char)/CharSequence/region all encoded via write");
    }

    // ---- 14. flush/close propagation semantics ----
    static void testFlushPropagation() throws Exception {
        ProbeWriter pw = new ProbeWriter();
        BufferedWriter w = new BufferedWriter(pw, 100);
        w.write("m");
        w.flush();
        w.flush();
        check(pw.flushes == 2, "each flush() flushes the underlying writer once");
        check("m".equals(pw.sunk()), "content sunk by flush");
        w.write("n");
        w.close();
        check("mn".equals(pw.sunk()), "close flushes remaining buffered chars");
        check(pw.closes == 1, "underlying closed exactly once");
        w.close();
        check(pw.closes == 1, "close is idempotent (no second underlying close)");
    }

    // ---- 15. closed-state write matrix (exact messages) ----
    static void testClosedWriteMatrix() throws Exception {
        final BufferedWriter w = new BufferedWriter(new StringWriter());
        w.close();
        checkMsg(IOException.class, "Stream closed", () -> w.write('x'), "closed write(int)");
        checkMsg(IOException.class, "Stream closed",
                () -> w.write(new char[]{'x'}, 0, 1), "closed write(char[])");
        checkMsg(IOException.class, "Stream closed", () -> w.write("x"), "closed write(String)");
        checkMsg(IOException.class, "Stream closed", () -> w.newLine(), "closed newLine()");
        checkMsg(IOException.class, "Stream closed", () -> w.flush(), "closed flush()");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteString();
        testNewLine();
        testFlushClose();
        testConstructorsDeep();
        testNewLineExactChars();
        testBufferBoundaryAutoFlush();
        testLongWritePassesThrough();
        testWriteStringEdgeCases();
        testWriteArrayEdgeCases();
        testAppendDelegation();
        testFlushPropagation();
        testClosedWriteMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

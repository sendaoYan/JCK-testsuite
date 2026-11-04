/*
 * @test
 * @summary java.io.BufferedWriter conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (null->NPE, size<=0->IAE),
 *          write(int) low 16 bits, write(char[],off,len) validation, write(String,off,len),
 *          newLine, flush/close semantics.
 * @run main BufferedWriterTest
 */

import java.io.BufferedWriter;
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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteString();
        testNewLine();
        testFlushClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

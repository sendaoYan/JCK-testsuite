/*
 * @test
 * @summary java.io.StringWriter conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (negative->IAE), write(int) low 16 bits,
 *          write(char[],off,len) validation, write(String) null->"null", write(String,off,len),
 *          append variants, getBuffer live semantics, toString, flush/close no-op.
 * @run main StringWriterTest
 */

import java.io.StringWriter;
import java.lang.reflect.Modifier;

public class StringWriterTest {
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
        Class<?> c = Class.forName("java.io.StringWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "write", int.class);
        hasMethod(c, "append", CharSequence.class);
        hasMethod(c, "getBuffer");
        hasMethod(c, "toString");
        hasMethod(c, "flush");
    }

    // ---- 1. constructors ----
    static void testConstructors() {
        StringWriter def = new StringWriter();
        check(def.toString().length() == 0, "default empty");
        checkThrows(IllegalArgumentException.class, () -> new StringWriter(-1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. write(int) low 16 bits ----
    static void testWriteInt() {
        StringWriter w = new StringWriter();
        w.write('A');
        w.write(0x10041);
        check("AA".equals(w.toString()), "write(int) low 16 bits");
    }

    // ---- 3. write(char[],off,len) validation ----
    static void testWriteArrayValidation() {
        final StringWriter w = new StringWriter();
        checkThrows(NullPointerException.class, () -> w.write((char[]) null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], -1, 1),
                    "write(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], 0, 3),
                    "write(c,0,3) -> IndexOutOfBoundsException");
        w.write(new char[1], 0, 0);
        check(w.toString().length() == 0, "len==0 no-op");
    }

    // ---- 4. write(String) / write(String,off,len) ----
    static void testWriteString() {
        StringWriter w = new StringWriter();
        w.write("hello");
        w.write((String) null);
        check("hellonull".equals(w.toString()), "write(String) + null -> \"null\"");

        StringWriter sub = new StringWriter();
        sub.write("hello world", 0, 5);
        check("hello".equals(sub.toString()), "write(String,off,len) content");

        final StringWriter n = new StringWriter();
        // JDK 9+: write(String,off,len) delegates to StringBuffer.append(null,off,len) -> "null"
        n.write((String) null, 0, 4);
        check("null".equals(n.toString()), "write((String)null,0,4) appends \"null\" in JDK 9+");
        checkThrows(IndexOutOfBoundsException.class, () -> n.write("abc", 0, 5),
                    "write(String, bad off/len) -> IndexOutOfBoundsException");
    }

    // ---- 5. append variants ----
    static void testAppend() {
        StringWriter w = new StringWriter();
        w.append("ab");
        w.append('c');
        w.append((CharSequence) null);
        w.append("XY", 0, 1);
        check("abcnullX".equals(w.toString()), "append variants incl. null csq");
    }

    // ---- 6. getBuffer live semantics ----
    static void testGetBuffer() {
        StringWriter w = new StringWriter();
        w.write("ab");
        StringBuffer b = w.getBuffer();
        b.append("cd");
        check("abcd".equals(w.toString()), "getBuffer returns live buffer");
    }

    // ---- 7. flush/close no-op ----
    static void testFlushClose() throws Exception {
        StringWriter w = new StringWriter();
        w.write("data");
        w.flush();
        w.close();
        w.close();
        w.write("more");
        check("datamore".equals(w.toString()), "flush/close are no-ops");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteString();
        testAppend();
        testGetBuffer();
        testFlushClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

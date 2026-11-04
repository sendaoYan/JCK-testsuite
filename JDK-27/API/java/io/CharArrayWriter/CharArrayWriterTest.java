/*
 * @test
 * @summary java.io.CharArrayWriter conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (negative->IAE), write(int) low 16 bits,
 *          write(char[],off,len) validation, write(String,off,len), writeTo, append variants,
 *          reset/size/toCharArray copy, toString, flush/close no-op.
 * @run main CharArrayWriterTest
 */

import java.io.CharArrayWriter;
import java.io.StringWriter;
import java.lang.reflect.Modifier;

public class CharArrayWriterTest {
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
        Class<?> c = Class.forName("java.io.CharArrayWriter");
        check(!c.isInterface() && !Modifier.isAbstract(c.getModifiers()), "is concrete class");
        check(c.getSuperclass() == java.io.Writer.class, "extends Writer");
        hasMethod(c, "write", int.class);
        hasMethod(c, "writeTo", java.io.Writer.class);
        hasMethod(c, "toCharArray");
        hasMethod(c, "size");
        hasMethod(c, "reset");
    }

    // ---- 1. constructors ----
    static void testConstructors() {
        CharArrayWriter def = new CharArrayWriter();
        check(def.size() == 0, "default size 0");
        checkThrows(IllegalArgumentException.class, () -> new CharArrayWriter(-1),
                    "negative size -> IllegalArgumentException");
    }

    // ---- 2. write(int) ----
    static void testWriteInt() {
        CharArrayWriter w = new CharArrayWriter();
        w.write('A');
        w.write(0x10041);
        check("AA".equals(w.toString()), "write(int) low 16 bits");
    }

    // ---- 3. write(char[],off,len) validation ----
    static void testWriteArrayValidation() {
        final CharArrayWriter w = new CharArrayWriter();
        checkThrows(NullPointerException.class, () -> w.write((char[]) null, 0, 1),
                    "write(null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], -1, 1),
                    "write(c,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write(new char[2], 0, 3),
                    "write(c,0,3) -> IndexOutOfBoundsException");
        w.write(new char[1], 0, 0);
        check(w.size() == 0, "len==0 no-op");
    }

    // ---- 4. write(String) / write(String,off,len) ----
    static void testWriteString() {
        CharArrayWriter w = new CharArrayWriter();
        w.write("hello world", 0, 5);
        check("hello".equals(w.toString()), "write(String,off,len) content");

        final CharArrayWriter n = new CharArrayWriter();
        checkThrows(NullPointerException.class, () -> n.write((String) null, 0, 1),
                    "write((String)null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> n.write("abc", 0, 5),
                    "write(String, bad off/len) -> IndexOutOfBoundsException");
    }

    // ---- 5. writeTo ----
    static void testWriteTo() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("abc");
        StringWriter dest = new StringWriter();
        w.writeTo(dest);
        check("abc".equals(dest.toString()), "writeTo copies content");
        check(w.size() == 3, "writeTo does not consume source");

        checkThrows(NullPointerException.class, () -> w.writeTo(null), "writeTo(null) -> NPE");
    }

    // ---- 6. append variants ----
    static void testAppend() {
        CharArrayWriter w = new CharArrayWriter();
        w.append("ab");
        w.append('c');
        w.append((CharSequence) null);
        check("abcnull".equals(w.toString()), "append variants incl. null csq");
    }

    // ---- 7. reset / size / toCharArray copy ----
    static void testResetSizeToCharArray() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("hello");
        check(w.size() == 5, "size");
        char[] first = w.toCharArray();
        first[0] = 'X';
        char[] second = w.toCharArray();
        check(second[0] == 'h', "toCharArray returns a fresh copy");
        check("hello".equals(w.toString()), "toString");

        w.reset();
        check(w.size() == 0 && w.toCharArray().length == 0, "reset clears");
        w.write('z');
        check("z".equals(w.toString()), "reuse after reset");
    }

    // ---- 8. flush/close no-op ----
    static void testFlushClose() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("data");
        w.flush();
        w.close();
        w.close();
        w.write("more");
        check("datamore".equals(w.toString()), "flush/close no-ops");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteString();
        testWriteTo();
        testAppend();
        testResetSizeToCharArray();
        testFlushClose();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

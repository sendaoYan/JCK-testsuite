/*
 * @test
 * @summary java.io.StringWriter API conformance: full contract coverage.
 *          Constructors (negative size->IAE), write(int) low 16 bits,
 *          write(char[],off,len) validation (NPE/IOBE), write(String) null->"null",
 *          write(String,off,len) content + null->NPE, append variants (incl. null csq->"null"),
 *          getBuffer live semantics, toString, flush/close no-op.
 * @run main StringWriterTest
 */

import java.io.StringWriter;

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
        w.write(0x10041);               // low 16 bits = 'A'
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
        w.write((String) null);         // StringBuffer.append(null) -> "null"
        check("hellonull".equals(w.toString()), "write(String) + null -> \"null\"");

        StringWriter sub = new StringWriter();
        sub.write("hello world", 0, 5);
        check("hello".equals(sub.toString()), "write(String,off,len) content");

        final StringWriter n = new StringWriter();
        checkThrows(NullPointerException.class, () -> n.write((String) null, 0, 1),
                    "write((String)null,0,1) -> NPE");
        checkThrows(IndexOutOfBoundsException.class, () -> n.write("abc", 0, 5),
                    "write(String, bad off/len) -> IndexOutOfBoundsException");
    }

    // ---- 5. append variants ----
    static void testAppend() {
        StringWriter w = new StringWriter();
        w.append("ab");
        w.append('c');
        w.append((CharSequence) null);  // -> "null"
        w.append("XY", 0, 1);           // -> "X"
        check("abcnullX".equals(w.toString()), "append variants incl. null csq");
    }

    // ---- 6. getBuffer live semantics ----
    static void testGetBuffer() {
        StringWriter w = new StringWriter();
        w.write("ab");
        StringBuffer b = w.getBuffer();
        b.append("cd");                 // live buffer: mutation is visible
        check("abcd".equals(w.toString()), "getBuffer returns live buffer");
    }

    // ---- 7. flush/close no-op ----
    static void testFlushClose() throws Exception {
        StringWriter w = new StringWriter();
        w.write("data");
        w.flush();
        w.close();
        w.close();                      // idempotent
        w.write("more");                // still usable after close
        check("datamore".equals(w.toString()), "flush/close are no-ops");
    }

    public static void main(String[] args) throws Exception {
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

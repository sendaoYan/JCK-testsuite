/*
 * @test
 * @summary java.io.StringWriter API conformance: full contract coverage.
 *          Constructors (negative size->IAE), write(int) low 16 bits,
 *          write(char[],off,len) validation (NPE/IOBE), write(String) null->"null",
 *          write(String,off,len) content + null->NPE, append variants (incl. null csq->"null"),
 *          getBuffer live semantics, toString, flush/close no-op.
 *          Deepening: write(int) low-16 boundary matrix, inherited write(char[]) null NPE,
 *          write(String,off,len) full bounds matrix (failed writes leave the buffer
 *          untouched), covariant append returns, append bounds, getBuffer identity +
 *          immutable toString snapshot, initial-capacity growth, close no-op matrix.
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

    // ---- 8. write(int) low-16-bit boundary matrix ----
    static void testWriteIntEdge() {
        StringWriter w = new StringWriter();
        w.write(0);                // NUL
        w.write(0xD7FF);           // last code point before the surrogate block
        w.write(0xE000);           // first private-use code point
        w.write(0xFFFF);           // largest BMP value
        w.write(0x10041);          // low 16 bits = 'A'
        w.write(0x1FFFF);          // low 16 bits = 0xFFFF
        StringBuffer b = w.getBuffer();
        check(b.length() == 6, "six chars written");
        check(b.charAt(0) == 0, "write(0) stores the NUL char");
        check(b.charAt(1) == 0xD7FF && b.charAt(2) == 0xE000, "BMP boundary values stored");
        check(b.charAt(3) == 0xFFFF, "0xFFFF stored");
        check(b.charAt(4) == 'A', "only the low 16 bits of 0x10041 are written");
        check(b.charAt(5) == 0xFFFF, "only the low 16 bits of 0x1FFFF are written");
    }

    // ---- 9. inherited write(char[]) from Writer ----
    static void testWriteCharArray() throws Exception {
        StringWriter w = new StringWriter();
        w.write("hi".toCharArray());            // Writer.write(char[]) -> write(cbuf,0,2)
        check("hi".equals(w.toString()), "inherited write(char[]) delegates");
        w.write(new char[0]);
        check("hi".equals(w.toString()), "write(empty char[]) is a no-op");
        final StringWriter n = new StringWriter();
        checkThrows(NullPointerException.class, () -> n.write((char[]) null),
                    "inherited write((char[])null) -> NPE");
        check(n.toString().length() == 0, "nothing written by the failed call");
    }

    // ---- 10. write(String,off,len) bounds matrix ----
    static void testWriteStringBounds() {
        final StringWriter w = new StringWriter();
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", -1, 1),
                    "negative off -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 0, -1),
                    "negative len -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 1, 3),
                    "off+len > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 4, 0),
                    "off > length -> IndexOutOfBoundsException");
        check(w.toString().length() == 0, "failed writes leave the buffer untouched");
        w.write("", 0, 0);
        check(w.toString().length() == 0, "write(\"\",0,0) is a no-op");
        w.write("abc", 3, 0);                        // off == length, len == 0: empty region
        check(w.toString().length() == 0, "write(str, length, 0) is a legal empty region");
        w.write("abcde", 2, 3);
        check("cde".equals(w.toString()), "mid-string region write");
        // a whole surrogate pair goes through the region form verbatim
        String sup = new String(Character.toChars(0x1F600));
        StringWriter s = new StringWriter();
        s.write(sup, 0, sup.length());
        StringBuffer sb = s.getBuffer();
        check(sb.length() == 2 && sb.charAt(0) == 0xD83D && sb.charAt(1) == 0xDE00,
              "surrogate pair written verbatim");
    }

    // ---- 11. append: covariant returns and bounds ----
    static void testAppendDeep() {
        StringWriter a = new StringWriter();
        StringWriter r1 = a.append('x');              // covariant override -> StringWriter
        StringWriter r2 = a.append("yz");
        StringWriter r3 = a.append((CharSequence) null, 0, 4);
        check(r1 == a && r2 == a && r3 == a, "append overloads are covariant and return this");
        check("xyznull".equals(a.toString()), "append(null,0,4) writes the four-char \"null\"");
        checkThrows(IndexOutOfBoundsException.class, () -> a.append("abc", 2, 5),
                    "append(csq,start,end) end > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> a.append("abc", 3, 2),
                    "append(csq,start,end) start > end -> IndexOutOfBoundsException");
        check("xyznull".equals(a.toString()), "failed appends leave the buffer untouched");
    }

    // ---- 12. getBuffer identity, live view, immutable toString snapshot ----
    static void testGetBufferDeep() {
        StringWriter w = new StringWriter();
        w.write("ab");
        StringBuffer b1 = w.getBuffer();
        check(w.getBuffer() == b1, "getBuffer returns the same live buffer every time");
        check("ab".equals(w.toString()) && w.toString().contentEquals(b1),
              "toString() always matches the buffer");
        String snapshot = w.toString();
        w.write("cd");
        check("ab".equals(snapshot), "a toString() result is an immutable snapshot");
        check("abcd".contentEquals(b1), "writes are visible through an earlier getBuffer ref");
        b1.append('e');                               // direct mutation of the exposed buffer
        check("abcde".equals(w.toString()), "mutating the exposed buffer is visible");
        check(w.getBuffer().length() == 5, "length is tracked");
    }

    // ---- 13. initial capacity ----
    static void testInitialCapacity() {
        StringWriter zero = new StringWriter(0);      // legal: grows on demand
        zero.write("abc");
        check("abc".equals(zero.toString()), "zero initial capacity still accepts writes");
        StringWriter one = new StringWriter(1);
        for (int i = 0; i < 100; i++) one.write('x');
        check(one.getBuffer().length() == 100, "growth far beyond the initial capacity");
        check(one.toString().length() == 100, "all 100 chars present after growth");
    }

    // ---- 14. close is a documented no-op ----
    static void testCloseNoopDeep() throws Exception {
        StringWriter w = new StringWriter();
        w.write("a");
        w.close();
        // spec: closing a StringWriter has no effect; every operation keeps working
        w.write('b');
        w.write("c");
        w.write("def".toCharArray(), 0, 3);
        w.append('g');
        w.flush();
        check("abcdefg".equals(w.toString()), "all operations still work after close");
        w.close();                                    // repeated close is harmless
        check(w.getBuffer().length() == 7, "getBuffer after close");
        check("abcdefg".equals(w.toString()), "toString after close");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testWriteInt();
        testWriteArrayValidation();
        testWriteString();
        testAppend();
        testGetBuffer();
        testFlushClose();
        testWriteIntEdge();
        testWriteCharArray();
        testWriteStringBounds();
        testAppendDeep();
        testGetBufferDeep();
        testInitialCapacity();
        testCloseNoopDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

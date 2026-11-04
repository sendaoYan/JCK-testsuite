/*
 * @test
 * @summary java.io.CharArrayWriter conformance: white-box structure (reflection) + full
 *          black-box contract coverage. Constructors (negative->IAE), write(int) low 16 bits,
 *          write(char[],off,len) validation, write(String,off,len), writeTo, append variants,
 *          reset/size/toCharArray copy, toString, flush/close no-op.
 *          Deepening: write(int) low-16 boundary matrix, inherited write(char[])/write(String)
 *          from Writer, write(String,off,len) bounds matrix, covariant append returns,
 *          writeTo delegate probe (one call, full buffer, failure propagation), growth
 *          across the default 32-char capacity, toCharArray independence, close no-op matrix.
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

    // ---- 9. write(int) low-16-bit boundary matrix ----
    static void testWriteIntEdge() {
        CharArrayWriter w = new CharArrayWriter();
        w.write(0);                // NUL
        w.write(0xD7FF);           // last code point before the surrogate block
        w.write(0xE000);           // first private-use code point
        w.write(0xFFFF);           // largest BMP value
        w.write(0x10041);          // low 16 bits = 'A'
        w.write(0x1FFFF);          // low 16 bits = 0xFFFF
        char[] a = w.toCharArray();
        check(a.length == 6, "six chars written");
        check(a[0] == 0, "write(0) stores the NUL char");
        check(a[1] == 0xD7FF && a[2] == 0xE000, "BMP boundary values stored");
        check(a[3] == 0xFFFF, "0xFFFF stored");
        check(a[4] == 'A', "only the low 16 bits of 0x10041 are written");
        check(a[5] == 0xFFFF, "only the low 16 bits of 0x1FFFF are written");
    }

    // ---- 10. inherited write(char[]) and write(String) from Writer ----
    static void testInheritedWrites() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("ok".toCharArray());            // Writer.write(char[]) -> write(cbuf,0,2)
        check(w.size() == 2 && "ok".equals(w.toString()), "inherited write(char[]) delegates");
        w.write(new char[0]);
        check(w.size() == 2, "write(empty char[]) is a no-op");
        final CharArrayWriter n = new CharArrayWriter();
        checkThrows(NullPointerException.class, () -> n.write((char[]) null),
                    "inherited write((char[])null) -> NPE");
        CharArrayWriter v = new CharArrayWriter();
        v.write("hi");                          // Writer.write(String) -> write(str,0,2)
        check(v.size() == 2 && "hi".equals(v.toString()), "inherited write(String) delegates");
        final CharArrayWriter m = new CharArrayWriter();
        checkThrows(NullPointerException.class, () -> m.write((String) null),
                    "inherited write((String)null) -> NPE");
        check(m.size() == 0, "nothing written by the failed call");
    }

    // ---- 11. write(String,off,len) bounds matrix + surrogate pair ----
    static void testWriteStringDeep() {
        final CharArrayWriter w = new CharArrayWriter();
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", -1, 1),
                    "negative off -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 0, -1),
                    "negative len -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 1, 3),
                    "off+len > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> w.write("abc", 4, 0),
                    "off > length -> IndexOutOfBoundsException");
        check(w.size() == 0, "failed writes leave the buffer untouched");
        w.write("", 0, 0);
        check(w.size() == 0, "write(\"\",0,0) is a no-op");
        w.write("abc", 3, 0);                   // off == length, len == 0: empty region
        check(w.size() == 0, "write(str, length, 0) is a legal empty region");
        w.write("abcde", 2, 3);
        check("cde".equals(w.toString()), "mid-string region write");
        // a whole surrogate pair goes through the region form verbatim
        String sup = new String(Character.toChars(0x1F600));
        CharArrayWriter s = new CharArrayWriter();
        s.write(sup, 0, sup.length());
        char[] arr = s.toCharArray();
        check(arr.length == 2 && arr[0] == 0xD83D && arr[1] == 0xDE00,
              "surrogate pair written verbatim");
    }

    // ---- 12. append: covariant returns and bounds ----
    static void testAppendDeep() {
        CharArrayWriter a = new CharArrayWriter();
        CharArrayWriter r1 = a.append('x');             // covariant override -> CharArrayWriter
        CharArrayWriter r2 = a.append("yz");
        CharArrayWriter r3 = a.append((CharSequence) null, 0, 4);
        check(r1 == a && r2 == a && r3 == a, "append overloads are covariant and return this");
        check("xyznull".equals(a.toString()), "append(null,0,4) writes the four-char \"null\"");
        checkThrows(IndexOutOfBoundsException.class, () -> a.append("abc", 2, 5),
                    "append(csq,start,end) end > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> a.append("abc", 3, 2),
                    "append(csq,start,end) start > end -> IndexOutOfBoundsException");
        check("xyznull".equals(a.toString()), "failed appends leave the buffer untouched");
    }

    // ---- 13. writeTo: one delegate call with the whole buffer ----
    static void testWriteToDelegation() throws Exception {
        final CharArrayWriter src = new CharArrayWriter();
        src.write("mart");
        final int[] calls = {0};
        final StringBuilder seen = new StringBuilder();
        java.io.Writer probe = new java.io.Writer() {
            public void write(char[] c, int o, int l) { calls[0]++; seen.append(c, o, l); }
            public void flush() { }
            public void close() { }
        };
        src.writeTo(probe);
        check(calls[0] == 1, "writeTo hands the whole buffer to out.write in a single call");
        check("mart".contentEquals(seen), "destination receives the exact content");
        check(src.size() == 4, "writeTo does not consume the source");
        // a failing destination propagates its IOException with the original message
        java.io.Writer boom = new java.io.Writer() {
            public void write(char[] c, int o, int l) throws java.io.IOException {
                throw new java.io.IOException("sink-boom");
            }
            public void flush() { }
            public void close() { }
        };
        try {
            src.writeTo(boom);
            check(false, "destination failure must propagate");
        } catch (java.io.IOException ioe) {
            check("sink-boom".equals(ioe.getMessage()), "original message kept");
        }
        checkThrows(NullPointerException.class, () -> new CharArrayWriter().writeTo(null),
                    "writeTo(null) on an empty writer -> NPE");
        // writeTo of an empty writer delegates an empty write
        final int[] emptyCalls = {0};
        java.io.Writer probe2 = new java.io.Writer() {
            public void write(char[] c, int o, int l) { emptyCalls[0] += l; }
            public void flush() { }
            public void close() { }
        };
        new CharArrayWriter().writeTo(probe2);
        check(emptyCalls[0] == 0, "empty writer writes nothing to the destination");
    }

    // ---- 14. growth across the default 32-char capacity ----
    static void testGrowth() {
        CharArrayWriter w = new CharArrayWriter();      // default initial capacity 32
        StringBuilder expect = new StringBuilder();
        for (int i = 0; i < 100; i++) {
            char c = (char) ('A' + (i % 26));
            w.write(c);
            expect.append(c);
        }
        check(w.size() == 100, "size after growing past 32");
        check(expect.toString().equals(w.toString()), "content intact across growth");
        char[] arr = w.toCharArray();
        boolean match = arr.length == 100;
        for (int i = 0; i < 100 && match; i++) match = arr[i] == expect.charAt(i);
        check(match, "toCharArray intact after growth");
        // one bulk write past the initial capacity
        CharArrayWriter b = new CharArrayWriter(4);
        b.write("0123456789".toCharArray(), 0, 10);
        check(b.size() == 10 && "0123456789".equals(b.toString()),
              "single bulk write grows past the initial capacity");
        // one string write past the initial capacity
        CharArrayWriter s = new CharArrayWriter(2);
        s.write("abcdefgh", 0, 8);
        check(s.size() == 8 && "abcdefgh".equals(s.toString()),
              "string write grows past the initial capacity");
    }

    // ---- 15. toCharArray independence and reset reuse ----
    static void testToCharArrayReset() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("hello");
        char[] a = w.toCharArray();
        char[] b = w.toCharArray();
        check(a != b, "each toCharArray returns a fresh array");
        a[0] = 'X';
        check(b[0] == 'h' && w.toCharArray()[0] == 'h', "mutating a copy affects nobody");
        w.reset();
        check(w.size() == 0 && w.toCharArray().length == 0, "reset clears size and content");
        w.write('z');
        check(w.size() == 1 && "z".equals(w.toString()), "writer reusable after reset");
        CharArrayWriter e = new CharArrayWriter();
        e.write("junk");
        e.reset();
        e.reset();                                      // reset on an empty writer
        check(e.size() == 0, "repeated reset on empty stays empty");
    }

    // ---- 16. close/flush are documented no-ops ----
    static void testCloseNoopDeep() throws Exception {
        CharArrayWriter w = new CharArrayWriter();
        w.write("a");
        w.close();
        // spec: close has no effect; everything keeps working
        w.write('b');
        w.write("c");
        w.write("def".toCharArray(), 0, 3);
        w.append('g');
        w.flush();
        check("abcdefg".equals(w.toString()), "all operations still work after close");
        w.close();                                      // repeated close is harmless
        check(w.size() == 7, "size after close");
        check(w.toCharArray().length == 7, "toCharArray after close");
        check("abcdefg".equals(w.toString()), "toString after close");
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
        testWriteIntEdge();
        testInheritedWrites();
        testWriteStringDeep();
        testAppendDeep();
        testWriteToDelegation();
        testGrowth();
        testToCharArrayReset();
        testCloseNoopDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

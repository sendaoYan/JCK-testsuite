/*
 * @test
 * @summary java.lang.Readable conformance: white-box structure + black-box behavior
 *          (read(CharBuffer) copies and returns count, -1 at EOF, NPE for null).
 * @run main ReadableTest
 */

import java.io.StringReader;
import java.nio.CharBuffer;

public class ReadableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.Readable");
        check(c.isInterface(), "Readable is an interface");
        java.lang.reflect.Method read = c.getMethod("read", CharBuffer.class);
        check(read.getReturnType() == int.class, "read(CharBuffer) returns int");
        boolean throwsIO = false;
        for (Class<?> t : read.getExceptionTypes()) {
            if (t == java.io.IOException.class) throwsIO = true;
        }
        check(throwsIO, "read(CharBuffer) declares IOException");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testRead() throws Exception {
        Readable r = new StringReader("hello");
        CharBuffer buf = CharBuffer.allocate(3);
        int n = r.read(buf);
        check(n == 3, "read returns chars copied");
        check(buf.flip().toString().equals("hel"), "buffer contents");
        CharBuffer rest = CharBuffer.allocate(10);
        check(r.read(rest) == 2, "read remaining 2");
        check(rest.flip().toString().equals("lo"), "remaining contents");
        check(r.read(CharBuffer.allocate(10)) == -1, "read at EOF returns -1");
    }

    static void testNull() throws Exception {
        Readable r = new StringReader("x");
        try {
            r.read(null);
            throw new AssertionError("read(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    // ===================== 契约深化（2026-11）追加 =====================
    // CharArrayReader 路径 + 空缓冲返回 0 + 只读缓冲 + position 推进 +
    // 包装数组可见性 + 自定义 Readable 记录调用 + 空输入。

    static void testCharArrayReaderPath() throws Exception {
        Readable r = new java.io.CharArrayReader("abc".toCharArray());
        CharBuffer b2 = CharBuffer.allocate(2);
        check(r.read(b2) == 2, "CharArrayReader read fills capacity");
        check(b2.flip().toString().equals("ab"), "CharArrayReader first chunk");
        CharBuffer b5 = CharBuffer.allocate(5);
        check(r.read(b5) == 1, "CharArrayReader partial fill (1 char left)");
        check(b5.flip().toString().equals("c"), "CharArrayReader last chunk");
        check(r.read(CharBuffer.allocate(5)) == -1, "CharArrayReader EOF");
    }

    static void testZeroRemainingBuffer() throws Exception {
        Readable r = new StringReader("abc");
        check(r.read(CharBuffer.allocate(0)) == 0,
              "buffer with no remaining space returns 0 (not -1, not throw)");
        CharBuffer b = CharBuffer.allocate(3);
        check(r.read(b) == 3, "data still fully available after 0-space read");
        check(b.flip().toString().equals("abc"), "content after 0-space read");
    }

    static void testReadOnlyBuffer() throws Exception {
        CharBuffer ro = CharBuffer.wrap("xy").asReadOnlyBuffer();
        try {
            new StringReader("ab").read(ro);
            throw new AssertionError("read(readonly): expected ReadOnlyBufferException");
        } catch (java.nio.ReadOnlyBufferException expected) {
            passed++;
        }
    }

    static void testBufferPositionAdvances() throws Exception {
        Readable r = new StringReader("abcdef");
        CharBuffer b = CharBuffer.allocate(4);
        int n = r.read(b);
        check(n == 4 && b.position() == 4, "position advances by chars read");
        check(b.flip().toString().equals("abcd"), "first window");
        b.clear();
        int m = r.read(b);
        check(m == 2 && b.position() == 2, "second read positions at 2");
        check(b.flip().toString().equals("ef"), "second window");
    }

    static void testWrappedArrayVisibility() throws Exception {
        char[] backing = new char[5];
        CharBuffer b = CharBuffer.wrap(backing);
        int n = new StringReader("abc").read(b);
        check(n == 3 && b.position() == 3, "wrapped array read count/position");
        check(new String(backing, 0, 3).equals("abc"), "bytes visible in backing array");
        check(backing[3] == 0 && backing[4] == 0, "unread tail untouched");
    }

    static void testCustomReadablePassthrough() throws Exception {
        final CharBuffer received = CharBuffer.allocate(1);
        final java.util.List<String> calls = new java.util.ArrayList<String>();
        Readable cust = new Readable() {
            public int read(CharBuffer target) {
                calls.add(target == received ? "same-buffer" : "other-buffer");
                return 7;
            }
        };
        check(cust.read(received) == 7, "custom read return value passes through");
        check(calls.equals(java.util.Arrays.asList("same-buffer")),
              "custom read receives the exact buffer instance");
        Readable minusOne = new Readable() {
            public int read(CharBuffer target) { return -1; }
        };
        check(minusOne.read(CharBuffer.allocate(1)) == -1, "custom -1 passes through");
        Readable zero = new Readable() {
            public int read(CharBuffer target) { return 0; }
        };
        check(zero.read(CharBuffer.allocate(1)) == 0, "custom 0 passes through");
    }

    static void testCustomReadableException() throws Exception {
        final java.io.IOException boom = new java.io.IOException("boom");
        Readable failing = new Readable() {
            public int read(CharBuffer target) throws java.io.IOException { throw boom; }
        };
        try {
            failing.read(CharBuffer.allocate(1));
            throw new AssertionError("custom read: expected IOException");
        } catch (java.io.IOException e) {
            check(e == boom, "custom read propagates the same IOException instance");
        }
    }

    static void testImplAssignment() {
        check(new java.io.StringReader("x") instanceof Readable, "StringReader is a Readable");
        check(new java.io.CharArrayReader(new char[1]) instanceof Readable,
              "CharArrayReader is a Readable");
        check(new java.io.BufferedReader(new StringReader("x")) instanceof Readable,
              "BufferedReader is a Readable");
    }

    static void testSequentialAccumulation() throws Exception {
        Readable r = new StringReader("abcdefgh");
        StringBuilder acc = new StringBuilder();
        CharBuffer b1 = CharBuffer.allocate(3);
        int n1 = r.read(b1);
        acc.append(b1.flip().toString());
        CharBuffer b2 = CharBuffer.allocate(3);
        int n2 = r.read(b2);
        acc.append(b2.flip().toString());
        CharBuffer b3 = CharBuffer.allocate(3);
        int n3 = r.read(b3);
        acc.append(b3.flip().toString());
        check(n1 == 3 && n2 == 3 && n3 == 2, "chunk sizes 3/3/2");
        check(acc.toString().equals("abcdefgh"), "chunks reassemble the input");
        check(r.read(CharBuffer.allocate(3)) == -1, "EOF after exhaustion");
        check(r.read(CharBuffer.allocate(3)) == -1, "repeated EOF stays -1");
    }

    static void testEmptyInput() throws Exception {
        check(new StringReader("").read(CharBuffer.allocate(3)) == -1,
              "empty StringReader: immediate EOF");
        check(new java.io.CharArrayReader(new char[0]).read(CharBuffer.allocate(3)) == -1,
              "empty CharArrayReader: immediate EOF");
        Readable r = new java.io.CharArrayReader("lo".toCharArray());
        CharBuffer big = CharBuffer.allocate(10);
        check(r.read(big) == 2, "short input partial fill");
        check(big.flip().toString().equals("lo"), "short input content");
        check(r.read(big) == -1, "flipped buffer keeps remaining; exhausted reader returns -1");
    }

    static void testNullBufferOtherImpl() throws Exception {
        try {
            new java.io.CharArrayReader("x".toCharArray()).read((CharBuffer) null);
            throw new AssertionError("CharArrayReader read(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
        try {
            new java.io.BufferedReader(new StringReader("x")).read((CharBuffer) null);
            throw new AssertionError("BufferedReader read(null): expected NullPointerException");
        } catch (NullPointerException expected) {
            passed++;
        }
    }

    static void testBlackBox() throws Exception {
        testRead();
        testNull();
    }
public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        // 契约深化（2026-11）追加
        testCharArrayReaderPath();
        testZeroRemainingBuffer();
        testReadOnlyBuffer();
        testBufferPositionAdvances();
        testWrappedArrayVisibility();
        testCustomReadablePassthrough();
        testCustomReadableException();
        testImplAssignment();
        testSequentialAccumulation();
        testEmptyInput();
        testNullBufferOtherImpl();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

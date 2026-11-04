/*
 * @test
 * @summary java.nio.CharBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable/Appendable/CharSequence/Readable) plus black-box
 *          allocate/wrap, relative/absolute get/put, CharSequence/Appendable surface,
 *          views, equality and bounds exceptions.
 * @run main CharBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: CharBuffer 是 char 元素缓冲区, 同时实现 CharSequence/Appendable/Readable;
 *    toString() 返回 remaining 区间的字符串; order() 返回创建它的 ByteBuffer 字节序.
 * 2. 场景: 字符串/字符数组 -> wrap/allocate -> put/append 组装 -> flip -> charAt/length/
 *    subSequence 读取; read() 从另一个 CharBuffer 抽字符.
 * 3. 组合链路: wrap(CharSequence) -> append(char/CharSequence) -> flip -> charAt/
 *    subSequence/toString; put(String) 批量写入.
 * 4. 不变量: 堆缓冲区 hasArray()==true; length() 恒等于 remaining(); 视图/duplicate/slice
 *    共享 char[] 存储; equals/compareTo/mismatch 按 remaining 逐字符.
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; charAt 越界抛
 *    IndexOutOfBoundsException; 绝对 put/get 越界抛 IndexOutOfBoundsException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;

public class CharBufferTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void checkThrows(Class<? extends Throwable> type, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (type.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + " (expected " + type.getSimpleName()
                    + ", got " + t.getClass().getName() + ")");
        }
        throw new AssertionError(msg + " (no exception thrown)");
    }

    static boolean hasMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    static boolean implementsInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.CharBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "CharBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "CharBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(implementsInterface(c, "java.lang.Appendable"), "implements Appendable");
        check(implementsInterface(c, "java.lang.CharSequence"), "implements CharSequence");
        check(implementsInterface(c, "java.lang.Readable"), "implements Readable");

        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", char[].class), "has wrap(char[])");
        check(hasMethod(c, "wrap", char[].class, int.class, int.class), "has wrap(char[],int,int)");
        check(hasMethod(c, "wrap", CharSequence.class), "has wrap(CharSequence)");
        check(hasMethod(c, "length"), "has length()");
        check(hasMethod(c, "charAt", int.class), "has charAt(int)");
        check(hasMethod(c, "subSequence", int.class, int.class), "has subSequence(int,int)");
        check(hasMethod(c, "append", char.class), "has append(char)");
        check(hasMethod(c, "read", CharBuffer.class), "has read(CharBuffer)");
        check(hasMethod(c, "mismatch", CharBuffer.class), "has mismatch(CharBuffer)");
    }

    static void testBlackBox() throws Exception {
        CharBuffer cb = CharBuffer.allocate(4);
        cb.put('a').put('b').put('c');
        check(cb.position() == 3, "relative put advances");
        cb.flip();
        check(cb.get() == 'a' && cb.get() == 'b' && cb.get() == 'c', "relative get");
        check(!cb.hasRemaining(), "consumed");

        CharBuffer cs = CharBuffer.wrap("hello world");
        check(cs.length() == 11, "length == remaining");
        check(cs.isEmpty() == false, "not empty");
        check(cs.charAt(0) == 'h' && cs.charAt(5) == ' ', "charAt");
        check("hello".contentEquals(cs.subSequence(0, 5)), "subSequence");
        check("hello world".equals(cs.toString()), "toString returns remaining");
        cs.position(6);
        check("world".equals(cs.toString()), "toString after position");
        check(cs.length() == 5, "length after position");
        check(CharBuffer.wrap("").isEmpty(), "empty is empty");

        CharBuffer ap = CharBuffer.allocate(16);
        ap.append('a').append('b');
        ap.append("cd");
        ap.append(new StringBuilder("ef"), 0, 2);
        ap.flip();
        check("abcdef".contentEquals(ap), "append chains into abcdef");

        CharBuffer src = CharBuffer.wrap("xyz");
        CharBuffer dst = CharBuffer.allocate(4);
        int n = src.read(dst);
        check(n == 3, "read count");
        dst.flip();
        check("xyz".contentEquals(dst), "read copies chars");

        char[] backing = {'1', '2', '3', '4'};
        CharBuffer cb2 = CharBuffer.wrap(backing);
        cb2.put(0, '9');
        check(backing[0] == '9', "wrap shares array");
        check(cb2.get(0) == '9', "absolute get");
        check(cb2.position() == 0, "absolute op leaves position");

        cb2.put("AB");
        check(cb2.position() == 2, "put(String) advances");
        cb2.flip();
        check("AB".contentEquals(cb2), "put(String) content");

        char[] rangeArr = {'5', '6', '7', '8'};
        CharBuffer range = CharBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == '6' && range.get() == '7', "wrap range content");

        CharBuffer csq = CharBuffer.wrap("hello", 1, 3);
        check(csq.remaining() == 2 && "el".contentEquals(csq), "wrap(CharSequence,start,end)");

        CharBuffer v = CharBuffer.wrap(new char[] {'a', 'b', 'c', 'd'});
        CharBuffer dup = v.duplicate();
        dup.put(0, 'Z');
        check(v.get(0) == 'Z', "duplicate shares storage");
        v.position(1).limit(3);
        CharBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 'b' && sl.get() == 'c', "slice range");

        CharBuffer cp = CharBuffer.allocate(4);
        cp.put('1').put('2').put('3');
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == '2' && cp.get(1) == '3', "compact preserves order");

        CharBuffer a = CharBuffer.wrap("abc");
        CharBuffer b = CharBuffer.wrap("abc");
        check(a.equals(b) && a.compareTo(b) == 0, "equals/compareTo");
        check(a.mismatch(b) == -1, "mismatch equal");
        CharBuffer c3 = CharBuffer.wrap("axc");
        check(a.mismatch(c3) == 1, "mismatch index");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");

        CharBuffer ro = CharBuffer.wrap("ab").asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put('x'), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> CharBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> CharBuffer.allocate(2).limit(0).put('a'), "put overflow");
        CharBuffer e = CharBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 'a'), "absolute put OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.wrap("abc").charAt(3), "charAt OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");

        // ---- boundary/equivalence additions (M2) ----
        CharBuffer zero = CharBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> CharBuffer.allocate(-1), "allocate(-1) IAE");

        CharBuffer mark = CharBuffer.allocate(4);
        mark.put('a').put('b').flip();
        mark.mark();
        check(mark.get() == 'a', "get after mark");
        mark.reset();
        check(mark.get() == 'a', "reset restores position");

        CharBuffer src2 = CharBuffer.wrap("hello");
        CharBuffer small = CharBuffer.allocate(3);
        int n2 = src2.read(small);
        check(n2 == 3, "read into smaller buffer partial count");
        small.flip();
        check("hel".contentEquals(small), "partial read copies min(remaining)");

        CharBuffer sub = CharBuffer.wrap("abc");
        check(sub.subSequence(0, 0).length() == 0, "empty subSequence");
        checkThrows(IndexOutOfBoundsException.class, () -> sub.subSequence(-1, 1), "subSequence negative IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> sub.subSequence(1, 4), "subSequence past end IOOBE");

        CharBuffer apb = CharBuffer.allocate(8);
        checkThrows(IndexOutOfBoundsException.class, () -> apb.append("xy", -1, 1), "append bad start IOOBE");
        checkThrows(IndexOutOfBoundsException.class, () -> apb.append("xy", 0, 3), "append end>len IOOBE");
    }

    // ===================== A1 batch: contract deepening =====================

    /** read() throws IOException (checked); adapt it to Runnable for checkThrows. */
    static Runnable uncheckedRead(final CharBuffer src, final CharBuffer dst) {
        return () -> {
            try {
                src.read(dst);
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
        };
    }

    /** read(CharBuffer): drain semantics, partial copy, negative cases. */
    static void testReadSemantics() throws Exception {
        CharBuffer src = CharBuffer.wrap("hello");
        CharBuffer dst = CharBuffer.allocate(8);
        check(src.read(dst) == 5, "read copies all remaining chars");
        check(src.position() == 5 && dst.position() == 5, "read advances both cursors");
        check(src.read(dst) == -1, "read from a drained source returns -1");
        check(CharBuffer.wrap("").read(dst) == -1, "read from an empty source returns -1");
        CharBuffer ro = CharBuffer.allocate(2).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, uncheckedRead(CharBuffer.wrap("x"), ro),
                "read into a read-only target -> ReadOnlyBufferException");
        CharBuffer big = CharBuffer.wrap("abcdef");
        CharBuffer small = CharBuffer.allocate(2);
        check(big.read(small) == 2, "read copies min(src.remaining, dst.remaining)");
        small.flip();
        check("ab".contentEquals(small) && big.position() == 2 && big.charAt(0) == 'c',
                "read copies in order and advances the source");
    }

    /** Bulk put/get over arrays and strings: matrices with precise exception types. */
    static void testBulkPutGet() {
        CharBuffer src = CharBuffer.wrap("xyz");
        CharBuffer dst = CharBuffer.allocate(5);
        dst.put(src);
        check(dst.position() == 3 && src.position() == 3, "put(CharBuffer) advances both cursors");
        check(dst.get(0) == 'x' && dst.get(1) == 'y' && dst.get(2) == 'z',
                "put(CharBuffer) copies in order");
        CharBuffer self = CharBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> self.put(self),
                "put(this) -> IllegalArgumentException");
        checkThrows(NullPointerException.class, () -> CharBuffer.allocate(2).put((CharBuffer) null),
                "put((CharBuffer)null) -> NullPointerException");
        checkThrows(BufferOverflowException.class, () -> CharBuffer.allocate(1).put(CharBuffer.wrap("ab")),
                "put(CharBuffer) without room -> BufferOverflowException");
        CharBuffer roSrc = CharBuffer.wrap("ab").asReadOnlyBuffer();
        CharBuffer writable = CharBuffer.allocate(2);
        writable.put(roSrc);
        check(writable.get(0) == 'a', "a read-only buffer may serve as a put source");
        checkThrows(NullPointerException.class, () -> CharBuffer.allocate(2).put((char[]) null),
                "put((char[])null) -> NullPointerException");
        checkThrows(NullPointerException.class, () -> CharBuffer.allocate(2).get((char[]) null),
                "get((char[])null) -> NullPointerException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put(new char[2], -1, 1),
                "put(src,off,len) negative offset -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put(new char[2], 1, 2),
                "put(src,off,len) off+len > src.length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).get(new char[2], 0, 3),
                "get(dst,off,len) len > dst.length -> IndexOutOfBoundsException");
        checkThrows(BufferOverflowException.class, () -> CharBuffer.allocate(2).put(new char[3]),
                "put(char[]) without room -> BufferOverflowException");
        checkThrows(BufferUnderflowException.class, () -> CharBuffer.allocate(2).get(new char[3]),
                "get(char[]) beyond remaining -> BufferUnderflowException");
        CharBuffer part = CharBuffer.wrap("abc");
        char[] out = new char[4];
        part.get(out, 1, 2);
        check(part.position() == 2 && out[0] == 0 && out[1] == 'a' && out[2] == 'b' && out[3] == 0,
                "bulk get writes exactly len chars at the offset");
        checkThrows(BufferOverflowException.class, () -> CharBuffer.allocate(1).put("ab"),
                "put(String) without room -> BufferOverflowException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put("abc", -1, 1),
                "put(String,-1,1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put("abc", 2, -1),
                "put(String,2,-1) negative end -> IndexOutOfBoundsException");
        // NOTE: put(String,int,int) takes (start,end) indices, not (offset,length).
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put("abc", 1, 4),
                "put(String,1,4) end > string length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.allocate(4).put("abc", 3, 1),
                "put(String,3,1) end < start -> IndexOutOfBoundsException");
        CharBuffer emptyRange = CharBuffer.allocate(4);
        emptyRange.put("abc", 2, 2);
        check(emptyRange.position() == 0, "put(String,start,start) copies an empty range");
        CharBuffer ps = CharBuffer.allocate(8);
        ps.put("abcdef", 2, 5);
        check(ps.position() == 3, "put(String,start,end) advances by end-start");
        ps.flip();
        check("cde".contentEquals(ps), "put(String,start,end) copies the [start,end) substring");
    }

    /** CharSequence/Appendable surface: relative indexing and live views. */
    static void testCharSequenceContract() {
        CharBuffer cb = CharBuffer.wrap("abcd");
        cb.position(2);
        check(cb.charAt(0) == 'c', "charAt is relative to position");
        check("cd".contentEquals(cb.subSequence(0, 2)), "subSequence is relative to position");
        checkThrows(IndexOutOfBoundsException.class, () -> cb.charAt(2),
                "charAt(remaining) relative -> IndexOutOfBoundsException");
        CharBuffer shared = CharBuffer.wrap(new char[]{'a', 'b', 'c', 'd'});
        CharBuffer sub = (CharBuffer) shared.subSequence(1, 3);
        check(sub.charAt(0) == 'b' && sub.length() == 2,
                "subSequence view exposes [start,end) relative to the current position");
        check(sub.position() == 1 && sub.limit() == 3,
                "subSequence view: position = position+start, limit = position+end");
        sub.put(sub.position(), 'X');
        check(shared.charAt(1) == 'X', "subSequence view shares the backing array");
        CharBuffer ap = CharBuffer.allocate(6);
        check(ap.append('a') == ap, "append(char) returns this");
        check(ap.append("b") == ap, "append(CharSequence) returns this");
        check(ap.append("XXcd", 2, 4) == ap,
                "append(CharSequence,int,int) appends the [start,end) subsequence");
        check(ap.position() == 4, "append advances the position by the appended char count");
        ap.flip();
        check("abcd".contentEquals(ap), "append appends in order");
        checkThrows(java.nio.BufferOverflowException.class, () -> CharBuffer.allocate(1).append("ab"),
                "append overflowing the remaining space -> BufferOverflowException");
        check(CharBuffer.wrap("abc", 0, 0).length() == 0, "wrap(csq,0,0) is an empty view");
        CharBuffer w = CharBuffer.wrap("hello", 2, 5);
        check(w.position() == 2 && w.limit() == 5 && w.capacity() == 5,
                "wrap(csq,start,end): position=start, limit=end, capacity=csq.length");
        check("llo".contentEquals(w), "wrap(csq,start,end) exposes the selected range");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.wrap("abc", 1, 4),
                "wrap(csq,1,4) end > length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.wrap("abc", -1, 2),
                "wrap(csq,-1,2) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> CharBuffer.wrap("abc", 3, 0),
                "wrap(csq,3,0) start > end -> IndexOutOfBoundsException");
    }

    /**
     * Byte order of char buffers: allocation/wrapping uses the native order
     * (LITTLE_ENDIAN on x86_64, asserted against nativeOrder()); views inherit
     * the creating byte buffer's order.
     */
    static void testOrderSemantics() {
        check(CharBuffer.allocate(4).order() == ByteOrder.nativeOrder(),
                "heap CharBuffer order is the native order");
        check(CharBuffer.allocate(4).order() == ByteOrder.LITTLE_ENDIAN,
                "native order is LITTLE_ENDIAN on x86_64");
        check(CharBuffer.wrap("ab").order() == ByteOrder.nativeOrder(),
                "wrap(CharSequence) order is the native order");
        check(CharBuffer.wrap(new char[2]).order() == ByteOrder.nativeOrder(),
                "wrap(char[]) order is the native order");
        check(ByteBuffer.allocate(8).asCharBuffer().order() == ByteOrder.BIG_ENDIAN,
                "char view of a big-endian byte buffer inherits BIG_ENDIAN");
        ByteBuffer le = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        check(le.asCharBuffer().order() == ByteOrder.LITTLE_ENDIAN,
                "char view of a little-endian byte buffer inherits LITTLE_ENDIAN");
    }

    /** slice/duplicate/asReadOnly independence, mark copy, compact semantics. */
    static void testViewsAndCompact() {
        CharBuffer ro = CharBuffer.wrap("abcd").asReadOnlyBuffer();
        check(ro.duplicate().isReadOnly(), "duplicate of a read-only buffer is read-only");
        CharBuffer roS = CharBuffer.wrap("abcd").asReadOnlyBuffer();
        roS.position(1);
        roS.limit(3);
        CharBuffer slice = roS.slice();
        check(slice.isReadOnly() && slice.capacity() == 2,
                "slice of a read-only buffer is read-only with region capacity");
        checkThrows(ReadOnlyBufferException.class, () -> slice.put(0, 'x'),
                "read-only slice put -> ReadOnlyBufferException");
        CharBuffer mk = CharBuffer.wrap(new char[6]);
        mk.position(2);
        mk.mark();
        mk.position(4);
        mk.limit(5);
        CharBuffer du = mk.duplicate();
        check(du.position() == 4 && du.limit() == 5, "duplicate copies position and limit");
        du.reset();
        check(du.position() == 2, "duplicate copies the mark");
        CharBuffer cp = CharBuffer.allocate(6);
        cp.put('a').put('b').put('c');
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 6 && cp.get(0) == 'b' && cp.get(1) == 'c',
                "compact shifts [position,limit) to the front");
        CharBuffer cp2 = CharBuffer.allocate(4);
        cp2.put('x');
        cp2.mark();
        cp2.flip();
        cp2.get();
        cp2.compact();
        checkThrows(java.nio.InvalidMarkException.class, () -> cp2.reset(),
                "compact discards the mark");
        checkThrows(ReadOnlyBufferException.class,
                () -> CharBuffer.allocate(2).asReadOnlyBuffer().compact(),
                "compact on a read-only buffer -> ReadOnlyBufferException");
        check(CharBuffer.allocate(2).asReadOnlyBuffer().asReadOnlyBuffer().isReadOnly(),
                "asReadOnlyBuffer of a read-only buffer stays read-only");
        char[] backing = {'a', 'b', 'c', 'd'};
        CharBuffer src = CharBuffer.wrap(backing);
        src.position(1);
        src.limit(3);
        CharBuffer sl = src.slice();
        sl.put(0, 'Z');
        check(backing[1] == 'Z' && sl.get(0) == 'Z',
                "slice writes through to the backing array");
        check(sl.arrayOffset() == 1, "heap slice arrayOffset == source position");
    }

    /** equals/compareTo/hashCode compare only [position,limit), by char value. */
    static void testEqualsCompareToHashCode() {
        CharBuffer a = CharBuffer.wrap("ab");
        CharBuffer p = CharBuffer.wrap("xxab");
        p.position(2);
        check(a.equals(p) && p.equals(a), "equals compares only [position,limit)");
        check(a.hashCode() == p.hashCode(), "hashCode depends only on the remaining region");
        CharBuffer x = CharBuffer.wrap("ab");
        CharBuffer y = CharBuffer.wrap("ax");
        check(x.compareTo(y) < 0 && y.compareTo(x) > 0,
                "compareTo sign follows the first differing char");
        CharBuffer s = CharBuffer.wrap("ab");
        CharBuffer l = CharBuffer.wrap("abcd");
        check(s.compareTo(l) < 0 && l.compareTo(s) > 0,
                "compareTo sign follows the remaining length when the prefix is equal");
        CharBuffer p2 = CharBuffer.wrap("zzab");
        p2.position(2);
        check(s.compareTo(p2) == 0, "compareTo ignores the position prefix");
        check(s.compareTo(s) == 0, "compareTo with itself is zero");
        check(CharBuffer.wrap("A").compareTo(CharBuffer.wrap("a")) < 0,
                "compareTo compares raw char values ('A' < 'a')");
        check(CharBuffer.allocate(0).equals(CharBuffer.allocate(0)),
                "two empty char buffers are equal");
        CharBuffer heapEq = CharBuffer.wrap(new char[]{'a', 'b'});
        check(a.equals(heapEq), "heap and CharSequence-backed buffers with equal content are equal");
    }

    /** mismatch(CharBuffer) (JDK-13+, absent from JDK-8; see version note there). */
    static void testJdk13Mismatch() {
        check(CharBuffer.wrap("abc").mismatch(CharBuffer.wrap("abc")) == -1,
                "mismatch equal -> -1");
        check(CharBuffer.wrap("abc").mismatch(CharBuffer.wrap("axc")) == 1,
                "mismatch returns the relative index of the first differing char");
        check(CharBuffer.wrap("ab").mismatch(CharBuffer.wrap("abz")) == 2,
                "mismatch equal prefix, shorter -> length of the shorter remaining region");
        check(CharBuffer.wrap("ab").mismatch(CharBuffer.wrap("ab")) == -1,
                "mismatch of two empty-remaining views -> -1");
        CharBuffer pp = CharBuffer.wrap("xxabx");
        pp.position(2);
        pp.limit(4);
        check(CharBuffer.wrap("ab").mismatch(pp) == -1, "mismatch uses [position,limit)");
        CharBuffer firstDiffers = CharBuffer.wrap("zzXb");
        firstDiffers.position(2);
        check(CharBuffer.wrap("ab").mismatch(firstDiffers) == 0,
                "mismatch index is relative to the source position");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testReadSemantics();
        testBulkPutGet();
        testCharSequenceContract();
        testOrderSemantics();
        testViewsAndCompact();
        testEqualsCompareToHashCode();
        testJdk13Mismatch();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

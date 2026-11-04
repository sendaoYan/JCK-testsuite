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

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

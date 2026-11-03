/*
 * @test
 * @summary java.nio.IntBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main IntBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: IntBuffer 是按 4 字节 int 元素索引的类型化缓冲区, 元素间不透明 (无字节序
 *    概念, 但 order() 返回创建它的 ByteBuffer 字节序), 继承 Buffer 游标状态机.
 * 2. 场景: 数值数组 -> wrap/allocate -> 相对/绝对 put/get -> flip -> 遍历读回;
 *    duplicate/slice 共享 int[] 存储但游标独立.
 * 3. 组合链路: wrap(int[]) -> put(绝对) -> get(相对) -> mark/reset -> flip -> get;
 *    duplicate 写回影响源; compact 把未读元素搬到头部.
 * 4. 不变量: 堆缓冲区 hasArray()==true, array() 返回底层 int[]; remaining() 元素个数;
 *    equals/compareTo/mismatch 按 remaining 区间逐元素比较.
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;

public class IntBufferTest {
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
        Class<?> c = Class.forName("java.nio.IntBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "IntBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "IntBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", int[].class), "has wrap(int[])");
        check(hasMethod(c, "wrap", int[].class, int.class, int.class), "has wrap(int[],int,int)");
        check(hasMethod(c, "get"), "has get()");
        check(hasMethod(c, "put", int.class), "has put(int)");
        check(hasMethod(c, "get", int.class), "has get(int)");
        check(hasMethod(c, "put", int.class, int.class), "has put(int,int)");
        check(hasMethod(c, "mismatch", IntBuffer.class), "has mismatch(IntBuffer)");
    }

    static void testBlackBox() {
        IntBuffer ib = IntBuffer.allocate(4);
        check(ib.capacity() == 4 && ib.position() == 0 && ib.limit() == 4, "initial state");
        ib.put(10).put(20).put(30);
        check(ib.position() == 3, "relative put advances");
        ib.flip();
        check(ib.get() == 10 && ib.get() == 20 && ib.get() == 30, "relative get in order");
        check(!ib.hasRemaining(), "consumed");
        ib.clear();
        check(ib.position() == 0 && ib.limit() == 4, "clear");

        int[] backing = {1, 2, 3, 4};
        IntBuffer w = IntBuffer.wrap(backing);
        w.put(0, 99);
        check(backing[0] == 99, "wrap shares array");
        check(w.get(0) == 99, "absolute get");
        check(w.position() == 0, "absolute op leaves position");

        w.put(new int[] {7, 8}, 0, 2);
        check(w.position() == 2, "bulk put advances");
        int[] out = new int[2];
        w.flip();
        w.get(out, 0, 2);
        check(out[0] == 7 && out[1] == 8, "bulk get");

        int[] rangeArr = {5, 6, 7, 8};
        IntBuffer range = IntBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == 6 && range.get() == 7, "wrap range content");
        range.put(0, 55);
        check(rangeArr[0] == 55, "wrap range shares array");

        IntBuffer v = IntBuffer.wrap(new int[] {1, 2, 3, 4});
        IntBuffer dup = v.duplicate();
        dup.put(0, -1);
        check(v.get(0) == -1, "duplicate shares storage");
        check(dup.position() == 0 && v.position() == 0, "duplicate independent cursor");
        v.position(1).limit(3);
        IntBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 2 && sl.get() == 3, "slice range");

        IntBuffer cp = IntBuffer.allocate(4);
        cp.put(1).put(2).put(3);
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == 2 && cp.get(1) == 3, "compact preserves unread order");

        IntBuffer a = IntBuffer.wrap(new int[] {1, 2, 3});
        IntBuffer b = IntBuffer.wrap(new int[] {1, 2, 3});
        check(a.equals(b), "equals");
        check(a.compareTo(b) == 0, "compareTo equal");
        check(a.mismatch(b) == -1, "mismatch equal");
        IntBuffer c3 = IntBuffer.wrap(new int[] {1, 9, 3});
        check(a.mismatch(c3) == 1, "mismatch index");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");

        IntBuffer ro = IntBuffer.wrap(new int[] {1, 2}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> IntBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> IntBuffer.allocate(2).limit(0).put(1), "put overflow");
        IntBuffer e = IntBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
        checkThrows(IllegalArgumentException.class, () -> e.limit(-1), "limit negative");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary java.nio.ShortBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main ShortBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: ShortBuffer 是按 2 字节 short 元素索引的类型化缓冲区, 继承 Buffer 游标状态机,
 *    元素间不透明; order() 返回创建它的 ByteBuffer 的字节序.
 * 2. 场景: short 数组 -> wrap/allocate -> 相对/绝对 put/get -> flip -> 读回;
 *    duplicate/slice 共享存储; compact 压缩未读元素.
 * 3. 组合链路: wrap(short[]) -> put(绝对) -> get(相对) -> mark/reset -> flip -> get;
 *    duplicate 写回影响源.
 * 4. 不变量: 堆缓冲区 hasArray()==true; equals/compareTo/mismatch 按 remaining 逐元素.
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ReadOnlyBufferException;
import java.nio.ShortBuffer;

public class ShortBufferTest {
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
        Class<?> c = Class.forName("java.nio.ShortBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "ShortBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "ShortBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", short[].class), "has wrap(short[])");
        check(hasMethod(c, "wrap", short[].class, int.class, int.class), "has wrap(short[],int,int)");
        check(hasMethod(c, "get"), "has get()");
        check(hasMethod(c, "put", short.class), "has put(short)");
        check(hasMethod(c, "get", int.class), "has get(int)");
        check(hasMethod(c, "put", int.class, short.class), "has put(int,short)");
        check(hasMethod(c, "mismatch", ShortBuffer.class), "has mismatch(ShortBuffer)");
    }

    static void testBlackBox() {
        ShortBuffer b = ShortBuffer.allocate(3);
        b.put((short) 10).put((short) 20).put((short) 30);
        check(b.position() == 3, "relative put advances");
        b.flip();
        check(b.get() == 10 && b.get() == 20 && b.get() == 30, "relative get");
        check(!b.hasRemaining(), "consumed");

        short[] backing = {1, 2, 3, 4};
        ShortBuffer w = ShortBuffer.wrap(backing);
        w.put(0, (short) 99);
        check(backing[0] == 99, "wrap shares array");
        check(w.get(0) == 99, "absolute get");
        check(w.position() == 0, "absolute op leaves position");

        w.put(new short[] {7, 8}, 0, 2);
        check(w.position() == 2, "bulk put advances");
        short[] out = new short[2];
        w.flip();
        w.get(out, 0, 2);
        check(out[0] == 7 && out[1] == 8, "bulk get");

        short[] rangeArr = {5, 6, 7, 8};
        ShortBuffer range = ShortBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == 6 && range.get() == 7, "wrap range content");
        range.put(0, (short) 55);
        check(rangeArr[0] == 55, "wrap range shares array");

        ShortBuffer v = ShortBuffer.wrap(new short[] {1, 2, 3, 4});
        ShortBuffer dup = v.duplicate();
        dup.put(0, (short) -1);
        check(v.get(0) == -1, "duplicate shares storage");
        v.position(1).limit(3);
        ShortBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 2 && sl.get() == 3, "slice range");

        ShortBuffer cp = ShortBuffer.allocate(4);
        cp.put((short) 1).put((short) 2).put((short) 3);
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == 2 && cp.get(1) == 3, "compact preserves order");

        ShortBuffer a = ShortBuffer.wrap(new short[] {1, 2, 3});
        ShortBuffer b2 = ShortBuffer.wrap(new short[] {1, 2, 3});
        check(a.equals(b2) && a.compareTo(b2) == 0, "equals/compareTo");
        check(a.mismatch(b2) == -1, "mismatch equal");
        ShortBuffer c3 = ShortBuffer.wrap(new short[] {1, 9, 3});
        check(a.mismatch(c3) == 1, "mismatch index");

        ShortBuffer ro = ShortBuffer.wrap(new short[] {1}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((short) 1), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> ShortBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> ShortBuffer.allocate(2).limit(0).put((short) 1), "put overflow");
        ShortBuffer e = ShortBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, (short) 1), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

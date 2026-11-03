/*
 * @test
 * @summary java.nio.DoubleBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main DoubleBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: DoubleBuffer 是按 8 字节 double 元素索引的类型化缓冲区, 继承 Buffer 游标
 *    状态机, 元素间不透明; order() 返回创建它的 ByteBuffer 的字节序.
 * 2. 场景: double 数组 -> wrap/allocate -> 相对/绝对 put/get -> flip -> 读回;
 *    duplicate/slice 共享存储; compact 压缩未读元素.
 * 3. 组合链路: wrap(double[]) -> put(绝对) -> get(相对) -> mark/reset -> flip -> get;
 *    duplicate 写回影响源.
 * 4. 不变量: 堆缓冲区 hasArray()==true; equals/compareTo/mismatch 按 remaining 逐元素;
 *    compareTo 对 double 用 Double.compare 语义 (NaN 最大).
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.DoubleBuffer;
import java.nio.ReadOnlyBufferException;

public class DoubleBufferTest {
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
        Class<?> c = Class.forName("java.nio.DoubleBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "DoubleBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "DoubleBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", double[].class), "has wrap(double[])");
        check(hasMethod(c, "wrap", double[].class, int.class, int.class), "has wrap(double[],int,int)");
        check(hasMethod(c, "get"), "has get()");
        check(hasMethod(c, "put", double.class), "has put(double)");
        check(hasMethod(c, "get", int.class), "has get(int)");
        check(hasMethod(c, "put", int.class, double.class), "has put(int,double)");
        check(hasMethod(c, "mismatch", DoubleBuffer.class), "has mismatch(DoubleBuffer)");
    }

    static void testBlackBox() {
        DoubleBuffer b = DoubleBuffer.allocate(3);
        b.put(1.5).put(2.5).put(3.5);
        check(b.position() == 3, "relative put advances");
        b.flip();
        check(b.get() == 1.5 && b.get() == 2.5 && b.get() == 3.5, "relative get");
        check(!b.hasRemaining(), "consumed");

        double[] backing = {1, 2, 3, 4};
        DoubleBuffer w = DoubleBuffer.wrap(backing);
        w.put(0, 99.0);
        check(backing[0] == 99.0, "wrap shares array");
        check(w.get(0) == 99.0, "absolute get");
        check(w.position() == 0, "absolute op leaves position");

        w.put(new double[] {7, 8}, 0, 2);
        check(w.position() == 2, "bulk put advances");
        double[] out = new double[2];
        w.flip();
        w.get(out, 0, 2);
        check(out[0] == 7 && out[1] == 8, "bulk get");

        double[] rangeArr = {5, 6, 7, 8};
        DoubleBuffer range = DoubleBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == 6 && range.get() == 7, "wrap range content");

        DoubleBuffer v = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        DoubleBuffer dup = v.duplicate();
        dup.put(0, -1.0);
        check(v.get(0) == -1.0, "duplicate shares storage");
        v.position(1).limit(3);
        DoubleBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 2 && sl.get() == 3, "slice range");

        DoubleBuffer cp = DoubleBuffer.allocate(4);
        cp.put(1).put(2).put(3);
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == 2 && cp.get(1) == 3, "compact preserves order");

        DoubleBuffer a = DoubleBuffer.wrap(new double[] {1, 2, 3});
        DoubleBuffer b2 = DoubleBuffer.wrap(new double[] {1, 2, 3});
        check(a.equals(b2) && a.compareTo(b2) == 0, "equals/compareTo");
        check(a.mismatch(b2) == -1, "mismatch equal");
        DoubleBuffer c3 = DoubleBuffer.wrap(new double[] {1, 9, 3});
        check(a.mismatch(c3) == 1, "mismatch index");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");

        DoubleBuffer ro = DoubleBuffer.wrap(new double[] {1}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1.0), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> DoubleBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> DoubleBuffer.allocate(2).limit(0).put(1.0), "put overflow");
        DoubleBuffer e = DoubleBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1.0), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

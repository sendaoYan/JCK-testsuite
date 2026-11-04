/*
 * @test
 * @summary java.nio.FloatBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main FloatBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: FloatBuffer 是按 4 字节 float 元素索引的类型化缓冲区, 继承 Buffer 游标状态机,
 *    元素间不透明; order() 返回创建它的 ByteBuffer 的字节序.
 * 2. 场景: float 数组 -> wrap/allocate -> 相对/绝对 put/get -> flip -> 读回;
 *    duplicate/slice 共享存储; compact 压缩未读元素.
 * 3. 组合链路: wrap(float[]) -> put(绝对) -> get(相对) -> mark/reset -> flip -> get;
 *    duplicate 写回影响源.
 * 4. 不变量: 堆缓冲区 hasArray()==true; equals/compareTo/mismatch 按 remaining 逐元素;
 *    compareTo 对 float 用 Float.compare 语义 (NaN 最大).
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.ReadOnlyBufferException;

public class FloatBufferTest {
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
        Class<?> c = Class.forName("java.nio.FloatBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "FloatBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "FloatBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", float[].class), "has wrap(float[])");
        check(hasMethod(c, "wrap", float[].class, int.class, int.class), "has wrap(float[],int,int)");
        check(hasMethod(c, "get"), "has get()");
        check(hasMethod(c, "put", float.class), "has put(float)");
        check(hasMethod(c, "get", int.class), "has get(int)");
        check(hasMethod(c, "put", int.class, float.class), "has put(int,float)");
        check(hasMethod(c, "mismatch", FloatBuffer.class), "has mismatch(FloatBuffer)");
    }

    static void testBlackBox() {
        FloatBuffer b = FloatBuffer.allocate(3);
        b.put(1.5f).put(2.5f).put(3.5f);
        check(b.position() == 3, "relative put advances");
        b.flip();
        check(b.get() == 1.5f && b.get() == 2.5f && b.get() == 3.5f, "relative get");
        check(!b.hasRemaining(), "consumed");

        float[] backing = {1f, 2f, 3f, 4f};
        FloatBuffer w = FloatBuffer.wrap(backing);
        w.put(0, 99f);
        check(backing[0] == 99f, "wrap shares array");
        check(w.get(0) == 99f, "absolute get");
        check(w.position() == 0, "absolute op leaves position");

        w.put(new float[] {7f, 8f}, 0, 2);
        check(w.position() == 2, "bulk put advances");
        float[] out = new float[2];
        w.flip();
        w.get(out, 0, 2);
        check(out[0] == 7f && out[1] == 8f, "bulk get");

        float[] rangeArr = {5f, 6f, 7f, 8f};
        FloatBuffer range = FloatBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == 6f && range.get() == 7f, "wrap range content");

        FloatBuffer v = FloatBuffer.wrap(new float[] {1f, 2f, 3f, 4f});
        FloatBuffer dup = v.duplicate();
        dup.put(0, -1f);
        check(v.get(0) == -1f, "duplicate shares storage");
        v.position(1).limit(3);
        FloatBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 2f && sl.get() == 3f, "slice range");

        FloatBuffer cp = FloatBuffer.allocate(4);
        cp.put(1f).put(2f).put(3f);
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == 2f && cp.get(1) == 3f, "compact preserves order");

        FloatBuffer a = FloatBuffer.wrap(new float[] {1f, 2f, 3f});
        FloatBuffer b2 = FloatBuffer.wrap(new float[] {1f, 2f, 3f});
        check(a.equals(b2) && a.compareTo(b2) == 0, "equals/compareTo");
        check(a.mismatch(b2) == -1, "mismatch equal");
        FloatBuffer c3 = FloatBuffer.wrap(new float[] {1f, 9f, 3f});
        check(a.mismatch(c3) == 1, "mismatch index");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");

        FloatBuffer ro = FloatBuffer.wrap(new float[] {1f}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1f), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> FloatBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> FloatBuffer.allocate(2).limit(0).put(1f), "put overflow");
        FloatBuffer e = FloatBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1f), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

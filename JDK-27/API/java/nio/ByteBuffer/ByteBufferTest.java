/*
 * @test
 * @summary java.nio.ByteBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable, factories) plus black-box allocate/direct/wrap, relative
 *          and absolute get/put, byte order, views, duplicate/slice, equality, bounds.
 * @run main ByteBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: ByteBuffer 是最通用的字节缓冲区, 支持相对/绝对 get/put、批量拷贝、类型化
 *    getXxx/putXxx、视图 (asXxxBuffer) 与字节序 order; 继承 Buffer 游标状态机.
 * 2. 场景: 网络/序列化中常见 allocate -> putInt/putShort 按大端/小端写入 -> flip ->
 *    切 order 读回; 通过 asIntBuffer 等视图以元素粒度读写同一块内存.
 * 3. 组合链路: allocate/wrap/allocateDirect -> order -> putInt -> flip -> order 切换 ->
 *    getInt; duplicate/slice 独立游标共享存储; compact 压缩未读数据到头部.
 * 4. 不变量: 视图/duplicate/slice 与源共享底层字节; order 只影响多字节读写的解释;
 *    equals/compareTo 按 remaining 区间逐字节比较; 堆缓冲区 hasArray()==true.
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException;
 *    只读视图 put 抛 ReadOnlyBufferException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.CharBuffer;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;

public class ByteBufferTest {
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
        Class<?> c = Class.forName("java.nio.ByteBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "ByteBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "ByteBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");

        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "allocateDirect", int.class), "has allocateDirect(int)");
        check(hasMethod(c, "wrap", byte[].class), "has wrap(byte[])");
        check(hasMethod(c, "wrap", byte[].class, int.class, int.class), "has wrap(byte[],int,int)");
        check(hasMethod(c, "order"), "has order()");
        check(hasMethod(c, "order", ByteOrder.class), "has order(ByteOrder)");
        check(hasMethod(c, "getInt"), "has getInt()");
        check(hasMethod(c, "putInt", int.class), "has putInt(int)");
        check(hasMethod(c, "asIntBuffer"), "has asIntBuffer()");
        check(hasMethod(c, "asLongBuffer"), "has asLongBuffer()");
        check(hasMethod(c, "asCharBuffer"), "has asCharBuffer()");
        check(hasMethod(c, "mismatch", ByteBuffer.class), "has mismatch(ByteBuffer)");
    }

    static void testBlackBox() {
        ByteBuffer bb = ByteBuffer.allocate(4);
        bb.put((byte) 1).put((byte) 2).put((byte) 3);
        check(bb.position() == 3, "position advances on relative put");
        bb.flip();
        check(bb.get() == 1 && bb.get() == 2 && bb.get() == 3, "relative get in order");
        check(!bb.hasRemaining(), "consumed");

        ByteBuffer abs = ByteBuffer.allocate(8);
        abs.put(0, (byte) 7);
        check(abs.get(0) == 7, "absolute put/get does not move position");
        check(abs.position() == 0, "absolute ops leave position");

        abs.put(new byte[] {10, 11, 12, 13}, 0, 4);
        check(abs.position() == 4, "bulk put advances position");
        byte[] out = new byte[4];
        abs.flip();
        abs.get(out, 0, 4);
        check(out[0] == 10 && out[1] == 11 && out[2] == 12 && out[3] == 13, "bulk get");

        byte[] backing = new byte[] {5, 6, 7, 8};
        ByteBuffer w = ByteBuffer.wrap(backing);
        check(w.capacity() == 4 && w.position() == 0 && w.limit() == 4, "wrap full array");
        w.put(0, (byte) 99);
        check(backing[0] == 99, "wrap shares array");

        ByteBuffer w2 = ByteBuffer.wrap(backing, 1, 2);
        check(w2.capacity() == 4 && w2.position() == 1 && w2.limit() == 3, "wrap range positions");
        check(w2.get() == 6 && w2.get() == 7, "wrap range content");
        w2.put(0, (byte) 42);
        check(backing[0] == 42, "wrap range shares array");

        ByteBuffer le = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        check(le.order() == ByteOrder.LITTLE_ENDIAN, "order getter");
        le.putInt(0x01020304);
        le.flip();
        check(le.get() == 0x04 && le.get() == 0x03 && le.get() == 0x02 && le.get() == 0x01,
              "little-endian byte layout");
        le.rewind().order(ByteOrder.BIG_ENDIAN);
        check(le.getInt() == 0x04030201, "reinterpret as big-endian");

        ByteBuffer le2 = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        le2.putShort((short) 0x1234).putLong(0x0102030405060708L);
        le2.flip();
        check(le2.get() == 0x34 && le2.get() == 0x12, "little-endian short");
        check(le2.getLong() == 0x0102030405060708L, "little-endian long roundtrip");

        ByteBuffer tv = ByteBuffer.allocate(16);
        tv.order(ByteOrder.BIG_ENDIAN);
        tv.putInt(1).putInt(2).putInt(3);
        tv.flip();
        IntBuffer ib = tv.asIntBuffer();
        check(ib.remaining() == 3, "int view remaining");
        check(ib.get() == 1 && ib.get() == 2 && ib.get() == 3, "int view reads backing");
        tv.rewind();
        ib.put(0, 42);
        check(tv.getInt(0) == 42, "view shares storage");

        tv.clear().putChar('A').putChar('B');
        tv.flip();
        CharBuffer cb = tv.asCharBuffer();
        check(cb.remaining() == 2 && cb.get() == 'A' && cb.get() == 'B', "char view");

        ByteBuffer d = ByteBuffer.wrap(new byte[] {1, 2, 3, 4});
        ByteBuffer dup = d.duplicate();
        dup.put(0, (byte) 9);
        check(d.get(0) == 9, "duplicate shares storage");
        check(dup.position() == 0 && d.position() == 0, "duplicate independent cursor");

        d.position(1).limit(3);
        ByteBuffer sl = d.slice();
        check(sl.capacity() == 2 && sl.position() == 0, "slice is compacted range");
        check(sl.get() == 2 && sl.get() == 3, "slice content");

        ByteBuffer c2 = ByteBuffer.allocate(4);
        c2.put((byte) 1).put((byte) 2).put((byte) 3);
        c2.flip();
        c2.get();
        c2.compact();
        check(c2.position() == 2 && c2.limit() == 4, "compact moves unread to front");
        check(c2.get(0) == 2 && c2.get(1) == 3, "compact preserves order");

        ByteBuffer ro = ByteBuffer.wrap(new byte[] {1, 2}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((byte) 1), "read-only put");

        ByteBuffer direct = ByteBuffer.allocateDirect(4);
        check(direct.isDirect(), "direct buffer");
        check(!direct.hasArray(), "direct buffer has no array");
        direct.putInt(7);
        direct.flip();
        check(direct.getInt() == 7, "direct roundtrip");

        ByteBuffer a = ByteBuffer.wrap(new byte[] {1, 2, 3});
        ByteBuffer b = ByteBuffer.wrap(new byte[] {1, 2, 3});
        check(a.equals(b), "equals same remaining bytes");
        check(a.compareTo(b) == 0, "compareTo equal");
        check(a.mismatch(b) == -1, "mismatch equal -> -1");
        ByteBuffer c3 = ByteBuffer.wrap(new byte[] {1, 9, 3});
        check(!a.equals(c3), "equals differs");
        check(a.mismatch(c3) == 1, "mismatch first difference index");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");
        ByteBuffer d2 = ByteBuffer.wrap(new byte[] {9, 9, 1, 2, 3});
        d2.position(2);
        check(a.equals(d2), "equals ignores position prefix");

        checkThrows(BufferUnderflowException.class, () -> ByteBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> ByteBuffer.allocate(2).limit(0).put((byte) 1), "put overflow");
        ByteBuffer e = ByteBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, (byte) 1), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
        checkThrows(IllegalArgumentException.class, () -> e.limit(-1), "limit negative");

        // ---- boundary/equivalence additions (M2) ----
        ByteBuffer zero = ByteBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> ByteBuffer.allocate(-1), "allocate(-1) IAE");
        checkThrows(IllegalArgumentException.class, () -> ByteBuffer.allocateDirect(-1), "allocateDirect(-1) IAE");

        ByteBuffer mark = ByteBuffer.allocate(4);
        mark.put((byte) 1).put((byte) 2);
        mark.flip();
        mark.mark();
        check(mark.get() == 1, "first get after mark");
        mark.reset();
        check(mark.get() == 1, "reset restores position");

        ByteBuffer f = ByteBuffer.allocate(4);
        f.put((byte) 1).put((byte) 2).put((byte) 3);
        f.flip();
        check(f.position() == 0 && f.limit() == 3, "flip sets position 0, limit old position");
        f.rewind();
        check(f.position() == 0 && f.limit() == 3, "rewind keeps limit, resets position");
        check(f.hasRemaining() && f.remaining() == 3, "remaining after rewind");

        checkThrows(IndexOutOfBoundsException.class,
                () -> ByteBuffer.wrap(new byte[] {1, 2}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ByteBuffer.wrap(new byte[] {1, 2}, 1, 5), "wrap offset+length overflow IOOBE");

        ByteBuffer putIt = ByteBuffer.allocate(4);
        putIt.put((byte) 1).put((byte) 2).put((byte) 3).put((byte) 4);
        checkThrows(BufferOverflowException.class, () -> putIt.put((byte) 5), "put past capacity overflow");
        ByteBuffer getIt = ByteBuffer.allocate(2);
        getIt.put((byte) 1).put((byte) 2).flip();
        getIt.get();
        getIt.get();
        checkThrows(BufferUnderflowException.class, getIt::get, "get past limit underflow");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

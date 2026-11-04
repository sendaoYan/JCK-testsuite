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
import java.nio.Buffer;
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

    // ===================== A1 batch: contract deepening =====================

    /** One check per state: position/limit/remaining/hasRemaining plus the invariant chain. */
    static void state(Buffer b, int pos, int lim, String msg) {
        check(b.position() == pos && b.limit() == lim
                && b.remaining() == lim - pos && b.hasRemaining() == (pos < lim)
                && b.position() >= 0 && b.position() <= b.limit() && b.limit() <= b.capacity(),
                msg + " (expected pos=" + pos + " lim=" + lim + ", actual pos=" + b.position()
                        + " lim=" + b.limit() + " rem=" + b.remaining() + ")");
    }

    /** Table-driven full cursor transition table over one buffer (capacity 6). */
    static void runStateTable(String tag, ByteBuffer b) {
        state(b, 0, 6, tag + " allocate");
        b.put((byte) 1); state(b, 1, 6, tag + " put #1");
        b.put((byte) 2); state(b, 2, 6, tag + " put #2");
        b.put((byte) 3); state(b, 3, 6, tag + " put #3");
        b.position(2);   state(b, 2, 6, tag + " position(2)");
        b.mark();        state(b, 2, 6, tag + " mark");
        b.position(4);   state(b, 4, 6, tag + " position(4)");
        b.reset();       state(b, 2, 6, tag + " reset");
        b.limit(5);      state(b, 2, 5, tag + " limit(5)");
        b.put((byte) 9); state(b, 3, 5, tag + " put at index 2");
        b.flip();        state(b, 0, 3, tag + " flip");
        b.get();         state(b, 1, 3, tag + " get");
        b.mark();        state(b, 1, 3, tag + " mark");
        b.get();         state(b, 2, 3, tag + " get again");
        b.reset();       state(b, 1, 3, tag + " reset");
        b.rewind();      state(b, 0, 3, tag + " rewind");
        b.compact();     state(b, 3, 6, tag + " compact");
        b.clear();       state(b, 0, 6, tag + " clear");
        check(b.get(0) == 1 && b.get(1) == 2 && b.get(2) == 9,
                tag + " compact kept [0,3) content {1,2,9}");
    }

    /** The same state machine must hold for heap and direct buffers. */
    static void testStateTable() {
        runStateTable("heap", ByteBuffer.allocate(6));
        runStateTable("direct", ByteBuffer.allocateDirect(6));
    }

    /** wrap(array, offset, length) parameter matrix and geometry. */
    static void testWrapMatrix() {
        byte[] a = {1, 2, 3, 4};
        ByteBuffer w = ByteBuffer.wrap(a, 1, 2);
        check(w.capacity() == 4 && w.position() == 1 && w.limit() == 3 && w.remaining() == 2,
                "wrap(off,len): position=off, limit=off+len, capacity=array.length");
        check(w.hasArray() && w.array() == a && w.arrayOffset() == 0,
                "wrap(off,len): array identity with arrayOffset 0");
        check(w.get() == 2 && w.get() == 3, "wrap(off,len) reads the given range");
        ByteBuffer zeroAtEnd = ByteBuffer.wrap(a, 4, 0);
        check(zeroAtEnd.position() == 4 && zeroAtEnd.limit() == 4 && !zeroAtEnd.hasRemaining(),
                "wrap(len,len,0): zero-length window at the end is legal");
        ByteBuffer full = ByteBuffer.wrap(a);
        check(full.position() == 0 && full.limit() == 4 && full.capacity() == 4,
                "wrap(array): position 0, limit == length");
        // Out-of-bounds wrap arguments are IndexOutOfBoundsException, not IAE.
        checkThrows(IndexOutOfBoundsException.class, () -> ByteBuffer.wrap(a, -1, 2),
                "wrap(-1,2) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> ByteBuffer.wrap(a, 1, -1),
                "wrap(1,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> ByteBuffer.wrap(a, 2, 3),
                "wrap(2,3) offset+length > array.length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> ByteBuffer.wrap(a, 5, 0),
                "wrap(5,0) offset > array.length -> IndexOutOfBoundsException");
        checkThrows(NullPointerException.class, () -> ByteBuffer.wrap(null),
                "wrap(null) -> NullPointerException");
        byte[] backing = {7, 8};
        ByteBuffer hw = ByteBuffer.wrap(backing);
        hw.put(0, (byte) 77);
        check(backing[0] == 77, "writing through a heap buffer is visible in the array");
        ByteBuffer al = ByteBuffer.allocate(2);
        check(al.arrayOffset() == 0 && !al.isDirect() && !al.isReadOnly() && al.hasArray(),
                "allocate(): heap, writable, arrayOffset 0");
    }

    /** Relative and absolute typed accessors for all six primitive types. */
    static void testTypedAccessors() {
        ByteBuffer bc = ByteBuffer.allocate(16);
        bc.putChar('Z');
        check(bc.position() == 2 && bc.getChar(0) == 'Z', "char relative put + absolute get");
        check(bc.position() == 2, "absolute typed get leaves the position");
        bc.putChar(2, 'Y');
        check(bc.getChar(2) == 'Y' && bc.position() == 2, "char absolute put/get");

        ByteBuffer bs = ByteBuffer.allocate(16);
        bs.putShort((short) -2);
        check(bs.position() == 2 && bs.getShort(0) == (short) -2, "short round trip");
        bs.putShort(2, (short) 12345);
        check(bs.getShort(2) == (short) 12345 && bs.position() == 2, "short absolute put/get");

        ByteBuffer bi = ByteBuffer.allocate(16);
        bi.putInt(0x01020304);
        check(bi.position() == 4 && bi.getInt(0) == 0x01020304, "int round trip");
        bi.putInt(4, Integer.MIN_VALUE);
        check(bi.getInt(4) == Integer.MIN_VALUE && bi.position() == 4, "int absolute put/get");

        ByteBuffer bl = ByteBuffer.allocate(16);
        bl.putLong(Long.MAX_VALUE);
        check(bl.position() == 8 && bl.getLong(0) == Long.MAX_VALUE, "long round trip");
        bl.putLong(8, 0x0102030405060708L);
        check(bl.getLong(8) == 0x0102030405060708L && bl.position() == 8, "long absolute put/get");

        ByteBuffer bf = ByteBuffer.allocate(16);
        bf.putFloat(3.5f);
        check(bf.position() == 4 && bf.getFloat(0) == 3.5f, "float round trip");
        bf.putFloat(4, -0.0f);
        check(Float.floatToRawIntBits(bf.getFloat(4)) == Float.floatToRawIntBits(-0.0f)
                && bf.position() == 4, "float absolute put/get preserves -0.0 bits");

        ByteBuffer bd = ByteBuffer.allocate(16);
        bd.putDouble(Math.PI);
        check(bd.position() == 8 && bd.getDouble(0) == Math.PI, "double round trip");
        bd.putDouble(8, 1.0);
        check(bd.getDouble(8) == 1.0 && bd.position() == 8, "double absolute put/get");

        bc.position(0);
        check(bc.getChar() == 'Z' && bc.position() == 2, "relative typed get advances the position");
    }

    /**
     * Precise three-way distinction of bounds failures:
     * relative put when full -> BufferOverflowException, relative get when empty ->
     * BufferUnderflowException, absolute index out of range -> IndexOutOfBoundsException.
     */
    static void testExceptionMatrix() {
        ByteBuffer full = ByteBuffer.allocate(3);
        full.put((byte) 1).put((byte) 2).put((byte) 3);
        checkThrows(BufferOverflowException.class, () -> full.put((byte) 4),
                "relative put when full -> BufferOverflowException");
        ByteBuffer empty = ByteBuffer.allocate(3);
        empty.limit(0);
        checkThrows(BufferUnderflowException.class, () -> empty.get(),
                "relative get when empty -> BufferUnderflowException");
        checkThrows(IndexOutOfBoundsException.class, () -> empty.get(0),
                "absolute get at index==limit -> IndexOutOfBoundsException");
        ByteBuffer cap2 = ByteBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> cap2.get(2),
                "absolute get at index==capacity -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> cap2.get(-1),
                "absolute get with negative index -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> cap2.put(-1, (byte) 1),
                "absolute put with negative index -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> cap2.put(2, (byte) 1),
                "absolute put at index==capacity -> IndexOutOfBoundsException");
        ByteBuffer lim = ByteBuffer.allocate(8);
        lim.limit(4);
        checkThrows(IndexOutOfBoundsException.class, () -> lim.put(4, (byte) 1),
                "absolute put at index==limit (capacity 8) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> lim.getInt(1),
                "absolute getInt spanning the limit -> IndexOutOfBoundsException");
        ByteBuffer four = ByteBuffer.allocate(4);
        checkThrows(BufferOverflowException.class, () -> four.putLong(1L),
                "relative putLong with 4 remaining -> BufferOverflowException");
        checkThrows(BufferOverflowException.class, () -> four.putDouble(1.0),
                "relative putDouble with 4 remaining -> BufferOverflowException");
        ByteBuffer fourRemaining = ByteBuffer.allocate(8);
        fourRemaining.limit(4);
        checkThrows(BufferUnderflowException.class, () -> fourRemaining.getLong(),
                "relative getLong with 4 remaining -> BufferUnderflowException");
        checkThrows(BufferUnderflowException.class, () -> fourRemaining.getDouble(),
                "relative getDouble with 4 remaining -> BufferUnderflowException");
    }

    static void testOrderSemantics() {
        check(ByteBuffer.allocate(4).order() == ByteOrder.BIG_ENDIAN,
                "heap default order is BIG_ENDIAN");
        check(ByteBuffer.allocateDirect(4).order() == ByteOrder.BIG_ENDIAN,
                "direct default order is BIG_ENDIAN");
        ByteBuffer bb = ByteBuffer.allocate(4);
        check(bb.order(ByteOrder.LITTLE_ENDIAN) == bb, "order(ByteOrder) returns this");
        // order only changes the interpretation, never the stored bytes
        ByteBuffer le = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        le.putInt(0x01020304);
        check(le.get(0) == 0x04 && le.get(1) == 0x03 && le.get(2) == 0x02 && le.get(3) == 0x01,
                "putInt LITTLE_ENDIAN byte layout");
        ByteBuffer be = ByteBuffer.allocate(4);
        be.putInt(0x01020304);
        check(be.get(0) == 0x01 && be.get(1) == 0x02 && be.get(2) == 0x03 && be.get(3) == 0x04,
                "putInt BIG_ENDIAN byte layout");
        ByteBuffer lb = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        lb.putLong(0x0102030405060708L);
        check(lb.get(0) == 0x01 && lb.get(1) == 0x02 && lb.get(2) == 0x03 && lb.get(3) == 0x04
                && lb.get(4) == 0x05 && lb.get(5) == 0x06 && lb.get(6) == 0x07 && lb.get(7) == 0x08,
                "putLong BIG_ENDIAN writes bytes 01..08");
        ByteBuffer ll = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        ll.putLong(0x0102030405060708L);
        check(ll.get(0) == 0x08 && ll.get(1) == 0x07 && ll.get(2) == 0x06 && ll.get(3) == 0x05
                && ll.get(4) == 0x04 && ll.get(5) == 0x03 && ll.get(6) == 0x02 && ll.get(7) == 0x01,
                "putLong LITTLE_ENDIAN writes bytes 08..01");
        ByteBuffer sw = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        sw.putLong(0x0102030405060708L);
        sw.order(ByteOrder.BIG_ENDIAN);
        check(sw.getLong(0) == 0x0807060504030201L,
                "switching to BIG_ENDIAN reinterprets LE-written bytes");
        sw.order(ByteOrder.LITTLE_ENDIAN);
        check(sw.getLong(0) == 0x0102030405060708L, "switching back restores the value");
        // All six view families inherit the byte buffer's order.
        ByteBuffer lv = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        check(lv.asCharBuffer().order() == ByteOrder.LITTLE_ENDIAN, "char view inherits LITTLE_ENDIAN");
        check(lv.asShortBuffer().order() == ByteOrder.LITTLE_ENDIAN, "short view inherits LITTLE_ENDIAN");
        check(lv.asIntBuffer().order() == ByteOrder.LITTLE_ENDIAN, "int view inherits LITTLE_ENDIAN");
        check(lv.asLongBuffer().order() == ByteOrder.LITTLE_ENDIAN, "long view inherits LITTLE_ENDIAN");
        check(lv.asFloatBuffer().order() == ByteOrder.LITTLE_ENDIAN, "float view inherits LITTLE_ENDIAN");
        check(lv.asDoubleBuffer().order() == ByteOrder.LITTLE_ENDIAN, "double view inherits LITTLE_ENDIAN");
        check(ByteBuffer.allocate(16).asIntBuffer().order() == ByteOrder.BIG_ENDIAN,
                "int view of a default buffer inherits BIG_ENDIAN");
        // slice/duplicate/asReadOnlyBuffer restart with the default BIG_ENDIAN order
        // (unspecified in the javadoc; verified identical on JDK-8 and JDK-27 by probe)
        ByteBuffer leBuf = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        check(leBuf.slice().order() == ByteOrder.BIG_ENDIAN,
                "slice() resets the order to BIG_ENDIAN (implementation-stable)");
        check(leBuf.duplicate().order() == ByteOrder.BIG_ENDIAN,
                "duplicate() resets the order to BIG_ENDIAN");
        check(leBuf.asReadOnlyBuffer().order() == ByteOrder.BIG_ENDIAN,
                "asReadOnlyBuffer() resets the order to BIG_ENDIAN");
    }

    static void testViewFamily() {
        ByteBuffer bb = ByteBuffer.allocate(32);
        for (int i = 0; i < 32; i++) bb.put(i, (byte) (i + 1));
        bb.position(4);
        bb.limit(20);
        CharBuffer cv = bb.asCharBuffer();
        check(cv.capacity() == 8 && cv.position() == 0, "char view capacity == remaining/2, position 0");
        cv.put('A');
        check(bb.get(4) == 0 && bb.get(5) == 'A', "char view writes through to the byte buffer (BIG_ENDIAN)");
        check(cv.position() == 1 && bb.position() == 4, "view cursor moves, byte buffer cursor does not");
        ByteBuffer ss = ByteBuffer.allocate(8);
        ss.asShortBuffer().put((short) 0x0102);
        check(ss.get(0) == 1 && ss.get(1) == 2, "short view writes 2 BIG_ENDIAN bytes");
        ByteBuffer si = ByteBuffer.allocate(8);
        IntBuffer iv = si.asIntBuffer();
        iv.put(0x0A0B0C0D);
        check(iv.capacity() == 2 && si.get(0) == 0x0A && si.get(1) == 0x0B
                && si.get(2) == 0x0C && si.get(3) == 0x0D, "int view writes 4 BIG_ENDIAN bytes");
        ByteBuffer sl = ByteBuffer.allocate(8);
        sl.asLongBuffer().put(0x0102030405060708L);
        check(sl.get(0) == 1 && sl.get(7) == 8, "long view writes 8 BIG_ENDIAN bytes");
        ByteBuffer sf = ByteBuffer.allocate(8);
        sf.asFloatBuffer().put(2.5f);
        check(sf.getInt(0) == Float.floatToIntBits(2.5f), "float view encoding equals putFloat");
        ByteBuffer sd = ByteBuffer.allocate(16);
        sd.asDoubleBuffer().put(1.5);
        check(sd.getLong(0) == Double.doubleToLongBits(1.5), "double view encoding equals putDouble");
        ByteBuffer leB = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        leB.asIntBuffer().put(0x01020304);
        check(leB.get(0) == 4 && leB.get(1) == 3 && leB.get(2) == 2 && leB.get(3) == 1,
                "int view of a LITTLE_ENDIAN buffer writes reversed bytes");
        ByteBuffer ro = ByteBuffer.allocate(8).asReadOnlyBuffer();
        check(ro.asIntBuffer().isReadOnly(), "view of a read-only buffer is read-only");
        ByteBuffer rt = ByteBuffer.allocate(4);
        rt.putInt(0, 0x11223344);
        check(rt.asIntBuffer().get(0) == 0x11223344, "view reads bytes written through the byte buffer");
    }

    static void testSliceDuplicateReadOnlyCompact() {
        byte[] a = {0, 1, 2, 3, 4, 5};
        ByteBuffer src = ByteBuffer.wrap(a);
        src.position(2);
        src.limit(5);
        ByteBuffer sl = src.slice();
        check(sl.capacity() == 3 && sl.position() == 0 && sl.limit() == 3,
                "slice: capacity == remaining, position 0");
        check(sl.hasArray() && sl.array() == a && sl.arrayOffset() == 2,
                "heap slice arrayOffset == source position, same backing array");
        sl.put(0, (byte) 99);
        check(a[2] == 99, "slice write is visible in the original array");
        ByteBuffer mk = ByteBuffer.allocate(6);
        mk.position(2);
        mk.mark();
        mk.position(4);
        mk.limit(5);
        ByteBuffer du = mk.duplicate();
        check(du.position() == 4 && du.limit() == 5, "duplicate copies position and limit");
        du.reset();
        check(du.position() == 2, "duplicate copies the mark");
        du.position(3);
        check(mk.position() == 4, "duplicate cursor changes do not affect the original");
        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        check(ro.duplicate().isReadOnly(), "duplicate of a read-only buffer stays read-only");
        ByteBuffer roSl = ByteBuffer.allocate(4).asReadOnlyBuffer();
        roSl.position(1);
        roSl.limit(3);
        check(roSl.slice().isReadOnly(), "slice of a read-only buffer stays read-only");
        check(ro.asReadOnlyBuffer().isReadOnly(), "asReadOnlyBuffer of a read-only buffer stays read-only");
        ByteBuffer dsl = ByteBuffer.allocateDirect(8);
        dsl.position(2);
        dsl.limit(6);
        check(dsl.slice().isDirect(), "slice of a direct buffer is direct");
        // compact: [position,limit) moves to 0, pos=remaining, limit=capacity, mark discarded
        ByteBuffer cp = ByteBuffer.allocate(6);
        cp.put(1, (byte) 11);
        cp.put(2, (byte) 22);
        cp.put(3, (byte) 33);
        cp.position(1);
        cp.mark();
        cp.position(3);
        cp.limit(4);
        cp.compact();
        check(cp.position() == 1 && cp.limit() == 6, "compact: pos=remaining, limit=capacity");
        check(cp.get(0) == 33, "compact copies the element at the old position (3) to index 0");
        checkThrows(java.nio.InvalidMarkException.class, () -> cp.reset(),
                "compact discards the mark");
        ByteBuffer cp0 = ByteBuffer.allocate(4);
        cp0.put((byte) 5);
        cp0.flip();
        cp0.compact();
        check(cp0.position() == 1 && cp0.limit() == 4 && cp0.get(0) == 5,
                "compact of a flipped buffer with position 0 keeps pos=remaining");
        ByteBuffer roC = ByteBuffer.allocate(4).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roC.compact(),
                "compact on a read-only buffer -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> roC.putLong(0, 1L),
                "typed absolute put on a read-only buffer -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> roC.put(new byte[2], 0, 2),
                "bulk put on a read-only buffer -> ReadOnlyBufferException");
    }

    static void testEqualsCompareToHashCode() {
        ByteBuffer a = ByteBuffer.wrap(new byte[]{1, 2, 3});
        ByteBuffer p = ByteBuffer.wrap(new byte[]{9, 9, 1, 2, 3});
        p.position(2);
        check(a.equals(p) && p.equals(a), "equals compares only [position,limit)");
        check(a.hashCode() == p.hashCode(), "hashCode depends only on the remaining region");
        ByteBuffer d = ByteBuffer.allocateDirect(3);
        d.put((byte) 1).put((byte) 2).put((byte) 3);
        d.flip();
        check(a.equals(d), "heap and direct buffers with equal content are equal");
        ByteBuffer ro = ByteBuffer.allocate(3);
        ro.put((byte) 1).put((byte) 2).put((byte) 3);
        ro.flip();
        check(a.equals(ro.asReadOnlyBuffer()), "read-only view equals the writable buffer");
        ByteBuffer e0 = ByteBuffer.allocate(0);
        ByteBuffer e1 = ByteBuffer.allocate(0);
        check(e0.equals(e1) && e0.hashCode() == e1.hashCode(), "two empty buffers are equal, equal hash");
        check(!e0.equals(ByteBuffer.allocate(1)), "empty buffer is not equal to a non-empty one");
        ByteBuffer x = ByteBuffer.wrap(new byte[]{1, 2});
        ByteBuffer y = ByteBuffer.wrap(new byte[]{1, 9});
        check(x.compareTo(y) < 0 && y.compareTo(x) > 0, "compareTo sign follows the first differing byte");
        ByteBuffer s = ByteBuffer.wrap(new byte[]{1, 2});
        ByteBuffer l = ByteBuffer.wrap(new byte[]{1, 2, 3});
        check(s.compareTo(l) < 0 && l.compareTo(s) > 0, "compareTo sign follows the remaining length");
        check(s.compareTo(s) == 0, "compareTo with itself is zero");
        ByteBuffer pp = ByteBuffer.wrap(new byte[]{9, 9, 1, 2});
        pp.position(2);
        check(s.compareTo(pp) == 0, "compareTo ignores the position prefix");
    }

    static void testBulkTransfer() {
        ByteBuffer dst = ByteBuffer.allocate(4);
        ByteBuffer src = ByteBuffer.wrap(new byte[]{1, 2, 3});
        dst.put(src);
        check(dst.position() == 3 && src.position() == 3, "put(ByteBuffer) advances both positions");
        check(dst.get(0) == 1 && dst.get(1) == 2 && dst.get(2) == 3, "put(ByteBuffer) copies in order");
        ByteBuffer self = ByteBuffer.allocate(4);
        checkThrows(IllegalArgumentException.class, () -> self.put(self),
                "put(this) -> IllegalArgumentException");
        checkThrows(NullPointerException.class, () -> self.put((ByteBuffer) null),
                "put((ByteBuffer)null) -> NullPointerException");
        checkThrows(BufferOverflowException.class,
                () -> ByteBuffer.allocate(1).put(ByteBuffer.wrap(new byte[]{1, 2})),
                "put(ByteBuffer) without room -> BufferOverflowException");
        ByteBuffer z = ByteBuffer.allocate(0);
        z.get(new byte[0]);
        check(true, "zero-length bulk get on an empty buffer does not throw");
        ByteBuffer zz = ByteBuffer.allocate(2);
        zz.put(new byte[0], 0, 0);
        check(zz.position() == 0, "zero-length bulk put does not advance the position");
        checkThrows(NullPointerException.class, () -> ByteBuffer.allocate(2).put((byte[]) null),
                "put((byte[])null) -> NullPointerException");
        checkThrows(NullPointerException.class, () -> ByteBuffer.allocate(2).get((byte[]) null),
                "get((byte[])null) -> NullPointerException");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ByteBuffer.allocate(2).put(new byte[2], -1, 1),
                "put(src,off,len) negative offset -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ByteBuffer.allocate(2).put(new byte[2], 1, 2),
                "put(src,off,len) off+len > src.length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ByteBuffer.allocate(2).get(new byte[2], 0, 3),
                "get(dst,off,len) len > dst.length -> IndexOutOfBoundsException");
        ByteBuffer part = ByteBuffer.wrap(new byte[]{1, 2, 3});
        byte[] out = new byte[5];
        part.get(out, 0, 2);
        check(part.position() == 2 && out[0] == 1 && out[1] == 2 && out[4] == 0,
                "partial bulk get copies exactly len bytes");
        checkThrows(BufferUnderflowException.class, () -> part.get(out, 0, 5),
                "bulk get beyond remaining -> BufferUnderflowException");
        ByteBuffer into = ByteBuffer.allocate(2);
        into.put(new byte[]{7, 8}, 0, 2);
        check(into.position() == 2, "bulk put fills up to the limit");
        checkThrows(BufferOverflowException.class, () -> into.put(new byte[]{9}, 0, 1),
                "bulk put on a full buffer -> BufferOverflowException");
    }

    /** APIs added after JDK-8: slice(int,int), absolute bulk get/put, mismatch, aligned* (JDK-13+). */
    static void testJdk13PlusApis() {
        // ---- slice(int index, int length) ----
        byte[] a = {0, 1, 2, 3, 4, 5};
        ByteBuffer bb = ByteBuffer.wrap(a);
        ByteBuffer s1 = bb.slice(2, 3);
        check(s1.capacity() == 3 && s1.position() == 0 && s1.limit() == 3,
                "slice(index,length) geometry");
        check(s1.get(0) == 2 && s1.get(1) == 3 && s1.get(2) == 4,
                "slice(index,length) content starts at index");
        s1.put(0, (byte) 100);
        check(a[2] == 100 && bb.position() == 0,
                "slice(index,length) shares content, source cursor untouched");
        check(bb.slice(6, 0).capacity() == 0, "slice(capacity,0) is an empty slice");
        checkThrows(IndexOutOfBoundsException.class, () -> bb.slice(-1, 2),
                "slice(-1,2) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> bb.slice(2, -1),
                "slice(2,-1) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> bb.slice(4, 3),
                "slice(4,3) index+length > capacity -> IndexOutOfBoundsException");
        ByteBuffer lim = ByteBuffer.wrap(a);
        lim.position(1);
        lim.limit(4);
        ByteBuffer absSlice = lim.slice(2, 2);
        check(absSlice.capacity() == 2 && absSlice.get(0) == 100 && absSlice.get(1) == 3,
                "slice(index,length) is absolute with respect to position");
        checkThrows(IndexOutOfBoundsException.class, () -> lim.slice(4, 2),
                "slice(index,length) bounds are checked against limit -> IndexOutOfBoundsException");

        // ---- get(int index, byte[] dst) and get(int, byte[], int, int) ----
        ByteBuffer g = ByteBuffer.wrap(new byte[]{10, 11, 12, 13, 14});
        byte[] dst = new byte[3];
        check(g.get(1, dst) == g, "get(int,byte[]) returns this");
        check(dst[0] == 11 && dst[1] == 12 && dst[2] == 13 && g.position() == 0,
                "get(int,byte[]) copies without moving the position");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(3, dst),
                "get(int,byte[]) index+length > capacity -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(-1, dst),
                "get(-1,byte[]) -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(1, dst, 1, 3),
                "get(int,dst,off,len) off+len > dst.length -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(1, dst, -1, 1),
                "get(int,dst,-1,1) -> IndexOutOfBoundsException");
        byte[] dst2 = new byte[4];
        g.get(1, dst2, 1, 3);
        check(dst2[0] == 0 && dst2[1] == 11 && dst2[2] == 12 && dst2[3] == 13,
                "get(int,dst,off,len) places bytes at the given offset");

        // ---- put(int index, byte[] src) and put(int, byte[], int, int) ----
        ByteBuffer q = ByteBuffer.allocate(6);
        check(q.put(2, new byte[]{7, 8, 9}) == q, "put(int,byte[]) returns this");
        check(q.get(2) == 7 && q.get(3) == 8 && q.get(4) == 9 && q.position() == 0,
                "put(int,byte[]) writes at index without moving the position");
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(4, new byte[]{7, 8, 9}),
                "put(int,byte[]) index+length > capacity -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(0, new byte[]{7, 8}, 1, 2),
                "put(int,src,off,len) off+len > src.length -> IndexOutOfBoundsException");
        q.put(6, new byte[0]);
        check(q.position() == 0, "put(capacity, empty array) is a no-op");

        // ---- put(int index, ByteBuffer src, int offset, int length) ----
        ByteBuffer tgt = ByteBuffer.allocate(8);
        ByteBuffer srcBuf = ByteBuffer.wrap(new byte[]{1, 2, 3, 4, 5});
        tgt.put(3, srcBuf, 1, 3);
        check(tgt.get(3) == 2 && tgt.get(4) == 3 && tgt.get(5) == 4,
                "put(index,src,off,len) copies the given range");
        check(tgt.position() == 0 && srcBuf.position() == 0,
                "put(index,src,off,len) leaves both cursors");
        checkThrows(IndexOutOfBoundsException.class, () -> tgt.put(6, srcBuf, 1, 3),
                "put(index,src,off,len) index+length > capacity -> IndexOutOfBoundsException");
        checkThrows(IndexOutOfBoundsException.class, () -> tgt.put(0, srcBuf, 4, 2),
                "put(index,src,off,len) off+len > src capacity -> IndexOutOfBoundsException");
        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, new byte[]{1}),
                "read-only put(int,byte[]) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, ByteBuffer.wrap(new byte[]{1}), 0, 1),
                "read-only put(int,ByteBuffer,int,int) -> ReadOnlyBufferException");

        // ---- mismatch(ByteBuffer) ----
        ByteBuffer m1 = ByteBuffer.wrap(new byte[]{1, 2, 3});
        check(m1.mismatch(ByteBuffer.wrap(new byte[]{1, 2, 3})) == -1, "mismatch equal -> -1");
        check(m1.mismatch(ByteBuffer.wrap(new byte[]{1, 9, 3})) == 1,
                "mismatch differing -> relative index of the first difference");
        check(ByteBuffer.wrap(new byte[]{1, 9, 3}).mismatch(m1) == 1,
                "mismatch index is symmetric");
        check(m1.mismatch(ByteBuffer.wrap(new byte[]{1, 2})) == 2,
                "mismatch equal prefix, shorter -> length of the shorter remaining region");
        check(ByteBuffer.wrap(new byte[]{1, 2}).mismatch(m1) == 2, "mismatch shorter vs longer symmetric");
        ByteBuffer pos = ByteBuffer.wrap(new byte[]{9, 9, 1, 2, 3});
        pos.position(2);
        check(m1.mismatch(pos) == -1 && m1.equals(pos), "mismatch/equals use [position,limit)");
        ByteBuffer lim6 = ByteBuffer.wrap(new byte[]{1, 2, 3, 4});
        lim6.limit(3);
        check(m1.mismatch(lim6) == -1, "mismatch respects the limit");
        check(ByteBuffer.allocate(0).mismatch(ByteBuffer.allocate(3)) == 0,
                "mismatch empty vs non-empty -> 0");
        check(ByteBuffer.allocate(0).mismatch(ByteBuffer.allocate(0)) == -1,
                "mismatch both empty -> -1");
        ByteBuffer dd = ByteBuffer.allocateDirect(3);
        dd.put((byte) 1).put((byte) 2).put((byte) 9);
        dd.flip();
        check(ByteBuffer.wrap(new byte[]{1, 2, 8}).mismatch(dd) == 2, "mismatch heap vs direct");

        // ---- alignedSlice(int) / alignmentOffset(int,int) (JDK-9+; unit sizes > 1
        //      are only supported for direct buffers on this platform) ----
        ByteBuffer dir = ByteBuffer.allocateDirect(64);
        check(dir.alignmentOffset(0, 8) == 0, "direct buffer base address is 8-byte aligned");
        check(dir.alignmentOffset(1, 8) == 1, "alignmentOffset(1,8) == 1 for an aligned base");
        check(dir.alignedSlice(8).capacity() == 64,
                "alignedSlice(8) keeps the capacity of an already aligned direct buffer");
        ByteBuffer pat = ByteBuffer.allocateDirect(32);
        for (int i = 0; i < 32; i++) pat.put(i, (byte) i);
        ByteBuffer trimmed = pat.alignedSlice(8);
        check(trimmed.capacity() == 32 && trimmed.get(0) == 0 && trimmed.get(31) == 31,
                "alignedSlice(8) preserves content of an aligned buffer");
        checkThrows(UnsupportedOperationException.class, () -> ByteBuffer.allocate(64).alignmentOffset(0, 8),
                "heap alignmentOffset(0,8) -> UnsupportedOperationException");
        checkThrows(UnsupportedOperationException.class, () -> ByteBuffer.allocate(64).alignedSlice(8),
                "heap alignedSlice(8) -> UnsupportedOperationException");
        check(ByteBuffer.allocate(64).alignedSlice(1).capacity() == 64,
                "unit size 1 is supported on heap buffers");
        checkThrows(IllegalArgumentException.class, () -> dir.alignmentOffset(0, 0),
                "alignmentOffset unitSize 0 -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> dir.alignmentOffset(0, 3),
                "alignmentOffset unitSize not a power of two -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> dir.alignmentOffset(-1, 8),
                "alignmentOffset negative index -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> dir.alignedSlice(0),
                "alignedSlice(0) -> IllegalArgumentException");
        checkThrows(IllegalArgumentException.class, () -> dir.alignedSlice(3),
                "alignedSlice(3) not a power of two -> IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testStateTable();
        testWrapMatrix();
        testTypedAccessors();
        testExceptionMatrix();
        testOrderSemantics();
        testViewFamily();
        testSliceDuplicateReadOnlyCompact();
        testEqualsCompareToHashCode();
        testBulkTransfer();
        testJdk13PlusApis();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

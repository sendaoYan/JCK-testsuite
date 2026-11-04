/*
 * @test
 * @summary java.nio.LongBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main LongBufferTest
 *
 * 黑盒设计案:
 * 1. 契约: LongBuffer 是按 8 字节 long 元素索引的类型化缓冲区, 继承 Buffer 游标状态机,
 *    元素间不透明; order() 返回创建它的 ByteBuffer 的字节序.
 * 2. 场景: long 数组 -> wrap/allocate -> 相对/绝对 put/get -> flip -> 读回;
 *    duplicate/slice 共享存储; compact 压缩未读元素.
 * 3. 组合链路: wrap(long[]) -> put(绝对) -> get(相对) -> mark/reset -> flip -> get;
 *    duplicate 写回影响源.
 * 4. 不变量: 堆缓冲区 hasArray()==true; equals/compareTo/mismatch 按 remaining 逐元素.
 * 5. 边界/异常: 相对 get/put 越界抛 BufferUnderflow/OverflowException; 绝对索引越界抛
 *    IndexOutOfBoundsException; position/limit 非法抛 IllegalArgumentException.
 */
import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

public class LongBufferTest {
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
        Class<?> c = Class.forName("java.nio.LongBuffer");
        check(Modifier.isAbstract(c.getModifiers()), "LongBuffer is abstract");
        check(c.getSuperclass() == java.nio.Buffer.class, "LongBuffer extends Buffer");
        check(implementsInterface(c, "java.lang.Comparable"), "implements Comparable");
        check(hasMethod(c, "allocate", int.class), "has allocate(int)");
        check(hasMethod(c, "wrap", long[].class), "has wrap(long[])");
        check(hasMethod(c, "wrap", long[].class, int.class, int.class), "has wrap(long[],int,int)");
        check(hasMethod(c, "get"), "has get()");
        check(hasMethod(c, "put", long.class), "has put(long)");
        check(hasMethod(c, "get", int.class), "has get(int)");
        check(hasMethod(c, "put", int.class, long.class), "has put(int,long)");
        check(hasMethod(c, "mismatch", LongBuffer.class), "has mismatch(LongBuffer)");
    }

    static void testBlackBox() {
        LongBuffer b = LongBuffer.allocate(3);
        b.put(10L).put(20L).put(30L);
        check(b.position() == 3, "relative put advances");
        b.flip();
        check(b.get() == 10L && b.get() == 20L && b.get() == 30L, "relative get");
        check(!b.hasRemaining(), "consumed");
        b.rewind();
        check(b.position() == 0 && b.limit() == 3, "rewind");

        long[] backing = {1L, 2L, 3L, 4L};
        LongBuffer w = LongBuffer.wrap(backing);
        w.put(0, 99L);
        check(backing[0] == 99L, "wrap shares array");
        check(w.get(0) == 99L, "absolute get");
        check(w.position() == 0, "absolute op leaves position");

        w.put(new long[] {7L, 8L}, 0, 2);
        check(w.position() == 2, "bulk put advances");
        long[] out = new long[2];
        w.flip();
        w.get(out, 0, 2);
        check(out[0] == 7L && out[1] == 8L, "bulk get");

        long[] rangeArr = {5L, 6L, 7L, 8L};
        LongBuffer range = LongBuffer.wrap(rangeArr, 1, 2);
        check(range.capacity() == 4 && range.position() == 1 && range.limit() == 3, "wrap range positions");
        check(range.get() == 6L && range.get() == 7L, "wrap range content");

        LongBuffer v = LongBuffer.wrap(new long[] {1L, 2L, 3L, 4L});
        LongBuffer dup = v.duplicate();
        dup.put(0, -1L);
        check(v.get(0) == -1L, "duplicate shares storage");
        v.position(1).limit(3);
        LongBuffer sl = v.slice();
        check(sl.capacity() == 2 && sl.get() == 2L && sl.get() == 3L, "slice range");

        LongBuffer cp = LongBuffer.allocate(4);
        cp.put(1L).put(2L).put(3L);
        cp.flip();
        cp.get();
        cp.compact();
        check(cp.position() == 2 && cp.limit() == 4, "compact position/limit");
        check(cp.get(0) == 2L && cp.get(1) == 3L, "compact preserves order");

        LongBuffer a = LongBuffer.wrap(new long[] {1L, 2L, 3L});
        LongBuffer b2 = LongBuffer.wrap(new long[] {1L, 2L, 3L});
        check(a.equals(b2) && a.compareTo(b2) == 0, "equals/compareTo");
        check(a.mismatch(b2) == -1, "mismatch equal");
        LongBuffer c3 = LongBuffer.wrap(new long[] {1L, 9L, 3L});
        check(a.mismatch(c3) == 1, "mismatch index");

        LongBuffer ro = LongBuffer.wrap(new long[] {1L}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1L), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> LongBuffer.allocate(0).get(), "get underflow");
        checkThrows(BufferOverflowException.class, () -> LongBuffer.allocate(2).limit(0).put(1L), "put overflow");
        LongBuffer e = LongBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1L), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
                testStateMachineTable();
        testMarkDiscardRules();
        testAccessSemantics();
        testBoundsExceptionMatrix();
        testBulkTransferMatrix();
        testWrapContract();
        testViewBuffers();
        testEqualsCompareHash();
        testCompactContract();
        testByteBufferView();
        testJdk13PlusApi();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Batch A2 contract deepening: state machine / bounds matrices / views /
    // byte order / comparison semantics. Facts below were probed on JDK-8
    // (jdk8u372-b07) and JDK-27 (27.0.1.0.2+1) and behave identically unless noted.

    // Five-value snapshot (position/limit/capacity/remaining/hasRemaining).
    static void checkState(LongBuffer b, int pos, int lim, int cap, String step) {
        check(b.position() == pos, step + ": position");
        check(b.limit() == lim, step + ": limit");
        check(b.capacity() == cap, step + ": capacity");
        check(b.remaining() == lim - pos, step + ": remaining");
        check(b.hasRemaining() == (pos < lim), step + ": hasRemaining");
    }

    // Table-driven walk of the buffer state machine: every mutating step asserts
    // the full cursor state (allocate -> put xk -> position/limit -> mark/reset
    // -> flip/rewind/clear -> limit clamp).
    static void testStateMachineTable() {
        LongBuffer b = LongBuffer.allocate(4);
        checkState(b, 0, 4, 4, "allocate");
        b.put(1L);
        checkState(b, 1, 4, 4, "put#1");
        b.put(2L);
        checkState(b, 2, 4, 4, "put#2");
        b.mark();
        b.position(3);
        checkState(b, 3, 4, 4, "position(3) above mark keeps mark");
        b.reset();
        checkState(b, 2, 4, 4, "reset returns to mark");
        b.limit(3);
        checkState(b, 2, 3, 4, "limit(3)");
        b.flip();
        checkState(b, 0, 2, 4, "flip");
        check(b.get() == 1L, "flip exposes unread prefix first");
        checkState(b, 1, 2, 4, "relative get");
        b.rewind();
        checkState(b, 0, 2, 4, "rewind");
        b.clear();
        checkState(b, 0, 4, 4, "clear");
        b.put(1L).put(2L).put(3L);
        checkState(b, 3, 4, 4, "put x3");
        b.limit(2);                       // limit below position clamps position
        checkState(b, 2, 2, 4, "limit(2)<position clamps position to limit");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position()>limit -> IAE");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IAE");
        LongBuffer c = LongBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> c.limit(4),
                "limit()>capacity -> IAE");
        checkThrows(IllegalArgumentException.class, () -> c.limit(-1),
                "limit(-1) -> IAE");
        c.position(2);
        c.limit(3);
        checkState(c, 2, 3, 3, "position<=limit accepted");
        LongBuffer t = LongBuffer.allocate(3);
        t.put(1L);
        t.limit(2);
        check(t.toString().contains("pos=1 lim=2 cap=3"), "toString carries pos/lim/cap");
    }

    // mark lifecycle: which operations discard the mark (probed both JDKs).
    static void testMarkDiscardRules() {
        LongBuffer b = LongBuffer.allocate(4);
        b.position(2);
        b.mark();
        b.position(3);
        b.reset();
        check(b.position() == 2, "reset after forward position returns to mark");
        b.position(1);
        checkThrows(InvalidMarkException.class, b::reset,
                "position moved below mark discards mark");
        LongBuffer l = LongBuffer.allocate(4);
        l.position(2);
        l.mark();
        l.limit(1);
        checkThrows(InvalidMarkException.class, l::reset,
                "limit moved below mark discards mark");
        LongBuffer f = LongBuffer.allocate(4);
        f.position(2);
        f.mark();
        f.flip();
        checkThrows(InvalidMarkException.class, f::reset, "flip discards mark");
        LongBuffer r = LongBuffer.allocate(4);
        r.position(2);
        r.mark();
        r.rewind();
        checkThrows(InvalidMarkException.class, r::reset, "rewind discards mark");
        LongBuffer cl = LongBuffer.allocate(4);
        cl.position(2);
        cl.mark();
        cl.clear();
        checkThrows(InvalidMarkException.class, cl::reset, "clear discards mark");
        LongBuffer k = LongBuffer.allocate(4);
        k.position(2);
        k.mark();
        k.position(3);
        k.reset();
        k.position(3);
        k.reset();
        check(k.position() == 2, "reset keeps the mark (repeatable)");
        checkThrows(InvalidMarkException.class,
                () -> LongBuffer.allocate(2).reset(), "fresh buffer has no mark");
        LongBuffer s = LongBuffer.allocate(4);
        s.position(1);
        s.mark();
        checkThrows(InvalidMarkException.class, () -> s.slice().reset(),
                "slice starts with undefined mark");
    }

    // relative vs absolute access, allocate zero-fill, chainability.
    static void testAccessSemantics() {
        LongBuffer b = LongBuffer.allocate(3);
        check(b.get(0) == 0L, "allocate zero-fills element 0");
        check(b.get(1) == 0L, "allocate zero-fills element 1");
        check(b.get(2) == 0L, "allocate zero-fills element 2");
        check(b.position() == 0, "absolute get leaves position");
        LongBuffer r = b.put(5L);
        check(r == b, "relative put returns this");
        check(b.position() == 1, "relative put advances position");
        check(b.get(0) == 5L, "relative put stored at the old position");
        b.put(7L);
        check(b.get(1) == 7L && b.position() == 2, "second relative put lands at index 1");
        b.get();
        check(b.position() == 3, "relative get advances position");
        b.put(0, 9L);
        check(b.position() == 3, "absolute put leaves position");
        check(b.get(0) == 9L, "absolute get reads back absolute put");
        LongBuffer ch = LongBuffer.allocate(3);
        check(ch.put(1L).put(2L).put(3L) == ch,
                "relative puts chain, each returning this");
    }

    // Precise exception matrix: relative full/empty, absolute index bounds,
    // allocate(-1), wrap bounds; failed operations leave the cursor untouched.
    static void testBoundsExceptionMatrix() {
        LongBuffer full = LongBuffer.allocate(2);
        full.put(1L).put(2L);
        checkThrows(BufferOverflowException.class, () -> full.put(3L),
                "relative put on full -> BufferOverflowException");
        check(full.position() == 2, "failed relative put leaves position");
        LongBuffer empty = LongBuffer.allocate(2);
        empty.flip();
        checkThrows(BufferUnderflowException.class, empty::get,
                "relative get on empty -> BufferUnderflowException");
        check(empty.position() == 0, "failed relative get leaves position");
        LongBuffer b = LongBuffer.allocate(3);
        b.limit(2);
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(2),
                "absolute get(limit) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(3),
                "absolute get(capacity) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(-1),
                "absolute get(-1) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(2, 9L),
                "absolute put(limit,v) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(-1, 9L),
                "absolute put(-1,v) -> IndexOOB");
        check(b.get(1) == 0L, "absolute get inside limit succeeds");
        checkThrows(IllegalArgumentException.class, () -> LongBuffer.allocate(-1),
                "allocate(-1) -> IAE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> LongBuffer.wrap(new long[] {1, 2, 3, 4}, -1, 2), "wrap off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> LongBuffer.wrap(new long[] {1, 2, 3, 4}, 0, -1), "wrap len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> LongBuffer.wrap(new long[] {1, 2, 3, 4}, 3, 2), "wrap off+len>length -> IndexOOB");
        checkState(LongBuffer.wrap(new long[] {1, 2, 3, 4}, 1, 2), 1, 3, 4, "wrap(arr,1,2) legal boundary");
        checkState(LongBuffer.wrap(new long[] {1, 2, 3, 4}, 4, 0), 4, 4, 4, "wrap(arr,len=0) at tail legal");
    }

    // Bulk get/put parameter matrix (bounds error outranks over/underflow,
    // probed on both JDKs) plus put(XBuffer) semantics.
    static void testBulkTransferMatrix() {
        long[] src = new long[] {1, 2, 3, 4};
        LongBuffer g = LongBuffer.allocate(2);
        g.put(9L).put(9L);
        g.flip();
        long[] dst = new long[] {0, 0, 0};
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(dst, -1, 2),
                "bulk get off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(dst, 0, -1),
                "bulk get len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(dst, 2, 2),
                "bulk get off+len>dst.length -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(dst, -1, 3),
                "bulk get bounds error outranks underflow");
        check(g.position() == 0, "failed bulk get leaves position");
        checkThrows(BufferUnderflowException.class, () -> g.get(dst, 0, 3),
                "bulk get len>remaining -> BufferUnderflowException");
        checkThrows(BufferUnderflowException.class,
                () -> g.get(new long[] {0L, 0L, 0L}),
                "get(array) full-array -> BufferUnderflowException");
        long[] out = new long[] {0, 0};
        g.get(out, 0, 2);
        check(out[0] == 9L && out[1] == 9L, "bulk get copies exactly the window");
        check(g.position() == 2, "bulk get advances position");
        LongBuffer p = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        p.position(1);
        long[] win = new long[] {7, 7, 7, 7};
        p.get(win, 1, 2);
        check(win[0] == 7L && win[1] == 2L && win[2] == 3L && win[3] == 7L,
                "bulk get writes only dst[off,off+len)");
        LongBuffer q = LongBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(src, -1, 2),
                "bulk put off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(src, 0, -1),
                "bulk put len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(src, 3, 2),
                "bulk put off+len>src.length -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> q.put(src, 2, 3),
                "bulk put bounds error outranks overflow");
        checkThrows(BufferOverflowException.class, () -> q.put(src, 0, 3),
                "bulk put len>remaining -> BufferOverflowException");
        check(q.position() == 0, "failed bulk put leaves position");
        q.put(src, 4, 0);
        check(q.position() == 0, "bulk put len=0 at tail is a legal no-op");
        q.put(src, 1, 2);
        check(q.position() == 2, "bulk put advances position");
        check(q.get(0) == 2L && q.get(1) == 3L, "bulk put copies src[off,off+len)");
        LongBuffer from = LongBuffer.wrap(new long[] {1, 2, 3});
        from.position(1);                       // remaining {2,3}
        LongBuffer to = LongBuffer.allocate(3);
        LongBuffer ret = to.put(from);
        check(ret == to, "put(XBuffer) returns this");
        check(to.get(0) == 2L && to.get(1) == 3L,
                "put(XBuffer) copies the src remaining region");
        check(to.position() == 2 && from.position() == 3,
                "put(XBuffer) advances both positions");
        checkThrows(IllegalArgumentException.class, () -> to.put(to),
                "put(this) -> IllegalArgumentException");
        LongBuffer big = LongBuffer.wrap(new long[] {1, 2, 3});
        checkThrows(BufferOverflowException.class, () -> to.put(big),
                "put(XBuffer) with insufficient remaining -> BufferOverflowException");
        LongBuffer roTo = LongBuffer.allocate(3).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roTo.put(big),
                "put(XBuffer) on read-only -> ReadOnlyBufferException");
    }

    // wrap(array): identity/offset, bidirectional visibility, ranged wrap.
    static void testWrapContract() {
        long[] arr = new long[] {1, 2, 3, 4};
        LongBuffer w = LongBuffer.wrap(arr);
        checkState(w, 0, 4, 4, "wrap(array)");
        check(w.hasArray(), "heap wrap hasArray");
        check(w.array() == arr, "wrap(array) exposes backing array identity");
        check(w.arrayOffset() == 0, "wrap(array) arrayOffset 0");
        arr[0] = 40L;
        check(w.get(0) == 40L, "array write visible through buffer");
        w.put(1, 50L);
        check(arr[1] == 50L, "buffer absolute put visible in array");
        w.rewind();
        w.put(60L);
        check(arr[0] == 60L, "buffer relative put writes array at position");
        long[] arr2 = new long[] {9, 1, 2, 9};
        LongBuffer r = LongBuffer.wrap(arr2, 1, 2);
        checkState(r, 1, 3, 4, "wrap(array,1,2)");
        check(r.array() == arr2 && r.arrayOffset() == 0, "ranged wrap same array");
        check(r.get() == 1L && r.get() == 2L, "ranged wrap reads [off,off+len)");
        checkThrows(BufferUnderflowException.class, r::get,
                "ranged wrap exhausted at off+len");
        r.put(0, 11L);
        check(arr2[0] == 11L, "ranged wrap index 0 maps to array[0] (cursor starts at off)");
        LongBuffer z = LongBuffer.wrap(arr2, 2, 0);
        check(!z.hasRemaining(), "wrap len 0 has nothing remaining");
        LongBuffer ro = LongBuffer.wrap(arr).asReadOnlyBuffer();
        check(!ro.hasArray(), "read-only wrap hides the array");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "read-only array() -> ReadOnlyBufferException");
    }

    // slice/duplicate/asReadOnlyBuffer: cursor independence + shared content.
    static void testViewBuffers() {
        LongBuffer b = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        b.position(1);
        b.limit(3);
        b.mark();
        LongBuffer d = b.duplicate();
        checkState(d, 1, 3, 4, "duplicate copies pos/limit/capacity");
        d.reset();
        check(d.position() == 1, "duplicate inherits the mark");
        d.put(20L);                            // writes index 1
        check(b.get(1) == 20L, "duplicate shares content with source");
        d.position(2);
        check(b.position() == 1, "duplicate cursor independent of source");
        LongBuffer s = b.slice();
        checkState(s, 0, 2, 2, "slice is a fresh cursor over [pos,limit)");
        check(s.get(0) == 20L && s.get(1) == 3L,
                "slice index 0 maps to source position");
        s.put(1, 30L);
        check(b.get(2) == 30L, "slice writes through to source");
        check(b.position() == 1 && b.limit() == 3,
                "creating a slice leaves the source cursor unchanged");
        s.position(1);
        check(b.position() == 1, "slice cursor independent of source");
        LongBuffer ro = b.asReadOnlyBuffer();
        check(ro.isReadOnly() && !b.isReadOnly(), "asReadOnlyBuffer flag, source writable");
        checkState(ro, 1, 3, 4, "asReadOnlyBuffer copies pos/limit/capacity");
        ro.position(2);
        check(b.position() == 1, "read-only cursor independent of source");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1L),
                "read-only relative put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, 1L),
                "read-only absolute put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new long[] {1, 2}, 0, 2),
                "read-only bulk put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(b.duplicate()),
                "read-only put(XBuffer) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "read-only compact -> ReadOnlyBufferException");
        b.put(2, 45L);
        check(ro.get(2) == 45L, "read-only view sees source mutation");
        check(b.duplicate().equals(b), "duplicate equals source (same remaining)");
    }

    // equals/compareTo/hashCode compare the remaining region only.
    static void testEqualsCompareHash() {
        LongBuffer a = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        a.position(1);
        a.limit(3);                              // remaining {2,3}
        LongBuffer b = LongBuffer.wrap(new long[] {9, 2, 3, 9}); // different prefix/suffix
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        check(a.equals(b), "equals compares only the remaining region");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal remaining -> equal hashCode");
        check(a.compareTo(b) == 0, "compareTo equal on shifted regions");
        LongBuffer base = LongBuffer.wrap(new long[] {1, 2, 3});   // {1,2,3}
        LongBuffer bigEl = LongBuffer.wrap(new long[] {1, 5, 9}); // {1,5,9}
        check(!base.equals(bigEl), "differing element -> not equal");
        check(base.compareTo(bigEl) < 0, "first difference decides (2 < 5)");
        check(bigEl.compareTo(base) > 0, "compareTo sign antisymmetric");
        check(LongBuffer.wrap(new long[] {1, 2, 3}).hashCode() == LongBuffer.wrap(new long[] {1, 2, 3}).hashCode(),
                "independent buffers with equal content hash equally");
        LongBuffer shorter = LongBuffer.wrap(new long[] {1, 2}); // {1,2}
        check(!base.equals(shorter), "different remaining length -> not equal");
        check(base.compareTo(shorter) > 0, "prefix: longer remaining is greater");
        check(shorter.compareTo(base) < 0, "prefix: shorter remaining is less");
        check(!base.equals(null), "equals(null) false");
        check(!base.equals("not a buffer"), "equals(other type) false");
        check(base.equals(base), "equals self");
        check(base.equals(base.asReadOnlyBuffer()), "read-only same content equal");
        check(base.equals(base.duplicate()), "duplicate equal");
    }

    // compact: [pos,limit) -> 0, pos=remaining, limit=capacity, mark discarded.
    static void testCompactContract() {
        LongBuffer b = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        b.mark();
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        b.compact();
        checkState(b, 2, 4, 4, "compact: pos=remaining, limit=capacity");
        check(b.get(0) == 2L && b.get(1) == 3L,
                "compact copies [position,limit) to the front");
        checkThrows(InvalidMarkException.class, b::reset, "compact discards mark");
        b.put(8L);
        check(b.position() == 3 && b.get(2) == 8L,
                "compacted buffer still has room at the tail");
        LongBuffer whole = LongBuffer.allocate(3);
        whole.put(1L).put(2L).put(3L);
        whole.flip();                             // nothing consumed: position 0
        whole.compact();
        checkState(whole, 3, 3, 3, "compact with nothing consumed -> pos=limit=capacity");
        check(whole.get(0) == 1L && whole.get(2) == 3L,
                "compact with nothing consumed preserves content");
        LongBuffer done = LongBuffer.allocate(3);
        done.put(1L).put(2L).put(3L);
        done.flip();
        done.position(3);                         // everything consumed
        done.compact();
        checkState(done, 0, 3, 3, "compact with empty remaining -> pos 0");
        done.put(9L);
        check(done.get(0) == 9L, "empty-compacted buffer writable at index 0");
    }

    // Views over a ByteBuffer: capacity from remaining, order captured at
    // creation, BIG/LITTLE_ENDIAN layouts verified byte-by-byte through the
    // byte layer, write-through in both directions.
    static void testByteBufferView() {
        ByteBuffer head = ByteBuffer.allocate(32);
        head.position(8);
        LongBuffer v = head.asLongBuffer();
        check(v.capacity() == 3, "view capacity = byteBuffer.remaining()/elementSize");
        checkState(v, 0, 3, 3, "view starts with a fresh cursor");
        check(head.position() == 8, "creating the view leaves the byte position");
        ByteBuffer big = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        LongBuffer vb = big.asLongBuffer();
        vb.put(0x0102030405060708L);
        boolean bigOk = true;
        for (int i = 0; i < 8; i++) bigOk &= (big.get(i) & 0xFF) == (i + 1);
        check(bigOk, "BIG_ENDIAN view stores the high-order byte first");
        check(vb.order() == ByteOrder.BIG_ENDIAN, "view inherits BIG_ENDIAN");
        ByteBuffer lit = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        LongBuffer vl = lit.asLongBuffer();
        vl.put(0x0102030405060708L);
        boolean litOk = true;
        for (int i = 0; i < 8; i++) litOk &= (lit.get(i) & 0xFF) == (8 - i);
        check(litOk, "LITTLE_ENDIAN view stores the low-order byte first");
        check(vl.order() == ByteOrder.LITTLE_ENDIAN, "view inherits LITTLE_ENDIAN");
        vb.put(0x0102030405060708L);
        boolean wbOk = true;
        for (int i = 0; i < 8; i++) wbOk &= (big.get(8 + i) & 0xFF) == (i + 1);
        check(wbOk, "view put writes through to the byte buffer");
        for (int i = 0; i < 8; i++) big.put(8 + i, (byte) (0x10 + i));
        check(vb.get(1) == 0x1011121314151617L, "byte buffer mutation visible through the view");
        check(vb.position() == 2, "view cursor tracks its own puts");
        LongBuffer ro = big.asReadOnlyBuffer().asLongBuffer();
        check(ro.isReadOnly(), "read-only byte buffer yields a read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1L),
                "read-only view rejects put");
    }

    // JDK-11/13 additions: mismatch(XBuffer) (JDK 11); slice(int,int),
    // get(int,arr,off,len), put(int,arr,off,len), put(int,XBuffer,off,len)
    // (JDK 13). The JDK-8 twin test asserts their absence by reflection.
    static void testJdk13PlusApi() {
        LongBuffer b = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        LongBuffer s = b.slice(1, 2);
        checkState(s, 0, 2, 2, "slice(index,length) fresh cursor");
        check(s.get(0) == 2L && s.get(1) == 3L, "slice(index,length) starts at index");
        s.put(0, 20L);
        check(b.get(1) == 20L, "slice(index,length) shares content");
        check(b.position() == 0, "slice(index,length) leaves source cursor");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(-1, 2),
                "slice(index<0) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(0, -1),
                "slice(length<0) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(3, 2),
                "slice(index+length>capacity) -> IndexOOB");
        check(b.slice(4, 0).capacity() == 0, "slice(capacity,0) empty is legal");
        LongBuffer g = LongBuffer.wrap(new long[] {1, 2, 3, 4});
        long[] dst = new long[] {0, 0, 0};
        LongBuffer ret = g.get(1, dst, 0, 2);
        check(ret == g, "get(index,dst,off,len) returns this");
        check(dst[0] == 2L && dst[1] == 3L && dst[2] == 0L,
                "absolute bulk get copies the window");
        check(g.position() == 0, "absolute bulk get leaves position");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(3, dst, 0, 2),
                "abs bulk get index+len>limit -> IndexOOB (not underflow)");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(0, dst, -1, 2),
                "abs bulk get off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(0, dst, 0, 4),
                "abs bulk get off+len>dst.length -> IndexOOB");
        g.put(1, new long[] {1, 2}, 0, 2);
        check(g.get(1) == 1L && g.get(2) == 2L && g.position() == 0,
                "absolute bulk put writes window, position fixed");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(3, new long[] {1, 2}, 0, 2),
                "abs bulk put index+len>limit -> IndexOOB (not overflow)");
        checkThrows(ReadOnlyBufferException.class,
                () -> g.asReadOnlyBuffer().put(0, new long[] {1, 2}, 0, 2),
                "abs bulk put on read-only -> ReadOnlyBufferException");
        LongBuffer src = LongBuffer.wrap(new long[] {1, 2});
        g.put(0, src, 0, 2);
        check(g.get(0) == 1L && g.get(1) == 2L,
                "put(index,XBuffer,off,len) copies the window");
        check(g.position() == 0 && src.position() == 0,
                "put(index,XBuffer,off,len) fixes both cursors");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(3, src, 0, 2),
                "put(index,src,off,len) target overflow -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(0, src, 1, 2),
                "put(index,src,off,len) bad src range -> IndexOOB");
        LongBuffer m1 = LongBuffer.wrap(new long[] {1, 2, 3});
        check(m1.mismatch(LongBuffer.wrap(new long[] {1, 2, 3})) == -1, "mismatch equal -> -1");
        check(m1.mismatch(LongBuffer.wrap(new long[] {1, 9, 3})) == 1,
                "mismatch returns first differing relative index");
        check(m1.mismatch(LongBuffer.wrap(new long[] {1, 2})) == 2,
                "mismatch proper prefix -> shorter remaining");
        check(LongBuffer.wrap(new long[] {1, 2}).mismatch(m1) == 2, "mismatch prefix symmetric");
        check(m1.mismatch(LongBuffer.wrap(new long[] {5, 2, 3})) == 0, "mismatch at index 0");
        check(LongBuffer.allocate(0).mismatch(LongBuffer.allocate(0)) == -1,
                "mismatch both empty -> -1");
        check(LongBuffer.allocate(0).mismatch(m1) == 0, "mismatch empty vs nonempty -> 0");
        LongBuffer sh1 = LongBuffer.wrap(new long[] {9, 1, 2});
        sh1.position(1);
        check(sh1.mismatch(LongBuffer.wrap(new long[] {1, 2})) == -1, "mismatch ignores positions");
        LongBuffer sh2 = LongBuffer.wrap(new long[] {1, 9});
        check(sh1.mismatch(sh2) == 1, "mismatch on shifted regions");
    }
}

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
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

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
    static void checkState(DoubleBuffer b, int pos, int lim, int cap, String step) {
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
        DoubleBuffer b = DoubleBuffer.allocate(4);
        checkState(b, 0, 4, 4, "allocate");
        b.put(1d);
        checkState(b, 1, 4, 4, "put#1");
        b.put(2d);
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
        check(b.get() == 1d, "flip exposes unread prefix first");
        checkState(b, 1, 2, 4, "relative get");
        b.rewind();
        checkState(b, 0, 2, 4, "rewind");
        b.clear();
        checkState(b, 0, 4, 4, "clear");
        b.put(1d).put(2d).put(3d);
        checkState(b, 3, 4, 4, "put x3");
        b.limit(2);                       // limit below position clamps position
        checkState(b, 2, 2, 4, "limit(2)<position clamps position to limit");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position()>limit -> IAE");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IAE");
        DoubleBuffer c = DoubleBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> c.limit(4),
                "limit()>capacity -> IAE");
        checkThrows(IllegalArgumentException.class, () -> c.limit(-1),
                "limit(-1) -> IAE");
        c.position(2);
        c.limit(3);
        checkState(c, 2, 3, 3, "position<=limit accepted");
        DoubleBuffer t = DoubleBuffer.allocate(3);
        t.put(1d);
        t.limit(2);
        check(t.toString().contains("pos=1 lim=2 cap=3"), "toString carries pos/lim/cap");
    }

    // mark lifecycle: which operations discard the mark (probed both JDKs).
    static void testMarkDiscardRules() {
        DoubleBuffer b = DoubleBuffer.allocate(4);
        b.position(2);
        b.mark();
        b.position(3);
        b.reset();
        check(b.position() == 2, "reset after forward position returns to mark");
        b.position(1);
        checkThrows(InvalidMarkException.class, b::reset,
                "position moved below mark discards mark");
        DoubleBuffer l = DoubleBuffer.allocate(4);
        l.position(2);
        l.mark();
        l.limit(1);
        checkThrows(InvalidMarkException.class, l::reset,
                "limit moved below mark discards mark");
        DoubleBuffer f = DoubleBuffer.allocate(4);
        f.position(2);
        f.mark();
        f.flip();
        checkThrows(InvalidMarkException.class, f::reset, "flip discards mark");
        DoubleBuffer r = DoubleBuffer.allocate(4);
        r.position(2);
        r.mark();
        r.rewind();
        checkThrows(InvalidMarkException.class, r::reset, "rewind discards mark");
        DoubleBuffer cl = DoubleBuffer.allocate(4);
        cl.position(2);
        cl.mark();
        cl.clear();
        checkThrows(InvalidMarkException.class, cl::reset, "clear discards mark");
        DoubleBuffer k = DoubleBuffer.allocate(4);
        k.position(2);
        k.mark();
        k.position(3);
        k.reset();
        k.position(3);
        k.reset();
        check(k.position() == 2, "reset keeps the mark (repeatable)");
        checkThrows(InvalidMarkException.class,
                () -> DoubleBuffer.allocate(2).reset(), "fresh buffer has no mark");
        DoubleBuffer s = DoubleBuffer.allocate(4);
        s.position(1);
        s.mark();
        checkThrows(InvalidMarkException.class, () -> s.slice().reset(),
                "slice starts with undefined mark");
    }

    // relative vs absolute access, allocate zero-fill, chainability.
    static void testAccessSemantics() {
        DoubleBuffer b = DoubleBuffer.allocate(3);
        check(b.get(0) == 0d, "allocate zero-fills element 0");
        check(b.get(1) == 0d, "allocate zero-fills element 1");
        check(b.get(2) == 0d, "allocate zero-fills element 2");
        check(b.position() == 0, "absolute get leaves position");
        DoubleBuffer r = b.put(5d);
        check(r == b, "relative put returns this");
        check(b.position() == 1, "relative put advances position");
        check(b.get(0) == 5d, "relative put stored at the old position");
        b.put(7d);
        check(b.get(1) == 7d && b.position() == 2, "second relative put lands at index 1");
        b.get();
        check(b.position() == 3, "relative get advances position");
        b.put(0, 9d);
        check(b.position() == 3, "absolute put leaves position");
        check(b.get(0) == 9d, "absolute get reads back absolute put");
        DoubleBuffer ch = DoubleBuffer.allocate(3);
        check(ch.put(1d).put(2d).put(3d) == ch,
                "relative puts chain, each returning this");
    }

    // Precise exception matrix: relative full/empty, absolute index bounds,
    // allocate(-1), wrap bounds; failed operations leave the cursor untouched.
    static void testBoundsExceptionMatrix() {
        DoubleBuffer full = DoubleBuffer.allocate(2);
        full.put(1d).put(2d);
        checkThrows(BufferOverflowException.class, () -> full.put(3d),
                "relative put on full -> BufferOverflowException");
        check(full.position() == 2, "failed relative put leaves position");
        DoubleBuffer empty = DoubleBuffer.allocate(2);
        empty.flip();
        checkThrows(BufferUnderflowException.class, empty::get,
                "relative get on empty -> BufferUnderflowException");
        check(empty.position() == 0, "failed relative get leaves position");
        DoubleBuffer b = DoubleBuffer.allocate(3);
        b.limit(2);
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(2),
                "absolute get(limit) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(3),
                "absolute get(capacity) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(-1),
                "absolute get(-1) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(2, 9d),
                "absolute put(limit,v) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(-1, 9d),
                "absolute put(-1,v) -> IndexOOB");
        check(b.get(1) == 0d, "absolute get inside limit succeeds");
        checkThrows(IllegalArgumentException.class, () -> DoubleBuffer.allocate(-1),
                "allocate(-1) -> IAE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> DoubleBuffer.wrap(new double[] {1, 2, 3, 4}, -1, 2), "wrap off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> DoubleBuffer.wrap(new double[] {1, 2, 3, 4}, 0, -1), "wrap len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> DoubleBuffer.wrap(new double[] {1, 2, 3, 4}, 3, 2), "wrap off+len>length -> IndexOOB");
        checkState(DoubleBuffer.wrap(new double[] {1, 2, 3, 4}, 1, 2), 1, 3, 4, "wrap(arr,1,2) legal boundary");
        checkState(DoubleBuffer.wrap(new double[] {1, 2, 3, 4}, 4, 0), 4, 4, 4, "wrap(arr,len=0) at tail legal");
    }

    // Bulk get/put parameter matrix (bounds error outranks over/underflow,
    // probed on both JDKs) plus put(XBuffer) semantics.
    static void testBulkTransferMatrix() {
        double[] src = new double[] {1, 2, 3, 4};
        DoubleBuffer g = DoubleBuffer.allocate(2);
        g.put(9d).put(9d);
        g.flip();
        double[] dst = new double[] {0, 0, 0};
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
                () -> g.get(new double[] {0d, 0d, 0d}),
                "get(array) full-array -> BufferUnderflowException");
        double[] out = new double[] {0, 0};
        g.get(out, 0, 2);
        check(out[0] == 9d && out[1] == 9d, "bulk get copies exactly the window");
        check(g.position() == 2, "bulk get advances position");
        DoubleBuffer p = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        p.position(1);
        double[] win = new double[] {7, 7, 7, 7};
        p.get(win, 1, 2);
        check(win[0] == 7d && win[1] == 2d && win[2] == 3d && win[3] == 7d,
                "bulk get writes only dst[off,off+len)");
        DoubleBuffer q = DoubleBuffer.allocate(2);
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
        check(q.get(0) == 2d && q.get(1) == 3d, "bulk put copies src[off,off+len)");
        DoubleBuffer from = DoubleBuffer.wrap(new double[] {1, 2, 3});
        from.position(1);                       // remaining {2,3}
        DoubleBuffer to = DoubleBuffer.allocate(3);
        DoubleBuffer ret = to.put(from);
        check(ret == to, "put(XBuffer) returns this");
        check(to.get(0) == 2d && to.get(1) == 3d,
                "put(XBuffer) copies the src remaining region");
        check(to.position() == 2 && from.position() == 3,
                "put(XBuffer) advances both positions");
        checkThrows(IllegalArgumentException.class, () -> to.put(to),
                "put(this) -> IllegalArgumentException");
        DoubleBuffer big = DoubleBuffer.wrap(new double[] {1, 2, 3});
        checkThrows(BufferOverflowException.class, () -> to.put(big),
                "put(XBuffer) with insufficient remaining -> BufferOverflowException");
        DoubleBuffer roTo = DoubleBuffer.allocate(3).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roTo.put(big),
                "put(XBuffer) on read-only -> ReadOnlyBufferException");
    }

    // wrap(array): identity/offset, bidirectional visibility, ranged wrap.
    static void testWrapContract() {
        double[] arr = new double[] {1, 2, 3, 4};
        DoubleBuffer w = DoubleBuffer.wrap(arr);
        checkState(w, 0, 4, 4, "wrap(array)");
        check(w.hasArray(), "heap wrap hasArray");
        check(w.array() == arr, "wrap(array) exposes backing array identity");
        check(w.arrayOffset() == 0, "wrap(array) arrayOffset 0");
        arr[0] = 40d;
        check(w.get(0) == 40d, "array write visible through buffer");
        w.put(1, 50d);
        check(arr[1] == 50d, "buffer absolute put visible in array");
        w.rewind();
        w.put(60d);
        check(arr[0] == 60d, "buffer relative put writes array at position");
        double[] arr2 = new double[] {9, 1, 2, 9};
        DoubleBuffer r = DoubleBuffer.wrap(arr2, 1, 2);
        checkState(r, 1, 3, 4, "wrap(array,1,2)");
        check(r.array() == arr2 && r.arrayOffset() == 0, "ranged wrap same array");
        check(r.get() == 1d && r.get() == 2d, "ranged wrap reads [off,off+len)");
        checkThrows(BufferUnderflowException.class, r::get,
                "ranged wrap exhausted at off+len");
        r.put(0, 11d);
        check(arr2[0] == 11d, "ranged wrap index 0 maps to array[0] (cursor starts at off)");
        DoubleBuffer z = DoubleBuffer.wrap(arr2, 2, 0);
        check(!z.hasRemaining(), "wrap len 0 has nothing remaining");
        DoubleBuffer ro = DoubleBuffer.wrap(arr).asReadOnlyBuffer();
        check(!ro.hasArray(), "read-only wrap hides the array");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "read-only array() -> ReadOnlyBufferException");
    }

    // slice/duplicate/asReadOnlyBuffer: cursor independence + shared content.
    static void testViewBuffers() {
        DoubleBuffer b = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        b.position(1);
        b.limit(3);
        b.mark();
        DoubleBuffer d = b.duplicate();
        checkState(d, 1, 3, 4, "duplicate copies pos/limit/capacity");
        d.reset();
        check(d.position() == 1, "duplicate inherits the mark");
        d.put(20d);                            // writes index 1
        check(b.get(1) == 20d, "duplicate shares content with source");
        d.position(2);
        check(b.position() == 1, "duplicate cursor independent of source");
        DoubleBuffer s = b.slice();
        checkState(s, 0, 2, 2, "slice is a fresh cursor over [pos,limit)");
        check(s.get(0) == 20d && s.get(1) == 3d,
                "slice index 0 maps to source position");
        s.put(1, 30d);
        check(b.get(2) == 30d, "slice writes through to source");
        check(b.position() == 1 && b.limit() == 3,
                "creating a slice leaves the source cursor unchanged");
        s.position(1);
        check(b.position() == 1, "slice cursor independent of source");
        DoubleBuffer ro = b.asReadOnlyBuffer();
        check(ro.isReadOnly() && !b.isReadOnly(), "asReadOnlyBuffer flag, source writable");
        checkState(ro, 1, 3, 4, "asReadOnlyBuffer copies pos/limit/capacity");
        ro.position(2);
        check(b.position() == 1, "read-only cursor independent of source");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1d),
                "read-only relative put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, 1d),
                "read-only absolute put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new double[] {1, 2}, 0, 2),
                "read-only bulk put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(b.duplicate()),
                "read-only put(XBuffer) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "read-only compact -> ReadOnlyBufferException");
        b.put(2, 45d);
        check(ro.get(2) == 45d, "read-only view sees source mutation");
        check(b.duplicate().equals(b), "duplicate equals source (same remaining)");
    }

    // equals/compareTo/hashCode compare the remaining region only.
    static void testEqualsCompareHash() {
        DoubleBuffer a = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        a.position(1);
        a.limit(3);                              // remaining {2,3}
        DoubleBuffer b = DoubleBuffer.wrap(new double[] {9, 2, 3, 9}); // different prefix/suffix
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        check(a.equals(b), "equals compares only the remaining region");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal remaining -> equal hashCode");
        check(a.compareTo(b) == 0, "compareTo equal on shifted regions");
        DoubleBuffer base = DoubleBuffer.wrap(new double[] {1, 2, 3});   // {1,2,3}
        DoubleBuffer bigEl = DoubleBuffer.wrap(new double[] {1, 5, 9}); // {1,5,9}
        check(!base.equals(bigEl), "differing element -> not equal");
        check(base.compareTo(bigEl) < 0, "first difference decides (2 < 5)");
        check(bigEl.compareTo(base) > 0, "compareTo sign antisymmetric");
        check(DoubleBuffer.wrap(new double[] {1, 2, 3}).hashCode() == DoubleBuffer.wrap(new double[] {1, 2, 3}).hashCode(),
                "independent buffers with equal content hash equally");
        DoubleBuffer shorter = DoubleBuffer.wrap(new double[] {1, 2}); // {1,2}
        check(!base.equals(shorter), "different remaining length -> not equal");
        check(base.compareTo(shorter) > 0, "prefix: longer remaining is greater");
        check(shorter.compareTo(base) < 0, "prefix: shorter remaining is less");
        check(!base.equals(null), "equals(null) false");
        check(!base.equals("not a buffer"), "equals(other type) false");
        check(base.equals(base), "equals self");
        check(base.equals(base.asReadOnlyBuffer()), "read-only same content equal");
        check(base.equals(base.duplicate()), "duplicate equal");

        // Float-specific semantics (javadoc, probed identical on JDK-8/JDK-27):
        // NaN equals NaN and compares greater than every other value including
        // +Infinity; -0.0 equals +0.0 (unlike Double.equals); hashCode truncates
        // through the double->int cast, so equal buffers hash equal.
        DoubleBuffer n1 = DoubleBuffer.wrap(new double[] {Double.NaN});
        DoubleBuffer n2 = DoubleBuffer.wrap(new double[] {Double.NaN});
        check(n1.equals(n2), "NaN equals NaN");
        check(n1.compareTo(n2) == 0, "compareTo NaN == NaN");
        check(n1.hashCode() == n2.hashCode(), "NaN buffers hash equal");
        DoubleBuffer inf = DoubleBuffer.wrap(new double[] {Double.POSITIVE_INFINITY});
        check(!n1.equals(inf), "NaN not equal to +Infinity");
        check(n1.compareTo(inf) > 0 && inf.compareTo(n1) < 0, "NaN > +Infinity");
        DoubleBuffer one = DoubleBuffer.wrap(new double[] {1d});
        check(n1.compareTo(one) > 0 && one.compareTo(n1) < 0, "NaN > any finite value");
        DoubleBuffer nz = DoubleBuffer.wrap(new double[] {-0.0d});
        DoubleBuffer pz = DoubleBuffer.wrap(new double[] {0.0d});
        check(nz.equals(pz) && pz.equals(nz), "-0.0 equals +0.0 (unlike Double.equals)");
        check(nz.compareTo(pz) == 0 && pz.compareTo(nz) == 0,
                "compareTo treats -0.0 and +0.0 as equal");
        check(nz.hashCode() == pz.hashCode(), "zero-sign buffers hash equal");
        DoubleBuffer t1 = DoubleBuffer.wrap(new double[] {5d, Double.NaN});
        DoubleBuffer t2 = DoubleBuffer.wrap(new double[] {5d, 9d});
        check(t1.compareTo(t2) > 0 && t2.compareTo(t1) < 0,
                "NaN greatest at any element index");
    }

    // compact: [pos,limit) -> 0, pos=remaining, limit=capacity, mark discarded.
    static void testCompactContract() {
        DoubleBuffer b = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        b.mark();
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        b.compact();
        checkState(b, 2, 4, 4, "compact: pos=remaining, limit=capacity");
        check(b.get(0) == 2d && b.get(1) == 3d,
                "compact copies [position,limit) to the front");
        checkThrows(InvalidMarkException.class, b::reset, "compact discards mark");
        b.put(8d);
        check(b.position() == 3 && b.get(2) == 8d,
                "compacted buffer still has room at the tail");
        DoubleBuffer whole = DoubleBuffer.allocate(3);
        whole.put(1d).put(2d).put(3d);
        whole.flip();                             // nothing consumed: position 0
        whole.compact();
        checkState(whole, 3, 3, 3, "compact with nothing consumed -> pos=limit=capacity");
        check(whole.get(0) == 1d && whole.get(2) == 3d,
                "compact with nothing consumed preserves content");
        DoubleBuffer done = DoubleBuffer.allocate(3);
        done.put(1d).put(2d).put(3d);
        done.flip();
        done.position(3);                         // everything consumed
        done.compact();
        checkState(done, 0, 3, 3, "compact with empty remaining -> pos 0");
        done.put(9d);
        check(done.get(0) == 9d, "empty-compacted buffer writable at index 0");
    }

    // Views over a ByteBuffer: capacity from remaining, order captured at
    // creation, BIG/LITTLE_ENDIAN layouts verified byte-by-byte through the
    // byte layer, write-through in both directions.
    static void testByteBufferView() {
        ByteBuffer head = ByteBuffer.allocate(32);
        head.position(8);
        DoubleBuffer v = head.asDoubleBuffer();
        check(v.capacity() == 3, "view capacity = byteBuffer.remaining()/elementSize");
        checkState(v, 0, 3, 3, "view starts with a fresh cursor");
        check(head.position() == 8, "creating the view leaves the byte position");
        ByteBuffer big = ByteBuffer.allocate(16).order(ByteOrder.BIG_ENDIAN);
        DoubleBuffer vb = big.asDoubleBuffer();
        vb.put(Double.longBitsToDouble(0x0102030405060708L));
        boolean bigOk = true;
        for (int i = 0; i < 8; i++) bigOk &= (big.get(i) & 0xFF) == (i + 1);
        check(bigOk, "BIG_ENDIAN view stores the high-order byte first");
        check(vb.order() == ByteOrder.BIG_ENDIAN, "view inherits BIG_ENDIAN");
        ByteBuffer lit = ByteBuffer.allocate(16).order(ByteOrder.LITTLE_ENDIAN);
        DoubleBuffer vl = lit.asDoubleBuffer();
        vl.put(Double.longBitsToDouble(0x0102030405060708L));
        boolean litOk = true;
        for (int i = 0; i < 8; i++) litOk &= (lit.get(i) & 0xFF) == (8 - i);
        check(litOk, "LITTLE_ENDIAN view stores the low-order byte first");
        check(vl.order() == ByteOrder.LITTLE_ENDIAN, "view inherits LITTLE_ENDIAN");
        vb.put(Double.longBitsToDouble(0x0102030405060708L));
        boolean wbOk = true;
        for (int i = 0; i < 8; i++) wbOk &= (big.get(8 + i) & 0xFF) == (i + 1);
        check(wbOk, "view put writes through to the byte buffer");
        for (int i = 0; i < 8; i++) big.put(8 + i, (byte) (0x10 + i));
        check(vb.get(1) == Double.longBitsToDouble(0x1011121314151617L), "byte buffer mutation visible through the view");
        check(vb.position() == 2, "view cursor tracks its own puts");
        DoubleBuffer ro = big.asReadOnlyBuffer().asDoubleBuffer();
        check(ro.isReadOnly(), "read-only byte buffer yields a read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1d),
                "read-only view rejects put");
    }

    // JDK-11/13 additions: mismatch(XBuffer) (JDK 11); slice(int,int),
    // get(int,arr,off,len), put(int,arr,off,len), put(int,XBuffer,off,len)
    // (JDK 13). The JDK-8 twin test asserts their absence by reflection.
    static void testJdk13PlusApi() {
        DoubleBuffer b = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        DoubleBuffer s = b.slice(1, 2);
        checkState(s, 0, 2, 2, "slice(index,length) fresh cursor");
        check(s.get(0) == 2d && s.get(1) == 3d, "slice(index,length) starts at index");
        s.put(0, 20d);
        check(b.get(1) == 20d, "slice(index,length) shares content");
        check(b.position() == 0, "slice(index,length) leaves source cursor");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(-1, 2),
                "slice(index<0) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(0, -1),
                "slice(length<0) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.slice(3, 2),
                "slice(index+length>capacity) -> IndexOOB");
        check(b.slice(4, 0).capacity() == 0, "slice(capacity,0) empty is legal");
        DoubleBuffer g = DoubleBuffer.wrap(new double[] {1, 2, 3, 4});
        double[] dst = new double[] {0, 0, 0};
        DoubleBuffer ret = g.get(1, dst, 0, 2);
        check(ret == g, "get(index,dst,off,len) returns this");
        check(dst[0] == 2d && dst[1] == 3d && dst[2] == 0d,
                "absolute bulk get copies the window");
        check(g.position() == 0, "absolute bulk get leaves position");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(3, dst, 0, 2),
                "abs bulk get index+len>limit -> IndexOOB (not underflow)");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(0, dst, -1, 2),
                "abs bulk get off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.get(0, dst, 0, 4),
                "abs bulk get off+len>dst.length -> IndexOOB");
        g.put(1, new double[] {1, 2}, 0, 2);
        check(g.get(1) == 1d && g.get(2) == 2d && g.position() == 0,
                "absolute bulk put writes window, position fixed");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(3, new double[] {1, 2}, 0, 2),
                "abs bulk put index+len>limit -> IndexOOB (not overflow)");
        checkThrows(ReadOnlyBufferException.class,
                () -> g.asReadOnlyBuffer().put(0, new double[] {1, 2}, 0, 2),
                "abs bulk put on read-only -> ReadOnlyBufferException");
        DoubleBuffer src = DoubleBuffer.wrap(new double[] {1, 2});
        g.put(0, src, 0, 2);
        check(g.get(0) == 1d && g.get(1) == 2d,
                "put(index,XBuffer,off,len) copies the window");
        check(g.position() == 0 && src.position() == 0,
                "put(index,XBuffer,off,len) fixes both cursors");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(3, src, 0, 2),
                "put(index,src,off,len) target overflow -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> g.put(0, src, 1, 2),
                "put(index,src,off,len) bad src range -> IndexOOB");
        DoubleBuffer m1 = DoubleBuffer.wrap(new double[] {1, 2, 3});
        check(m1.mismatch(DoubleBuffer.wrap(new double[] {1, 2, 3})) == -1, "mismatch equal -> -1");
        check(m1.mismatch(DoubleBuffer.wrap(new double[] {1, 9, 3})) == 1,
                "mismatch returns first differing relative index");
        check(m1.mismatch(DoubleBuffer.wrap(new double[] {1, 2})) == 2,
                "mismatch proper prefix -> shorter remaining");
        check(DoubleBuffer.wrap(new double[] {1, 2}).mismatch(m1) == 2, "mismatch prefix symmetric");
        check(m1.mismatch(DoubleBuffer.wrap(new double[] {5, 2, 3})) == 0, "mismatch at index 0");
        check(DoubleBuffer.allocate(0).mismatch(DoubleBuffer.allocate(0)) == -1,
                "mismatch both empty -> -1");
        check(DoubleBuffer.allocate(0).mismatch(m1) == 0, "mismatch empty vs nonempty -> 0");
        DoubleBuffer sh1 = DoubleBuffer.wrap(new double[] {9, 1, 2});
        sh1.position(1);
        check(sh1.mismatch(DoubleBuffer.wrap(new double[] {1, 2})) == -1, "mismatch ignores positions");
        DoubleBuffer sh2 = DoubleBuffer.wrap(new double[] {1, 9});
        check(sh1.mismatch(sh2) == 1, "mismatch on shifted regions");

        // Float mismatch uses the same equality as equals (probed): NaN matches
        // NaN, -0.0 matches +0.0.
        check(DoubleBuffer.wrap(new double[] {Double.NaN})
                .mismatch(DoubleBuffer.wrap(new double[] {Double.NaN})) == -1,
                "mismatch NaN == NaN");
        check(DoubleBuffer.wrap(new double[] {Double.NaN})
                .mismatch(DoubleBuffer.wrap(new double[] {1d})) == 0,
                "mismatch NaN vs finite");
        check(DoubleBuffer.wrap(new double[] {-0.0d})
                .mismatch(DoubleBuffer.wrap(new double[] {0.0d})) == -1,
                "mismatch treats -0.0/+0.0 equal");
    }
}

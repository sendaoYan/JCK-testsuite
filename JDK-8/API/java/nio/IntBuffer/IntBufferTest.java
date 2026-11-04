/*
 * @test
 * @summary java.nio.IntBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main IntBufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.IntBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

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
        check(hasMethod(c, "duplicate"), "has duplicate()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "compact"), "has compact()");
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
        check(w.hasArray() && w.array() == backing && w.arrayOffset() == 0, "wrap array identity");
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
        check(a.hashCode() == b.hashCode(), "equal hashCodes");
        IntBuffer c3 = IntBuffer.wrap(new int[] {1, 9, 3});
        check(!a.equals(c3), "equals differs");
        check(a.compareTo(c3) < 0, "compareTo lexicographic");
        IntBuffer shorter = IntBuffer.wrap(new int[] {1, 2});
        check(a.compareTo(shorter) > 0, "compareTo longer prefix > shorter");

        IntBuffer ro = IntBuffer.wrap(new int[] {1, 2}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        check(!ro.hasArray(), "read-only hasArray false");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> IntBuffer.allocate(0).get(), "get underflow");
        IntBuffer overflowBuf = IntBuffer.allocate(2);
        overflowBuf.limit(0);
        checkThrows(BufferOverflowException.class, () -> overflowBuf.put(1), "put overflow");
        IntBuffer e = IntBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");
        checkThrows(IllegalArgumentException.class, () -> e.limit(-1), "limit negative");

        IntBuffer zero = IntBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> IntBuffer.allocate(-1), "allocate(-1) IAE");

        IntBuffer mark = IntBuffer.allocate(4);
        mark.put(1).put(2).flip();
        mark.mark();
        check(mark.get() == 1, "get after mark");
        mark.reset();
        check(mark.get() == 1, "reset restores position");

        IntBuffer full = IntBuffer.allocate(3);
        full.put(1).put(2).put(3).flip();
        full.get();
        full.get();
        full.get();
        full.compact();
        check(full.position() == 0 && full.limit() == 3, "compact after full consumption");

        IntBuffer mk = IntBuffer.allocate(4);
        mk.put(1).put(2).put(3).flip();
        mk.mark();
        mk.get();
        mk.rewind();
        check(mk.position() == 0 && mk.limit() == 3, "rewind keeps limit, resets position");

        checkThrows(IndexOutOfBoundsException.class,
                () -> IntBuffer.wrap(new int[] {1, 2}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> IntBuffer.wrap(new int[] {1, 2}, 1, 5), "wrap offset+length overflow IOOBE");

        IntBuffer src = IntBuffer.wrap(new int[] {1, 2, 3});
        IntBuffer dst = IntBuffer.allocate(3);
        dst.put(src);
        dst.flip();
        check(dst.get() == 1 && dst.get() == 2 && dst.get() == 3, "put(IntBuffer) copies");

        checkThrows(BufferUnderflowException.class, () -> IntBuffer.allocate(1).get(new int[2]), "bulk get underflow");
        checkThrows(BufferOverflowException.class, () -> IntBuffer.allocate(1).put(new int[] {1, 2}), "bulk put overflow");
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
        testJdk13PlusApiAbsent();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- Batch A2 contract deepening: state machine / bounds matrices / views /
    // byte order / comparison semantics. Facts below were probed on JDK-8
    // (jdk8u372-b07) and JDK-27 (27.0.1.0.2+1) and behave identically unless noted.

    // Five-value snapshot (position/limit/capacity/remaining/hasRemaining).
    static void checkState(IntBuffer b, int pos, int lim, int cap, String step) {
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
        IntBuffer b = IntBuffer.allocate(4);
        checkState(b, 0, 4, 4, "allocate");
        b.put(1);
        checkState(b, 1, 4, 4, "put#1");
        b.put(2);
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
        check(b.get() == 1, "flip exposes unread prefix first");
        checkState(b, 1, 2, 4, "relative get");
        b.rewind();
        checkState(b, 0, 2, 4, "rewind");
        b.clear();
        checkState(b, 0, 4, 4, "clear");
        b.put(1).put(2).put(3);
        checkState(b, 3, 4, 4, "put x3");
        b.limit(2);                       // limit below position clamps position
        checkState(b, 2, 2, 4, "limit(2)<position clamps position to limit");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position()>limit -> IAE");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IAE");
        IntBuffer c = IntBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> c.limit(4),
                "limit()>capacity -> IAE");
        checkThrows(IllegalArgumentException.class, () -> c.limit(-1),
                "limit(-1) -> IAE");
        c.position(2);
        c.limit(3);
        checkState(c, 2, 3, 3, "position<=limit accepted");
        IntBuffer t = IntBuffer.allocate(3);
        t.put(1);
        t.limit(2);
        check(t.toString().contains("pos=1 lim=2 cap=3"), "toString carries pos/lim/cap");
    }

    // mark lifecycle: which operations discard the mark (probed both JDKs).
    static void testMarkDiscardRules() {
        IntBuffer b = IntBuffer.allocate(4);
        b.position(2);
        b.mark();
        b.position(3);
        b.reset();
        check(b.position() == 2, "reset after forward position returns to mark");
        b.position(1);
        checkThrows(InvalidMarkException.class, b::reset,
                "position moved below mark discards mark");
        IntBuffer l = IntBuffer.allocate(4);
        l.position(2);
        l.mark();
        l.limit(1);
        checkThrows(InvalidMarkException.class, l::reset,
                "limit moved below mark discards mark");
        IntBuffer f = IntBuffer.allocate(4);
        f.position(2);
        f.mark();
        f.flip();
        checkThrows(InvalidMarkException.class, f::reset, "flip discards mark");
        IntBuffer r = IntBuffer.allocate(4);
        r.position(2);
        r.mark();
        r.rewind();
        checkThrows(InvalidMarkException.class, r::reset, "rewind discards mark");
        IntBuffer cl = IntBuffer.allocate(4);
        cl.position(2);
        cl.mark();
        cl.clear();
        checkThrows(InvalidMarkException.class, cl::reset, "clear discards mark");
        IntBuffer k = IntBuffer.allocate(4);
        k.position(2);
        k.mark();
        k.position(3);
        k.reset();
        k.position(3);
        k.reset();
        check(k.position() == 2, "reset keeps the mark (repeatable)");
        checkThrows(InvalidMarkException.class,
                () -> IntBuffer.allocate(2).reset(), "fresh buffer has no mark");
        IntBuffer s = IntBuffer.allocate(4);
        s.position(1);
        s.mark();
        checkThrows(InvalidMarkException.class, () -> s.slice().reset(),
                "slice starts with undefined mark");
    }

    // relative vs absolute access, allocate zero-fill, chainability.
    static void testAccessSemantics() {
        IntBuffer b = IntBuffer.allocate(3);
        check(b.get(0) == 0, "allocate zero-fills element 0");
        check(b.get(1) == 0, "allocate zero-fills element 1");
        check(b.get(2) == 0, "allocate zero-fills element 2");
        check(b.position() == 0, "absolute get leaves position");
        IntBuffer r = b.put(5);
        check(r == b, "relative put returns this");
        check(b.position() == 1, "relative put advances position");
        check(b.get(0) == 5, "relative put stored at the old position");
        b.put(7);
        check(b.get(1) == 7 && b.position() == 2, "second relative put lands at index 1");
        b.get();
        check(b.position() == 3, "relative get advances position");
        b.put(0, 9);
        check(b.position() == 3, "absolute put leaves position");
        check(b.get(0) == 9, "absolute get reads back absolute put");
        IntBuffer ch = IntBuffer.allocate(3);
        check(ch.put(1).put(2).put(3) == ch,
                "relative puts chain, each returning this");
    }

    // Precise exception matrix: relative full/empty, absolute index bounds,
    // allocate(-1), wrap bounds; failed operations leave the cursor untouched.
    static void testBoundsExceptionMatrix() {
        IntBuffer full = IntBuffer.allocate(2);
        full.put(1).put(2);
        checkThrows(BufferOverflowException.class, () -> full.put(3),
                "relative put on full -> BufferOverflowException");
        check(full.position() == 2, "failed relative put leaves position");
        IntBuffer empty = IntBuffer.allocate(2);
        empty.flip();
        checkThrows(BufferUnderflowException.class, empty::get,
                "relative get on empty -> BufferUnderflowException");
        check(empty.position() == 0, "failed relative get leaves position");
        IntBuffer b = IntBuffer.allocate(3);
        b.limit(2);
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(2),
                "absolute get(limit) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(3),
                "absolute get(capacity) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(-1),
                "absolute get(-1) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(2, 9),
                "absolute put(limit,v) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(-1, 9),
                "absolute put(-1,v) -> IndexOOB");
        check(b.get(1) == 0, "absolute get inside limit succeeds");
        checkThrows(IllegalArgumentException.class, () -> IntBuffer.allocate(-1),
                "allocate(-1) -> IAE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> IntBuffer.wrap(new int[] {1, 2, 3, 4}, -1, 2), "wrap off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> IntBuffer.wrap(new int[] {1, 2, 3, 4}, 0, -1), "wrap len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> IntBuffer.wrap(new int[] {1, 2, 3, 4}, 3, 2), "wrap off+len>length -> IndexOOB");
        checkState(IntBuffer.wrap(new int[] {1, 2, 3, 4}, 1, 2), 1, 3, 4, "wrap(arr,1,2) legal boundary");
        checkState(IntBuffer.wrap(new int[] {1, 2, 3, 4}, 4, 0), 4, 4, 4, "wrap(arr,len=0) at tail legal");
    }

    // Bulk get/put parameter matrix (bounds error outranks over/underflow,
    // probed on both JDKs) plus put(XBuffer) semantics.
    static void testBulkTransferMatrix() {
        int[] src = new int[] {1, 2, 3, 4};
        IntBuffer g = IntBuffer.allocate(2);
        g.put(9).put(9);
        g.flip();
        int[] dst = new int[] {0, 0, 0};
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
                () -> g.get(new int[] {0, 0, 0}),
                "get(array) full-array -> BufferUnderflowException");
        int[] out = new int[] {0, 0};
        g.get(out, 0, 2);
        check(out[0] == 9 && out[1] == 9, "bulk get copies exactly the window");
        check(g.position() == 2, "bulk get advances position");
        IntBuffer p = IntBuffer.wrap(new int[] {1, 2, 3, 4});
        p.position(1);
        int[] win = new int[] {7, 7, 7, 7};
        p.get(win, 1, 2);
        check(win[0] == 7 && win[1] == 2 && win[2] == 3 && win[3] == 7,
                "bulk get writes only dst[off,off+len)");
        IntBuffer q = IntBuffer.allocate(2);
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
        check(q.get(0) == 2 && q.get(1) == 3, "bulk put copies src[off,off+len)");
        IntBuffer from = IntBuffer.wrap(new int[] {1, 2, 3});
        from.position(1);                       // remaining {2,3}
        IntBuffer to = IntBuffer.allocate(3);
        IntBuffer ret = to.put(from);
        check(ret == to, "put(XBuffer) returns this");
        check(to.get(0) == 2 && to.get(1) == 3,
                "put(XBuffer) copies the src remaining region");
        check(to.position() == 2 && from.position() == 3,
                "put(XBuffer) advances both positions");
        checkThrows(IllegalArgumentException.class, () -> to.put(to),
                "put(this) -> IllegalArgumentException");
        IntBuffer big = IntBuffer.wrap(new int[] {1, 2, 3});
        checkThrows(BufferOverflowException.class, () -> to.put(big),
                "put(XBuffer) with insufficient remaining -> BufferOverflowException");
        IntBuffer roTo = IntBuffer.allocate(3).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roTo.put(big),
                "put(XBuffer) on read-only -> ReadOnlyBufferException");
    }

    // wrap(array): identity/offset, bidirectional visibility, ranged wrap.
    static void testWrapContract() {
        int[] arr = new int[] {1, 2, 3, 4};
        IntBuffer w = IntBuffer.wrap(arr);
        checkState(w, 0, 4, 4, "wrap(array)");
        check(w.hasArray(), "heap wrap hasArray");
        check(w.array() == arr, "wrap(array) exposes backing array identity");
        check(w.arrayOffset() == 0, "wrap(array) arrayOffset 0");
        arr[0] = 40;
        check(w.get(0) == 40, "array write visible through buffer");
        w.put(1, 50);
        check(arr[1] == 50, "buffer absolute put visible in array");
        w.rewind();
        w.put(60);
        check(arr[0] == 60, "buffer relative put writes array at position");
        int[] arr2 = new int[] {9, 1, 2, 9};
        IntBuffer r = IntBuffer.wrap(arr2, 1, 2);
        checkState(r, 1, 3, 4, "wrap(array,1,2)");
        check(r.array() == arr2 && r.arrayOffset() == 0, "ranged wrap same array");
        check(r.get() == 1 && r.get() == 2, "ranged wrap reads [off,off+len)");
        checkThrows(BufferUnderflowException.class, r::get,
                "ranged wrap exhausted at off+len");
        r.put(0, 11);
        check(arr2[0] == 11, "ranged wrap index 0 maps to array[0] (cursor starts at off)");
        IntBuffer z = IntBuffer.wrap(arr2, 2, 0);
        check(!z.hasRemaining(), "wrap len 0 has nothing remaining");
        IntBuffer ro = IntBuffer.wrap(arr).asReadOnlyBuffer();
        check(!ro.hasArray(), "read-only wrap hides the array");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "read-only array() -> ReadOnlyBufferException");
    }

    // slice/duplicate/asReadOnlyBuffer: cursor independence + shared content.
    static void testViewBuffers() {
        IntBuffer b = IntBuffer.wrap(new int[] {1, 2, 3, 4});
        b.position(1);
        b.limit(3);
        b.mark();
        IntBuffer d = b.duplicate();
        checkState(d, 1, 3, 4, "duplicate copies pos/limit/capacity");
        d.reset();
        check(d.position() == 1, "duplicate inherits the mark");
        d.put(20);                            // writes index 1
        check(b.get(1) == 20, "duplicate shares content with source");
        d.position(2);
        check(b.position() == 1, "duplicate cursor independent of source");
        IntBuffer s = b.slice();
        checkState(s, 0, 2, 2, "slice is a fresh cursor over [pos,limit)");
        check(s.get(0) == 20 && s.get(1) == 3,
                "slice index 0 maps to source position");
        s.put(1, 30);
        check(b.get(2) == 30, "slice writes through to source");
        check(b.position() == 1 && b.limit() == 3,
                "creating a slice leaves the source cursor unchanged");
        s.position(1);
        check(b.position() == 1, "slice cursor independent of source");
        IntBuffer ro = b.asReadOnlyBuffer();
        check(ro.isReadOnly() && !b.isReadOnly(), "asReadOnlyBuffer flag, source writable");
        checkState(ro, 1, 3, 4, "asReadOnlyBuffer copies pos/limit/capacity");
        ro.position(2);
        check(b.position() == 1, "read-only cursor independent of source");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1),
                "read-only relative put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, 1),
                "read-only absolute put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new int[] {1, 2}, 0, 2),
                "read-only bulk put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(b.duplicate()),
                "read-only put(XBuffer) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "read-only compact -> ReadOnlyBufferException");
        b.put(2, 45);
        check(ro.get(2) == 45, "read-only view sees source mutation");
        check(b.duplicate().equals(b), "duplicate equals source (same remaining)");
    }

    // equals/compareTo/hashCode compare the remaining region only.
    static void testEqualsCompareHash() {
        IntBuffer a = IntBuffer.wrap(new int[] {1, 2, 3, 4});
        a.position(1);
        a.limit(3);                              // remaining {2,3}
        IntBuffer b = IntBuffer.wrap(new int[] {9, 2, 3, 9}); // different prefix/suffix
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        check(a.equals(b), "equals compares only the remaining region");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal remaining -> equal hashCode");
        check(a.compareTo(b) == 0, "compareTo equal on shifted regions");
        IntBuffer base = IntBuffer.wrap(new int[] {1, 2, 3});   // {1,2,3}
        IntBuffer bigEl = IntBuffer.wrap(new int[] {1, 5, 9}); // {1,5,9}
        check(!base.equals(bigEl), "differing element -> not equal");
        check(base.compareTo(bigEl) < 0, "first difference decides (2 < 5)");
        check(bigEl.compareTo(base) > 0, "compareTo sign antisymmetric");
        check(IntBuffer.wrap(new int[] {1, 2, 3}).hashCode() == IntBuffer.wrap(new int[] {1, 2, 3}).hashCode(),
                "independent buffers with equal content hash equally");
        IntBuffer shorter = IntBuffer.wrap(new int[] {1, 2}); // {1,2}
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
        IntBuffer b = IntBuffer.wrap(new int[] {1, 2, 3, 4});
        b.mark();
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        b.compact();
        checkState(b, 2, 4, 4, "compact: pos=remaining, limit=capacity");
        check(b.get(0) == 2 && b.get(1) == 3,
                "compact copies [position,limit) to the front");
        checkThrows(InvalidMarkException.class, b::reset, "compact discards mark");
        b.put(8);
        check(b.position() == 3 && b.get(2) == 8,
                "compacted buffer still has room at the tail");
        IntBuffer whole = IntBuffer.allocate(3);
        whole.put(1).put(2).put(3);
        whole.flip();                             // nothing consumed: position 0
        whole.compact();
        checkState(whole, 3, 3, 3, "compact with nothing consumed -> pos=limit=capacity");
        check(whole.get(0) == 1 && whole.get(2) == 3,
                "compact with nothing consumed preserves content");
        IntBuffer done = IntBuffer.allocate(3);
        done.put(1).put(2).put(3);
        done.flip();
        done.position(3);                         // everything consumed
        done.compact();
        checkState(done, 0, 3, 3, "compact with empty remaining -> pos 0");
        done.put(9);
        check(done.get(0) == 9, "empty-compacted buffer writable at index 0");
    }

    // Views over a ByteBuffer: capacity from remaining, order captured at
    // creation, BIG/LITTLE_ENDIAN layouts verified byte-by-byte through the
    // byte layer, write-through in both directions.
    static void testByteBufferView() {
        ByteBuffer head = ByteBuffer.allocate(16);
        head.position(4);
        IntBuffer v = head.asIntBuffer();
        check(v.capacity() == 3, "view capacity = byteBuffer.remaining()/elementSize");
        checkState(v, 0, 3, 3, "view starts with a fresh cursor");
        check(head.position() == 4, "creating the view leaves the byte position");
        ByteBuffer big = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        IntBuffer vb = big.asIntBuffer();
        vb.put(0x01020304);
        boolean bigOk = true;
        for (int i = 0; i < 4; i++) bigOk &= (big.get(i) & 0xFF) == (i + 1);
        check(bigOk, "BIG_ENDIAN view stores the high-order byte first");
        check(vb.order() == ByteOrder.BIG_ENDIAN, "view inherits BIG_ENDIAN");
        ByteBuffer lit = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        IntBuffer vl = lit.asIntBuffer();
        vl.put(0x01020304);
        boolean litOk = true;
        for (int i = 0; i < 4; i++) litOk &= (lit.get(i) & 0xFF) == (4 - i);
        check(litOk, "LITTLE_ENDIAN view stores the low-order byte first");
        check(vl.order() == ByteOrder.LITTLE_ENDIAN, "view inherits LITTLE_ENDIAN");
        vb.put(0x01020304);
        boolean wbOk = true;
        for (int i = 0; i < 4; i++) wbOk &= (big.get(4 + i) & 0xFF) == (i + 1);
        check(wbOk, "view put writes through to the byte buffer");
        for (int i = 0; i < 4; i++) big.put(4 + i, (byte) (0x10 + i));
        check(vb.get(1) == 0x10111213, "byte buffer mutation visible through the view");
        check(vb.position() == 2, "view cursor tracks its own puts");
        IntBuffer ro = big.asReadOnlyBuffer().asIntBuffer();
        check(ro.isReadOnly(), "read-only byte buffer yields a read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1),
                "read-only view rejects put");
    }

    // JDK-11/13 APIs absent from JDK-8 (verified by reflection; the JDK-27 twin
    // exercises them): mismatch(XBuffer) was added in JDK 11; slice(int,int),
    // get(int,arr,off,len), get(int,arr), put(int,arr,off,len), put(int,arr)
    // and put(int,XBuffer,off,len) were added in JDK 13.
    static void checkAbsent(String name, Class<?>... params) {
        try {
            IntBuffer.class.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            passed++;
            return;
        }
        throw new AssertionError("unexpected method " + name
                + " on JDK-8 " + IntBuffer.class.getSimpleName());
    }

    static void testJdk13PlusApiAbsent() {
        checkAbsent("slice", int.class, int.class);
        checkAbsent("get", int.class, int[].class, int.class, int.class);
        checkAbsent("get", int.class, int[].class);
        checkAbsent("put", int.class, int[].class, int.class, int.class);
        checkAbsent("put", int.class, int[].class);
        checkAbsent("put", int.class, IntBuffer.class, int.class, int.class);
        checkAbsent("mismatch", IntBuffer.class);
    }
}

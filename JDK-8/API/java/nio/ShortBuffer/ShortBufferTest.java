/*
 * @test
 * @summary java.nio.ShortBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main ShortBufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ReadOnlyBufferException;
import java.nio.ShortBuffer;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

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
        check(hasMethod(c, "duplicate"), "has duplicate()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "compact"), "has compact()");
    }

    static void testBlackBox() {
        ShortBuffer b = ShortBuffer.allocate(3);
        b.put((short) 10).put((short) 20).put((short) 30);
        check(b.position() == 3, "relative put advances");
        b.flip();
        check(b.get() == 10 && b.get() == 20 && b.get() == 30, "relative get");
        check(!b.hasRemaining(), "consumed");
        b.rewind();
        check(b.position() == 0 && b.limit() == 3, "rewind");

        short[] backing = {1, 2, 3, 4};
        ShortBuffer w = ShortBuffer.wrap(backing);
        check(w.hasArray() && w.array() == backing && w.arrayOffset() == 0, "wrap array identity");
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
        check(a.hashCode() == b2.hashCode(), "equal hashCodes");
        ShortBuffer c3 = ShortBuffer.wrap(new short[] {1, 9, 3});
        check(!a.equals(c3) && a.compareTo(c3) < 0, "compareTo lexicographic");
        ShortBuffer shorter = ShortBuffer.wrap(new short[] {1, 2});
        check(a.compareTo(shorter) > 0, "compareTo longer prefix > shorter");

        ShortBuffer ro = ShortBuffer.wrap(new short[] {1}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        check(!ro.hasArray(), "read-only hasArray false");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((short) 1), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> ShortBuffer.allocate(0).get(), "get underflow");
        ShortBuffer overflowBuf = ShortBuffer.allocate(2);
        overflowBuf.limit(0);
        checkThrows(BufferOverflowException.class, () -> overflowBuf.put((short) 1), "put overflow");
        ShortBuffer e = ShortBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, (short) 1), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");

        ShortBuffer zero = ShortBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> ShortBuffer.allocate(-1), "allocate(-1) IAE");

        ShortBuffer mark = ShortBuffer.allocate(4);
        mark.put((short) 1).put((short) 2).flip();
        mark.mark();
        check(mark.get() == 1, "get after mark");
        mark.reset();
        check(mark.get() == 1, "reset restores position");

        checkThrows(IndexOutOfBoundsException.class,
                () -> ShortBuffer.wrap(new short[] {1, 2}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ShortBuffer.wrap(new short[] {1, 2}, 1, 5), "wrap offset+length overflow IOOBE");
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
    static void checkState(ShortBuffer b, int pos, int lim, int cap, String step) {
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
        ShortBuffer b = ShortBuffer.allocate(4);
        checkState(b, 0, 4, 4, "allocate");
        b.put((short) 1);
        checkState(b, 1, 4, 4, "put#1");
        b.put((short) 2);
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
        check(b.get() == (short) 1, "flip exposes unread prefix first");
        checkState(b, 1, 2, 4, "relative get");
        b.rewind();
        checkState(b, 0, 2, 4, "rewind");
        b.clear();
        checkState(b, 0, 4, 4, "clear");
        b.put((short) 1).put((short) 2).put((short) 3);
        checkState(b, 3, 4, 4, "put x3");
        b.limit(2);                       // limit below position clamps position
        checkState(b, 2, 2, 4, "limit(2)<position clamps position to limit");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position()>limit -> IAE");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IAE");
        ShortBuffer c = ShortBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> c.limit(4),
                "limit()>capacity -> IAE");
        checkThrows(IllegalArgumentException.class, () -> c.limit(-1),
                "limit(-1) -> IAE");
        c.position(2);
        c.limit(3);
        checkState(c, 2, 3, 3, "position<=limit accepted");
        ShortBuffer t = ShortBuffer.allocate(3);
        t.put((short) 1);
        t.limit(2);
        check(t.toString().contains("pos=1 lim=2 cap=3"), "toString carries pos/lim/cap");
    }

    // mark lifecycle: which operations discard the mark (probed both JDKs).
    static void testMarkDiscardRules() {
        ShortBuffer b = ShortBuffer.allocate(4);
        b.position(2);
        b.mark();
        b.position(3);
        b.reset();
        check(b.position() == 2, "reset after forward position returns to mark");
        b.position(1);
        checkThrows(InvalidMarkException.class, b::reset,
                "position moved below mark discards mark");
        ShortBuffer l = ShortBuffer.allocate(4);
        l.position(2);
        l.mark();
        l.limit(1);
        checkThrows(InvalidMarkException.class, l::reset,
                "limit moved below mark discards mark");
        ShortBuffer f = ShortBuffer.allocate(4);
        f.position(2);
        f.mark();
        f.flip();
        checkThrows(InvalidMarkException.class, f::reset, "flip discards mark");
        ShortBuffer r = ShortBuffer.allocate(4);
        r.position(2);
        r.mark();
        r.rewind();
        checkThrows(InvalidMarkException.class, r::reset, "rewind discards mark");
        ShortBuffer cl = ShortBuffer.allocate(4);
        cl.position(2);
        cl.mark();
        cl.clear();
        checkThrows(InvalidMarkException.class, cl::reset, "clear discards mark");
        ShortBuffer k = ShortBuffer.allocate(4);
        k.position(2);
        k.mark();
        k.position(3);
        k.reset();
        k.position(3);
        k.reset();
        check(k.position() == 2, "reset keeps the mark (repeatable)");
        checkThrows(InvalidMarkException.class,
                () -> ShortBuffer.allocate(2).reset(), "fresh buffer has no mark");
        ShortBuffer s = ShortBuffer.allocate(4);
        s.position(1);
        s.mark();
        checkThrows(InvalidMarkException.class, () -> s.slice().reset(),
                "slice starts with undefined mark");
    }

    // relative vs absolute access, allocate zero-fill, chainability.
    static void testAccessSemantics() {
        ShortBuffer b = ShortBuffer.allocate(3);
        check(b.get(0) == (short) 0, "allocate zero-fills element 0");
        check(b.get(1) == (short) 0, "allocate zero-fills element 1");
        check(b.get(2) == (short) 0, "allocate zero-fills element 2");
        check(b.position() == 0, "absolute get leaves position");
        ShortBuffer r = b.put((short) 5);
        check(r == b, "relative put returns this");
        check(b.position() == 1, "relative put advances position");
        check(b.get(0) == (short) 5, "relative put stored at the old position");
        b.put((short) 7);
        check(b.get(1) == (short) 7 && b.position() == 2, "second relative put lands at index 1");
        b.get();
        check(b.position() == 3, "relative get advances position");
        b.put(0, (short) 9);
        check(b.position() == 3, "absolute put leaves position");
        check(b.get(0) == (short) 9, "absolute get reads back absolute put");
        ShortBuffer ch = ShortBuffer.allocate(3);
        check(ch.put((short) 1).put((short) 2).put((short) 3) == ch,
                "relative puts chain, each returning this");
    }

    // Precise exception matrix: relative full/empty, absolute index bounds,
    // allocate(-1), wrap bounds; failed operations leave the cursor untouched.
    static void testBoundsExceptionMatrix() {
        ShortBuffer full = ShortBuffer.allocate(2);
        full.put((short) 1).put((short) 2);
        checkThrows(BufferOverflowException.class, () -> full.put((short) 3),
                "relative put on full -> BufferOverflowException");
        check(full.position() == 2, "failed relative put leaves position");
        ShortBuffer empty = ShortBuffer.allocate(2);
        empty.flip();
        checkThrows(BufferUnderflowException.class, empty::get,
                "relative get on empty -> BufferUnderflowException");
        check(empty.position() == 0, "failed relative get leaves position");
        ShortBuffer b = ShortBuffer.allocate(3);
        b.limit(2);
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(2),
                "absolute get(limit) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(3),
                "absolute get(capacity) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(-1),
                "absolute get(-1) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(2, (short) 9),
                "absolute put(limit,v) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(-1, (short) 9),
                "absolute put(-1,v) -> IndexOOB");
        check(b.get(1) == (short) 0, "absolute get inside limit succeeds");
        checkThrows(IllegalArgumentException.class, () -> ShortBuffer.allocate(-1),
                "allocate(-1) -> IAE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ShortBuffer.wrap(new short[] {1, 2, 3, 4}, -1, 2), "wrap off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ShortBuffer.wrap(new short[] {1, 2, 3, 4}, 0, -1), "wrap len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> ShortBuffer.wrap(new short[] {1, 2, 3, 4}, 3, 2), "wrap off+len>length -> IndexOOB");
        checkState(ShortBuffer.wrap(new short[] {1, 2, 3, 4}, 1, 2), 1, 3, 4, "wrap(arr,1,2) legal boundary");
        checkState(ShortBuffer.wrap(new short[] {1, 2, 3, 4}, 4, 0), 4, 4, 4, "wrap(arr,len=0) at tail legal");
    }

    // Bulk get/put parameter matrix (bounds error outranks over/underflow,
    // probed on both JDKs) plus put(XBuffer) semantics.
    static void testBulkTransferMatrix() {
        short[] src = new short[] {1, 2, 3, 4};
        ShortBuffer g = ShortBuffer.allocate(2);
        g.put((short) 9).put((short) 9);
        g.flip();
        short[] dst = new short[] {0, 0, 0};
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
                () -> g.get(new short[] {(short) 0, (short) 0, (short) 0}),
                "get(array) full-array -> BufferUnderflowException");
        short[] out = new short[] {0, 0};
        g.get(out, 0, 2);
        check(out[0] == (short) 9 && out[1] == (short) 9, "bulk get copies exactly the window");
        check(g.position() == 2, "bulk get advances position");
        ShortBuffer p = ShortBuffer.wrap(new short[] {1, 2, 3, 4});
        p.position(1);
        short[] win = new short[] {7, 7, 7, 7};
        p.get(win, 1, 2);
        check(win[0] == (short) 7 && win[1] == (short) 2 && win[2] == (short) 3 && win[3] == (short) 7,
                "bulk get writes only dst[off,off+len)");
        ShortBuffer q = ShortBuffer.allocate(2);
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
        check(q.get(0) == (short) 2 && q.get(1) == (short) 3, "bulk put copies src[off,off+len)");
        ShortBuffer from = ShortBuffer.wrap(new short[] {1, 2, 3});
        from.position(1);                       // remaining {2,3}
        ShortBuffer to = ShortBuffer.allocate(3);
        ShortBuffer ret = to.put(from);
        check(ret == to, "put(XBuffer) returns this");
        check(to.get(0) == (short) 2 && to.get(1) == (short) 3,
                "put(XBuffer) copies the src remaining region");
        check(to.position() == 2 && from.position() == 3,
                "put(XBuffer) advances both positions");
        checkThrows(IllegalArgumentException.class, () -> to.put(to),
                "put(this) -> IllegalArgumentException");
        ShortBuffer big = ShortBuffer.wrap(new short[] {1, 2, 3});
        checkThrows(BufferOverflowException.class, () -> to.put(big),
                "put(XBuffer) with insufficient remaining -> BufferOverflowException");
        ShortBuffer roTo = ShortBuffer.allocate(3).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roTo.put(big),
                "put(XBuffer) on read-only -> ReadOnlyBufferException");
    }

    // wrap(array): identity/offset, bidirectional visibility, ranged wrap.
    static void testWrapContract() {
        short[] arr = new short[] {1, 2, 3, 4};
        ShortBuffer w = ShortBuffer.wrap(arr);
        checkState(w, 0, 4, 4, "wrap(array)");
        check(w.hasArray(), "heap wrap hasArray");
        check(w.array() == arr, "wrap(array) exposes backing array identity");
        check(w.arrayOffset() == 0, "wrap(array) arrayOffset 0");
        arr[0] = (short) 40;
        check(w.get(0) == (short) 40, "array write visible through buffer");
        w.put(1, (short) 50);
        check(arr[1] == (short) 50, "buffer absolute put visible in array");
        w.rewind();
        w.put((short) 60);
        check(arr[0] == (short) 60, "buffer relative put writes array at position");
        short[] arr2 = new short[] {9, 1, 2, 9};
        ShortBuffer r = ShortBuffer.wrap(arr2, 1, 2);
        checkState(r, 1, 3, 4, "wrap(array,1,2)");
        check(r.array() == arr2 && r.arrayOffset() == 0, "ranged wrap same array");
        check(r.get() == (short) 1 && r.get() == (short) 2, "ranged wrap reads [off,off+len)");
        checkThrows(BufferUnderflowException.class, r::get,
                "ranged wrap exhausted at off+len");
        r.put(0, (short) 11);
        check(arr2[0] == (short) 11, "ranged wrap index 0 maps to array[0] (cursor starts at off)");
        ShortBuffer z = ShortBuffer.wrap(arr2, 2, 0);
        check(!z.hasRemaining(), "wrap len 0 has nothing remaining");
        ShortBuffer ro = ShortBuffer.wrap(arr).asReadOnlyBuffer();
        check(!ro.hasArray(), "read-only wrap hides the array");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "read-only array() -> ReadOnlyBufferException");
    }

    // slice/duplicate/asReadOnlyBuffer: cursor independence + shared content.
    static void testViewBuffers() {
        ShortBuffer b = ShortBuffer.wrap(new short[] {1, 2, 3, 4});
        b.position(1);
        b.limit(3);
        b.mark();
        ShortBuffer d = b.duplicate();
        checkState(d, 1, 3, 4, "duplicate copies pos/limit/capacity");
        d.reset();
        check(d.position() == 1, "duplicate inherits the mark");
        d.put((short) 20);                            // writes index 1
        check(b.get(1) == (short) 20, "duplicate shares content with source");
        d.position(2);
        check(b.position() == 1, "duplicate cursor independent of source");
        ShortBuffer s = b.slice();
        checkState(s, 0, 2, 2, "slice is a fresh cursor over [pos,limit)");
        check(s.get(0) == (short) 20 && s.get(1) == (short) 3,
                "slice index 0 maps to source position");
        s.put(1, (short) 30);
        check(b.get(2) == (short) 30, "slice writes through to source");
        check(b.position() == 1 && b.limit() == 3,
                "creating a slice leaves the source cursor unchanged");
        s.position(1);
        check(b.position() == 1, "slice cursor independent of source");
        ShortBuffer ro = b.asReadOnlyBuffer();
        check(ro.isReadOnly() && !b.isReadOnly(), "asReadOnlyBuffer flag, source writable");
        checkState(ro, 1, 3, 4, "asReadOnlyBuffer copies pos/limit/capacity");
        ro.position(2);
        check(b.position() == 1, "read-only cursor independent of source");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((short) 1),
                "read-only relative put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, (short) 1),
                "read-only absolute put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new short[] {1, 2}, 0, 2),
                "read-only bulk put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(b.duplicate()),
                "read-only put(XBuffer) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "read-only compact -> ReadOnlyBufferException");
        b.put(2, (short) 45);
        check(ro.get(2) == (short) 45, "read-only view sees source mutation");
        check(b.duplicate().equals(b), "duplicate equals source (same remaining)");
    }

    // equals/compareTo/hashCode compare the remaining region only.
    static void testEqualsCompareHash() {
        ShortBuffer a = ShortBuffer.wrap(new short[] {1, 2, 3, 4});
        a.position(1);
        a.limit(3);                              // remaining {2,3}
        ShortBuffer b = ShortBuffer.wrap(new short[] {9, 2, 3, 9}); // different prefix/suffix
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        check(a.equals(b), "equals compares only the remaining region");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal remaining -> equal hashCode");
        check(a.compareTo(b) == 0, "compareTo equal on shifted regions");
        ShortBuffer base = ShortBuffer.wrap(new short[] {1, 2, 3});   // {1,2,3}
        ShortBuffer bigEl = ShortBuffer.wrap(new short[] {1, 5, 9}); // {1,5,9}
        check(!base.equals(bigEl), "differing element -> not equal");
        check(base.compareTo(bigEl) < 0, "first difference decides (2 < 5)");
        check(bigEl.compareTo(base) > 0, "compareTo sign antisymmetric");
        check(ShortBuffer.wrap(new short[] {1, 2, 3}).hashCode() == ShortBuffer.wrap(new short[] {1, 2, 3}).hashCode(),
                "independent buffers with equal content hash equally");
        ShortBuffer shorter = ShortBuffer.wrap(new short[] {1, 2}); // {1,2}
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
        ShortBuffer b = ShortBuffer.wrap(new short[] {1, 2, 3, 4});
        b.mark();
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        b.compact();
        checkState(b, 2, 4, 4, "compact: pos=remaining, limit=capacity");
        check(b.get(0) == (short) 2 && b.get(1) == (short) 3,
                "compact copies [position,limit) to the front");
        checkThrows(InvalidMarkException.class, b::reset, "compact discards mark");
        b.put((short) 8);
        check(b.position() == 3 && b.get(2) == (short) 8,
                "compacted buffer still has room at the tail");
        ShortBuffer whole = ShortBuffer.allocate(3);
        whole.put((short) 1).put((short) 2).put((short) 3);
        whole.flip();                             // nothing consumed: position 0
        whole.compact();
        checkState(whole, 3, 3, 3, "compact with nothing consumed -> pos=limit=capacity");
        check(whole.get(0) == (short) 1 && whole.get(2) == (short) 3,
                "compact with nothing consumed preserves content");
        ShortBuffer done = ShortBuffer.allocate(3);
        done.put((short) 1).put((short) 2).put((short) 3);
        done.flip();
        done.position(3);                         // everything consumed
        done.compact();
        checkState(done, 0, 3, 3, "compact with empty remaining -> pos 0");
        done.put((short) 9);
        check(done.get(0) == (short) 9, "empty-compacted buffer writable at index 0");
    }

    // Views over a ByteBuffer: capacity from remaining, order captured at
    // creation, BIG/LITTLE_ENDIAN layouts verified byte-by-byte through the
    // byte layer, write-through in both directions.
    static void testByteBufferView() {
        ByteBuffer head = ByteBuffer.allocate(8);
        head.position(2);
        ShortBuffer v = head.asShortBuffer();
        check(v.capacity() == 3, "view capacity = byteBuffer.remaining()/elementSize");
        checkState(v, 0, 3, 3, "view starts with a fresh cursor");
        check(head.position() == 2, "creating the view leaves the byte position");
        ByteBuffer big = ByteBuffer.allocate(4).order(ByteOrder.BIG_ENDIAN);
        ShortBuffer vb = big.asShortBuffer();
        vb.put((short) 0x0102);
        boolean bigOk = true;
        for (int i = 0; i < 2; i++) bigOk &= (big.get(i) & 0xFF) == (i + 1);
        check(bigOk, "BIG_ENDIAN view stores the high-order byte first");
        check(vb.order() == ByteOrder.BIG_ENDIAN, "view inherits BIG_ENDIAN");
        ByteBuffer lit = ByteBuffer.allocate(4).order(ByteOrder.LITTLE_ENDIAN);
        ShortBuffer vl = lit.asShortBuffer();
        vl.put((short) 0x0102);
        boolean litOk = true;
        for (int i = 0; i < 2; i++) litOk &= (lit.get(i) & 0xFF) == (2 - i);
        check(litOk, "LITTLE_ENDIAN view stores the low-order byte first");
        check(vl.order() == ByteOrder.LITTLE_ENDIAN, "view inherits LITTLE_ENDIAN");
        vb.put((short) 0x0102);
        boolean wbOk = true;
        for (int i = 0; i < 2; i++) wbOk &= (big.get(2 + i) & 0xFF) == (i + 1);
        check(wbOk, "view put writes through to the byte buffer");
        for (int i = 0; i < 2; i++) big.put(2 + i, (byte) (0x10 + i));
        check(vb.get(1) == (short) 0x1011, "byte buffer mutation visible through the view");
        check(vb.position() == 2, "view cursor tracks its own puts");
        ShortBuffer ro = big.asReadOnlyBuffer().asShortBuffer();
        check(ro.isReadOnly(), "read-only byte buffer yields a read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((short) 1),
                "read-only view rejects put");
    }

    // JDK-11/13 APIs absent from JDK-8 (verified by reflection; the JDK-27 twin
    // exercises them): mismatch(XBuffer) was added in JDK 11; slice(int,int),
    // get(int,arr,off,len), get(int,arr), put(int,arr,off,len), put(int,arr)
    // and put(int,XBuffer,off,len) were added in JDK 13.
    static void checkAbsent(String name, Class<?>... params) {
        try {
            ShortBuffer.class.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            passed++;
            return;
        }
        throw new AssertionError("unexpected method " + name
                + " on JDK-8 " + ShortBuffer.class.getSimpleName());
    }

    static void testJdk13PlusApiAbsent() {
        checkAbsent("slice", int.class, int.class);
        checkAbsent("get", int.class, short[].class, int.class, int.class);
        checkAbsent("get", int.class, short[].class);
        checkAbsent("put", int.class, short[].class, int.class, int.class);
        checkAbsent("put", int.class, short[].class);
        checkAbsent("put", int.class, ShortBuffer.class, int.class, int.class);
        checkAbsent("mismatch", ShortBuffer.class);
    }
}

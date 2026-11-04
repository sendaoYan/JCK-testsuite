/*
 * @test
 * @summary java.nio.FloatBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main FloatBufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.FloatBuffer;
import java.nio.ReadOnlyBufferException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.InvalidMarkException;

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
        check(hasMethod(c, "duplicate"), "has duplicate()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "compact"), "has compact()");
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
        check(w.hasArray() && w.array() == backing && w.arrayOffset() == 0, "wrap array identity");
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
        check(a.hashCode() == b2.hashCode(), "equal hashCodes");
        FloatBuffer c3 = FloatBuffer.wrap(new float[] {1f, 9f, 3f});
        check(!a.equals(c3) && a.compareTo(c3) < 0, "compareTo lexicographic");

        // Float.compare semantics: -0.0f equals 0.0f; NaN sorts last.
        FloatBuffer nz = FloatBuffer.wrap(new float[] {-0.0f});
        FloatBuffer pz = FloatBuffer.wrap(new float[] {0.0f});
        check(nz.equals(pz), "-0.0f equals 0.0f");
        check(nz.compareTo(pz) == 0, "compareTo -0.0f == 0.0f");
        FloatBuffer nan = FloatBuffer.wrap(new float[] {Float.NaN});
        FloatBuffer one = FloatBuffer.wrap(new float[] {1f});
        check(nan.compareTo(one) > 0, "NaN compares greater than 1");

        FloatBuffer ro = FloatBuffer.wrap(new float[] {1f}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        check(!ro.hasArray(), "read-only hasArray false");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1f), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> FloatBuffer.allocate(0).get(), "get underflow");
        FloatBuffer overflowBuf = FloatBuffer.allocate(2);
        overflowBuf.limit(0);
        checkThrows(BufferOverflowException.class, () -> overflowBuf.put(1f), "put overflow");
        FloatBuffer e = FloatBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1f), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");

        FloatBuffer zero = FloatBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> FloatBuffer.allocate(-1), "allocate(-1) IAE");

        FloatBuffer mark = FloatBuffer.allocate(4);
        mark.put(1f).put(2f).flip();
        mark.mark();
        check(mark.get() == 1f, "get after mark");
        mark.reset();
        check(mark.get() == 1f, "reset restores position");

        checkThrows(IndexOutOfBoundsException.class,
                () -> FloatBuffer.wrap(new float[] {1f, 2f}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> FloatBuffer.wrap(new float[] {1f, 2f}, 1, 5), "wrap offset+length overflow IOOBE");
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
    static void checkState(FloatBuffer b, int pos, int lim, int cap, String step) {
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
        FloatBuffer b = FloatBuffer.allocate(4);
        checkState(b, 0, 4, 4, "allocate");
        b.put(1f);
        checkState(b, 1, 4, 4, "put#1");
        b.put(2f);
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
        check(b.get() == 1f, "flip exposes unread prefix first");
        checkState(b, 1, 2, 4, "relative get");
        b.rewind();
        checkState(b, 0, 2, 4, "rewind");
        b.clear();
        checkState(b, 0, 4, 4, "clear");
        b.put(1f).put(2f).put(3f);
        checkState(b, 3, 4, 4, "put x3");
        b.limit(2);                       // limit below position clamps position
        checkState(b, 2, 2, 4, "limit(2)<position clamps position to limit");
        checkThrows(IllegalArgumentException.class, () -> b.position(3),
                "position()>limit -> IAE");
        checkThrows(IllegalArgumentException.class, () -> b.position(-1),
                "position(-1) -> IAE");
        FloatBuffer c = FloatBuffer.allocate(3);
        checkThrows(IllegalArgumentException.class, () -> c.limit(4),
                "limit()>capacity -> IAE");
        checkThrows(IllegalArgumentException.class, () -> c.limit(-1),
                "limit(-1) -> IAE");
        c.position(2);
        c.limit(3);
        checkState(c, 2, 3, 3, "position<=limit accepted");
        FloatBuffer t = FloatBuffer.allocate(3);
        t.put(1f);
        t.limit(2);
        check(t.toString().contains("pos=1 lim=2 cap=3"), "toString carries pos/lim/cap");
    }

    // mark lifecycle: which operations discard the mark (probed both JDKs).
    static void testMarkDiscardRules() {
        FloatBuffer b = FloatBuffer.allocate(4);
        b.position(2);
        b.mark();
        b.position(3);
        b.reset();
        check(b.position() == 2, "reset after forward position returns to mark");
        b.position(1);
        checkThrows(InvalidMarkException.class, b::reset,
                "position moved below mark discards mark");
        FloatBuffer l = FloatBuffer.allocate(4);
        l.position(2);
        l.mark();
        l.limit(1);
        checkThrows(InvalidMarkException.class, l::reset,
                "limit moved below mark discards mark");
        FloatBuffer f = FloatBuffer.allocate(4);
        f.position(2);
        f.mark();
        f.flip();
        checkThrows(InvalidMarkException.class, f::reset, "flip discards mark");
        FloatBuffer r = FloatBuffer.allocate(4);
        r.position(2);
        r.mark();
        r.rewind();
        checkThrows(InvalidMarkException.class, r::reset, "rewind discards mark");
        FloatBuffer cl = FloatBuffer.allocate(4);
        cl.position(2);
        cl.mark();
        cl.clear();
        checkThrows(InvalidMarkException.class, cl::reset, "clear discards mark");
        FloatBuffer k = FloatBuffer.allocate(4);
        k.position(2);
        k.mark();
        k.position(3);
        k.reset();
        k.position(3);
        k.reset();
        check(k.position() == 2, "reset keeps the mark (repeatable)");
        checkThrows(InvalidMarkException.class,
                () -> FloatBuffer.allocate(2).reset(), "fresh buffer has no mark");
        FloatBuffer s = FloatBuffer.allocate(4);
        s.position(1);
        s.mark();
        checkThrows(InvalidMarkException.class, () -> s.slice().reset(),
                "slice starts with undefined mark");
    }

    // relative vs absolute access, allocate zero-fill, chainability.
    static void testAccessSemantics() {
        FloatBuffer b = FloatBuffer.allocate(3);
        check(b.get(0) == 0f, "allocate zero-fills element 0");
        check(b.get(1) == 0f, "allocate zero-fills element 1");
        check(b.get(2) == 0f, "allocate zero-fills element 2");
        check(b.position() == 0, "absolute get leaves position");
        FloatBuffer r = b.put(5f);
        check(r == b, "relative put returns this");
        check(b.position() == 1, "relative put advances position");
        check(b.get(0) == 5f, "relative put stored at the old position");
        b.put(7f);
        check(b.get(1) == 7f && b.position() == 2, "second relative put lands at index 1");
        b.get();
        check(b.position() == 3, "relative get advances position");
        b.put(0, 9f);
        check(b.position() == 3, "absolute put leaves position");
        check(b.get(0) == 9f, "absolute get reads back absolute put");
        FloatBuffer ch = FloatBuffer.allocate(3);
        check(ch.put(1f).put(2f).put(3f) == ch,
                "relative puts chain, each returning this");
    }

    // Precise exception matrix: relative full/empty, absolute index bounds,
    // allocate(-1), wrap bounds; failed operations leave the cursor untouched.
    static void testBoundsExceptionMatrix() {
        FloatBuffer full = FloatBuffer.allocate(2);
        full.put(1f).put(2f);
        checkThrows(BufferOverflowException.class, () -> full.put(3f),
                "relative put on full -> BufferOverflowException");
        check(full.position() == 2, "failed relative put leaves position");
        FloatBuffer empty = FloatBuffer.allocate(2);
        empty.flip();
        checkThrows(BufferUnderflowException.class, empty::get,
                "relative get on empty -> BufferUnderflowException");
        check(empty.position() == 0, "failed relative get leaves position");
        FloatBuffer b = FloatBuffer.allocate(3);
        b.limit(2);
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(2),
                "absolute get(limit) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(3),
                "absolute get(capacity) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.get(-1),
                "absolute get(-1) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(2, 9f),
                "absolute put(limit,v) -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class, () -> b.put(-1, 9f),
                "absolute put(-1,v) -> IndexOOB");
        check(b.get(1) == 0f, "absolute get inside limit succeeds");
        checkThrows(IllegalArgumentException.class, () -> FloatBuffer.allocate(-1),
                "allocate(-1) -> IAE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> FloatBuffer.wrap(new float[] {1, 2, 3, 4}, -1, 2), "wrap off<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> FloatBuffer.wrap(new float[] {1, 2, 3, 4}, 0, -1), "wrap len<0 -> IndexOOB");
        checkThrows(IndexOutOfBoundsException.class,
                () -> FloatBuffer.wrap(new float[] {1, 2, 3, 4}, 3, 2), "wrap off+len>length -> IndexOOB");
        checkState(FloatBuffer.wrap(new float[] {1, 2, 3, 4}, 1, 2), 1, 3, 4, "wrap(arr,1,2) legal boundary");
        checkState(FloatBuffer.wrap(new float[] {1, 2, 3, 4}, 4, 0), 4, 4, 4, "wrap(arr,len=0) at tail legal");
    }

    // Bulk get/put parameter matrix (bounds error outranks over/underflow,
    // probed on both JDKs) plus put(XBuffer) semantics.
    static void testBulkTransferMatrix() {
        float[] src = new float[] {1, 2, 3, 4};
        FloatBuffer g = FloatBuffer.allocate(2);
        g.put(9f).put(9f);
        g.flip();
        float[] dst = new float[] {0, 0, 0};
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
                () -> g.get(new float[] {0f, 0f, 0f}),
                "get(array) full-array -> BufferUnderflowException");
        float[] out = new float[] {0, 0};
        g.get(out, 0, 2);
        check(out[0] == 9f && out[1] == 9f, "bulk get copies exactly the window");
        check(g.position() == 2, "bulk get advances position");
        FloatBuffer p = FloatBuffer.wrap(new float[] {1, 2, 3, 4});
        p.position(1);
        float[] win = new float[] {7, 7, 7, 7};
        p.get(win, 1, 2);
        check(win[0] == 7f && win[1] == 2f && win[2] == 3f && win[3] == 7f,
                "bulk get writes only dst[off,off+len)");
        FloatBuffer q = FloatBuffer.allocate(2);
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
        check(q.get(0) == 2f && q.get(1) == 3f, "bulk put copies src[off,off+len)");
        FloatBuffer from = FloatBuffer.wrap(new float[] {1, 2, 3});
        from.position(1);                       // remaining {2,3}
        FloatBuffer to = FloatBuffer.allocate(3);
        FloatBuffer ret = to.put(from);
        check(ret == to, "put(XBuffer) returns this");
        check(to.get(0) == 2f && to.get(1) == 3f,
                "put(XBuffer) copies the src remaining region");
        check(to.position() == 2 && from.position() == 3,
                "put(XBuffer) advances both positions");
        checkThrows(IllegalArgumentException.class, () -> to.put(to),
                "put(this) -> IllegalArgumentException");
        FloatBuffer big = FloatBuffer.wrap(new float[] {1, 2, 3});
        checkThrows(BufferOverflowException.class, () -> to.put(big),
                "put(XBuffer) with insufficient remaining -> BufferOverflowException");
        FloatBuffer roTo = FloatBuffer.allocate(3).asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roTo.put(big),
                "put(XBuffer) on read-only -> ReadOnlyBufferException");
    }

    // wrap(array): identity/offset, bidirectional visibility, ranged wrap.
    static void testWrapContract() {
        float[] arr = new float[] {1, 2, 3, 4};
        FloatBuffer w = FloatBuffer.wrap(arr);
        checkState(w, 0, 4, 4, "wrap(array)");
        check(w.hasArray(), "heap wrap hasArray");
        check(w.array() == arr, "wrap(array) exposes backing array identity");
        check(w.arrayOffset() == 0, "wrap(array) arrayOffset 0");
        arr[0] = 40f;
        check(w.get(0) == 40f, "array write visible through buffer");
        w.put(1, 50f);
        check(arr[1] == 50f, "buffer absolute put visible in array");
        w.rewind();
        w.put(60f);
        check(arr[0] == 60f, "buffer relative put writes array at position");
        float[] arr2 = new float[] {9, 1, 2, 9};
        FloatBuffer r = FloatBuffer.wrap(arr2, 1, 2);
        checkState(r, 1, 3, 4, "wrap(array,1,2)");
        check(r.array() == arr2 && r.arrayOffset() == 0, "ranged wrap same array");
        check(r.get() == 1f && r.get() == 2f, "ranged wrap reads [off,off+len)");
        checkThrows(BufferUnderflowException.class, r::get,
                "ranged wrap exhausted at off+len");
        r.put(0, 11f);
        check(arr2[0] == 11f, "ranged wrap index 0 maps to array[0] (cursor starts at off)");
        FloatBuffer z = FloatBuffer.wrap(arr2, 2, 0);
        check(!z.hasRemaining(), "wrap len 0 has nothing remaining");
        FloatBuffer ro = FloatBuffer.wrap(arr).asReadOnlyBuffer();
        check(!ro.hasArray(), "read-only wrap hides the array");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(),
                "read-only array() -> ReadOnlyBufferException");
    }

    // slice/duplicate/asReadOnlyBuffer: cursor independence + shared content.
    static void testViewBuffers() {
        FloatBuffer b = FloatBuffer.wrap(new float[] {1, 2, 3, 4});
        b.position(1);
        b.limit(3);
        b.mark();
        FloatBuffer d = b.duplicate();
        checkState(d, 1, 3, 4, "duplicate copies pos/limit/capacity");
        d.reset();
        check(d.position() == 1, "duplicate inherits the mark");
        d.put(20f);                            // writes index 1
        check(b.get(1) == 20f, "duplicate shares content with source");
        d.position(2);
        check(b.position() == 1, "duplicate cursor independent of source");
        FloatBuffer s = b.slice();
        checkState(s, 0, 2, 2, "slice is a fresh cursor over [pos,limit)");
        check(s.get(0) == 20f && s.get(1) == 3f,
                "slice index 0 maps to source position");
        s.put(1, 30f);
        check(b.get(2) == 30f, "slice writes through to source");
        check(b.position() == 1 && b.limit() == 3,
                "creating a slice leaves the source cursor unchanged");
        s.position(1);
        check(b.position() == 1, "slice cursor independent of source");
        FloatBuffer ro = b.asReadOnlyBuffer();
        check(ro.isReadOnly() && !b.isReadOnly(), "asReadOnlyBuffer flag, source writable");
        checkState(ro, 1, 3, 4, "asReadOnlyBuffer copies pos/limit/capacity");
        ro.position(2);
        check(b.position() == 1, "read-only cursor independent of source");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1f),
                "read-only relative put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, 1f),
                "read-only absolute put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new float[] {1, 2}, 0, 2),
                "read-only bulk put -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(b.duplicate()),
                "read-only put(XBuffer) -> ReadOnlyBufferException");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(),
                "read-only compact -> ReadOnlyBufferException");
        b.put(2, 45f);
        check(ro.get(2) == 45f, "read-only view sees source mutation");
        check(b.duplicate().equals(b), "duplicate equals source (same remaining)");
    }

    // equals/compareTo/hashCode compare the remaining region only.
    static void testEqualsCompareHash() {
        FloatBuffer a = FloatBuffer.wrap(new float[] {1, 2, 3, 4});
        a.position(1);
        a.limit(3);                              // remaining {2,3}
        FloatBuffer b = FloatBuffer.wrap(new float[] {9, 2, 3, 9}); // different prefix/suffix
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        check(a.equals(b), "equals compares only the remaining region");
        check(b.equals(a), "equals symmetric");
        check(a.hashCode() == b.hashCode(), "equal remaining -> equal hashCode");
        check(a.compareTo(b) == 0, "compareTo equal on shifted regions");
        FloatBuffer base = FloatBuffer.wrap(new float[] {1, 2, 3});   // {1,2,3}
        FloatBuffer bigEl = FloatBuffer.wrap(new float[] {1, 5, 9}); // {1,5,9}
        check(!base.equals(bigEl), "differing element -> not equal");
        check(base.compareTo(bigEl) < 0, "first difference decides (2 < 5)");
        check(bigEl.compareTo(base) > 0, "compareTo sign antisymmetric");
        check(FloatBuffer.wrap(new float[] {1, 2, 3}).hashCode() == FloatBuffer.wrap(new float[] {1, 2, 3}).hashCode(),
                "independent buffers with equal content hash equally");
        FloatBuffer shorter = FloatBuffer.wrap(new float[] {1, 2}); // {1,2}
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
        // +Infinity; -0.0 equals +0.0 (unlike Float.equals); hashCode truncates
        // through the float->int cast, so equal buffers hash equal.
        FloatBuffer n1 = FloatBuffer.wrap(new float[] {Float.NaN});
        FloatBuffer n2 = FloatBuffer.wrap(new float[] {Float.NaN});
        check(n1.equals(n2), "NaN equals NaN");
        check(n1.compareTo(n2) == 0, "compareTo NaN == NaN");
        check(n1.hashCode() == n2.hashCode(), "NaN buffers hash equal");
        FloatBuffer inf = FloatBuffer.wrap(new float[] {Float.POSITIVE_INFINITY});
        check(!n1.equals(inf), "NaN not equal to +Infinity");
        check(n1.compareTo(inf) > 0 && inf.compareTo(n1) < 0, "NaN > +Infinity");
        FloatBuffer one = FloatBuffer.wrap(new float[] {1f});
        check(n1.compareTo(one) > 0 && one.compareTo(n1) < 0, "NaN > any finite value");
        FloatBuffer nz = FloatBuffer.wrap(new float[] {-0.0f});
        FloatBuffer pz = FloatBuffer.wrap(new float[] {0.0f});
        check(nz.equals(pz) && pz.equals(nz), "-0.0 equals +0.0 (unlike Float.equals)");
        check(nz.compareTo(pz) == 0 && pz.compareTo(nz) == 0,
                "compareTo treats -0.0 and +0.0 as equal");
        check(nz.hashCode() == pz.hashCode(), "zero-sign buffers hash equal");
        FloatBuffer t1 = FloatBuffer.wrap(new float[] {5f, Float.NaN});
        FloatBuffer t2 = FloatBuffer.wrap(new float[] {5f, 9f});
        check(t1.compareTo(t2) > 0 && t2.compareTo(t1) < 0,
                "NaN greatest at any element index");
    }

    // compact: [pos,limit) -> 0, pos=remaining, limit=capacity, mark discarded.
    static void testCompactContract() {
        FloatBuffer b = FloatBuffer.wrap(new float[] {1, 2, 3, 4});
        b.mark();
        b.position(1);
        b.limit(3);                              // remaining {2,3}
        b.compact();
        checkState(b, 2, 4, 4, "compact: pos=remaining, limit=capacity");
        check(b.get(0) == 2f && b.get(1) == 3f,
                "compact copies [position,limit) to the front");
        checkThrows(InvalidMarkException.class, b::reset, "compact discards mark");
        b.put(8f);
        check(b.position() == 3 && b.get(2) == 8f,
                "compacted buffer still has room at the tail");
        FloatBuffer whole = FloatBuffer.allocate(3);
        whole.put(1f).put(2f).put(3f);
        whole.flip();                             // nothing consumed: position 0
        whole.compact();
        checkState(whole, 3, 3, 3, "compact with nothing consumed -> pos=limit=capacity");
        check(whole.get(0) == 1f && whole.get(2) == 3f,
                "compact with nothing consumed preserves content");
        FloatBuffer done = FloatBuffer.allocate(3);
        done.put(1f).put(2f).put(3f);
        done.flip();
        done.position(3);                         // everything consumed
        done.compact();
        checkState(done, 0, 3, 3, "compact with empty remaining -> pos 0");
        done.put(9f);
        check(done.get(0) == 9f, "empty-compacted buffer writable at index 0");
    }

    // Views over a ByteBuffer: capacity from remaining, order captured at
    // creation, BIG/LITTLE_ENDIAN layouts verified byte-by-byte through the
    // byte layer, write-through in both directions.
    static void testByteBufferView() {
        ByteBuffer head = ByteBuffer.allocate(16);
        head.position(4);
        FloatBuffer v = head.asFloatBuffer();
        check(v.capacity() == 3, "view capacity = byteBuffer.remaining()/elementSize");
        checkState(v, 0, 3, 3, "view starts with a fresh cursor");
        check(head.position() == 4, "creating the view leaves the byte position");
        ByteBuffer big = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        FloatBuffer vb = big.asFloatBuffer();
        vb.put(Float.intBitsToFloat(0x01020304));
        boolean bigOk = true;
        for (int i = 0; i < 4; i++) bigOk &= (big.get(i) & 0xFF) == (i + 1);
        check(bigOk, "BIG_ENDIAN view stores the high-order byte first");
        check(vb.order() == ByteOrder.BIG_ENDIAN, "view inherits BIG_ENDIAN");
        ByteBuffer lit = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
        FloatBuffer vl = lit.asFloatBuffer();
        vl.put(Float.intBitsToFloat(0x01020304));
        boolean litOk = true;
        for (int i = 0; i < 4; i++) litOk &= (lit.get(i) & 0xFF) == (4 - i);
        check(litOk, "LITTLE_ENDIAN view stores the low-order byte first");
        check(vl.order() == ByteOrder.LITTLE_ENDIAN, "view inherits LITTLE_ENDIAN");
        vb.put(Float.intBitsToFloat(0x01020304));
        boolean wbOk = true;
        for (int i = 0; i < 4; i++) wbOk &= (big.get(4 + i) & 0xFF) == (i + 1);
        check(wbOk, "view put writes through to the byte buffer");
        for (int i = 0; i < 4; i++) big.put(4 + i, (byte) (0x10 + i));
        check(vb.get(1) == Float.intBitsToFloat(0x10111213), "byte buffer mutation visible through the view");
        check(vb.position() == 2, "view cursor tracks its own puts");
        FloatBuffer ro = big.asReadOnlyBuffer().asFloatBuffer();
        check(ro.isReadOnly(), "read-only byte buffer yields a read-only view");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1f),
                "read-only view rejects put");
    }

    // JDK-11/13 APIs absent from JDK-8 (verified by reflection; the JDK-27 twin
    // exercises them): mismatch(XBuffer) was added in JDK 11; slice(int,int),
    // get(int,arr,off,len), get(int,arr), put(int,arr,off,len), put(int,arr)
    // and put(int,XBuffer,off,len) were added in JDK 13.
    static void checkAbsent(String name, Class<?>... params) {
        try {
            FloatBuffer.class.getMethod(name, params);
        } catch (NoSuchMethodException e) {
            passed++;
            return;
        }
        throw new AssertionError("unexpected method " + name
                + " on JDK-8 " + FloatBuffer.class.getSimpleName());
    }

    static void testJdk13PlusApiAbsent() {
        checkAbsent("slice", int.class, int.class);
        checkAbsent("get", int.class, float[].class, int.class, int.class);
        checkAbsent("get", int.class, float[].class);
        checkAbsent("put", int.class, float[].class, int.class, int.class);
        checkAbsent("put", int.class, float[].class);
        checkAbsent("put", int.class, FloatBuffer.class, int.class, int.class);
        checkAbsent("mismatch", FloatBuffer.class);
    }
}

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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

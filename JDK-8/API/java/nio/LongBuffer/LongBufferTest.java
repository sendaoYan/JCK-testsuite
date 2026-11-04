/*
 * @test
 * @summary java.nio.LongBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main LongBufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.LongBuffer;
import java.nio.ReadOnlyBufferException;

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
        check(hasMethod(c, "duplicate"), "has duplicate()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "compact"), "has compact()");
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
        check(w.hasArray() && w.array() == backing && w.arrayOffset() == 0, "wrap array identity");
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
        check(a.hashCode() == b2.hashCode(), "equal hashCodes");
        LongBuffer c3 = LongBuffer.wrap(new long[] {1L, 9L, 3L});
        check(!a.equals(c3) && a.compareTo(c3) < 0, "compareTo lexicographic");
        LongBuffer shorter = LongBuffer.wrap(new long[] {1L, 2L});
        check(a.compareTo(shorter) > 0, "compareTo longer prefix > shorter");

        LongBuffer ro = LongBuffer.wrap(new long[] {1L}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        check(!ro.hasArray(), "read-only hasArray false");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1L), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> LongBuffer.allocate(0).get(), "get underflow");
        LongBuffer overflowBuf = LongBuffer.allocate(2);
        overflowBuf.limit(0);
        checkThrows(BufferOverflowException.class, () -> overflowBuf.put(1L), "put overflow");
        LongBuffer e = LongBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1L), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");

        LongBuffer zero = LongBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> LongBuffer.allocate(-1), "allocate(-1) IAE");

        LongBuffer mark = LongBuffer.allocate(4);
        mark.put(1L).put(2L).flip();
        mark.mark();
        check(mark.get() == 1L, "get after mark");
        mark.reset();
        check(mark.get() == 1L, "reset restores position");

        checkThrows(IndexOutOfBoundsException.class,
                () -> LongBuffer.wrap(new long[] {1L, 2L}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> LongBuffer.wrap(new long[] {1L, 2L}, 1, 5), "wrap offset+length overflow IOOBE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

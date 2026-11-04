/*
 * @test
 * @summary java.nio.DoubleBuffer conformance: white-box structure (abstract, extends Buffer,
 *          implements Comparable) plus black-box allocate/wrap, relative/absolute get/put,
 *          bulk ops, views, equality and bounds exceptions.
 * @run main DoubleBufferTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.DoubleBuffer;
import java.nio.ReadOnlyBufferException;

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
        check(hasMethod(c, "duplicate"), "has duplicate()");
        check(hasMethod(c, "slice"), "has slice()");
        check(hasMethod(c, "compact"), "has compact()");
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
        check(w.hasArray() && w.array() == backing && w.arrayOffset() == 0, "wrap array identity");
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
        check(a.hashCode() == b2.hashCode(), "equal hashCodes");
        DoubleBuffer c3 = DoubleBuffer.wrap(new double[] {1, 9, 3});
        check(!a.equals(c3) && a.compareTo(c3) < 0, "compareTo lexicographic");

        // Double.compare semantics: -0.0 equals 0.0; NaN sorts last.
        DoubleBuffer nz = DoubleBuffer.wrap(new double[] {-0.0});
        DoubleBuffer pz = DoubleBuffer.wrap(new double[] {0.0});
        check(nz.equals(pz), "-0.0 equals 0.0");
        check(nz.compareTo(pz) == 0, "compareTo -0.0 == 0.0");
        DoubleBuffer nan = DoubleBuffer.wrap(new double[] {Double.NaN});
        DoubleBuffer one = DoubleBuffer.wrap(new double[] {1.0});
        check(nan.compareTo(one) > 0, "NaN compares greater than 1");

        DoubleBuffer ro = DoubleBuffer.wrap(new double[] {1}).asReadOnlyBuffer();
        check(ro.isReadOnly(), "read-only");
        check(!ro.hasArray(), "read-only hasArray false");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(1.0), "read-only put");

        checkThrows(BufferUnderflowException.class, () -> DoubleBuffer.allocate(0).get(), "get underflow");
        DoubleBuffer overflowBuf = DoubleBuffer.allocate(2);
        overflowBuf.limit(0);
        checkThrows(BufferOverflowException.class, () -> overflowBuf.put(1.0), "put overflow");
        DoubleBuffer e = DoubleBuffer.allocate(2);
        checkThrows(IndexOutOfBoundsException.class, () -> e.get(5), "absolute get OOB");
        checkThrows(IndexOutOfBoundsException.class, () -> e.put(-1, 1.0), "absolute put OOB");
        checkThrows(IllegalArgumentException.class, () -> e.position(3), "position > capacity");

        DoubleBuffer zero = DoubleBuffer.allocate(0);
        check(zero.capacity() == 0 && zero.remaining() == 0 && !zero.hasRemaining(), "allocate(0) empty");
        checkThrows(IllegalArgumentException.class, () -> DoubleBuffer.allocate(-1), "allocate(-1) IAE");

        DoubleBuffer mark = DoubleBuffer.allocate(4);
        mark.put(1).put(2).flip();
        mark.mark();
        check(mark.get() == 1, "get after mark");
        mark.reset();
        check(mark.get() == 1, "reset restores position");

        checkThrows(IndexOutOfBoundsException.class,
                () -> DoubleBuffer.wrap(new double[] {1, 2}, -1, 2), "wrap negative offset IOOBE");
        checkThrows(IndexOutOfBoundsException.class,
                () -> DoubleBuffer.wrap(new double[] {1, 2}, 1, 5), "wrap offset+length overflow IOOBE");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

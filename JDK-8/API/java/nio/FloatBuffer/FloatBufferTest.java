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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

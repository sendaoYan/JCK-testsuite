/*
 * @test
 * @summary java.nio.ReadOnlyBufferException conformance: white-box (extends
 *          UnsupportedOperationException, no-arg constructor) plus black-box (put/array/
 *          compact on read-only buffers).
 * @run main ReadOnlyBufferExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.ReadOnlyBufferException;

public class ReadOnlyBufferExceptionTest {
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

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.ReadOnlyBufferException");
        check(c.getSuperclass() == java.lang.UnsupportedOperationException.class,
              "extends UnsupportedOperationException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        ReadOnlyBufferException e = new ReadOnlyBufferException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        check(ro.isReadOnly(), "isReadOnly");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put((byte) 1), "read-only put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(0, (byte) 1), "read-only absolute put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.put(new byte[] {1, 2}), "read-only bulk put");
        checkThrows(ReadOnlyBufferException.class, () -> ro.compact(), "read-only compact");
        checkThrows(ReadOnlyBufferException.class, () -> ro.array(), "read-only array()");

        // Cursor state is still mutable on a read-only buffer.
        ro.position(2).limit(3).mark();
        check(ro.position() == 2 && ro.limit() == 3, "cursor mutable on read-only buffer");

        // Read-only CharBuffer also rejects writes.
        CharBuffer roc = CharBuffer.wrap("abc").asReadOnlyBuffer();
        checkThrows(ReadOnlyBufferException.class, () -> roc.append('x'), "read-only append");

        // Getting from a read-only buffer works and shares storage with the source.
        ByteBuffer src = ByteBuffer.allocate(4).put((byte) 9);
        ByteBuffer roView = src.asReadOnlyBuffer();
        check(roView.get(0) == 9, "read-only view reads");
        src.put(0, (byte) 5);
        check(roView.get(0) == 5, "read-only view sees source mutation");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

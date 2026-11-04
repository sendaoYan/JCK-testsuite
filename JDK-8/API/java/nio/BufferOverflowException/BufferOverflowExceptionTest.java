/*
 * @test
 * @summary java.nio.BufferOverflowException conformance: white-box (extends
 *          RuntimeException, no-arg constructor) plus black-box (thrown on relative and
 *          bulk put past the limit; no data transferred on failure).
 * @run main BufferOverflowExceptionTest
 */

import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class BufferOverflowExceptionTest {
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
        Class<?> c = Class.forName("java.nio.BufferOverflowException");
        check(c.getSuperclass() == java.lang.RuntimeException.class,
              "extends RuntimeException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        BufferOverflowException e = new BufferOverflowException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        checkThrows(BufferOverflowException.class, () -> bb.put((byte) 2), "put past capacity");

        // Overflow on a zero-capacity buffer.
        checkThrows(BufferOverflowException.class,
                () -> ByteBuffer.allocate(0).put((byte) 1), "put on zero-capacity buffer");

        // Overflow when limit is reduced below position.
        ByteBuffer limited = ByteBuffer.allocate(4).put((byte) 1).put((byte) 2);
        limited.limit(2);
        checkThrows(BufferOverflowException.class, () -> limited.put((byte) 3),
                "put after limit reduction");

        // Bulk put past remaining.
        ByteBuffer bulk = ByteBuffer.allocate(2);
        checkThrows(BufferOverflowException.class,
                () -> bulk.put(new byte[] {1, 2, 3}), "bulk put past remaining");

        // Failed put must not advance position.
        ByteBuffer pos = ByteBuffer.allocate(1).put((byte) 1);
        check(pos.position() == 1, "position before failed put");
        try { pos.put((byte) 2); } catch (BufferOverflowException ignored) {}
        check(pos.position() == 1, "failed put leaves position unchanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

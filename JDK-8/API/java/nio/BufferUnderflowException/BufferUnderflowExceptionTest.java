/*
 * @test
 * @summary java.nio.BufferUnderflowException conformance: white-box (extends
 *          RuntimeException, no-arg constructor) plus black-box (thrown on relative and
 *          bulk get past the limit; no data transferred on failure).
 * @run main BufferUnderflowExceptionTest
 */

import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class BufferUnderflowExceptionTest {
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
        Class<?> c = Class.forName("java.nio.BufferUnderflowException");
        check(c.getSuperclass() == java.lang.RuntimeException.class,
              "extends RuntimeException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        BufferUnderflowException e = new BufferUnderflowException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        bb.flip();
        check(bb.get() == 1, "read the single byte");
        checkThrows(BufferUnderflowException.class, bb::get, "get past limit");

        checkThrows(BufferUnderflowException.class,
                () -> ByteBuffer.allocate(0).get(), "get on zero-capacity buffer");

        ByteBuffer bulk = ByteBuffer.allocate(2).put((byte) 1).put((byte) 2);
        bulk.flip();
        checkThrows(BufferUnderflowException.class,
                () -> bulk.get(new byte[3]), "bulk get past remaining");

        // Failed get must not advance position.
        ByteBuffer pos = ByteBuffer.allocate(1).put((byte) 7);
        pos.flip();
        check(pos.position() == 0, "position before failed get");
        try { pos.get(new byte[2]); } catch (BufferUnderflowException ignored) {}
        check(pos.position() == 0, "failed get leaves position unchanged");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

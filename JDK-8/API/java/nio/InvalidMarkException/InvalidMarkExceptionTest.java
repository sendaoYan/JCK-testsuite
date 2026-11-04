/*
 * @test
 * @summary java.nio.InvalidMarkException conformance: white-box (extends
 *          IllegalStateException, no-arg constructor) plus black-box (thrown on reset
 *          without a mark, and after mark discarded by position/limit/clear/flip/rewind).
 * @run main InvalidMarkExceptionTest
 */

import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class InvalidMarkExceptionTest {
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
        Class<?> c = Class.forName("java.nio.InvalidMarkException");
        check(c.getSuperclass() == java.lang.IllegalStateException.class,
              "extends IllegalStateException");
        check(c.getConstructor() != null, "has no-arg constructor");
        check(java.io.Serializable.class.isAssignableFrom(c), "is Serializable");
        InvalidMarkException e = new InvalidMarkException();
        check(e.getMessage() == null, "default message is null");
    }

    static void testBlackBox() {
        ByteBuffer buf = ByteBuffer.allocate(4);
        checkThrows(InvalidMarkException.class, () -> buf.reset(), "reset without mark");

        buf.position(3).mark();
        buf.position(1);
        checkThrows(InvalidMarkException.class, () -> buf.reset(),
                "mark discarded when position set below mark");

        ByteBuffer b2 = ByteBuffer.allocate(4);
        b2.position(3).mark();
        b2.limit(2);
        checkThrows(InvalidMarkException.class, () -> b2.reset(),
                "mark discarded when limit set below mark");

        ByteBuffer b3 = ByteBuffer.allocate(4);
        b3.position(3).mark();
        b3.flip();
        checkThrows(InvalidMarkException.class, () -> b3.reset(), "flip discards mark");

        ByteBuffer b4 = ByteBuffer.allocate(4);
        b4.position(2).mark();
        b4.rewind();
        checkThrows(InvalidMarkException.class, () -> b4.reset(), "rewind discards mark");

        ByteBuffer b5 = ByteBuffer.allocate(4);
        b5.position(2).mark();
        b5.clear();
        checkThrows(InvalidMarkException.class, () -> b5.reset(), "clear discards mark");

        // reset() does not discard the mark; it may be reset again.
        ByteBuffer b6 = ByteBuffer.allocate(4);
        b6.position(2).mark();
        b6.reset();
        b6.reset();
        check(b6.position() == 2, "reset is repeatable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

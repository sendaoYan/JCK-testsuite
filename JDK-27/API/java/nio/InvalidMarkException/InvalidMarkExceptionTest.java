/*
 * @test
 * @summary java.nio.InvalidMarkException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main InvalidMarkExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.InvalidMarkException;

public class InvalidMarkExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void expect(Class<? extends Throwable> ex, Runnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (ex.isInstance(t)) { passed++; return; }
            throw new AssertionError(msg + ": wrong exception " + t, t);
        }
        throw new AssertionError(msg + ": no exception thrown");
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.nio.InvalidMarkException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == IllegalStateException.class, "superclass is IllegalStateException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        InvalidMarkException e = new InvalidMarkException();
        check(e instanceof IllegalStateException, "is IllegalStateException");
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer buf = ByteBuffer.allocate(4);
        expect(InvalidMarkException.class, () -> buf.reset(), "reset without a mark throws");

        // mark then reset works, then clearing (discards mark) and resetting throws again.
        buf.mark();
        buf.position(2);
        buf.reset();
        check(buf.position() == 0, "reset returns to marked position");
        buf.clear();
        expect(InvalidMarkException.class, () -> buf.reset(), "reset after clear (mark discarded) throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

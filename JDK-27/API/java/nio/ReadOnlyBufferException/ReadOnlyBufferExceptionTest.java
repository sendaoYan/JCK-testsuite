/*
 * @test
 * @summary java.nio.ReadOnlyBufferException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main ReadOnlyBufferExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.ByteBuffer;
import java.nio.ReadOnlyBufferException;

public class ReadOnlyBufferExceptionTest {
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
        Class<?> c = Class.forName("java.nio.ReadOnlyBufferException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == UnsupportedOperationException.class, "superclass is UnsupportedOperationException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        ReadOnlyBufferException e = new ReadOnlyBufferException();
        check(e instanceof UnsupportedOperationException, "is UnsupportedOperationException");
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer ro = ByteBuffer.allocate(4).asReadOnlyBuffer();
        check(ro.isReadOnly(), "buffer is read-only");
        expect(ReadOnlyBufferException.class, () -> ro.put((byte) 1), "put on read-only buffer throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

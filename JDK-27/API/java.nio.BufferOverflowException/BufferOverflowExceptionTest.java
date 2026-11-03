/*
 * @test
 * @summary java.nio.BufferOverflowException conformance: white-box structure + black-box ctor chain and trigger.
 * @run main BufferOverflowExceptionTest
 */

import java.lang.reflect.Modifier;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

public class BufferOverflowExceptionTest {
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
        Class<?> c = Class.forName("java.nio.BufferOverflowException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass() == RuntimeException.class, "superclass is RuntimeException");
        check(c.getConstructor() != null, "no-arg constructor present");
    }

    static void testBlackBox() {
        BufferOverflowException e = new BufferOverflowException();
        check(e instanceof RuntimeException, "is RuntimeException");
        check(e.getMessage() == null, "no message");
        check(e.getCause() == null, "no cause");
        check(e.getStackTrace().length > 0, "stack trace captured");

        ByteBuffer bb = ByteBuffer.allocate(1);
        bb.put((byte) 1);
        expect(BufferOverflowException.class, () -> bb.put((byte) 2), "put past capacity throws");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

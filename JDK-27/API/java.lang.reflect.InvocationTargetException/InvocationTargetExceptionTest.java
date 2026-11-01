/*
 * @test
 * @summary java.lang.reflect.InvocationTargetException conformance: cause/target/message.
 * @run main InvocationTargetExceptionTest
 */

import java.lang.reflect.InvocationTargetException;

public class InvocationTargetExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testInvocationTargetException() {
        InvocationTargetException e =
                new InvocationTargetException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getTargetException() == e.getCause(), "getTargetException == cause");
        InvocationTargetException e2 =
                new InvocationTargetException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
    }

    public static void main(String[] args) {
        testInvocationTargetException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

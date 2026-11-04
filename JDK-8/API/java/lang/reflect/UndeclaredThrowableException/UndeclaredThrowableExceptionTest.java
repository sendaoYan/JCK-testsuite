/*
 * @test
 * @summary java.lang.reflect.UndeclaredThrowableException conformance: cause/undeclared/message.
 * @run main UndeclaredThrowableExceptionTest
 */

import java.lang.reflect.UndeclaredThrowableException;

public class UndeclaredThrowableExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testUndeclaredThrowableException() {
        UndeclaredThrowableException e =
                new UndeclaredThrowableException(new RuntimeException("cause"));
        check(e.getCause() instanceof RuntimeException, "getCause");
        check(e.getUndeclaredThrowable() == e.getCause(), "getUndeclaredThrowable == cause");
        UndeclaredThrowableException e2 =
                new UndeclaredThrowableException(new RuntimeException("cause"), "msg");
        check("msg".equals(e2.getMessage()), "message constructor");
        check(e2.getCause() instanceof RuntimeException, "cause constructor");
    }

    public static void main(String[] args) {
        testUndeclaredThrowableException();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

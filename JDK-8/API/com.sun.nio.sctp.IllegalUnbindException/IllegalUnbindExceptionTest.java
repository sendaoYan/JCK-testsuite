/*
 * @test
 * @summary com.sun.nio.sctp.IllegalUnbindException conformance: behavior of constructors, accessors and value semantics.
 * @run main IllegalUnbindExceptionTest
 */

public class IllegalUnbindExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.nio.sctp.IllegalUnbindException e = new com.sun.nio.sctp.IllegalUnbindException("msg");
        check(e instanceof java.lang.IllegalStateException, "extends java.lang.IllegalStateException");
        check("msg".equals(e.getMessage()), "message");
        com.sun.nio.sctp.IllegalUnbindException e2 = new com.sun.nio.sctp.IllegalUnbindException();
        check(e2.getMessage() == null, "no-arg ctor message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

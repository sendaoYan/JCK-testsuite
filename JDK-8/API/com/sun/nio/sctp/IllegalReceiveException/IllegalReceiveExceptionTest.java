/*
 * @test
 * @summary com.sun.nio.sctp.IllegalReceiveException conformance: behavior of constructors, accessors and value semantics.
 * @run main IllegalReceiveExceptionTest
 */

public class IllegalReceiveExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.nio.sctp.IllegalReceiveException e = new com.sun.nio.sctp.IllegalReceiveException("msg");
        check(e instanceof java.lang.IllegalStateException, "extends java.lang.IllegalStateException");
        check("msg".equals(e.getMessage()), "message");
        com.sun.nio.sctp.IllegalReceiveException e2 = new com.sun.nio.sctp.IllegalReceiveException();
        check(e2.getMessage() == null, "no-arg ctor message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

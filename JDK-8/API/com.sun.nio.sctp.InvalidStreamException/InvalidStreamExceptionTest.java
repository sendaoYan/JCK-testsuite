/*
 * @test
 * @summary com.sun.nio.sctp.InvalidStreamException conformance: behavior of constructors, accessors and value semantics.
 * @run main InvalidStreamExceptionTest
 */

public class InvalidStreamExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        com.sun.nio.sctp.InvalidStreamException e = new com.sun.nio.sctp.InvalidStreamException("msg");
        check(e instanceof java.lang.IllegalArgumentException, "extends java.lang.IllegalArgumentException");
        check("msg".equals(e.getMessage()), "message");
        com.sun.nio.sctp.InvalidStreamException e2 = new com.sun.nio.sctp.InvalidStreamException();
        check(e2.getMessage() == null, "no-arg ctor message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

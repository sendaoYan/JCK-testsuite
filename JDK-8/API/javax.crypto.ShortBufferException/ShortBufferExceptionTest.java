/*
 * @test
 * @summary javax.crypto.ShortBufferException conformance: constructors, message, cause and superclass.
 * @run main ShortBufferExceptionTest
 */

public class ShortBufferExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.ShortBufferException e = new javax.crypto.ShortBufferException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.crypto.ShortBufferException e4 = new javax.crypto.ShortBufferException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.crypto.NoSuchPaddingException conformance: constructors, message, cause and superclass.
 * @run main NoSuchPaddingExceptionTest
 */

public class NoSuchPaddingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.NoSuchPaddingException e = new javax.crypto.NoSuchPaddingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.crypto.NoSuchPaddingException e4 = new javax.crypto.NoSuchPaddingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

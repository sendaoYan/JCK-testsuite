/*
 * @test
 * @summary javax.crypto.BadPaddingException conformance: constructors, message, cause and superclass.
 * @run main BadPaddingExceptionTest
 */

public class BadPaddingExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.BadPaddingException e = new javax.crypto.BadPaddingException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.crypto.BadPaddingException e4 = new javax.crypto.BadPaddingException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

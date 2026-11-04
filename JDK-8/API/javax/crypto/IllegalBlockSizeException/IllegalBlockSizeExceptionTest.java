/*
 * @test
 * @summary javax.crypto.IllegalBlockSizeException conformance: constructors, message, cause and superclass.
 * @run main IllegalBlockSizeExceptionTest
 */

public class IllegalBlockSizeExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.crypto.IllegalBlockSizeException e = new javax.crypto.IllegalBlockSizeException("boom");
        check("boom".equals(e.getMessage()), "String ctor sets message");
        check(e instanceof java.security.GeneralSecurityException, "extends java.security.GeneralSecurityException");
        javax.crypto.IllegalBlockSizeException e4 = new javax.crypto.IllegalBlockSizeException();
        check(e4.getMessage() == null, "no-arg ctor message null");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

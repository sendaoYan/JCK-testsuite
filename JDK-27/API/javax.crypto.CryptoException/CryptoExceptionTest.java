/*
 * @test
 * @summary javax.crypto.CryptoException conformance: final runtime crypto exception with cause ctors.
 * @enablePreview
 * @run main CryptoExceptionTest
 */

import javax.crypto.CryptoException;

public class CryptoExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CryptoException e = new CryptoException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof RuntimeException, "is RuntimeException");
        Throwable c = new Throwable("cause");
        check(new CryptoException("m", c).getCause() == c, "string+cause ctor");
        check(new CryptoException(c).getCause() == c, "cause ctor");
        check(new CryptoException().getMessage() == null, "no-arg message null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

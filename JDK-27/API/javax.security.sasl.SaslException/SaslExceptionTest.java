/*
 * @test
 * @summary javax.security.sasl.SaslException conformance: SASL exception with cause and string accessors.
 * @run main SaslExceptionTest
 */

import javax.security.sasl.SaslException;

public class SaslExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        SaslException e = new SaslException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof java.io.IOException, "is IOException");
        check(new SaslException().getMessage() == null, "no-arg message null");
        Throwable c = new Throwable("cause");
        check(new SaslException("m", c).getCause() == c, "cause ctor");
        SaslException x = new SaslException();
        check(x.initCause(c) == x, "initCause returns this");
        check(x.toString().contains("SaslException"), "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.security.sasl.AuthenticationException conformance: SASL authentication exception hierarchy.
 * @run main AuthenticationExceptionTest
 */

import javax.security.sasl.AuthenticationException;
import javax.security.sasl.SaslException;

public class AuthenticationExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        AuthenticationException e = new AuthenticationException("msg");
        check("msg".equals(e.getMessage()), "message ctor");
        check(e instanceof SaslException, "extends SaslException");
        Throwable c = new Throwable("cause");
        check(new AuthenticationException("m", c).getCause() == c, "cause ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

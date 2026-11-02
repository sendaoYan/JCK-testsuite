/*
 * @test
 * @summary javax.security.auth.callback.UnsupportedCallbackException conformance: unsupported callback exception with callback accessor.
 * @run main UnsupportedCallbackExceptionTest
 */

import javax.security.auth.callback.*;

public class UnsupportedCallbackExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.Callback cb = new javax.security.auth.callback.NameCallback("p");
        UnsupportedCallbackException e = new UnsupportedCallbackException(cb);
        check(e.getCallback() == cb, "getCallback");
        check(e instanceof Exception, "is Exception");
        check(new UnsupportedCallbackException(cb, "m").getMessage().equals("m"), "message ctor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

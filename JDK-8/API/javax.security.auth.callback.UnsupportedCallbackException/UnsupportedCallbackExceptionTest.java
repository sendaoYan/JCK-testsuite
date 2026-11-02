/*
 * @test
 * @summary javax.security.auth.callback.UnsupportedCallbackException conformance: constructors and getCallback.
 * @run main UnsupportedCallbackExceptionTest
 */

public class UnsupportedCallbackExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.Callback cb = new javax.security.auth.callback.NameCallback("p");
        javax.security.auth.callback.UnsupportedCallbackException e = new javax.security.auth.callback.UnsupportedCallbackException(cb);
        check(cb.equals(e.getCallback()), "getCallback");
        javax.security.auth.callback.UnsupportedCallbackException e2 = new javax.security.auth.callback.UnsupportedCallbackException(cb, "msg");
        check("msg".equals(e2.getMessage()), "2-arg ctor message");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

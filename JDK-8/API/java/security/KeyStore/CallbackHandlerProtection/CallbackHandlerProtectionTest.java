/*
 * @test
 * @summary java.security.KeyStore.CallbackHandlerProtection conformance: constructor and getCallbackHandler.
 * @run main CallbackHandlerProtectionTest
 */

public class CallbackHandlerProtectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.CallbackHandler h = new javax.security.auth.callback.CallbackHandler() {
            public void handle(javax.security.auth.callback.Callback[] callbacks) {}
        };
        java.security.KeyStore.CallbackHandlerProtection p = new java.security.KeyStore.CallbackHandlerProtection(h);
        check(h.equals(p.getCallbackHandler()), "getCallbackHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

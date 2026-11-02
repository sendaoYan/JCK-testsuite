/*
 * @test
 * @summary java.security.KeyStore.CallbackHandlerProtection conformance: constructor and accessor.
 * @run main CallbackHandlerProtectionTest
 */

import java.security.KeyStore;
import javax.security.auth.callback.CallbackHandler;

public class CallbackHandlerProtectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        CallbackHandler handler = callbacks -> { };
        KeyStore.CallbackHandlerProtection p = new KeyStore.CallbackHandlerProtection(handler);
        check(handler == p.getCallbackHandler(), "getCallbackHandler");
        check(p instanceof KeyStore.ProtectionParameter, "implements ProtectionParameter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

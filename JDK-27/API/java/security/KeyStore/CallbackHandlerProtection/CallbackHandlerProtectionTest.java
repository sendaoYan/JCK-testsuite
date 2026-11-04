/*
 * @test
 * @summary java.security.KeyStore.CallbackHandlerProtection conformance: white-box structure + black-box behavior.
 * @run main CallbackHandlerProtectionTest
 */

import java.security.KeyStore;
import javax.security.auth.callback.CallbackHandler;

import javax.security.auth.callback.*;


public class CallbackHandlerProtectionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        CallbackHandler handler = callbacks -> { };
        KeyStore.CallbackHandlerProtection p = new KeyStore.CallbackHandlerProtection(handler);
        check(handler == p.getCallbackHandler(), "getCallbackHandler");
        check(p instanceof KeyStore.ProtectionParameter, "implements ProtectionParameter");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // Wraps a CallbackHandler as a ProtectionParameter
        CallbackHandler handler = callbacks -> {
            for (Callback cb : callbacks) {
                if (cb instanceof PasswordCallback pc) pc.setPassword("jck-pw".toCharArray());
            }
        };
        KeyStore.CallbackHandlerProtection prot = new KeyStore.CallbackHandlerProtection(handler);
        check(prot.getCallbackHandler() == handler, "handler round trip");
        check(prot instanceof KeyStore.ProtectionParameter, "is ProtectionParameter");
        // the handler actually serves password callbacks end-to-end
        PasswordCallback pcb = new PasswordCallback("prompt:", false);
        prot.getCallbackHandler().handle(new Callback[]{pcb});
        check(new String(pcb.getPassword()).equals("jck-pw"), "handler serves password");
        // null handler rejected
        try {
            new KeyStore.CallbackHandlerProtection(null);
            check(false, "null handler NPE");
        } catch (NullPointerException npe) {
            check(true, "null handler NPE");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

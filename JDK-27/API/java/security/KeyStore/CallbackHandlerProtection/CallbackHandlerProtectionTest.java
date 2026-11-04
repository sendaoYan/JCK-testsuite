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
        testContractDeep();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        // counting handler: identity retention + invocation accounting through the wrapper
        int[] count = {0};
        CallbackHandler counting = callbacks -> {
            count[0]++;
            for (Callback cb : callbacks) {
                if (cb instanceof PasswordCallback pc) pc.setPassword("deep".toCharArray());
            }
        };
        KeyStore.CallbackHandlerProtection prot = new KeyStore.CallbackHandlerProtection(counting);
        check(prot.getCallbackHandler() == counting, "handler retained by identity");
        PasswordCallback pcb = new PasswordCallback("p:", false);
        prot.getCallbackHandler().handle(new Callback[]{pcb});
        check(count[0] == 1, "handler invoked once");
        check("deep".equals(new String(pcb.getPassword())), "password served");
        check(pcb.getPassword() != pcb.getPassword(), "callback password cloned on read");

        // identity equality across wrappers sharing one handler
        KeyStore.CallbackHandlerProtection prot2 = new KeyStore.CallbackHandlerProtection(counting);
        check(prot.equals(prot) && !prot.equals(prot2), "identity equality semantics");

        // usable as builder protection (getKeyStore must precede getProtectionParameter)
        KeyStore ks = KeyStore.getInstance("JKS");
        ks.load(null, null);
        KeyStore.Builder b = KeyStore.Builder.newInstance(ks, prot);
        check(b.getKeyStore() == ks, "builder wraps store");
        check(b.getProtectionParameter("any") == prot, "builder returns CHP");

        // class structure mirrors JDK-8: public, concrete, non-final
        Class<?> c = Class.forName("java.security.KeyStore$CallbackHandlerProtection");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()) && !c.isInterface(),
                "public concrete class");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "class is extensible");

        // existing key entries cannot be recovered through a callback protection:
        // engineGetEntry only understands PasswordProtection
        KeyStore jce = KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"),
                "pw".toCharArray(), null);
        try {
            jce.getEntry("s", prot);
            check(false, "existing key entry getEntry(CHP) UOE");
        } catch (UnsupportedOperationException uoe) {
            check(true, "existing key entry getEntry(CHP) UOE");
        }

        // missing-alias getEntry never invokes the handler
        int before = count[0];
        check(jce.getEntry("missing", prot) == null,
                "getEntry missing alias with CHP null");
        check(count[0] == before, "missing alias does not invoke handler");

        // hashCode identity-stable; PasswordCallback accessors round-trip
        check(prot.hashCode() == prot.hashCode(), "hashCode stable");
        check(!prot.equals(null), "wrapper not equal to null");
        PasswordCallback echo = new PasswordCallback("visible?", true);
        check("visible?".equals(echo.getPrompt()) && echo.isEchoOn(),
                "PasswordCallback prompt and echoOn round-trip");

        // handler invoked even for an empty callback array
        int before2 = count[0];
        prot.getCallbackHandler().handle(new Callback[0]);
        check(count[0] == before2 + 1, "handler invoked for empty callback array");
    }
}

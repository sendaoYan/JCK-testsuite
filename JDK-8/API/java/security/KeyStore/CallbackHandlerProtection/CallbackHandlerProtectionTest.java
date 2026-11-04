/*
 * @test
 * @summary java.security.KeyStore.CallbackHandlerProtection conformance:
 *          constructor null matrix, handler identity round-trip,
 *          ProtectionParameter subtyping, end-to-end PasswordCallback
 *          service through the wrapped handler, and identity equality.
 * @run main CallbackHandlerProtectionTest
 */

public class CallbackHandlerProtectionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static int handleCount;

    public static void main(String[] args) throws Exception {
        javax.security.auth.callback.CallbackHandler h = new javax.security.auth.callback.CallbackHandler() {
            public void handle(javax.security.auth.callback.Callback[] callbacks) {}
        };
        java.security.KeyStore.CallbackHandlerProtection p = new java.security.KeyStore.CallbackHandlerProtection(h);
        check(h.equals(p.getCallbackHandler()), "getCallbackHandler");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        // null handler rejected
        checkThrows(NullPointerException.class,
                () -> new java.security.KeyStore.CallbackHandlerProtection(null),
                "null handler NPE");

        // counting handler: identity round-trip and invocation accounting
        javax.security.auth.callback.CallbackHandler counting =
                new javax.security.auth.callback.CallbackHandler() {
                    public void handle(javax.security.auth.callback.Callback[] callbacks) {
                        handleCount++;
                        for (javax.security.auth.callback.Callback cb : callbacks) {
                            if (cb instanceof javax.security.auth.callback.PasswordCallback) {
                                ((javax.security.auth.callback.PasswordCallback) cb)
                                        .setPassword("jck-pw".toCharArray());
                            }
                        }
                    }
                };
        java.security.KeyStore.CallbackHandlerProtection prot =
                new java.security.KeyStore.CallbackHandlerProtection(counting);
        check(prot.getCallbackHandler() == counting, "handler identity round-trip");
        check(prot instanceof java.security.KeyStore.ProtectionParameter,
                "implements ProtectionParameter");

        // the wrapped handler actually serves password callbacks end-to-end
        javax.security.auth.callback.PasswordCallback pcb =
                new javax.security.auth.callback.PasswordCallback("prompt:", false);
        int before = handleCount;
        prot.getCallbackHandler().handle(
                new javax.security.auth.callback.Callback[]{pcb});
        check(handleCount == before + 1, "handler invoked exactly once");
        check(pcb.getPassword() != null && "jck-pw".equals(new String(pcb.getPassword())),
                "handler serves password end-to-end");
        // PasswordCallback.getPassword returns a fresh clone each call (JDK-8 blanks on clear)
        check(pcb.getPassword() != pcb.getPassword(), "callback password cloned on read");

        // no equals/hashCode override: identity semantics
        java.security.KeyStore.CallbackHandlerProtection prot2 =
                new java.security.KeyStore.CallbackHandlerProtection(counting);
        check(prot.equals(prot), "reflexive equals");
        check(!prot.equals(prot2), "distinct wrappers with same handler unequal");
        check(prot.toString() != null, "toString non-null");

        // handler is retained by reference: same handler observable through both wrappers
        check(prot.getCallbackHandler() == prot2.getCallbackHandler(),
                "same handler shared by two wrappers");

        // class structure: public concrete ProtectionParameter implementation
        Class<?> c = Class.forName("java.security.KeyStore$CallbackHandlerProtection");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "class is public");
        check(!c.isInterface(), "class is concrete");
        check(java.security.KeyStore.ProtectionParameter.class.isAssignableFrom(c),
                "assignable to ProtectionParameter");

        // usable as builder protection parameter (after getKeyStore)
        java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
        ks.load(null, null);
        java.security.KeyStore.Builder b =
                java.security.KeyStore.Builder.newInstance(ks, prot);
        check(b.getKeyStore() == ks, "builder wraps the store");
        check(b.getProtectionParameter("any") == prot,
                "builder returns the CallbackHandlerProtection");

        // missing-alias getEntry with a callback protection yields null (no UOE;
        // the UOE path requires an existing key entry — covered in KeyStoreTest)
        check(ks.getEntry("missing", prot) == null,
                "getEntry missing alias with CallbackHandlerProtection null");

        // existing key entries cannot be recovered through a callback protection:
        // engineGetEntry only understands PasswordProtection
        java.security.KeyStore jce = java.security.KeyStore.getInstance("JCEKS");
        jce.load(null, null);
        jce.setKeyEntry("s", new javax.crypto.spec.SecretKeySpec(new byte[16], "AES"),
                "pw".toCharArray(), null);
        checkThrows(UnsupportedOperationException.class,
                () -> jce.getEntry("s", prot),
                "existing key entry getEntry(CHP) UOE");

        // hashCode is identity-stable like equals
        check(prot.hashCode() == prot.hashCode(), "hashCode stable");
        check(!prot.equals(null), "wrapper not equal to null");

        // PasswordCallback accessors: prompt and echoOn round-trip
        javax.security.auth.callback.PasswordCallback echo =
                new javax.security.auth.callback.PasswordCallback("visible?", true);
        check("visible?".equals(echo.getPrompt()) && echo.isEchoOn(),
                "PasswordCallback prompt and echoOn round-trip");

        // the handler is invoked even for an empty callback array (pure accounting)
        int before2 = handleCount;
        prot.getCallbackHandler().handle(
                new javax.security.auth.callback.Callback[0]);
        check(handleCount == before2 + 1, "handler invoked for empty callback array");
    }
}

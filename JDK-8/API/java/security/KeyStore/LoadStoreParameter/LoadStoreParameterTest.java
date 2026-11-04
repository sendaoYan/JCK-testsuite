/*
 * @test
 * @summary java.security.KeyStore.LoadStoreParameter conformance: interface
 *          structure, custom implementations, DomainLoadStoreParameter
 *          subtyping, and the JDK-8 UOE behavior of load/store(LSP) on
 *          stream-based keystore implementations.
 * @run main LoadStoreParameterTest
 */

public class LoadStoreParameterTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$LoadStoreParameter");
        check(c.isInterface(), "is interface");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("getProtectionParameter"), "declares getProtectionParameter");

        testContractDeep();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- contract-deepening section (appended) ----
    static void testContractDeep() throws Exception {
        Class<?> c = Class.forName("java.security.KeyStore$LoadStoreParameter");

        // declares exactly one method: getProtectionParameter, returning ProtectionParameter
        java.lang.reflect.Method[] own = c.getDeclaredMethods();
        check(own.length == 1, "exactly one declared method");
        check("getProtectionParameter".equals(own[0].getName()), "sole method name");
        check(java.security.KeyStore.ProtectionParameter.class
                .equals(own[0].getReturnType()), "returns ProtectionParameter");

        // custom implementation returns the fixed parameter by identity
        final java.security.KeyStore.ProtectionParameter fixed =
                new java.security.KeyStore.PasswordProtection("pw".toCharArray());
        class SimpleLSP implements java.security.KeyStore.LoadStoreParameter {
            public java.security.KeyStore.ProtectionParameter getProtectionParameter() {
                return fixed;
            }
        }
        java.security.KeyStore.LoadStoreParameter lsp = new SimpleLSP();
        check(lsp.getProtectionParameter() == fixed, "custom LSP fixed parameter identity");
        check(c.isInstance(lsp), "custom LSP instanceof");

        // null return is an implementation choice (record impl documents it for
        // DomainLoadStoreParameter: protection for the whole domain, not the LSP itself)
        java.security.DomainLoadStoreParameter dlsp = new java.security.DomainLoadStoreParameter(
                java.net.URI.create("file:/tmp/jck-lsp-domain.cfg"),
                new java.util.LinkedHashMap<String, java.security.KeyStore.ProtectionParameter>());
        check(c.isInstance(dlsp), "DomainLoadStoreParameter implements LSP");
        check(dlsp.getProtectionParameter() == null,
                "DomainLoadStoreParameter domain-level protection null");
        check(dlsp.getConfiguration().toString().equals("file:/tmp/jck-lsp-domain.cfg"),
                "DomainLoadStoreParameter configuration URI round-trip");
        check(dlsp.getProtectionParams().isEmpty(),
                "DomainLoadStoreParameter empty protection map");

        // JDK-8: stream-based keystores (JKS/PKCS12) reject load/store via LSP —
        // the LSP path exists only for keystores with their own data source (DKS)
        final java.security.KeyStore.LoadStoreParameter pwLsp = new SimpleLSP();
        checkThrows(UnsupportedOperationException.class, () -> {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
            ks.load(pwLsp);
        }, "JKS load(LSP) UOE (JDK-8)");
        checkThrows(UnsupportedOperationException.class, () -> {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("PKCS12");
            ks.load(null, null);
            ks.store(pwLsp);
        }, "PKCS12 store(LSP) UOE");
        checkThrows(UnsupportedOperationException.class, () -> {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JKS");
            ks.load(null, null);
            ks.store(pwLsp);
        }, "JKS store(LSP) UOE");

        // LSP instances keep identity semantics (no equals contract defined)
        check(lsp.equals(lsp), "reflexive equals");
        check(!lsp.equals(new SimpleLSP()), "distinct implementations not equal");

        // JCEKS (another stream-based format) rejects LSP paths identically
        checkThrows(UnsupportedOperationException.class, () -> {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JCEKS");
            ks.load(pwLsp);
        }, "JCEKS load(LSP) UOE (JDK-8)");
        checkThrows(UnsupportedOperationException.class, () -> {
            java.security.KeyStore ks = java.security.KeyStore.getInstance("JCEKS");
            ks.load(null, null);
            ks.store(pwLsp);
        }, "JCEKS store(LSP) UOE");

        // DomainLoadStoreParameter map: defensive copy on construction, unmodifiable view
        java.util.Map<String, java.security.KeyStore.ProtectionParameter> src =
                new java.util.LinkedHashMap<String, java.security.KeyStore.ProtectionParameter>();
        java.security.KeyStore.ProtectionParameter val =
                new java.security.KeyStore.PasswordProtection("v".toCharArray());
        src.put("entry", val);
        java.security.DomainLoadStoreParameter d2 =
                new java.security.DomainLoadStoreParameter(
                        java.net.URI.create("file:/jck-dlsp.cfg"), src);
        check(d2.getProtectionParams().get("entry") == val,
                "DomainLoadStoreParameter value identity");
        src.clear();
        check(d2.getProtectionParams().size() == 1,
                "DomainLoadStoreParameter snapshot of source map");
        boolean mapPutRejected = false;
        try { d2.getProtectionParams().put("new", val); }
        catch (UnsupportedOperationException uoe) { mapPutRejected = true; }
        check(mapPutRejected, "DomainLoadStoreParameter view unmodifiable");

        // standalone marker: no superinterfaces
        check(c.getInterfaces().length == 0, "no superinterfaces");
    }
}

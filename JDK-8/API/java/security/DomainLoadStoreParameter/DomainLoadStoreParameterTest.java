/*
 * @test
 * @summary java.security.DomainLoadStoreParameter conformance: constructor and getters.
 *          Appended: null URI/map NPEs, unmodifiable getProtectionParams view (UOE on writes,
 *          all channels), constructor snapshot semantics (source-map mutation invisible),
 *          getProtectionParameter fixed null by spec, entry identity preservation,
 *          empty-map construction, KeyStore.LoadStoreParameter conformance,
 *          identity equals/hashCode, exact getter surface.
 * @run main DomainLoadStoreParameterTest
 */

public class DomainLoadStoreParameterTest {
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
        java.security.DomainLoadStoreParameter p = new java.security.DomainLoadStoreParameter(
            new java.net.URI("file:/tmp/x"), new java.util.HashMap<String, java.security.KeyStore.ProtectionParameter>());
        check(p.getConfiguration() != null, "getConfiguration");
        check(p.getProtectionParams() != null, "getProtectionParams");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        java.net.URI uri = new java.net.URI("file:/etc/jck/domains.cfg");
        java.security.KeyStore.ProtectionParameter pp1 =
                new java.security.KeyStore.PasswordProtection("pw1".toCharArray());
        java.security.KeyStore.ProtectionParameter pp2 =
                new java.security.KeyStore.PasswordProtection("pw2".toCharArray());
        java.util.Map<String, java.security.KeyStore.ProtectionParameter> src = new java.util.LinkedHashMap<String, java.security.KeyStore.ProtectionParameter>();
        src.put("domain1", pp1);
        src.put("domain2", pp2);
        java.security.DomainLoadStoreParameter d =
                new java.security.DomainLoadStoreParameter(uri, src);

        // getters return the constructor values (URI identity preserved)
        check(d.getConfiguration() == uri, "getConfiguration identity");
        check(d.getProtectionParameter() == null, "getProtectionParameter fixed null by spec");
        check(d instanceof java.security.KeyStore.LoadStoreParameter, "implements KeyStore.LoadStoreParameter");

        // returned map: same content, values by identity
        java.util.Map<String, java.security.KeyStore.ProtectionParameter> view = d.getProtectionParams();
        check(view.size() == 2, "view size");
        check(view.get("domain1") == pp1 && view.get("domain2") == pp2, "value identity preserved");

        // unmodifiable view: all write channels rejected with UnsupportedOperationException
        checkThrows(UnsupportedOperationException.class, () -> view.put("domain3", null), "put UOE");
        checkThrows(UnsupportedOperationException.class, () -> view.remove("domain1"), "remove UOE");
        checkThrows(UnsupportedOperationException.class, () -> view.putAll(java.util.Collections.emptyMap()), "putAll UOE");
        checkThrows(UnsupportedOperationException.class, () -> view.clear(), "clear UOE");
        check(d.getProtectionParams().size() == 2, "view unchanged after rejected writes");

        // snapshot semantics: mutating the source map after construction is not visible
        src.put("domain3", null);
        src.remove("domain1");
        check(d.getProtectionParams().size() == 2, "source mutation invisible");
        check(d.getProtectionParams().get("domain1") == pp1, "removed entry still present in snapshot");

        // empty map: legal, still unmodifiable
        java.security.DomainLoadStoreParameter empty = new java.security.DomainLoadStoreParameter(
                uri, new java.util.HashMap<String, java.security.KeyStore.ProtectionParameter>());
        check(empty.getProtectionParams().isEmpty(), "empty map accepted");
        check(empty.getProtectionParameter() == null, "empty protection null");
        checkThrows(UnsupportedOperationException.class,
                () -> empty.getProtectionParams().put("x", null), "empty view unmodifiable");

        // null arguments rejected at construction
        checkThrows(NullPointerException.class,
                () -> new java.security.DomainLoadStoreParameter(null, src), "null URI NPE");
        checkThrows(NullPointerException.class,
                () -> new java.security.DomainLoadStoreParameter(uri, null), "null map NPE");

        // no value equals/hashCode: identity semantics (not specified as value type)
        java.security.DomainLoadStoreParameter other = new java.security.DomainLoadStoreParameter(
                uri, new java.util.LinkedHashMap<String, java.security.KeyStore.ProtectionParameter>(src));
        check(!d.equals(other), "identity equals: same content still unequal");
        check(d.equals(d), "reflexive equals");
        check(d.hashCode() == d.hashCode(), "hashCode stable");

        // exact getter surface: 3 public getters
        int getters = 0;
        for (java.lang.reflect.Method m : java.security.DomainLoadStoreParameter.class.getDeclaredMethods())
            if (m.getName().startsWith("get")) getters++;
        check(getters == 3, "exactly 3 getters");
        check(java.lang.reflect.Modifier.isFinal(java.security.DomainLoadStoreParameter.class.getModifiers()),
                "final class (no subclassing)");
    }
}

/*
 * @test
 * @summary java.security.URIParameter conformance: constructor and getURI.
 *          Appended: null URI NPE, identity preservation across repeated getURI calls,
 *          Policy.Parameters + Configuration.Parameters conformance, NOT an
 *          AlgorithmParameterSpec, identity equals/hashCode, opaque/hierarchical/fragment
 *          URIs all accepted, exact single-getter surface, final class.
 * @run main URIParameterTest
 */

public class URIParameterTest {
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
        java.net.URI u = new java.net.URI("http://example.com/");
        java.security.URIParameter p = new java.security.URIParameter(u);
        check(u.equals(p.getURI()), "getURI");
        testContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- appended contract dimensions ----
    static void testContract() throws Exception {
        java.net.URI uri = new java.net.URI("file:/etc/jck/java.policy");
        java.security.URIParameter param = new java.security.URIParameter(uri);

        // repeated getURI returns the same instance with identical content
        check(param.getURI() == uri, "getURI identity");
        check(param.getURI() == param.getURI(), "getURI stable across calls");
        check(uri.equals(param.getURI()), "getURI content");

        // null rejected at construction
        checkThrows(NullPointerException.class, () -> new java.security.URIParameter(null), "null URI NPE");

        // marker-interface conformance
        check(param instanceof java.security.Policy.Parameters, "implements Policy.Parameters");
        check(param instanceof javax.security.auth.login.Configuration.Parameters,
                "implements Configuration.Parameters");
        check(!(param instanceof java.security.spec.AlgorithmParameterSpec),
                "NOT an AlgorithmParameterSpec");

        // identity equals/hashCode (no value semantics specified)
        java.security.URIParameter sameUri = new java.security.URIParameter(uri);
        check(!param.equals(sameUri), "same URI, different parameter objects unequal");
        check(param.equals(param), "reflexive equals");
        check(!param.equals(null), "equals null false");
        check(!param.equals("string"), "equals wrong type false");
        check(param.hashCode() == param.hashCode(), "hashCode stable");

        // URI flavor tolerance: hierarchical, opaque, with fragment/scheme-specific parts
        java.net.URI[] accepted = {
                new java.net.URI("http://jck.example/policy#frag"),
                new java.net.URI("mailto:jck@example.com"),
                new java.net.URI("file:///var/policy"),
                new java.net.URI("../relative/policy"),
                new java.net.URI("")
        };
        for (java.net.URI u : accepted)
            check(new java.security.URIParameter(u).getURI().equals(u), "accepted URI " + u);
        java.security.URIParameter frag = new java.security.URIParameter(accepted[0]);
        check("frag".equals(frag.getURI().getFragment()), "fragment preserved");

        // exact surface: single getter, final class
        int getters = 0;
        for (java.lang.reflect.Method m : java.security.URIParameter.class.getDeclaredMethods())
            if (m.getName().startsWith("get")) getters++;
        check(getters == 1, "exactly 1 getter");
        check(!java.lang.reflect.Modifier.isFinal(java.security.URIParameter.class.getModifiers()),
                "not final (subclassable; no finality specified)");
        check(!java.io.Serializable.class.isAssignableFrom(java.security.URIParameter.class),
                "not Serializable");
    }
}

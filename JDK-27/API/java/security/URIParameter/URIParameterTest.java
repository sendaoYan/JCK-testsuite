/*
 * @test
 * @summary java.security.URIParameter conformance: white-box structure + black-box behavior.
 * @run main URIParameterTest
 */

import java.security.URIParameter;
import java.net.URI;
import java.security.*;
import java.util.*;

public class URIParameterTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        URI uri = new URI("file:///tmp/policy");
        URIParameter p = new URIParameter(uri);
        check(uri.equals(p.getURI()), "getURI");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        java.net.URI uri = java.net.URI.create("file:/etc/jck/java.policy");
        URIParameter p = new URIParameter(uri);
        check(p.getURI().equals(uri), "uri round trip");
        check(p.getURI() == uri, "same instance");
        // different URI, different parameter object content
        URIParameter q = new URIParameter(java.net.URI.create("file:/other.policy"));
        check(!p.getURI().equals(q.getURI()), "different uris");
        check(!p.equals(q), "not equal");
        // hierarchical and opaque URIs both accepted
        check(new URIParameter(java.net.URI.create("http://jck.example/policy#frag")).getURI().getFragment().equals("frag"), "fragment");
        // null URI rejected
        try {
            new URIParameter(null);
            check(false, "null URI NPE");
        } catch (NullPointerException npe) {
            check(true, "null URI NPE");
        }
        // implements both Policy.Parameters and login Configuration.Parameters
        check(p instanceof Policy.Parameters, "Policy.Parameters");
        check(p instanceof javax.security.auth.login.Configuration.Parameters, "Configuration.Parameters");

        // ---- appended contract dimensions ----
        // repeated getURI returns the same instance
        check(p.getURI() == p.getURI(), "getURI stable across calls");
        // NOT an AlgorithmParameterSpec (different family from spec/ classes)
        check(!(p instanceof java.security.spec.AlgorithmParameterSpec), "not AlgorithmParameterSpec");
        // identity equals/hashCode
        check(p.equals(p), "reflexive");
        check(!p.equals(null) && !p.equals("string"), "equals null/wrong type false");
        check(p.hashCode() == p.hashCode(), "hashCode stable");
        // URI flavor tolerance: opaque, relative, empty
        check(new URIParameter(java.net.URI.create("mailto:jck@example.com")).getURI().getScheme().equals("mailto"),
                "opaque URI accepted");
        check(!new URIParameter(java.net.URI.create("../rel/policy")).getURI().isAbsolute(), "relative URI accepted");
        check(new URIParameter(java.net.URI.create("")).getURI().equals(URI.create("")), "empty URI accepted");
        // single getter, final, not Serializable
        int getters = 0;
        for (java.lang.reflect.Method m : URIParameter.class.getDeclaredMethods())
            if (m.getName().startsWith("get")) getters++;
        check(getters == 1, "exactly 1 getter");
        check(!java.lang.reflect.Modifier.isFinal(URIParameter.class.getModifiers()), "not final");
        check(!java.io.Serializable.class.isAssignableFrom(URIParameter.class), "not Serializable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

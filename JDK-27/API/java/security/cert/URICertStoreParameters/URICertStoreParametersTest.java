/*
 * @test
 * @summary java.security.cert.URICertStoreParameters conformance: white-box structure + black-box behavior.
 * @run main URICertStoreParametersTest
 */

import java.security.cert.URICertStoreParameters;
import java.net.URI;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class URICertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        URI uri = new URI("ldap://example.com");
        URICertStoreParameters p = new URICertStoreParameters(uri);
        check(uri.equals(p.getURI()), "getURI");
        URICertStoreParameters clone = p.clone();
        check(p.equals(clone), "clone equals");
        check(p.hashCode() == clone.hashCode(), "hashCode");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // value class over a URI with proper equals/hashCode
        java.net.URI uri = java.net.URI.create("https://jck.example/certs");
        URICertStoreParameters p = new URICertStoreParameters(uri);
        check(p.getURI().equals(uri), "uri retained");
        check(p.getURI() == uri, "same reference");
        URICertStoreParameters same = new URICertStoreParameters(uri);
        check(p.equals(same) && p.hashCode() == same.hashCode(), "value equals");
        check(!p.equals(new URICertStoreParameters(java.net.URI.create("https://other/"))), "different uri unequal");
        // clone
        check(p.clone() != p, "clone distinct");
        check(p.clone().equals(p), "clone equal");
        check(p.toString().contains("jck.example"), "toString uri");
        // null uri rejected
        try { new URICertStoreParameters(null); check(false, "null uri"); }
        catch (NullPointerException npe) { check(true, "null uri NPE"); }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

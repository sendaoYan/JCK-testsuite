/*
 * @test
 * @summary java.security.cert.LDAPCertStoreParameters conformance: white-box structure + black-box behavior.
 * @run main LDAPCertStoreParametersTest
 */

import java.security.cert.LDAPCertStoreParameters;
import java.io.*;
import java.security.cert.*;
import java.util.*;
import java.util.Base64;


public class LDAPCertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static void testWhiteBox() throws Exception {
        LDAPCertStoreParameters p = new LDAPCertStoreParameters("ldap.example.com", 636);
        check("ldap.example.com".equals(p.getServerName()), "getServerName");
        check(p.getPort() == 636, "getPort");
        LDAPCertStoreParameters d = new LDAPCertStoreParameters();
        check("localhost".equals(d.getServerName()), "default serverName");
        check(d.getPort() == 389, "default port");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {

        // constructors and defaults (port 389)
        LDAPCertStoreParameters dflt = new LDAPCertStoreParameters();
        check(dflt.getServerName().equals("localhost"), "default host localhost");
        check(dflt.getPort() == 389, "default port 389");
        LDAPCertStoreParameters host = new LDAPCertStoreParameters("ldap.jck.example");
        check(host.getServerName().equals("ldap.jck.example"), "host ctor");
        check(host.getPort() == 389, "host default port");
        LDAPCertStoreParameters full = new LDAPCertStoreParameters("ldap.jck.example", 636);
        check(full.getPort() == 636, "explicit port");
        check(full.toString().contains("ldap.jck.example"), "toString host");
        // clone
        LDAPCertStoreParameters c = (LDAPCertStoreParameters) full.clone();
        check(c != full && c.getServerName().equals("ldap.jck.example") && c.getPort() == 636, "clone");
        // equals not overridden: identity semantics
        check(!full.equals(new LDAPCertStoreParameters("ldap.jck.example", 636)), "identity equality");
        // nulls rejected
        try { new LDAPCertStoreParameters(null); check(false, "null host"); }
        catch (NullPointerException npe) { check(true, "null host NPE"); }
        // negative ports are not validated by the constructor
        check(new LDAPCertStoreParameters("h", -1).getPort() == -1, "negative port retained");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

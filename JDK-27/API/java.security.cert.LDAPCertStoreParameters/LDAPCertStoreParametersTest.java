/*
 * @test
 * @summary java.security.cert.LDAPCertStoreParameters conformance: constructors and accessors.
 * @run main LDAPCertStoreParametersTest
 */

import java.security.cert.LDAPCertStoreParameters;

public class LDAPCertStoreParametersTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        LDAPCertStoreParameters p = new LDAPCertStoreParameters("ldap.example.com", 636);
        check("ldap.example.com".equals(p.getServerName()), "getServerName");
        check(p.getPort() == 636, "getPort");
        LDAPCertStoreParameters d = new LDAPCertStoreParameters();
        check("localhost".equals(d.getServerName()), "default serverName");
        check(d.getPort() == 389, "default port");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

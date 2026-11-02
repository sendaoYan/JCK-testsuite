/*
 * @test
 * @summary java.security.cert.LDAPCertStoreParameters conformance: constructors and getters.
 * @run main LDAPCertStoreParametersTest
 */

public class LDAPCertStoreParametersTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.security.cert.LDAPCertStoreParameters p = new java.security.cert.LDAPCertStoreParameters("localhost", 389);
        check("localhost".equals(p.getServerName()), "getServerName");
        check(p.getPort() == 389, "getPort");
        java.security.cert.LDAPCertStoreParameters d = new java.security.cert.LDAPCertStoreParameters();
        check("localhost".equals(d.getServerName()), "default server");
        check(p.clone() instanceof java.security.cert.LDAPCertStoreParameters, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.naming.ldap.StartTlsRequest conformance: oid/id.
 * @run main StartTlsRequestTest
 */


public class StartTlsRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.StartTlsRequest r = new javax.naming.ldap.StartTlsRequest();
        check(r.getID().equals(javax.naming.ldap.StartTlsRequest.OID), "getID OID");
        check("1.3.6.1.4.1.1466.20037".equals(javax.naming.ldap.StartTlsRequest.OID), "OID value");        System.out.println("PASS (" + passed + " assertions)");
    }
}

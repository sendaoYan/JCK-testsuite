/*
 * @test
 * @summary javax.naming.ldap.PagedResultsResponseControl conformance: result size/cookie.
 * @run main PagedResultsResponseControlTest
 */


public class PagedResultsResponseControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        byte[] ber = {0x30, 0x05, 0x02, 0x01, 0x00, 0x04, 0x00};
        javax.naming.ldap.PagedResultsResponseControl c = new javax.naming.ldap.PagedResultsResponseControl(javax.naming.ldap.PagedResultsResponseControl.OID, true, ber);
        check(c.getID().equals(javax.naming.ldap.PagedResultsResponseControl.OID), "getID OID");
        check(c.getResultSize() == 0, "getResultSize");        System.out.println("PASS (" + passed + " assertions)");
    }
}

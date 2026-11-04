/*
 * @test
 * @summary javax.naming.ldap.PagedResultsControl conformance: id/criticality.
 * @run main PagedResultsControlTest
 */


public class PagedResultsControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.PagedResultsControl c = new javax.naming.ldap.PagedResultsControl(100, true);
        check(c.getID().equals(javax.naming.ldap.PagedResultsControl.OID), "getID OID");
        check(c.isCritical(), "critical flag");
        javax.naming.ldap.PagedResultsControl c2 = new javax.naming.ldap.PagedResultsControl(50, new byte[0], false);
        check(!c2.isCritical(), "critical false");        System.out.println("PASS (" + passed + " assertions)");
    }
}

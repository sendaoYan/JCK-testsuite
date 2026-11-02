/*
 * @test
 * @summary javax.naming.ldap.SortControl conformance: id/criticality.
 * @run main SortControlTest
 */


public class SortControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.SortKey[] keys = { new javax.naming.ldap.SortKey("cn") };
        javax.naming.ldap.SortControl c = new javax.naming.ldap.SortControl(keys, true);
        check(c.getID().equals(javax.naming.ldap.SortControl.OID), "getID OID");
        check(c.isCritical(), "critical flag");        System.out.println("PASS (" + passed + " assertions)");
    }
}

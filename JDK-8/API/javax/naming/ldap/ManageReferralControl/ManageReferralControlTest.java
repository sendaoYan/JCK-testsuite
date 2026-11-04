/*
 * @test
 * @summary javax.naming.ldap.ManageReferralControl conformance: id/criticality.
 * @run main ManageReferralControlTest
 */


public class ManageReferralControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.ManageReferralControl c = new javax.naming.ldap.ManageReferralControl();
        check(c.getID().equals(javax.naming.ldap.ManageReferralControl.OID), "getID OID");
        check(c.isCritical(), "critical true default");
        javax.naming.ldap.ManageReferralControl c2 = new javax.naming.ldap.ManageReferralControl(false);
        check(!c2.isCritical(), "critical flag false");        System.out.println("PASS (" + passed + " assertions)");
    }
}

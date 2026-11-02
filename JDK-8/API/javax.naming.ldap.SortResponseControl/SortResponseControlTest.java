/*
 * @test
 * @summary javax.naming.ldap.SortResponseControl conformance: result code/sorted.
 * @run main SortResponseControlTest
 */


public class SortResponseControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        byte[] ber = {0x30, 0x03, 0x0a, 0x01, 0x00};
        javax.naming.ldap.SortResponseControl c = new javax.naming.ldap.SortResponseControl(javax.naming.ldap.SortResponseControl.OID, true, ber);
        check(c.getID().equals(javax.naming.ldap.SortResponseControl.OID), "getID OID");
        check(c.getResultCode() == 0, "getResultCode");
        check(c.isSorted(), "isSorted");        System.out.println("PASS (" + passed + " assertions)");
    }
}

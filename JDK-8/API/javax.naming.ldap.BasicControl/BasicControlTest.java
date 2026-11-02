/*
 * @test
 * @summary javax.naming.ldap.BasicControl conformance: id/critical/value.
 * @run main BasicControlTest
 */


public class BasicControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.BasicControl c = new javax.naming.ldap.BasicControl("1.2.3");
        check("1.2.3".equals(c.getID()), "getID");
        check(!c.isCritical(), "critical false default");
        check(c.getEncodedValue() == null, "encoded value null");
        javax.naming.ldap.BasicControl c2 = new javax.naming.ldap.BasicControl("1.2.3", true, new byte[]{1});
        check(c2.isCritical(), "critical flag");
        check(c2.getEncodedValue().length == 1, "getEncodedValue");        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.naming.ldap.Rdn conformance: type/value/escape.
 * @run main RdnTest
 */


public class RdnTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.Rdn r = new javax.naming.ldap.Rdn("cn=John");
        check("cn".equals(r.getType()), "getType");
        check("John".equals(r.getValue()), "getValue");
        check(r.size() == 1, "size");
        check("cn=John".equals(r.toString()), "toString");
        check("a\\+b".equals(javax.naming.ldap.Rdn.escapeValue("a+b")), "escapeValue");
        check("a+b".equals(javax.naming.ldap.Rdn.unescapeValue("a\\+b")), "unescapeValue");
        javax.naming.ldap.Rdn r2 = new javax.naming.ldap.Rdn("cn", "John");
        check(r.equals(r2) && r.compareTo(r2) == 0, "equals/compareTo");        System.out.println("PASS (" + passed + " assertions)");
    }
}

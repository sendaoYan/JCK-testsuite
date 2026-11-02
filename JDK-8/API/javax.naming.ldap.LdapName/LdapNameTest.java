/*
 * @test
 * @summary javax.naming.ldap.LdapName conformance: rdns/size/get.
 * @run main LdapNameTest
 */


public class LdapNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.LdapName n = new javax.naming.ldap.LdapName("cn=John Doe,dc=example,dc=com");
        check(n.size() == 3, "size");
        check("cn=John Doe,dc=example,dc=com".equals(n.toString()), "toString");
        check("dc=com".equals(n.get(0)), "get(0)");
        check("cn=John Doe".equals(n.get(2)), "get(2)");
        check("dc".equals(n.getRdn(0).getType()), "getRdn(0) type");
        check("cn".equals(n.getRdn(2).getType()), "getRdn(2) type");
        check(!n.isEmpty(), "not empty");
        javax.naming.ldap.LdapName clone = (javax.naming.ldap.LdapName) n.clone();
        check(n.equals(clone), "clone equals");        System.out.println("PASS (" + passed + " assertions)");
    }
}

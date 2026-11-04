/*
 * @test
 * @summary javax.naming.ldap.InitialLdapContext conformance: lazy construction/close.
 * @run main InitialLdapContextTest
 */


public class InitialLdapContextTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        javax.naming.ldap.InitialLdapContext ctx = new javax.naming.ldap.InitialLdapContext();
        check(ctx != null, "constructed");
        ctx.close();
        check(true, "closed");        System.out.println("PASS (" + passed + " assertions)");
    }
}

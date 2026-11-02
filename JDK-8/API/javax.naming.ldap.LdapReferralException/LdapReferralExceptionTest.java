/*
 * @test
 * @summary javax.naming.ldap.LdapReferralException conformance: abstract class hierarchy and members.
 * @run main LdapReferralExceptionTest
 */


public class LdapReferralExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapReferralException");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("javax.naming.ReferralException".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addSuppressed"), "has method addSuppressed");
        check(names.contains("appendRemainingComponent"), "has method appendRemainingComponent");
        check(names.contains("appendRemainingName"), "has method appendRemainingName");        System.out.println("PASS (" + passed + " assertions)");
    }
}

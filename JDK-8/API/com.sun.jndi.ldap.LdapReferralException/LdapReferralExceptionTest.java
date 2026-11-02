/*
 * @test
 * @summary com.sun.jndi.ldap.LdapReferralException conformance: hierarchy and declared members via reflection.
 * @run main LdapReferralExceptionTest
 */

public class LdapReferralExceptionTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jndi.ldap.LdapReferralException", false, null);
        check(c.getName().equals("com.sun.jndi.ldap.LdapReferralException"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.naming.ldap.LdapReferralException"), "superclass javax.naming.ldap.LdapReferralException");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("appendUnprocessedReferrals"), "declares appendUnprocessedReferrals");
        check(ms.contains("dump"), "declares dump");
        check(ms.contains("dumpState"), "declares dumpState");
        check(ms.contains("getNamingException"), "declares getNamingException");
        check(ms.contains("getNewRdn"), "declares getNewRdn");
        check(ms.contains("getNextReferral"), "declares getNextReferral");
        check(ms.contains("getReferralContext"), "declares getReferralContext");
        check(ms.contains("getReferralInfo"), "declares getReferralInfo");
        check(ms.contains("hasMoreReferralExceptions"), "declares hasMoreReferralExceptions");
        check(ms.contains("hasMoreReferrals"), "declares hasMoreReferrals");
        check(ms.contains("retryReferral"), "declares retryReferral");
        check(ms.contains("setHopCount"), "declares setHopCount");
        check(ms.contains("setNameResolved"), "declares setNameResolved");
        check(ms.contains("setNamingException"), "declares setNamingException");
        check(ms.contains("setNewRdn"), "declares setNewRdn");
        check(ms.contains("setReferralInfo"), "declares setReferralInfo");
        check(ms.contains("skipReferral"), "declares skipReferral");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

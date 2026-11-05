/*
 * @test
 * @summary javax.naming.ldap.LdapReferralException conformance: abstract class hierarchy and members.
 * @run main LdapReferralExceptionTest
 */



import javax.naming.CommunicationException;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ldap.Control;
import javax.naming.ldap.LdapReferralException;
import javax.naming.ReferralException;
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
        check(names.contains("appendRemainingName"), "has method appendRemainingName");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dLreSurface();
        dLreBehavior();
        dLreSerialization();
        System.out.println("deep: LdapReferralException contracts OK");
    }

    static class DeepLre extends LdapReferralException {
        int skips, retries;
        DeepLre(String msg) { super(msg); }
        DeepLre() { super(); }
        public javax.naming.Context getReferralContext() { return null; }
        public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> env) { return null; }
        public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> env,
                Control[] reqCtls) { return null; }
        public Object getReferralInfo() { return "ldap://referral/target"; }
        public boolean skipReferral() { skips++; return skips % 2 == 1; }
        public void retryReferral() { retries++; }
    }

    static void dLreSurface() throws Exception {
        check(javax.naming.ReferralException.class.isAssignableFrom(LdapReferralException.class),
                "extends ReferralException");
        check(java.lang.reflect.Modifier.isAbstract(LdapReferralException.class.getModifiers()),
                "abstract class");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : LdapReferralException.class.getMethods())
            names.add(m.getName());
        check(names.contains("getReferralContext"), "getReferralContext present");
        check(names.contains("getReferralInfo"), "getReferralInfo present");
        check(names.contains("skipReferral"), "skipReferral present");
        check(names.contains("retryReferral"), "retryReferral present");
    }

    static void dLreBehavior() throws Exception {
        DeepLre e = new DeepLre("referral encountered");
        check("referral encountered".equals(e.getMessage()), "message ctor");
        check(e.getReferralInfo().equals("ldap://referral/target"), "referral info");
        check(e.getReferralContext() == null, "referral context (stubbed)");
        check(e.skipReferral(), "skipReferral toggles true first");
        check(!e.skipReferral(), "skipReferral toggles false next");
        check(e.skips == 2, "skip counter");
        e.retryReferral();
        check(e.retries == 1, "retry counter");
        // no-message ctor leaves a null message
        check(new DeepLre().getMessage() == null, "no-arg ctor null message");
        // it is a NamingException with full chaining support
        javax.naming.NamingException cause = new javax.naming.CommunicationException("down");
        DeepLre chained = new DeepLre("wrapper");
        chained.initCause(cause);
        check(chained.getCause() == cause, "cause chaining works");
        check(chained.getRootCause() == cause, "root cause mirrors cause");
        // remaining-name machinery inherited
        chained.setRemainingName(new CompositeName("a/b"));
        check(chained.getRemainingName().size() == 2, "remaining name set");
    }

    static void dLreSerialization() throws Exception {
        DeepLre e = new DeepLre("serial");
        e.setRemainingName(new CompositeName("x/y"));
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        try (java.io.ObjectOutputStream oos = new java.io.ObjectOutputStream(bos)) { oos.writeObject(e); }
        try (java.io.ObjectInputStream ois = new java.io.ObjectInputStream(
                new java.io.ByteArrayInputStream(bos.toByteArray()))) {
            DeepLre back = (DeepLre) ois.readObject();
            check("serial".equals(back.getMessage()), "round-trip message");
            check(back.getRemainingName().size() == 2, "round-trip remaining name");
            check(back.getReferralInfo().equals("ldap://referral/target"), "round-trip referral info");
        }
    }

}

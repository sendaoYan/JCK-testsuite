/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotification conformance: interface hierarchy and members.
 * @run main UnsolicitedNotificationTest
 */



import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.UnsolicitedNotification;
public class UnsolicitedNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotification");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> expIfs = new java.util.HashSet<String>();
        expIfs.add("javax.naming.ldap.ExtendedResponse");
        expIfs.add("javax.naming.ldap.HasControls");
        java.util.Set<String> actIfs = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) actIfs.add(i.getName());
        check(actIfs.equals(expIfs), "direct interfaces");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getControls"), "has method getControls");
        check(names.contains("getEncodedValue"), "has method getEncodedValue");
        check(names.contains("getException"), "has method getException");        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        dUnSurface();
        dUnUse();
        System.out.println("deep: UnsolicitedNotification contracts OK");
    }

    static void dUnSurface() throws Exception {
        check(ExtendedResponse.class.isAssignableFrom(UnsolicitedNotification.class),
                "extends ExtendedResponse");
        check(HasControls.class.isAssignableFrom(UnsolicitedNotification.class),
                "extends HasControls");
        java.util.Set<String> names = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : UnsolicitedNotification.class.getMethods())
            names.add(m.getName());
        check(names.contains("getReferrals"), "declares getReferrals");
        check(names.contains("getException"), "declares getException");
        check(names.contains("getID"), "inherits getID");
        check(names.contains("getEncodedValue"), "inherits getEncodedValue");
        check(names.contains("getControls"), "inherits getControls");
    }

    static class DeepNotice implements UnsolicitedNotification {
        final String[] referrals;
        final NamingException exc;
        final Control[] controls;
        DeepNotice(String[] referrals, NamingException exc, Control[] controls) {
            this.referrals = referrals; this.exc = exc; this.controls = controls;
        }
        public String getID() { return "1.3.6.1.4.1.1466.20036"; }
        public byte[] getEncodedValue() { return new byte[0]; }
        public String[] getReferrals() { return referrals; }
        public NamingException getException() { return exc; }
        public Control[] getControls() { return controls; }
    }

    static void dUnUse() throws Exception {
        javax.naming.LimitExceededException limit = new javax.naming.LimitExceededException("busy");
        DeepNotice n = new DeepNotice(new String[]{"ldap://a", "ldap://b"}, limit,
                new Control[]{new BasicControl("1.1")});
        check(n.getReferrals().length == 2, "referral list");
        check(n.getReferrals()[0].equals("ldap://a"), "referral content");
        check(n.getException() == limit, "exception identity");
        check(n.getControls().length == 1, "controls present");
        check(n.getID() != null, "id present");
        // null referrals / null exception are representable
        DeepNotice empty = new DeepNotice(null, null, null);
        check(empty.getReferrals() == null, "null referrals");
        check(empty.getException() == null, "null exception");
        check(empty.getControls() == null, "no controls");
    }

}

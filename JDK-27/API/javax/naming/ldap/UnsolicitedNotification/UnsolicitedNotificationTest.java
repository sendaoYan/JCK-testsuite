/*
 * @test
 * @summary javax.naming.ldap.UnsolicitedNotification conformance: white-box structure + black-box behavior.
 * @run main UnsolicitedNotificationTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;
import javax.naming.NamingException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
import javax.naming.ldap.ExtendedResponse;
import javax.naming.ldap.HasControls;
import javax.naming.ldap.UnsolicitedNotification;
import java.util.Arrays;

public class UnsolicitedNotificationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.UnsolicitedNotification");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.isInterface(), "is interface");
        check(ExtendedResponse.class.isAssignableFrom(c), "extends ExtendedResponse");
        check(HasControls.class.isAssignableFrom(c), "extends HasControls");
        check(hasMethod(c, "getReferrals"), "getReferrals");
        check(hasMethod(c, "getException"), "getException");
    }

    static void testBlackBox() throws Exception {
        final NamingException ex = new NamingException("notify");
        UnsolicitedNotification n = new UnsolicitedNotification() {
            public String[] getReferrals() { return new String[]{"ldap://a"}; }
            public NamingException getException() { return ex; }
            public String getID() { return "1.2.3"; }
            public byte[] getEncodedValue() { return null; }
            public Control[] getControls() { return new Control[]{new BasicControl("c1")}; }
        };
        check(Arrays.equals(n.getReferrals(), new String[]{"ldap://a"}), "getReferrals");
        check(n.getException() == ex, "getException");
        check("1.2.3".equals(n.getID()), "getID");
        check(n.getEncodedValue() == null, "getEncodedValue");
        check(n.getControls().length == 1, "getControls");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepContracts();
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

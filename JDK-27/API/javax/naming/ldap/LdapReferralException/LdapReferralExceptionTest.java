/*
 * @test
 * @summary javax.naming.ldap.LdapReferralException conformance: white-box structure + black-box behavior.
 * @run main LdapReferralExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.naming.*;
import javax.naming.ldap.*;

public class LdapReferralExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.naming.ldap.LdapReferralException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == ReferralException.class, "extends ReferralException");
        check(hasMethod(c, "getReferralContext"), "getReferralContext");
    }

    static void testBlackBox() throws Exception {
        LdapReferralException e = new LdapReferralException() {
            public Object getReferralInfo() { return "referral-info"; }
            public Context getReferralContext() { return null; }
            public Context getReferralContext(Hashtable<?, ?> env) { return null; }
            public Context getReferralContext(Hashtable<?, ?> env, Control[] reqCtls) { return null; }
            public boolean skipReferral() { return true; }
            public void retryReferral() { }
        };
        check(e instanceof NamingException, "instanceof NamingException");
        check(e instanceof ReferralException, "instanceof ReferralException");
        check("referral-info".equals(e.getReferralInfo()), "getReferralInfo");
        check(e.getReferralContext() == null, "getReferralContext()");
        check(e.getReferralContext(new Hashtable<String, Object>()) == null, "getReferralContext(env)");
        check(e.getReferralContext(new Hashtable<String, Object>(), null) == null, "getReferralContext(env,controls)");
        check(e.skipReferral(), "skipReferral");
        e.retryReferral();
        check(true, "retryReferral callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

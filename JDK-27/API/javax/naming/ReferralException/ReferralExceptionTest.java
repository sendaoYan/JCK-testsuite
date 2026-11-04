/*
 * @test
 * @summary javax.naming.ReferralException conformance: white-box structure + black-box behavior.
 * @run main ReferralExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Hashtable;
import javax.naming.CompositeName;
import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import javax.naming.ServiceUnavailableException;

public class ReferralExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static final String REFERRAL_URL = "ldap://refhost:389/ou=remote,dc=example,dc=com";

    /** Minimal concrete referral exception following the documented contract. */
    static class TestReferralException extends ReferralException {
        boolean retried;
        TestReferralException(String msg) { super(msg); }
        public Object getReferralInfo() { return REFERRAL_URL; }
        public Context getReferralContext() throws NamingException { throw new ServiceUnavailableException("no network in test"); }
        public Context getReferralContext(Hashtable<?,?> env) throws NamingException { throw new ServiceUnavailableException("no network in test"); }
        public boolean skipReferral() { return true; }
        public void retryReferral() { retried = true; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = ReferralException.class;
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(c.getSuperclass() == NamingException.class, "extends NamingException");
        for (String m : new String[] {"getReferralInfo", "getReferralContext", "getReferralContext", "skipReferral", "retryReferral"}) {
            boolean found = false;
            for (Method mm : c.getDeclaredMethods()) if (mm.getName().equals(m)) { found = true; break; }
            check(found, "declares " + m);
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        TestReferralException re = new TestReferralException("referral received");
        check(re.getMessage().equals("referral received"), "message");
        check(re.getReferralInfo().equals(REFERRAL_URL), "referral info");
        check(re.skipReferral(), "skip semantics");
        re.retryReferral();
        check(re.retried, "retry hook invoked");

        try { re.getReferralContext(); throw new AssertionError("no throw"); }
        catch (ServiceUnavailableException e) { check(true, "getReferralContext may fail with NamingException"); }
        try { re.getReferralContext(new Hashtable<Object, Object>()); throw new AssertionError("no throw"); }
        catch (ServiceUnavailableException e) { check(true, "env variant same contract"); }

        // documented client loop: catch referral, read info, skip, continue
        int skipped = 0;
        try { throw re; } catch (ReferralException r) { if (r.skipReferral()) skipped++; }
        check(skipped == 1, "client loop pattern");
        re.setRemainingName(new CompositeName("ou=x"));
        check(re.getRemainingName().toString().equals("ou=x"), "inherited state plumbing");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

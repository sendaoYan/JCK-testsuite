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
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;

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
        testDeepContracts();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deep contract coverage (appended) --------------------------------

    static void testDeepContracts() throws Exception {
        Class<?> c = Class.forName("javax.naming.ReferralException");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "still abstract");
        java.util.Set<String> abs = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (java.lang.reflect.Modifier.isAbstract(m.getModifiers())) abs.add(m.getName());
        check(abs.contains("getReferralInfo"), "abstract getReferralInfo");
        check(abs.contains("skipReferral"), "abstract skipReferral");
        check(abs.contains("retryReferral"), "abstract retryReferral");
        check(abs.size() >= 3, "abstract surface: " + abs);
        dReInstance();
        System.out.println("deep: ReferralException contracts OK");
    }

    static class DeepReferral extends javax.naming.ReferralException {
        int skips, retries;
        java.util.Hashtable<?, ?> lastEnv;
        javax.naming.ldap.Control[] lastCtls;
        DeepReferral(String m) { super(m); }
        public javax.naming.Context getReferralContext() { return null; }
        public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> env) {
            lastEnv = env;
            return null;
        }
        public javax.naming.Context getReferralContext(java.util.Hashtable<?, ?> env,
                javax.naming.ldap.Control[] reqCtls) {
            lastEnv = env;
            lastCtls = reqCtls;
            return null;
        }
        public Object getReferralInfo() { return "ldap://deep/ref"; }
        public boolean skipReferral() { skips++; return true; }
        public void retryReferral() { retries++; }
    }

    static void dReInstance() throws Exception {
        DeepReferral r = new DeepReferral("refer-on");
        check("refer-on".equals(r.getMessage()), "message via protected constructor");
        check(r instanceof javax.naming.NamingException, "is a NamingException");
        try { throw r; }
        catch (javax.naming.NamingException x) {
            check(x == r, "caught as NamingException");
        }
        // referral info and continuation controls
        check("ldap://deep/ref".equals(r.getReferralInfo()), "getReferralInfo");
        javax.naming.ldap.Control c1 = new javax.naming.ldap.BasicControl("1.2.3");
        javax.naming.ldap.Control[] arr = { c1 };
        java.util.Hashtable<String, String> env = new java.util.Hashtable<String, String>();
        env.put("deep", "v");
        r.getReferralContext(env, arr);
        check(r.lastEnv == env && r.lastCtls == arr, "env/controls overload dispatches");
        r.getReferralContext(env);
        check(r.lastCtls == arr, "2-arg overload leaves earlier controls untouched");
        r.getReferralContext();
        check(r.getReferralContext() == null, "no-arg overload");
        // skip/retry drive the client loop
        check(r.skipReferral() == true, "skipReferral true");
        r.retryReferral();
        check(r.skips == 1 && r.retries == 1, "calls recorded");
        // NamingException surface still applies
        r.appendRemainingComponent("a");
        check("a".equals(r.getRemainingName().toString()), "remaining-name chain available");
        check(r.toString().contains("ReferralException"), "toString names the class");
    }

}

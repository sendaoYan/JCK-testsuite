/*
 * @test
 * @summary javax.naming.ReferralException conformance: abstract class hierarchy and members.
 * @run main ReferralExceptionTest
 */



import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.ReferralException;
import javax.naming.ldap.BasicControl;
import javax.naming.ldap.Control;
public class ReferralExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.naming.ReferralException");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(!c.isInterface(), "not interface");
        check("javax.naming.NamingException".equals(c.getSuperclass().getName()), "superclass");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addSuppressed"), "has method addSuppressed");
        check(names.contains("appendRemainingComponent"), "has method appendRemainingComponent");
        check(names.contains("appendRemainingName"), "has method appendRemainingName");        testDeepContracts();
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

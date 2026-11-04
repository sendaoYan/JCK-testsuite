/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerFactory conformance: full contract
 *          coverage. createMBeanServer default/named domain, newMBeanServer
 *          (untracked), findMBeanServer(null/agentId), releaseMBeanServer
 *          single/double (IllegalArgumentException), getClassLoaderRepository
 *          (null -> NPE), and delegate presence via getMBeanCount.
 * @run main MBeanServerFactoryTest
 */

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;

public class MBeanServerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface ThrowingRunnable { void run() throws Throwable; }

    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    // ---- original coverage (kept) ------------------------------------------

    static void testBasic() {
        MBeanServer s = MBeanServerFactory.createMBeanServer();
        check(s != null, "createMBeanServer");
        check(MBeanServerFactory.findMBeanServer(null).contains(s), "findMBeanServer");
        MBeanServerFactory.releaseMBeanServer(s);
        MBeanServer n = MBeanServerFactory.newMBeanServer();
        check(n != null, "newMBeanServer");
    }

    static void testDomains() {
        MBeanServer def = MBeanServerFactory.createMBeanServer();
        check("DefaultDomain".equals(def.getDefaultDomain()), "default domain");
        check(def.getMBeanCount() >= 1, "delegate MBean present");
        MBeanServerFactory.releaseMBeanServer(def);

        MBeanServer named = MBeanServerFactory.createMBeanServer("MyDomain");
        check("MyDomain".equals(named.getDefaultDomain()), "named domain");
        MBeanServerFactory.releaseMBeanServer(named);

        MBeanServer untracked = MBeanServerFactory.newMBeanServer();
        check("DefaultDomain".equals(untracked.getDefaultDomain()), "newMBeanServer default domain");
        check(!MBeanServerFactory.findMBeanServer(null).contains(untracked), "newMBeanServer untracked");
    }

    static void testReleaseSemantics() {
        MBeanServer s = MBeanServerFactory.createMBeanServer();
        MBeanServerFactory.releaseMBeanServer(s);
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { MBeanServerFactory.releaseMBeanServer(s); } },
                "double release -> IllegalArgumentException");

        final MBeanServer untracked = MBeanServerFactory.newMBeanServer();
        checkThrows(IllegalArgumentException.class,
                new ThrowingRunnable() { public void run() { MBeanServerFactory.releaseMBeanServer(untracked); } },
                "release untracked -> IllegalArgumentException");
    }

    static void testFindAndClassLoaderRepository() {
        MBeanServer a = MBeanServerFactory.createMBeanServer("DomainA");
        MBeanServer b = MBeanServerFactory.createMBeanServer("DomainB");
        check(MBeanServerFactory.findMBeanServer(null).contains(a), "find(null) contains a");
        check(MBeanServerFactory.findMBeanServer(null).contains(b), "find(null) contains b");
        // findMBeanServer(agentId) matches by MBeanServerId (not default domain)
        java.util.ArrayList<MBeanServer> all = MBeanServerFactory.findMBeanServer(null);
        check(all.size() >= 2, "find(null) size >= 2");
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() { MBeanServerFactory.getClassLoaderRepository(null); } },
                "getClassLoaderRepository(null)");
        MBeanServerFactory.releaseMBeanServer(a);
        MBeanServerFactory.releaseMBeanServer(b);
    }

    public static void main(String[] args) throws Exception {
        testBasic();
        testDomains();
        testReleaseSemantics();
        testFindAndClassLoaderRepository();
        deepUntrackedIndependence();
        deepTrackedLifecycle();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): closed-loop type ====

    public static interface TickerMBean { int getTicks(); }
    public static class Ticker implements TickerMBean { public int getTicks() { return 1; } }

    // ==== appended deepening (2026-11): untracked overloads + isolation ====

    static void deepUntrackedIndependence() throws Exception {
        javax.management.MBeanServer a = javax.management.MBeanServerFactory.newMBeanServer();
        javax.management.MBeanServer b = javax.management.MBeanServerFactory.newMBeanServer();
        check(a != b, "each newMBeanServer() is an independent server");
        check(!javax.management.MBeanServerFactory.findMBeanServer(null).contains(a), "newMBeanServer() stays untracked");
        check(!javax.management.MBeanServerFactory.findMBeanServer(null).contains(b), "second newMBeanServer() untracked");
        javax.management.MBeanServer named = javax.management.MBeanServerFactory.newMBeanServer("jck.fac");
        check("jck.fac".equals(named.getDefaultDomain()), "newMBeanServer(domain) sets the default domain");
        check(!javax.management.MBeanServerFactory.findMBeanServer(null).contains(named), "named overload is untracked too");
        javax.management.MBeanServer nullDom = javax.management.MBeanServerFactory.newMBeanServer(null);
        check("DefaultDomain".equals(nullDom.getDefaultDomain()), "newMBeanServer(null) falls back to DefaultDomain");
        // server isolation: an MBean registered in one server is invisible in others
        javax.management.ObjectName on = new javax.management.ObjectName("jck.fac:type=Ticker");
        a.registerMBean(new Ticker(), on);
        check(a.isRegistered(on) && !b.isRegistered(on) && !named.isRegistered(on), "registration is per-server");
        check(a.getMBeanCount().intValue() == 2 && b.getMBeanCount().intValue() == 1,
                "getMBeanCount is per-server (delegate + registered)");
    }

    // ==== appended deepening (2026-11): tracking by agent id + release lifecycle ====

    static void deepTrackedLifecycle() throws Exception {
        javax.management.MBeanServer s1 = null;
        javax.management.MBeanServer s2 = null;
        try {
            s1 = javax.management.MBeanServerFactory.createMBeanServer();
            s2 = javax.management.MBeanServerFactory.createMBeanServer("jck.fac2");
            check(javax.management.MBeanServerFactory.findMBeanServer(null).contains(s1), "find(null) contains first");
            check(javax.management.MBeanServerFactory.findMBeanServer(null).contains(s2), "find(null) contains second");
            check("DefaultDomain".equals(s1.getDefaultDomain()), "createMBeanServer() default domain");
            check("jck.fac2".equals(s2.getDefaultDomain()), "createMBeanServer(domain)");
            String id1 = (String) s1.getAttribute(javax.management.MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId");
            String id2 = (String) s2.getAttribute(javax.management.MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId");
            check(id1.length() > 0 && !id1.equals(id2), "each tracked server has a distinct MBeanServerId");
            check(javax.management.MBeanServerFactory.findMBeanServer(id1).contains(s1), "find by own agent id");
            check(!javax.management.MBeanServerFactory.findMBeanServer(id1).contains(s2), "find by agent id excludes others");
            check(javax.management.MBeanServerFactory.findMBeanServer("jck-no-such-agent-id").isEmpty(), "find(unknown id) is empty");
            check(javax.management.MBeanServerFactory.findMBeanServer(null) instanceof java.util.ArrayList,
                    "findMBeanServer returns ArrayList");
            check(javax.management.MBeanServerFactory.getClassLoaderRepository(s2) != null,
                    "getClassLoaderRepository non-null");
            javax.management.MBeanServerFactory.releaseMBeanServer(s1);
            s1 = null;
            check(javax.management.MBeanServerFactory.findMBeanServer(id1).isEmpty(), "find(own id) empty after release");
        } finally {
            if (s1 != null) {
                try { javax.management.MBeanServerFactory.releaseMBeanServer(s1); } catch (RuntimeException ignore) { }
            }
            if (s2 != null) {
                try { javax.management.MBeanServerFactory.releaseMBeanServer(s2); } catch (RuntimeException ignore) { }
            }
        }
        // release of servers that were never tracked is rejected
        final javax.management.MBeanServer untracked = javax.management.MBeanServerFactory.newMBeanServer();
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { javax.management.MBeanServerFactory.releaseMBeanServer(untracked); }
        }, "releaseMBeanServer(untracked) -> IllegalArgumentException");
        // double release of a tracked server is rejected
        javax.management.MBeanServer dup = javax.management.MBeanServerFactory.createMBeanServer("jck.fac3");
        javax.management.MBeanServerFactory.releaseMBeanServer(dup);
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { javax.management.MBeanServerFactory.releaseMBeanServer(dup); }
        }, "double releaseMBeanServer -> IllegalArgumentException");
        // null server argument is rejected like an untracked server
        checkThrows(IllegalArgumentException.class, new ThrowingRunnable() {
            public void run() { javax.management.MBeanServerFactory.releaseMBeanServer(null); }
        }, "releaseMBeanServer(null) -> IllegalArgumentException");
    }
}


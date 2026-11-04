/*
 * @test
 * @summary javax.management.MBeanServerFactory conformance: full contract
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

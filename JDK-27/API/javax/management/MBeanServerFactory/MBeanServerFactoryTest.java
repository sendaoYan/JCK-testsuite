/*
 * @test
 * @summary [deepened 2026-11: local server closed loop, exception matrices] javax.management.MBeanServerFactory conformance: white-box structure + black-box behavior.
 * @run main MBeanServerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import javax.management.*;

public class MBeanServerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    public interface ThingMBean {
        int getCount();
    }
    public static class Thing implements ThingMBean {
        public int getCount() { return 7; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.management.MBeanServerFactory");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public concrete class");
        check(hasMethod(c, "createMBeanServer"), "has createMBeanServer");
        check(hasMethod(c, "newMBeanServer"), "has newMBeanServer");
        check(hasMethod(c, "findMBeanServer"), "has findMBeanServer");
        check(hasMethod(c, "releaseMBeanServer"), "has releaseMBeanServer");
        check(hasMethod(c, "getClassLoaderRepository"), "has getClassLoaderRepository");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // newMBeanServer() is untracked; safe to use directly.
        MBeanServer fresh = MBeanServerFactory.newMBeanServer();
        check(fresh.getMBeanCount() == 1, "new server has only the delegate MBean");
        check(fresh.getDefaultDomain() != null, "default domain non-null");

        ObjectName on = new ObjectName("domain:type=Thing");
        fresh.registerMBean(new StandardMBean(new Thing(), ThingMBean.class), on);
        check(fresh.getMBeanCount() == 2, "registered one MBean");
        check(fresh.isRegistered(on), "isRegistered");
        fresh.unregisterMBean(on);
        check(fresh.getMBeanCount() == 1, "unregistered back to delegate only");

        // newMBeanServer(String domain) sets the default domain
        MBeanServer domainServer = MBeanServerFactory.newMBeanServer("my.domain");
        check("my.domain".equals(domainServer.getDefaultDomain()), "newMBeanServer(domain) default domain");

        // createMBeanServer tracks servers, findable via findMBeanServer, releasable.
        String agentDomain = "jck.agent";
        MBeanServer tracked = MBeanServerFactory.createMBeanServer(agentDomain);
        check(agentDomain.equals(tracked.getDefaultDomain()), "createMBeanServer(domain) default domain");

        // findMBeanServer(null) returns all tracked servers
        ArrayList<MBeanServer> all = MBeanServerFactory.findMBeanServer(null);
        check(all.contains(tracked), "findMBeanServer(null) includes created server");

        // findMBeanServer(agentId) matches the delegate MBeanServerId
        String id = (String) tracked.getAttribute(MBeanServerDelegate.DELEGATE_NAME, "MBeanServerId");
        ArrayList<MBeanServer> byId = MBeanServerFactory.findMBeanServer(id);
        check(byId.contains(tracked), "findMBeanServer(MBeanServerId) finds created server");

        // class loader repository available
        check(MBeanServerFactory.getClassLoaderRepository(tracked) != null, "getClassLoaderRepository non-null");

        MBeanServerFactory.releaseMBeanServer(tracked);
        check(!MBeanServerFactory.findMBeanServer(null).contains(tracked), "released server no longer tracked");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepUntrackedIndependence();
        deepTrackedLifecycle();

        System.out.println("PASS (" + passed + " assertions)");
    }

    // ==== appended deepening (2026-11): shared helpers ====

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


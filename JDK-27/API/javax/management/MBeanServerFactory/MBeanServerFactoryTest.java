/*
 * @test
 * @summary javax.management.MBeanServerFactory conformance: white-box structure + black-box behavior.
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerFactory conformance: white-box structure + black-box behavior.
 * @run main JMXConnectorServerFactoryTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.Method;
import java.lang.management.ManagementFactory;

import javax.management.MBeanServer;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXConnectorServerFactory;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnectorServer;

public class JMXConnectorServerFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static boolean has(Method[] mm, String n) {
        for (Method m : mm) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerFactory");
        check(!c.isInterface(), "concrete class");
        check(c.getConstructors().length == 0, "no public ctor");
        Method[] mm = c.getMethods();
        check(has(mm, "newJMXConnectorServer"), "newJMXConnectorServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        check(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES.equals("jmx.remote.protocol.provider.pkgs"), "PROTOCOL_PROVIDER_PACKAGES");
        check(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER.equals("jmx.remote.default.class.loader"), "DEFAULT_CLASS_LOADER");
        check(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME.equals("jmx.remote.default.class.loader.name"), "DEFAULT_CLASS_LOADER_NAME");
        check(JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER.equals("jmx.remote.protocol.provider.class.loader"), "PROTOCOL_PROVIDER_CLASS_LOADER");

        // newJMXConnectorServer creates an unstarted server without exporting RMI.
        JMXServiceURL rmiUrl = new JMXServiceURL("service:jmx:rmi:///jndi/rmi://localhost:9999/server");
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(rmiUrl, null, mbs);
        check(cs != null, "server non-null");
        check(cs instanceof RMIConnectorServer, "rmi protocol yields RMIConnectorServer");
        check(!cs.isActive(), "not active");
        check(cs.getMBeanServer() == mbs, "mbean server preserved");
        check(cs.getAddress() == null, "inactive server has null address");
        check(cs.getConnectionIds().length == 0, "no connections yet");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): constants, env matrix, protocol errors, loopback ====
    static void testDeepened() throws Exception {
        check("jmx.remote.protocol.provider.pkgs".equals(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES), "d-ppp constant");
        check("jmx.remote.default.class.loader".equals(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER), "d-dcl constant");
        check("jmx.remote.default.class.loader.name".equals(JMXConnectorServerFactory.DEFAULT_CLASS_LOADER_NAME), "d-dcln constant");
        check("jmx.remote.protocol.provider.class.loader".equals(JMXConnectorServerFactory.PROTOCOL_PROVIDER_CLASS_LOADER), "d-ppcl constant");

        MBeanServer server = MBeanServerFactory.newMBeanServer();

        // Unknown protocol: MalformedURLException with a stable message (both JDKs).
        try {
            JMXConnectorServerFactory.newJMXConnectorServer(new JMXServiceURL("service:jmx:bogus://x"), null, server);
            check(false, "d-bogus protocol should throw");
        } catch (java.net.MalformedURLException e) {
            check("Unsupported protocol: bogus".equals(e.getMessage()), "d-bogus message: " + e.getMessage());
        }
        // A bogus provider package does not shadow the built-in rmi provider (resolved via ServiceLoader):
        // creation still succeeds for rmi (recorded behavior, identical on both JDKs).
        java.util.Map<String, Object> badPkg = new java.util.HashMap<String, Object>();
        badPkg.put(JMXConnectorServerFactory.PROTOCOL_PROVIDER_PACKAGES, "no.such.pkg");
        JMXConnectorServer ignored = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), badPkg, server);
        check(ignored != null && !ignored.isActive(), "d-bad provider pkg still yields rmi server");
        check(ignored.getMBeanServer() == server, "d-bad provider pkg server wired");

        // env null, empty map, and unknown keys all behave the same: a working server is built.
        JMXConnectorServer a = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        java.util.Map<String, Object> empty = new java.util.HashMap<String, Object>();
        JMXConnectorServer b = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), empty, server);
        java.util.Map<String, Object> junk = new java.util.HashMap<String, Object>();
        junk.put("jck.unknown.key", "ignored");
        JMXConnectorServer c = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), junk, server);
        check(a instanceof JMXConnectorServer && b instanceof JMXConnectorServer && c instanceof JMXConnectorServer,
                "d-all env forms produce JMXConnectorServer");
        check(a.getMBeanServer() == server && b.getMBeanServer() == server && c.getMBeanServer() == server,
                "d-all env forms wire server");
        check(javax.management.remote.JMXConnectorServerMBean.class.isAssignableFrom(a.getClass()),
                "d-product implements JMXConnectorServerMBean");
        check(javax.management.remote.JMXAddressable.class.isAssignableFrom(a.getClass()),
                "d-product implements JMXAddressable");

        // One of them runs the full loopback: start, connect, remote read, close, stop.
        ObjectName on = new ObjectName("jck.dsf:type=Counter");
        server.registerMBean(new RemCounter(), on);
        a.start();
        try {
            JMXConnector conn = JMXConnectorFactory.connect(a.getAddress(), null);
            try {
                check(((Integer) conn.getMBeanServerConnection().getAttribute(on, "Count")).intValue() == 0,
                        "d-loopback remote getAttribute");
            } finally {
                conn.close();
            }
        } finally {
            a.stop();
        }
        // Unattached form: creation succeeds with a null MBeanServer (start is refused elsewhere).
        JMXConnectorServer unattached = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, null);
        check(unattached.getMBeanServer() == null, "d-null server accepted at creation");
    }

    // ---- deepened fixture: nested standard MBean (value source for the loopback) ----
    public static interface RemCounterMBean {
        int getCount();
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
    }

}

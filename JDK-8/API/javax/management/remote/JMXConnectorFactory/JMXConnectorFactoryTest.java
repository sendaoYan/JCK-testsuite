/*
 * @test
 * @summary javax.management.remote.JMXConnectorFactory conformance: concrete class reflection.
 * @run main JMXConnectorFactoryTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXConnectorFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorFactory");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "newJMXConnector"), "declares newJMXConnector");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): constants, env matrix, failure paths, loopback connect ====
    static void testDeepened() throws Exception {
        check("jmx.remote.protocol.provider.pkgs".equals(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES), "d-ppp constant");
        check("jmx.remote.default.class.loader".equals(JMXConnectorFactory.DEFAULT_CLASS_LOADER), "d-dcl constant");
        check("jmx.remote.protocol.provider.class.loader".equals(JMXConnectorFactory.PROTOCOL_PROVIDER_CLASS_LOADER), "d-ppcl constant");

        // connect(null) rejects the null URL with a NullPointerException.
        try {
            JMXConnectorFactory.connect(null);
            check(false, "d-connect null url should throw");
        } catch (NullPointerException e) {
            check("Null JMXServiceURL".equals(e.getMessage()), "d-null url message: " + e.getMessage());
        }
        // Unknown protocol: MalformedURLException with a stable message (both JDKs).
        try {
            JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:bogus://x"));
            check(false, "d-bogus protocol should throw");
        } catch (java.net.MalformedURLException e) {
            check("Unsupported protocol: bogus".equals(e.getMessage()), "d-bogus message: " + e.getMessage());
        }
        // A bogus provider package with the built-in rmi protocol still resolves the rmi provider
        // (via ServiceLoader); the failure comes from the URL shape instead.
        java.util.Map<String, Object> badPkg = new java.util.HashMap<String, Object>();
        badPkg.put(JMXConnectorFactory.PROTOCOL_PROVIDER_PACKAGES, "no.such.pkg");
        try {
            JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi://127.0.0.1"), badPkg);
            check(false, "d-bad provider pkg should throw");
        } catch (java.net.MalformedURLException e) {
            check(e.getMessage().startsWith("URL path must begin with /jndi/ or /stub/ or /ior/"),
                    "d-bad provider pkg message: " + e.getMessage());
        }
        // Dead port on the loopback: IOException whose cause chain bottoms out in ConnectException.
        try {
            JMXConnectorFactory.connect(new JMXServiceURL("service:jmx:rmi:///jndi/rmi://127.0.0.1:1/dead"));
            check(false, "d-dead port should throw");
        } catch (java.io.IOException e) {
            boolean sawConnect = false;
            for (Throwable c = e; c != null; c = c.getCause()) {
                if (c instanceof java.net.ConnectException) sawConnect = true;
            }
            check(sawConnect, "d-dead port cause chain has ConnectException");
        }

        // Real loopback: both connect forms (1-arg, 2-arg with null env) succeed and deliver the same server.
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.df:type=Counter");
        server.registerMBean(new RemCounter(), on);
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        cs.start();
        try {
            JMXConnector c1 = JMXConnectorFactory.connect(cs.getAddress());
            try {
                check(c1.getConnectionId() != null, "d-1arg connect id");
                check(((Integer) c1.getMBeanServerConnection().getAttribute(on, "Count")).intValue() == 0,
                        "d-1arg remote getAttribute");
            } finally { c1.close(); }
            JMXConnector c2 = JMXConnectorFactory.connect(cs.getAddress(), null);
            try {
                check(((Integer) c2.getMBeanServerConnection().invoke(on, "add",
                        new Object[]{ Integer.valueOf(5) }, new String[]{ "int" })).intValue() == 5,
                        "d-2arg null env remote invoke");
            } finally { c2.close(); }
            // newJMXConnector only builds; the connection id is unavailable before connect.
            JMXConnector unconnected = JMXConnectorFactory.newJMXConnector(cs.getAddress(), null);
            try {
                unconnected.getConnectionId();
                check(false, "d-newJMXConnector getConnectionId should throw");
            } catch (java.io.IOException e) {
                check("Not connected".equals(e.getMessage()), "d-newJMXConnector unconnected message: " + e.getMessage());
            } finally { unconnected.close(); }
        } finally {
            cs.stop();
        }
    }

    // ---- deepened fixture: nested standard MBean (value source for the loopback) ----
    public static interface RemCounterMBean {
        int getCount();
        int add(int x);
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
        public int add(int x) { count += x; return count; }
    }

}

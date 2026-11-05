/*
 * @test
 * @summary javax.management.remote.JMXConnectorServerMBean conformance: interface reflection.
 * @run main JMXConnectorServerMBeanTest
 */
import javax.management.*;
import javax.management.remote.*;


public class JMXConnectorServerMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXConnectorServerMBean");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(hasMethod(c, "getAddress"), "declares getAddress");
        check(hasMethod(c, "getAttributes"), "declares getAttributes");
        check(hasMethod(c, "getConnectionIds"), "declares getConnectionIds");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): drive a real RMI connector server through the management interface ====
    public static interface RemCounterMBean {
        int getCount();
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
    }

    static void testDeepened() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.dm:type=Counter");
        server.registerMBean(new RemCounter(), on);

        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        // The product connector server is usable purely through the management interface.
        JMXConnectorServerMBean view = cs;
        check(!view.isActive(), "d-pre-start inactive via interface");
        check(view.getAddress() == null, "d-pre-start address null");
        check(view.getConnectionIds().length == 0, "d-pre-start ids empty");
        check(view.getAttributes().isEmpty(), "d-getAttributes empty");

        view.start();
        try {
            check(view.isActive(), "d-started via interface");
            check(view.getAddress() != null && "rmi".equals(view.getAddress().getProtocol()), "d-address via interface");
            check(view.toJMXConnector(null) != null, "d-toJMXConnector via interface");

            JMXConnector conn = JMXConnectorFactory.connect(view.getAddress(), null);
            String id = conn.getConnectionId();
            check(java.util.Arrays.asList(view.getConnectionIds()).contains(id), "d-ids contain client via interface");
            check(((Integer) conn.getMBeanServerConnection().getAttribute(on, "Count")).intValue() == 0,
                    "d-remote read through interface-owned server");
            conn.close();
        } finally {
            view.stop();
        }
        check(!view.isActive(), "d-stopped via interface");

        // Registered as a real MBean: start/stop are invokable operations and Active/Address/
        // ConnectionIds are readable attributes, all introspected from the management interface.
        // A stopped connector server can never restart, so the MBean phase uses a fresh one.
        JMXConnectorServer registered = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        ObjectName csName = new ObjectName("jck.dm:type=ConnectorServer");
        server.registerMBean(registered, csName);
        try {
            check(Boolean.FALSE.equals(server.getAttribute(csName, "Active")), "d-MBean Active false pre-start");
            check(server.getAttribute(csName, "Address") == null, "d-MBean Address null pre-start");
            server.invoke(csName, "start", null, null);
            try {
                check(Boolean.TRUE.equals(server.getAttribute(csName, "Active")), "d-MBean Active true after start");
                JMXServiceURL addr = (JMXServiceURL) server.getAttribute(csName, "Address");
                check(addr != null && "rmi".equals(addr.getProtocol()), "d-MBean Address attribute");
                JMXConnector conn = JMXConnectorFactory.connect(addr, null);
                String id = conn.getConnectionId();
                String[] ids = (String[]) server.getAttribute(csName, "ConnectionIds");
                check(java.util.Arrays.asList(ids).contains(id), "d-MBean ConnectionIds contains client");
                conn.close();
                // start is idempotent even through the MBean server path.
                server.invoke(csName, "start", null, null);
                check(Boolean.TRUE.equals(server.getAttribute(csName, "Active")), "d-MBean start idempotent");
            } finally {
                server.invoke(csName, "stop", null, null);
            }
            check(Boolean.FALSE.equals(server.getAttribute(csName, "Active")), "d-MBean Active false after stop");
            // The exposed MBeanInfo carries the management interface attributes.
            MBeanInfo info = server.getMBeanInfo(csName);
            java.util.List<String> attrNames = new java.util.ArrayList<String>();
            for (MBeanAttributeInfo at : info.getAttributes()) attrNames.add(at.getName());
            check(attrNames.contains("Active") && attrNames.contains("Address") && attrNames.contains("ConnectionIds"),
                    "d-MBeanInfo attributes: " + attrNames);
        } finally {
            if (registered.isActive()) registered.stop();
            server.unregisterMBean(csName);
        }
    }

}

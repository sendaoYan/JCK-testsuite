/*
 * @test
 * @summary javax.management.remote.MBeanServerForwarder conformance: interface reflection.
 * @run main MBeanServerForwarderTest
 */
import javax.management.*;
import javax.management.remote.*;


public class MBeanServerForwarderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.MBeanServerForwarder");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("javax.management.MBeanServer").isAssignableFrom(c), "implements/extends javax.management.MBeanServer");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        check(hasMethod(c, "setMBeanServer"), "declares setMBeanServer");
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): chain semantics + real interception through an RMI loopback ====
    public static interface RemCounterMBean {
        int getCount();
        int add(int x);
    }
    public static class RemCounter implements RemCounterMBean {
        private int count;
        public int getCount() { return count; }
        public int add(int x) { count += x; return count; }
    }

    static MBeanServerForwarder deepNewForwarder(final java.util.List<String> calls) {
        final Object[] holder = new Object[1];
        return (MBeanServerForwarder) java.lang.reflect.Proxy.newProxyInstance(
                MBeanServerForwarderTest.class.getClassLoader(),
                new Class<?>[]{ MBeanServerForwarder.class },
                new java.lang.reflect.InvocationHandler() {
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("getMBeanServer")) return (MBeanServer) holder[0];
                        if (method.getName().equals("setMBeanServer")) {
                            if (args[0] == null) throw new IllegalArgumentException("Null mBeanServer");
                            if (holder[0] != null) throw new IllegalArgumentException("already set");
                            holder[0] = args[0];
                            return null;
                        }
                        if (holder[0] == null) throw new IllegalStateException("no MBeanServer set");
                        calls.add(method.getName());
                        try {
                            return method.invoke(holder[0], args);
                        } catch (java.lang.reflect.InvocationTargetException ite) {
                            throw ite.getCause();
                        }
                    }
                });
    }

    static void testDeepened() throws Exception {
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.fwd:type=Counter");
        server.registerMBean(new RemCounter(), on);
        // A fresh forwarder: getMBeanServer is null until setMBeanServer wires it.
        final java.util.List<String> calls = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
        MBeanServerForwarder fwd = deepNewForwarder(calls);
        check(fwd instanceof MBeanServer, "d-forwarder is an MBeanServer");
        check(fwd.getMBeanServer() == null, "d-getMBeanServer null before set");
        fwd.setMBeanServer(server);
        check(fwd.getMBeanServer() == server, "d-set/get round-trip");
        // The javadoc contract: null and double-set are refused (demonstrated by this implementation).
        try {
            fwd.setMBeanServer(null);
            check(false, "d-null setMBeanServer refused by impl");
        } catch (IllegalArgumentException e) {
            passed++;
        }
        try {
            fwd.setMBeanServer(server);
            check(false, "d-double setMBeanServer refused by impl");
        } catch (IllegalArgumentException e) {
            passed++;
        }

        // Real chain: setMBeanServerForwarder re-wires the connector server onto a fresh forwarder
        // (the first one is already wired and must not be re-set).
        final java.util.List<String> chainCalls = java.util.Collections.synchronizedList(new java.util.ArrayList<String>());
        MBeanServerForwarder chained = deepNewForwarder(chainCalls);
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        cs.setMBeanServerForwarder(chained);
        check(cs.getMBeanServer() == chained, "d-cs now fronts the forwarder");
        check(chained.getMBeanServer() == server, "d-forwarder fronts the original server");
        cs.start();
        try {
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            try {
                MBeanServerConnection mbsc = conn.getMBeanServerConnection();
                check(((Integer) mbsc.getAttribute(on, "Count")).intValue() == 0, "d-remote read via forwarder");
                check(((Integer) mbsc.invoke(on, "add", new Object[]{ Integer.valueOf(6) },
                        new String[]{ "int" })).intValue() == 6, "d-remote invoke via forwarder");
                check(chainCalls.contains("getAttribute") && chainCalls.contains("invoke"),
                        "d-forwarder saw getAttribute+invoke: " + chainCalls);
                // The forwarder is transparent: local and remote views agree.
                check(((Integer) server.getAttribute(on, "Count")).intValue() == 6, "d-transparent passthrough");
            } finally {
                conn.close();
            }
        } finally {
            cs.stop();
        }
    }

}

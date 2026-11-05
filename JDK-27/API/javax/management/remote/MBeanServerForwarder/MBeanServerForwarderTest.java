/*
 * @test
 * @summary javax.management.remote.MBeanServerForwarder conformance: white-box structure + black-box behavior.
 * @run main MBeanServerForwarderTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

import javax.management.MBeanServer;
import javax.management.remote.MBeanServerForwarder;

public class MBeanServerForwarderTest {
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
        Class<?> c = Class.forName("javax.management.remote.MBeanServerForwarder");
        check(c.isInterface(), "is interface");
        check(MBeanServer.class.isAssignableFrom(c), "extends MBeanServer");
        Method[] mm = c.getMethods();
        check(has(mm, "getMBeanServer"), "getMBeanServer");
        check(has(mm, "setMBeanServer"), "setMBeanServer");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // A dynamic proxy proves the two forwarder methods are callable
        // without writing a 30-method MBeanServer implementation by hand.
        final MBeanServer[] set = new MBeanServer[1];
        InvocationHandler handler = new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) {
                String name = method.getName();
                if (name.equals("getMBeanServer")) return set[0];
                if (name.equals("setMBeanServer")) { set[0] = (MBeanServer) args[0]; return null; }
                // Default return for other MBeanServer methods: null/false/0.
                Class<?> rt = method.getReturnType();
                if (!rt.isPrimitive()) return null;
                if (rt == boolean.class) return false;
                if (rt == char.class) return (char) 0;
                if (rt == byte.class) return (byte) 0;
                if (rt == short.class) return (short) 0;
                if (rt == int.class) return 0;
                if (rt == long.class) return 0L;
                if (rt == float.class) return 0f;
                if (rt == double.class) return 0d;
                return null;
            }
        };
        MBeanServerForwarder fwd = (MBeanServerForwarder) Proxy.newProxyInstance(
            MBeanServerForwarder.class.getClassLoader(),
            new Class<?>[]{ MBeanServerForwarder.class },
            handler);

        check(fwd.getMBeanServer() == null, "getMBeanServer initially null");
        MBeanServer delegate = java.lang.management.ManagementFactory.getPlatformMBeanServer();
        fwd.setMBeanServer(delegate);
        check(fwd.getMBeanServer() == delegate, "set/getMBeanServer round-trip");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
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

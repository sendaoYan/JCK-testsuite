/*
 * @test
 * @summary javax.management.remote.JMXServerErrorException conformance: white-box structure + black-box behavior.
 * @run main JMXServerErrorExceptionTest
 */
import javax.management.*;
import javax.management.remote.*;


import java.io.IOException;
import java.lang.reflect.Constructor;

import javax.management.remote.JMXServerErrorException;

public class JMXServerErrorExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.JMXServerErrorException");
        check(IOException.class.isAssignableFrom(c), "extends IOException");
        Constructor<?> k = c.getConstructor(String.class, Error.class);
        check(k != null, "(String,Error) ctor");
        check(c.getMethod("getCause") != null, "getCause");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Error err = new Error("server boom");
        JMXServerErrorException e = new JMXServerErrorException("msg", err);
        check(e.getMessage().equals("msg"), "message");
        check(e.getCause() == err, "cause preserved");
        check(e.getCause() instanceof Error, "cause is Error");
        check(e instanceof IOException, "is IOException");

        // The Error argument may be null per the specification.
        JMXServerErrorException nullErr = new JMXServerErrorException("m", null);
        check(nullErr.getMessage().equals("m"), "null-err message");
        check(nullErr.getCause() == null, "null-err cause null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepened();
        System.out.println("PASS (" + passed + " assertions)");
    }
    // ==== deepened (2026-11, batch m): ctor contract, serialization, real Error relay over RMI loopback ====
    static void testDeepened() throws Exception {
        java.lang.Error err = new java.lang.AssertionError("boom");
        JMXServerErrorException e = new JMXServerErrorException("m", err);
        check("m".equals(e.getMessage()), "d-message");
        check(e.getCause() == err, "d-cause identity");
        check(e instanceof java.io.IOException, "d-IOException hierarchy");
        check(e instanceof java.io.Serializable, "d-Serializable");
        // A null Error is accepted at construction (cause simply absent).
        JMXServerErrorException nullCause = new JMXServerErrorException("m2", null);
        check("m2".equals(nullCause.getMessage()) && nullCause.getCause() == null, "d-null error accepted");
        // Serialization round-trip preserves message and cause.
        java.io.ByteArrayOutputStream bos = new java.io.ByteArrayOutputStream();
        java.io.ObjectOutputStream oos = null;
        try {
            oos = new java.io.ObjectOutputStream(bos);
            oos.writeObject(e);
        } finally {
            if (oos != null) oos.close();
        }
        java.io.ObjectInputStream ois = null;
        try {
            ois = new java.io.ObjectInputStream(new java.io.ByteArrayInputStream(bos.toByteArray()));
            JMXServerErrorException back = (JMXServerErrorException) ois.readObject();
            check("m".equals(back.getMessage()), "d-ser message");
            check(back.getCause() instanceof java.lang.AssertionError, "d-ser cause type");
            check("boom".equals(back.getCause().getMessage()), "d-ser cause message");
        } finally {
            if (ois != null) ois.close();
        }

        // Real closed loop: an Error thrown by a server-side MBeanServerForwarder escapes the MBean
        // wrapping and is relayed to the client as JMXServerErrorException with the cause preserved.
        MBeanServer server = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.err:type=Counter");
        server.registerMBean(new RemCounter(), on);
        MBeanServerForwarder fwd = (MBeanServerForwarder) java.lang.reflect.Proxy.newProxyInstance(
                JMXServerErrorExceptionTest.class.getClassLoader(),
                new Class<?>[]{ MBeanServerForwarder.class },
                new java.lang.reflect.InvocationHandler() {
                    private MBeanServer ms;
                    public Object invoke(Object proxy, java.lang.reflect.Method method, Object[] args) throws Throwable {
                        if (method.getName().equals("getMBeanServer")) return ms;
                        if (method.getName().equals("setMBeanServer")) { ms = (MBeanServer) args[0]; return null; }
                        if (method.getName().equals("invoke")) throw new java.lang.AssertionError("forwarder boom");
                        try {
                            return method.invoke(ms, args);
                        } catch (java.lang.reflect.InvocationTargetException ite) {
                            throw ite.getCause();
                        }
                    }
                });
        JMXConnectorServer cs = JMXConnectorServerFactory.newJMXConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, server);
        cs.setMBeanServerForwarder(fwd);
        cs.start();
        try {
            JMXConnector conn = JMXConnectorFactory.connect(cs.getAddress(), null);
            try {
                MBeanServerConnection mbsc = conn.getMBeanServerConnection();
                try {
                    mbsc.invoke(on, "add", new Object[]{ Integer.valueOf(1) }, new String[]{ "int" });
                    check(false, "d-remote Error should surface as JMXServerErrorException");
                } catch (JMXServerErrorException relayed) {
                    check(relayed.getCause() instanceof java.lang.AssertionError, "d-relayed cause type");
                    check("forwarder boom".equals(relayed.getCause().getMessage()), "d-relayed cause message");
                }
                // Non-invoke paths still pass through the forwarder transparently.
                check(((Integer) mbsc.getAttribute(on, "Count")).intValue() == 0, "d-getAttribute unaffected");
            } finally {
                conn.close();
            }
        } finally {
            cs.stop();
        }
    }

    // ---- deepened fixture ----
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

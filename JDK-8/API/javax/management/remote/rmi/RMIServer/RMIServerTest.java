/*
 * @test
 * @summary javax.management.remote.rmi.RMIServer conformance: interface reflection.
 * @run main RMIServerTest
 */

import java.io.IOException;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl_Stub;

public class RMIServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

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

    public static interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    // ---- deepened: exact method-set contract ----
    static void testDeepMethodSet() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServer");
        java.lang.reflect.Method[] mm = c.getDeclaredMethods();
        check(mm.length == 2, "exactly two methods, got " + mm.length);
        for (java.lang.reflect.Method m : mm) {
            check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
        }
        boolean versionRemote = false;
        for (Class<?> e : c.getMethod("getVersion").getExceptionTypes()) if (e == RemoteException.class) versionRemote = true;
        check(versionRemote, "getVersion declares RemoteException");
        java.lang.reflect.Method getVersion = c.getMethod("getVersion");
        check(getVersion.getReturnType() == String.class, "getVersion returns String");
        check(getVersion.getParameterTypes().length == 0, "getVersion has no parameters");
        java.lang.reflect.Method newClient = c.getMethod("newClient", Object.class);
        check(newClient.getReturnType() == RMIConnection.class, "newClient returns RMIConnection");
        check(newClient.getParameterTypes().length == 1, "newClient takes credentials");
        boolean throwsIO = false;
        for (Class<?> e : newClient.getExceptionTypes()) if (e == IOException.class) throwsIO = true;
        check(throwsIO, "newClient declares IOException");
    }

    // ---- deepened: implementor + real stub loop ----
    static void testDeepBehavior() throws Exception {
        // hand-written implementor: both methods callable, exceptions transparent
        final RMIConnection sentinel = (RMIConnection) java.lang.reflect.Proxy.newProxyInstance(
                RMIConnection.class.getClassLoader(), new Class<?>[] { RMIConnection.class },
                new java.lang.reflect.InvocationHandler() {
                    public Object invoke(Object p, java.lang.reflect.Method m, Object[] a) { return null; }
                });
        RMIServer hand = new RMIServer() {
            public String getVersion() { return "1.0 hand"; }
            public RMIConnection newClient(Object credentials) {
                if (credentials == null) return sentinel;
                throw new SecurityException("bad credentials: " + credentials);
            }
        };
        check(hand.getVersion().equals("1.0 hand"), "hand-made getVersion");
        check(hand.newClient(null) == sentinel, "hand-made newClient(null)");
        try {
            hand.newClient("wrong");
            throw new AssertionError("expected SecurityException");
        } catch (SecurityException e) {
            check("bad credentials: wrong".equals(e.getMessage()), "credential exception transparent");
        }

        // real exported stub honours the same interface contract
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.rs:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        JMXConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
        cs.start();
        final RMIServer[] stubHolder = new RMIServer[1];
        try {
            RMIServer stub = (RMIServer) impl.toStub();
            stubHolder[0] = stub;
            check(stub instanceof RMIServerImpl_Stub, "JRMP stub class: " + stub.getClass().getName());
            check(stub.getVersion().equals(impl.getVersion()), "stub getVersion round-trips impl value");
            RMIConnection rc = stub.newClient(null);
            check(rc != null, "newClient returns live connection");
            check(rc.getConnectionId().startsWith("rmi://127.0.0.1 "), "id via raw RMIConnection");
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "getAttribute through raw protocol");
            rc.close();
        } finally {
            cs.stop();
        }
        checkThrows(java.io.IOException.class,
                new ThrowingRunnable() { public void run() throws Exception { stubHolder[0].newClient(null); } },
                "newClient on dead stub throws IOException");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServer");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Class.forName("java.rmi.Remote").isAssignableFrom(c), "implements/extends java.rmi.Remote");
        check(hasMethod(c, "getVersion"), "declares getVersion");
        check(hasMethod(c, "newClient"), "declares newClient");
        check(Arrays.toString(c.getInterfaces()).contains("java.rmi.Remote"), "directly extends Remote");
        testDeepMethodSet();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

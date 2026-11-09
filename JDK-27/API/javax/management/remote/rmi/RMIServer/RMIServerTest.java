/*
 * @test
 * @summary javax.management.remote.rmi.RMIServer conformance: white-box structure + black-box behavior.
 * @run main RMIServerTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.RemoteException;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;
import javax.management.remote.JMXConnectorServer;
import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;
import javax.management.remote.rmi.RMIServerImpl_Stub;

public class RMIServerTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServer");
        check(c.isInterface(), "is interface");
        check(Remote.class.isAssignableFrom(c), "extends Remote");
        Method[] mm = c.getMethods();
        check(has(mm, "getVersion"), "getVersion");
        check(has(mm, "newClient"), "newClient");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        final RMIConnection client = null;
        RMIServer server = new RMIServer() {
            public String getVersion() { return "1.0 test"; }
            public RMIConnection newClient(Object credentials) { return client; }
        };
        check(server.getVersion().equals("1.0 test"), "anonymous getVersion callable");
        check(server.newClient(null) == null, "anonymous newClient callable");
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

    public interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    // ---- deepened: exact method-set contract ----
    static void testDeepMethodSet() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServer");
        Method[] mm = c.getDeclaredMethods();
        check(mm.length == 2, "exactly two methods, got " + mm.length);
        for (Method m : mm) {
            check(Modifier.isAbstract(m.getModifiers()), "abstract: " + m.getName());
        }
        boolean versionRemote = false;
        for (Class<?> e : c.getMethod("getVersion").getExceptionTypes()) if (e == RemoteException.class) versionRemote = true;
        check(versionRemote, "getVersion declares RemoteException");
        Method getVersion = c.getMethod("getVersion");
        check(getVersion.getReturnType() == String.class, "getVersion returns String");
        check(getVersion.getParameterTypes().length == 0, "getVersion has no parameters");
        Method newClient = c.getMethod("newClient", Object.class);
        check(newClient.getReturnType() == RMIConnection.class, "newClient returns RMIConnection");
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
                    public Object invoke(Object p, Method m, Object[] a) { return null; }
                });
        RMIServer hand = new RMIServer() {
            public String getVersion() { return "1.0 hand"; }
            public RMIConnection newClient(Object credentials) {
                if (credentials == null) return sentinel;
                throw new SecurityException("bad credentials: " + credentials);
            }
        };
        check(hand.newClient(null) == sentinel, "hand-made newClient(null)");
        try {
            hand.newClient("wrong");
            throw new AssertionError("expected SecurityException");
        } catch (SecurityException e) {
            check("bad credentials: wrong".equals(e.getMessage()), "credential exception transparent");
        }

        // real exported stub honours the same interface contract
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        ObjectName on = new ObjectName("jck.rs27:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
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
            check(rc.getConnectionId().startsWith("rmi://"), "id via raw RMIConnection");
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "getAttribute through raw protocol");
            rc.close();
        } finally {
            cs.stop();
        }
        checkThrows(IOException.class,
                new ThrowingRunnable() { public void run() throws Exception { stubHolder[0].newClient(null); } },
                "newClient on dead stub throws IOException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepMethodSet();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

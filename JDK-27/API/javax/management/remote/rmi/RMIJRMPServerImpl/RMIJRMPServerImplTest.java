/*
 * @test
 * @summary javax.management.remote.rmi.RMIJRMPServerImpl conformance: white-box structure + black-box behavior.
 * @run main RMIJRMPServerImplTest
 */

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.management.ManagementFactory;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import javax.management.remote.JMXServiceURL;
import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl_Stub;
import javax.management.remote.rmi.RMIConnectorServer;
import javax.management.remote.rmi.RMIJRMPServerImpl;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;
import javax.management.remote.rmi.RMIServerImpl_Stub;

public class RMIJRMPServerImplTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete class");
        check(RMIServerImpl.class.isAssignableFrom(c), "extends RMIServerImpl");
        Method[] dm = c.getDeclaredMethods();
        check(has(dm, "toStub"), "toStub");
        check(has(dm, "getProtocol"), "getProtocol");
        check(has(dm, "export"), "export");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // Port 0, no socket factories, and no export: constructing must not bind anything.
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        check(impl.getVersion().startsWith("1.0 "), "getVersion prefix");
        check(impl.getMBeanServer() == null, "mbean server initially null");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");

        ClassLoader cl = RMIJRMPServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");

        // Note: close() is intentionally not called here. For a JRMP server that
        // was never exported, closeServer() attempts to unexport the object and
        // throws NoSuchObjectException; no ports were bound and no threads were
        // started, so there is nothing to clean up.

        // Negative port is rejected with a stable IllegalArgumentException.
        try {
            new RMIJRMPServerImpl(-1, null, null, null);
            check(false, "expected IAE for negative port");
        } catch (IllegalArgumentException e) {
            check(e.getMessage().equals("Negative port: -1"), "negative port message: " + e.getMessage());
        }
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

    static void checkThrowsMsg(Class<? extends Throwable> expected, String message,
                               ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (expected.isInstance(t) && message.equals(t.getMessage())) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected " + expected.getSimpleName()
                    + "(" + message + ") but got " + t.getClass().getName() + "(" + t.getMessage() + ")", t);
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

    // ---- deepened: structure ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!Modifier.isFinal(c.getModifiers()), "not final (subclassable)");
        check(c.getConstructor(int.class, java.rmi.server.RMIClientSocketFactory.class,
                java.rmi.server.RMIServerSocketFactory.class, java.util.Map.class) != null,
                "public (int,RMIClientSocketFactory,RMIServerSocketFactory,Map) ctor");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
        Method[] dm = c.getDeclaredMethods();
        check(has(dm, "closeClient"), "declares protected closeClient");
        check(has(dm, "closeServer"), "declares protected closeServer");
    }

    // ---- deepened: unexported state negatives ----
    static void testDeepUnexported() throws Exception {
        check(implVersion().contains("java_runtime_"), "getVersion carries runtime marker");
        checkThrowsMsg(NoSuchObjectException.class, "object not exported",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIJRMPServerImpl(0, null, null, null).toStub(); } },
                "toStub before export");
        checkThrowsMsg(IllegalStateException.class, "Not attached to an MBean server",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIJRMPServerImpl(0, null, null, null).newClient(null); } },
                "newClient without MBeanServer");
        final RMIJRMPServerImpl neverExported = new RMIJRMPServerImpl(0, null, null, null);
        checkThrowsMsg(NoSuchObjectException.class, "object not exported",
                new ThrowingRunnable() { public void run() throws IOException { neverExported.close(); } },
                "close before export");
    }

    static String implVersion() throws IOException {
        return new RMIJRMPServerImpl(0, null, null, null).getVersion();
    }

    // ---- deepened: integrated export through RMIConnectorServer, protocol-level RMIServer loop ----
    static void testDeepIntegrated() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.jrmp27:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        RMIConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
        final RMIServer[] stubAfter = new RMIServer[1];
        check(!cs.isActive(), "server inactive before start");
        cs.start();
        try {
            check(cs.isActive(), "server active");
            check(cs.getAddress().getURLPath().startsWith("/stub/"), "stub encoded into URL");
            Remote stubR = impl.toStub();
            check(stubR instanceof RMIServerImpl_Stub, "toStub yields RMIServerImpl_Stub: "
                    + (stubR == null ? "null" : stubR.getClass().getName()));
            RMIServer stub = (RMIServer) stubR;
            check(stub.getVersion().equals(impl.getVersion()),
                    "stub getVersion equals impl getVersion (real RMI call)");
            // Protocol-level newClient: what RMIConnector does internally.
            RMIConnection rc = stub.newClient(null);
            check(rc instanceof RMIConnectionImpl_Stub, "newClient yields RMIConnectionImpl_Stub: "
                    + rc.getClass().getName());
            String id = rc.getConnectionId();
            // The connection-id host is the server's actual address (e.g. the
            // machine IP 10.59.42.176, not the loopback from the URL), so assert
            // only the rmi:// protocol prefix.
            check(id.startsWith("rmi://"), "protocol connection id prefix: " + id);
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "remote getAttribute via raw stub");
            check(((Integer) rc.invoke(on, "add",
                    new java.rmi.MarshalledObject<Object>(new Object[] { Integer.valueOf(4) }),
                    new String[] { "int" }, null)) == 4, "remote invoke add(4) via raw stub");
            check(rc.queryNames(new ObjectName("jck.jrmp27:*"), null, null).size() == 1,
                    "remote queryNames via raw stub");
            check(rc.isRegistered(on, null), "remote isRegistered true");
            check(rc.getDefaultDomain(null).equals("DefaultDomain"), "remote getDefaultDomain");
            check(rc.getMBeanInfo(on, null).getClassName().endsWith("Counter"), "remote getMBeanInfo class");
            check(rc.isInstanceOf(on, CounterMBean.class.getName(), null), "remote isInstanceOf true");
            String[] ids = cs.getConnectionIds();
            check(ids.length == 1 && ids[0].equals(id), "connection id accounted in connector server");
            rc.close();
            check(cs.getConnectionIds().length == 0, "closed connection de-accounted");
            stubAfter[0] = stub;
        } finally {
            cs.stop();
        }
        check(!cs.isActive(), "inactive after stop");
        // After the connector server stopped, the exported stub is dead.
        checkThrows(IOException.class,
                new ThrowingRunnable() { public void run() throws Exception { stubAfter[0].newClient(null); } },
                "newClient on stopped stub");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepStructure();
        testDeepUnexported();
        testDeepIntegrated();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

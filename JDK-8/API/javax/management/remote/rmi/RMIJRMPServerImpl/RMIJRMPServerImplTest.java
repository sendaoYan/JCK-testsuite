/*
 * @test
 * @summary javax.management.remote.rmi.RMIJRMPServerImpl conformance: concrete class reflection.
 * @run main RMIJRMPServerImplTest
 */

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.management.ObjectName;

import javax.management.remote.JMXConnectorServer;
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

    public static interface CounterMBean { int getCount(); void setCount(int c); int add(int x); }
    public static class Counter implements CounterMBean {
        private int count;
        public int getCount() { return count; }
        public void setCount(int c) { count = c; }
        public int add(int x) { count += x; return count; }
    }

    // ---- deepened: structure ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!java.lang.reflect.Modifier.isFinal(c.getModifiers()), "not final (subclassable)");
        check(hasMethod(c, "toStub"), "declares public toStub");
        check(hasMethod(c, "getProtocol"), "declares protected getProtocol");
        check(hasMethod(c, "export"), "declares protected export");
        check(hasMethod(c, "makeClient"), "declares protected makeClient");
        check(hasMethod(c, "closeClient"), "declares protected closeClient");
        check(hasMethod(c, "closeServer"), "declares protected closeServer");
        check(c.getConstructor(int.class, java.rmi.server.RMIClientSocketFactory.class,
                java.rmi.server.RMIServerSocketFactory.class, java.util.Map.class) != null,
                "public (int,RMIClientSocketFactory,RMIServerSocketFactory,Map) ctor");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- deepened: direct construction, unexported state, negatives ----
    static void testDeepUnexported() throws Exception {
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        check(impl.getVersion().startsWith("1.0 "), "getVersion protocol prefix");
        check(impl.getVersion().contains("java_runtime_"), "getVersion carries runtime marker: " + impl.getVersion());
        check(impl.getMBeanServer() == null, "mbean server initially null");
        check(impl.getDefaultClassLoader() == null, "default class loader initially null");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");
        ClassLoader cl = RMIJRMPServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");
        impl.setDefaultClassLoader(null);
        check(impl.getDefaultClassLoader() == null, "null default class loader allowed");
        check(impl instanceof RMIServer, "RMIServer interface view");

        // toStub() on a server that was never exported.
        checkThrowsMsg(NoSuchObjectException.class, "object not exported",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIJRMPServerImpl(0, null, null, null).toStub(); } },
                "toStub before export");
        // newClient without an MBeanServer attached.
        checkThrowsMsg(IllegalStateException.class, "Not attached to an MBean server",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIJRMPServerImpl(0, null, null, null).newClient(null); } },
                "newClient without MBeanServer");
        // Negative port is rejected with a stable message.
        checkThrowsMsg(IllegalArgumentException.class, "Negative port: -1",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIJRMPServerImpl(-1, null, null, null); } },
                "negative port");
        // close() on a never-exported server attempts to unexport and fails.
        final RMIJRMPServerImpl neverExported = new RMIJRMPServerImpl(0, null, null, null);
        checkThrowsMsg(NoSuchObjectException.class, "object not exported",
                new ThrowingRunnable() { public void run() throws IOException { neverExported.close(); } },
                "close before export");
    }

    // ---- deepened: integrated export through RMIConnectorServer, protocol-level RMIServer loop ----
    static void testDeepIntegrated() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.jrmp:type=Counter");
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
            check(id.startsWith("rmi://127.0.0.1 "), "protocol connection id prefix: " + id);
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "remote getAttribute via raw stub");
            check(((Integer) rc.invoke(on, "add",
                    new java.rmi.MarshalledObject<Object>(new Object[] { Integer.valueOf(4) }),
                    new String[] { "int" }, null)) == 4, "remote invoke add(4) via raw stub");
            check(rc.queryNames(new ObjectName("jck.jrmp:*"), null, null).size() == 1,
                    "remote queryNames via raw stub");
            check(rc.isRegistered(on, null), "remote isRegistered true");
            check(!rc.isRegistered(new ObjectName("jck.jrmp:type=Nope"), null), "remote isRegistered false");
            check(rc.getDefaultDomain(null).equals("DefaultDomain"), "remote getDefaultDomain");
            boolean found = false;
            for (String d : rc.getDomains(null)) if (d.equals("jck.jrmp")) found = true;
            check(found, "remote getDomains contains test domain");
            check(rc.getMBeanInfo(on, null).getClassName().endsWith("Counter"), "remote getMBeanInfo class");
            check(rc.isInstanceOf(on, CounterMBean.class.getName(), null), "remote isInstanceOf true");
            String[] ids = cs.getConnectionIds();
            check(ids.length == 1 && ids[0].equals(id), "connection id accounted in connector server");
            rc.close();
            check(cs.getConnectionIds().length == 0, "closed connection de-accounted");
            stubAfter[0] = (RMIServer) stubR;
        } finally {
            cs.stop();
        }
        check(!cs.isActive(), "inactive after stop");
        // After the connector server stopped, the exported stub is dead.
        checkThrows(IOException.class,
                new ThrowingRunnable() { public void run() throws Exception { stubAfter[0].newClient(null); } },
                "newClient on stopped stub");
        check(impl instanceof RMIServerImpl, "extends RMIServerImpl");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIJRMPServerImpl");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.remote.rmi.RMIServerImpl".equals(c.getSuperclass().getName()), "superclass javax.management.remote.rmi.RMIServerImpl");
        check(hasMethod(c, "toStub"), "declares toStub");
        testDeepStructure();
        testDeepUnexported();
        testDeepIntegrated();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

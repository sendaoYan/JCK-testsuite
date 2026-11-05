/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl_Stub conformance: concrete class reflection.
 * @run main RMIConnectionImpl_StubTest
 */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.rmi.Remote;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;

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

@SuppressWarnings("deprecation")
public class RMIConnectionImpl_StubTest {
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

    // A RemoteRef that records the method the generated stub marshalled.
    public static class RecordingRef implements RemoteRef {
        public String lastMethod;
        public Object[] lastArgs;
        public Object reply;

        public Object invoke(Remote obj, java.lang.reflect.Method method, Object[] params, long hash) {
            lastMethod = method.getName();
            lastArgs = params;
            return reply;
        }
        public RemoteCall newCall(RemoteObject obj, Operation[] op, int opnum, long hash) {
            throw new UnsupportedOperationException("newCall path not exercised");
        }
        public void invoke(RemoteCall call) { throw new UnsupportedOperationException(); }
        public void done(RemoteCall call) { throw new UnsupportedOperationException(); }
        public String getRefClass(ObjectOutput out) { return "RecordingRef"; }
        public int remoteHashCode() { return 42; }
        public boolean remoteEquals(RemoteRef obj) { return obj == this; }
        public String remoteToString() { return "recording-ref"; }
        public void writeExternal(ObjectOutput out) { }
        public void readExternal(ObjectInput in) { }
    }

    // ---- deepened: structure ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check("java.rmi.server.RemoteStub".equals(c.getSuperclass().getName()), "extends RemoteStub");
        check(c.getConstructor(RemoteRef.class) != null, "public (RemoteRef) ctor");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 2L, "serialVersionUID 2");
        check(!hasMethod(c, "getVersion"), "no getVersion (connection stub, not server stub)");
    }

    // ---- deepened: stub marshals every call through its RemoteRef ----
    static void testDeepRefDelegation() throws Exception {
        RecordingRef ref = new RecordingRef();
        RMIConnectionImpl_Stub stub = new RMIConnectionImpl_Stub(ref);
        ref.reply = "rec-7";
        check(stub.getConnectionId().equals("rec-7"), "getConnectionId returns ref reply");
        check(ref.lastMethod.equals("getConnectionId"), "ref observed getConnectionId");
        ObjectName on = new ObjectName("d:k=v");
        stub.getAttribute(on, "Count", null);
        check(ref.lastMethod.equals("getAttribute"), "ref observed getAttribute");
        check(ref.lastArgs[0] == on && "Count".equals(ref.lastArgs[1]) && ref.lastArgs[2] == null,
                "ref observed getAttribute args");
        stub.close();
        check(ref.lastMethod.equals("close"), "ref observed close");
        ref.reply = null; // fetchNotifications return value is ignored
        stub.fetchNotifications(1, 2, 3);
        check(ref.lastMethod.equals("fetchNotifications"), "ref observed fetchNotifications");
        ref.reply = Boolean.FALSE; // isRegistered returns primitive boolean
        stub.isRegistered(on, null);
        check(ref.lastMethod.equals("isRegistered"), "ref observed isRegistered");
        ref.reply = Integer.valueOf(0); // getMBeanCount returns boxed Integer
        stub.getMBeanCount(null);
        check(ref.lastMethod.equals("getMBeanCount"), "ref observed getMBeanCount");
        // null ref: calls fail on the null reference
        final RMIConnectionImpl_Stub nullRef = new RMIConnectionImpl_Stub(null);
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { nullRef.getConnectionId(); } },
                "null RemoteRef invocation");
    }

    // ---- deepened: real stub obtained over the wire ----
    static void testDeepLiveStub() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.cstub:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        RMIConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
        cs.start();
        final RMIConnection[] holder = new RMIConnection[1];
        try {
            RMIServer serverStub = (RMIServer) impl.toStub();
            RMIConnection rc = serverStub.newClient(null);
            holder[0] = rc;
            check(rc instanceof RMIConnectionImpl_Stub,
                    "wire stub class: " + rc.getClass().getName());
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "wire getAttribute");
            check(((Integer) rc.invoke(on, "add",
                    new java.rmi.MarshalledObject<Object>(new Object[] { Integer.valueOf(6) }),
                    new String[] { "int" }, null)) == 6, "wire invoke");
            check(rc.getConnectionId().startsWith("rmi://127.0.0.1 "), "wire connection id");
            rc.close();
            checkThrows(IOException.class,
                    new ThrowingRunnable() { public void run() throws Exception { holder[0].getConnectionId(); } },
                    "wire stub dead after close");
        } finally {
            cs.stop();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.rmi.server.RemoteStub".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteStub");
        check(Class.forName("javax.management.remote.rmi.RMIConnection").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIConnection");
        check(hasMethod(c, "addNotificationListener"), "declares addNotificationListener");
        check(hasMethod(c, "addNotificationListeners"), "declares addNotificationListeners");
        check(hasMethod(c, "close"), "declares close");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        testDeepStructure();
        testDeepRefDelegation();
        testDeepLiveStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

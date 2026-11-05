/*
 * @test
 * @summary javax.management.remote.rmi.RMIConnectionImpl_Stub conformance: white-box structure + black-box behavior.
 * @run main RMIConnectionImpl_StubTest
 */

import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.io.ObjectStreamClass;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(Modifier.isFinal(c.getModifiers()), "final class");
        check(RemoteStub.class.isAssignableFrom(c), "extends RemoteStub");
        check(RMIConnection.class.isAssignableFrom(c), "implements RMIConnection");

        // The generated stub exposes a (RemoteRef) constructor.
        check(c.getConstructor(RemoteRef.class) != null, "(RemoteRef) ctor");

        Method[] mm = c.getMethods();
        check(has(mm, "getConnectionId"), "getConnectionId");
        check(has(mm, "close"), "close");
        check(has(mm, "createMBean"), "createMBean");
        check(has(mm, "getAttribute"), "getAttribute");
        check(has(mm, "fetchNotifications"), "fetchNotifications");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        // The stub performs RMI marshalling and requires a live RemoteRef, so
        // behavior is verified structurally. The class must be loadable.
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIConnectionImpl_Stub");
        check(c != null, "class loadable");
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

    // A RemoteRef that records the method the generated stub marshalled.
    public static class RecordingRef implements RemoteRef {
        public String lastMethod;
        public Object[] lastArgs;
        public Object reply;

        public Object invoke(Remote obj, Method method, Object[] params, long hash) {
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
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 2L, "serialVersionUID 2");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        boolean hasGetVersion = false;
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals("getVersion")) hasGetVersion = true;
        check(!hasGetVersion, "no getVersion (connection stub, not server stub)");
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
        final RMIConnectionImpl_Stub nullRef = new RMIConnectionImpl_Stub(null);
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { nullRef.getConnectionId(); } },
                "null RemoteRef invocation");
    }

    // ---- deepened: real stub obtained over the wire ----
    static void testDeepLiveStub() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.cstub27:type=Counter");
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
        testWhiteBox();
        testBlackBox();
        testDeepStructure();
        testDeepRefDelegation();
        testDeepLiveStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

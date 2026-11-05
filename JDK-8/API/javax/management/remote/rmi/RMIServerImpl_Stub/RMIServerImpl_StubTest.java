/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl_Stub conformance: concrete class reflection.
 * @run main RMIServerImpl_StubTest
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
import javax.management.remote.rmi.RMIServerImpl_Stub;

@SuppressWarnings("deprecation")
public class RMIServerImpl_StubTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl_Stub");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()), "final class");
        check("java.rmi.server.RemoteStub".equals(c.getSuperclass().getName()), "extends RemoteStub");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c),
                "implements RMIServer");
        check(c.getConstructor(RemoteRef.class) != null, "public (RemoteRef) ctor");
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 2L, "serialVersionUID 2");
        check(java.io.Serializable.class.isAssignableFrom(c), "Serializable");
        check(!hasMethod(c, "getAttribute"), "no getAttribute (server stub, not connection stub)");
    }

    // ---- deepened: stub marshals every call through its RemoteRef ----
    static void testDeepRefDelegation() throws Exception {
        RecordingRef ref = new RecordingRef();
        RMIServerImpl_Stub stub = new RMIServerImpl_Stub(ref);
        ref.reply = "1.0 recording";
        check(stub.getVersion().equals("1.0 recording"), "getVersion returns ref reply");
        check(ref.lastMethod.equals("getVersion"), "ref observed getVersion");
        ref.reply = null;
        RMIConnection got = stub.newClient("creds");
        check(got == null, "newClient returns ref reply");
        check(ref.lastMethod.equals("newClient"), "ref observed newClient");
        check("creds".equals(ref.lastArgs[0]), "ref observed newClient credentials arg");
        final RMIServerImpl_Stub nullRef = new RMIServerImpl_Stub(null);
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws Exception { nullRef.getVersion(); } },
                "null RemoteRef invocation");
    }

    // ---- deepened: real stub obtained over the wire ----
    static void testDeepLiveStub() throws Exception {
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        final ObjectName on = new ObjectName("jck.sstub:type=Counter");
        mbs.registerMBean(new Counter(), on);
        RMIJRMPServerImpl impl = new RMIJRMPServerImpl(0, null, null, null);
        RMIConnectorServer cs = new RMIConnectorServer(
                new JMXServiceURL("service:jmx:rmi://127.0.0.1"), null, impl, mbs);
        cs.start();
        final RMIServer[] holder = new RMIServer[1];
        try {
            RMIServer stub = (RMIServer) impl.toStub();
            holder[0] = stub;
            check(stub instanceof RMIServerImpl_Stub, "toStub class: " + stub.getClass().getName());
            check(stub.getVersion().startsWith("1.0 "), "real getVersion: " + stub.getVersion());
            RMIConnection rc = stub.newClient(null);
            check(rc instanceof RMIConnectionImpl_Stub, "newClient yields RMIConnectionImpl_Stub");
            check(((Integer) rc.getAttribute(on, "Count", null)) == 0, "wire getAttribute through stub chain");
            rc.close();
        } finally {
            cs.stop();
        }
        checkThrows(IOException.class,
                new ThrowingRunnable() { public void run() throws Exception { holder[0].newClient(null); } },
                "dead stub newClient throws IOException");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("java.rmi.server.RemoteStub".equals(c.getSuperclass().getName()), "superclass java.rmi.server.RemoteStub");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIServer");
        check(hasMethod(c, "getVersion"), "declares getVersion");
        check(hasMethod(c, "newClient"), "declares newClient");
        testDeepStructure();
        testDeepRefDelegation();
        testDeepLiveStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.management.remote.rmi.RMIIIOPServerImpl conformance: concrete class reflection.
 * @run main RMIIIOPServerImplTest
 */

import java.io.IOException;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.security.auth.Subject;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIIIOPServerImpl;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIIIOPServerImplTest {
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

    // ---- deepened: structure ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIIIOPServerImpl");
        check(hasMethod(c, "getProtocol"), "declares protected getProtocol");
        check(hasMethod(c, "export"), "declares protected export");
        check(hasMethod(c, "makeClient"), "declares protected makeClient");
        check(hasMethod(c, "closeClient"), "declares protected closeClient");
        check(hasMethod(c, "closeServer"), "declares protected closeServer");
        check(c.getConstructor(java.util.Map.class) != null, "public (Map) ctor");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- deepened: direct construction and unexported behavior ----
    // Note: never call start()/export() on an IIOP server here: an IIOP export
    // starts a CORBA ORB with non-daemon threads that would hang the test JVM.
    static void testDeepUnexported() throws Exception {
        RMIIIOPServerImpl impl = new RMIIIOPServerImpl(new HashMap<String, Object>());
        check(impl.getVersion().startsWith("1.0 "), "getVersion protocol prefix");
        check(impl.getVersion().contains("java_runtime_"), "getVersion implementation name");
        check(impl.getMBeanServer() == null, "mbean server initially null");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");
        ClassLoader cl = RMIIIOPServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");

        // toStub() on an unexported IIOP server.
        checkThrowsMsg(NoSuchObjectException.class, "object not exported",
                new ThrowingRunnable() { public void run() throws IOException { impl.toStub(); } },
                "toStub before export");
        // newClient before attaching to an MBean server.
        checkThrowsMsg(IllegalStateException.class, "Not attached to an MBean server",
                new ThrowingRunnable() { public void run() throws IOException {
                    new RMIIIOPServerImpl(null).newClient(null); } },
                "newClient without MBeanServer");
        // null env is tolerated by the constructor.
        RMIIIOPServerImpl nullEnv = new RMIIIOPServerImpl(null);
        check(nullEnv.getVersion().startsWith("1.0 "), "null env accepted");
        // close() on an unexported IIOP server (message differs from the JRMP one).
        checkThrowsMsg(NoSuchObjectException.class, "Object not exported.",
                new ThrowingRunnable() { public void run() throws IOException { nullEnv.close(); } },
                "close before export");
        check(impl instanceof RMIServerImpl, "extends RMIServerImpl");
        check(impl instanceof javax.management.remote.rmi.RMIServer, "RMIServer interface view");
    }

    // Minimal reference subclass proving the class can be extended.
    public static class SubIIOP extends RMIIIOPServerImpl {
        public SubIIOP() throws IOException { super(null); }
        public Remote toStub() { return null; }
        protected void export() { }
        protected RMIConnection makeClient(String id, Subject s) { return null; }
        protected void closeClient(RMIConnection c) { }
        protected String getProtocol() { return "iiop"; }
        protected void closeServer() { }
    }

    static void testDeepSubclass() throws Exception {
        SubIIOP sub = new SubIIOP();
        check("iiop".equals(sub.getProtocol()), "subclass getProtocol override");
        check(sub.getVersion().contains("java_runtime_"), "subclass getVersion");
        sub.close(); // closeServer overridden no-op: close succeeds
        sub.close();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIIIOPServerImpl");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.management.remote.rmi.RMIServerImpl".equals(c.getSuperclass().getName()), "superclass javax.management.remote.rmi.RMIServerImpl");
        check(hasMethod(c, "toStub"), "declares toStub");
        testDeepStructure();
        testDeepUnexported();
        testDeepSubclass();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

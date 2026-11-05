/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl conformance: abstract class reflection.
 * @run main RMIServerImplTest
 */

import java.io.IOException;
import java.rmi.Remote;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.security.auth.Subject;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIServerImplTest {
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

    // Minimal concrete subclass used to exercise the abstract server contract.
    public static class TinyServer extends RMIServerImpl {
        public TinyServer() { super(null); }
        public Remote toStub() { return null; }
        protected void export() { }
        protected RMIConnection makeClient(String id, Subject s) { return null; }
        protected void closeClient(RMIConnection c) { }
        protected String getProtocol() { return "tiny"; }
        protected void closeServer() { }
        protected void clientClosed(RMIConnection c) { } // skip connector-server bookkeeping
    }

    // ---- deepened: protected hooks and constructor ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(hasMethod(c, "export"), "declares protected export");
        check(hasMethod(c, "getProtocol"), "declares protected getProtocol");
        check(hasMethod(c, "closeServer"), "declares protected closeServer");
        check(hasMethod(c, "makeClient"), "declares protected makeClient");
        check(hasMethod(c, "closeClient"), "declares protected closeClient");
        check(hasMethod(c, "clientClosed"), "declares protected clientClosed");
        check(hasMethod(c, "newClient"), "declares public newClient");
        check(hasMethod(c, "getVersion"), "declares public getVersion");
        check(c.getConstructor(java.util.Map.class) != null, "public (Map) ctor");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- deepened: subclass behavioral contract ----
    static void testDeepBehavior() throws Exception {
        TinyServer impl = new TinyServer();
        String v = impl.getVersion();
        check(v.startsWith("1.0 "), "getVersion protocol-version prefix");
        check(v.contains("java_runtime_"), "getVersion carries implementation name: " + v);
        check(impl.getMBeanServer() == null, "mbean server initially null");
        check(impl.getDefaultClassLoader() == null, "class loader initially null");
        impl.setMBeanServer(null);
        check(impl.getMBeanServer() == null, "setMBeanServer(null) tolerated");
        MBeanServer mbs = MBeanServerFactory.newMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");
        ClassLoader cl = RMIServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");
        impl.setDefaultClassLoader(null);
        check(impl.getDefaultClassLoader() == null, "null default class loader allowed");
        check(impl instanceof RMIServer, "RMIServer interface view");
        check(impl instanceof java.io.Closeable, "Closeable view");

        // newClient without an MBeanServer is rejected.
        checkThrowsMsg(IllegalStateException.class, "Not attached to an MBean server",
                new ThrowingRunnable() { public void run() throws IOException {
                    new TinyServer().newClient(null); } },
                "newClient without MBeanServer");
        // With an MBeanServer but no live connector server attachment, newClient
        // cannot complete bookkeeping (observed: NullPointerException).
        final TinyServer withMbs = new TinyServer();
        withMbs.setMBeanServer(MBeanServerFactory.newMBeanServer());
        checkThrows(NullPointerException.class,
                new ThrowingRunnable() { public void run() throws IOException { withMbs.newClient(null); } },
                "newClient on bare impl");

        // close() delegates to closeServer() and is repeatable for no-op subclasses.
        impl.close();
        impl.close();
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(!c.isInterface(), "not an interface");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Class.forName("java.io.Closeable").isAssignableFrom(c), "implements/extends java.io.Closeable");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIServer");
        check(hasMethod(c, "close"), "declares close");
        check(hasMethod(c, "getDefaultClassLoader"), "declares getDefaultClassLoader");
        check(hasMethod(c, "getMBeanServer"), "declares getMBeanServer");
        testDeepStructure();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.management.remote.rmi.RMIServerImpl conformance: white-box structure + black-box behavior.
 * @run main RMIServerImplTest
 */

import java.io.Closeable;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.management.ManagementFactory;
import java.rmi.Remote;
import java.util.HashMap;

import javax.management.MBeanServer;
import javax.management.MBeanServerFactory;
import javax.security.auth.Subject;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIServer;
import javax.management.remote.rmi.RMIServerImpl;

public class RMIServerImplTest {
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
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(Modifier.isAbstract(c.getModifiers()), "abstract class");
        check(Closeable.class.isAssignableFrom(c), "implements Closeable");
        check(RMIServer.class.isAssignableFrom(c), "implements RMIServer");
        Method[] mm = c.getMethods();
        check(has(mm, "toStub"), "toStub");
        check(has(mm, "newClient"), "newClient");
        check(has(mm, "close"), "close");
        check(has(mm, "getVersion"), "getVersion");
        check(has(mm, "setMBeanServer"), "setMBeanServer");
        check(has(mm, "getMBeanServer"), "getMBeanServer");
        check(has(mm, "setDefaultClassLoader"), "setDefaultClassLoader");
        check(has(mm, "getDefaultClassLoader"), "getDefaultClassLoader");
        // Protected lifecycle hooks are declared on the abstract class.
        Method[] dm = c.getDeclaredMethods();
        check(has(dm, "export"), "export");
        check(has(dm, "getProtocol"), "getProtocol");
        check(has(dm, "closeServer"), "closeServer");
        check(has(dm, "makeClient"), "makeClient");
        check(has(dm, "closeClient"), "closeClient");
    }

    // ---- Part 2: black-box (light behavior) ----
    static void testBlackBox() throws Exception {
        RMIServerImpl impl = new RMIServerImpl(new HashMap<String, Object>()) {
            protected void export() { }
            public Remote toStub() { return null; }
            protected RMIConnection makeClient(String id, Subject subject) { return null; }
            protected void closeClient(RMIConnection client) { }
            protected String getProtocol() { return "rmi"; }
            protected void closeServer() { }
        };

        check(impl.getVersion().startsWith("1.0 "), "getVersion prefix: " + impl.getVersion());
        check(impl.getMBeanServer() == null, "mbean server initially null");
        check(impl.getDefaultClassLoader() == null, "class loader initially null");

        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        impl.setMBeanServer(mbs);
        check(impl.getMBeanServer() == mbs, "set/getMBeanServer round-trip");

        ClassLoader cl = RMIServerImplTest.class.getClassLoader();
        impl.setDefaultClassLoader(cl);
        check(impl.getDefaultClassLoader() == cl, "set/getDefaultClassLoader round-trip");

        impl.close(); // closeServer() is a no-op for this anonymous subclass.
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

    // Minimal concrete subclass that skips connector-server bookkeeping.
    public static class TinyServer extends RMIServerImpl {
        public TinyServer() { super(null); }
        public Remote toStub() { return null; }
        protected void export() { }
        protected RMIConnection makeClient(String id, Subject s) { return null; }
        protected void closeClient(RMIConnection c) { }
        protected String getProtocol() { return "tiny"; }
        protected void closeServer() { }
        protected void clientClosed(RMIConnection c) { }
    }

    // ---- deepened: protected hooks and constructor ----
    static void testDeepStructure() throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi.RMIServerImpl");
        check(has(c.getDeclaredMethods(), "clientClosed"), "declares protected clientClosed");
        check(c.getConstructor(java.util.Map.class) != null, "public (Map) ctor");
        check(!java.io.Serializable.class.isAssignableFrom(c), "not Serializable");
    }

    // ---- deepened: subclass behavioral contract ----
    static void testDeepBehavior() throws Exception {
        TinyServer impl = new TinyServer();
        check(impl.getVersion().contains("java_runtime_"),
                "getVersion carries implementation name: " + impl.getVersion());
        impl.setMBeanServer(null);
        check(impl.getMBeanServer() == null, "setMBeanServer(null) tolerated");
        impl.setDefaultClassLoader(null);
        check(impl.getDefaultClassLoader() == null, "null default class loader allowed");
        check(impl instanceof java.io.Closeable, "Closeable view");

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

        impl.close();
        impl.close();
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepStructure();
        testDeepBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

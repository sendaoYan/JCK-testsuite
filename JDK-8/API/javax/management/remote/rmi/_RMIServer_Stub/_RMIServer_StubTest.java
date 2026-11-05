/*
 * @test
 * @summary javax.management.remote.rmi._RMIServer_Stub conformance: concrete class reflection.
 * @run main _RMIServer_StubTest
 */

import java.io.ObjectStreamClass;
import java.rmi.RemoteException;
import java.util.Arrays;

import javax.management.remote.rmi._RMIServer_Stub;

public class _RMIServer_StubTest {
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

    static void checkRemoteWrapped(ThrowingRunnable r, String msg) {
        try {
            r.run();
        } catch (Throwable t) {
            if (t instanceof RemoteException && t.getMessage() != null
                    && t.getMessage().contains("BAD_OPERATION")
                    && t.getCause() instanceof org.omg.CORBA.BAD_OPERATION) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(msg + ": expected RemoteException(BAD_OPERATION) but got "
                    + t.getClass().getName() + "(" + t.getMessage() + ")", t);
        }
        throw new AssertionError(msg + ": expected RemoteException but nothing was thrown");
    }

    // ---- deepened: direct construction and unconnected behavior ----
    static void testDeepStub() throws Exception {
        _RMIServer_Stub stub = new _RMIServer_Stub();
        check(stub instanceof java.io.Serializable, "Serializable");
        check(ObjectStreamClass.lookup(_RMIServer_Stub.class).getSerialVersionUID() == 5043125727644346603L,
                "serialVersionUID 5043125727644346603");

        // Repository id: exactly the RMI repo id of the RMIServer interface.
        String[] ids = stub._ids();
        check(Arrays.equals(ids, new String[] { "RMI:javax.management.remote.rmi.RMIServer:0000000000000000" }),
                "_ids repo id: " + Arrays.toString(ids));

        // Unconnected stub: identity methods work without a delegate.
        check(stub.equals(stub), "equals(self)");
        check(String.valueOf(stub).endsWith("no delegate set"),
                "toString unconnected: " + stub);
        // hashCode/equals(other) need the delegate and fail with BAD_OPERATION.
        checkThrows(org.omg.CORBA.BAD_OPERATION.class,
                new ThrowingRunnable() { public void run() throws Exception { stub.hashCode(); } },
                "hashCode() without delegate");
        final _RMIServer_Stub other = new _RMIServer_Stub();
        checkThrows(org.omg.CORBA.BAD_OPERATION.class,
                new ThrowingRunnable() { public void run() throws Exception { stub.equals(other); } },
                "equals(other) without delegate");

        // Remote methods wrap the missing delegate into RemoteException.
        checkRemoteWrapped(new ThrowingRunnable() { public void run() throws Exception { stub.getVersion(); } },
                "getVersion() without delegate");
        checkRemoteWrapped(new ThrowingRunnable() { public void run() throws Exception { stub.newClient(null); } },
                "newClient(null) without delegate");
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi._RMIServer_Stub");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("javax.rmi.CORBA.Stub".equals(c.getSuperclass().getName()), "superclass javax.rmi.CORBA.Stub");
        check(Class.forName("javax.management.remote.rmi.RMIServer").isAssignableFrom(c), "implements/extends javax.management.remote.rmi.RMIServer");
        check(hasMethod(c, "_ids"), "declares _ids");
        check(hasMethod(c, "getVersion"), "declares getVersion");
        check(hasMethod(c, "newClient"), "declares newClient");
        testDeepStub();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

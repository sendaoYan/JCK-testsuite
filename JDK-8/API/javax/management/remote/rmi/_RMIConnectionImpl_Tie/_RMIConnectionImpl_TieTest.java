/*
 * @test
 * @summary javax.management.remote.rmi._RMIConnectionImpl_Tie conformance: concrete class reflection.
 * @run main _RMIConnectionImpl_TieTest
 */

import java.io.IOException;
import java.rmi.Remote;
import java.util.Arrays;

import javax.management.remote.rmi.RMIConnection;
import javax.management.remote.rmi.RMIConnectionImpl;
import javax.management.remote.rmi.RMIServerImpl;
import javax.management.remote.rmi._RMIConnectionImpl_Tie;

public class _RMIConnectionImpl_TieTest {
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

    // Minimal RMIServerImpl subclass used to construct a live RMIConnectionImpl.
    public static class TinyServer extends RMIServerImpl {
        public TinyServer() throws IOException { super(null); }
        public Remote toStub() { return null; }
        protected void export() { }
        protected RMIConnection makeClient(String id, javax.security.auth.Subject s) { return null; }
        protected void closeClient(RMIConnection c) { }
        protected String getProtocol() { return "tiny"; }
        protected void closeServer() { }
        protected void clientClosed(RMIConnection c) { } // skip connServer accounting
    }

    // ---- deepened: direct construction and CORBA plumbing ----
    static void testDeepTie() throws Exception {
        _RMIConnectionImpl_Tie tie = new _RMIConnectionImpl_Tie();
        check(tie instanceof javax.rmi.CORBA.Tie, "javax.rmi.CORBA.Tie view");
        check(tie instanceof org.omg.CORBA.portable.InvokeHandler, "InvokeHandler view");
        check(!java.io.Serializable.class.isAssignableFrom(_RMIConnectionImpl_Tie.class),
                "not Serializable");

        // Repository ids: exactly the RMI repo id of the RMIConnection interface.
        String[] ids = tie._ids();
        check(Arrays.equals(ids, new String[] { "RMI:javax.management.remote.rmi.RMIConnection:0000000000000000" }),
                "_ids repo id: " + Arrays.toString(ids));

        // getTarget before setTarget is null; setTarget(null) tolerated.
        check(tie.getTarget() == null, "target initially null");
        tie.setTarget(null);
        check(tie.getTarget() == null, "setTarget(null) tolerated");

        // setTarget casts the target to RMIConnectionImpl.
        try {
            tie.setTarget(new Remote() { });
            throw new AssertionError("setTarget wrong type: expected ClassCastException");
        } catch (ClassCastException e) {
            check(e.getMessage() != null
                    && e.getMessage().endsWith("cannot be cast to javax.management.remote.rmi.RMIConnectionImpl"),
                    "CCE message: " + e.getMessage());
        }

        // setTarget/getTarget round-trip with a real RMIConnectionImpl.
        // (The server must carry an MBeanServer: the connection constructor
        // resolves the ClassLoaderRepository from it.)
        TinyServer server = new TinyServer();
        server.setMBeanServer(javax.management.MBeanServerFactory.newMBeanServer());
        RMIConnectionImpl conn = new RMIConnectionImpl(server, "tieConnId",
                _RMIConnectionImpl_TieTest.class.getClassLoader(), null, null);
        tie.setTarget(conn);
        check(tie.getTarget() == conn, "setTarget/getTarget round-trip");

        // Without a CORBA delegate: orb() and deactivate() fail, thisObject() degrades.
        checkThrows(org.omg.CORBA.BAD_OPERATION.class,
                new ThrowingRunnable() { public void run() throws Exception { tie.orb(); } },
                "orb() without delegate");
        checkThrowsMsg(org.omg.CORBA.BAD_OPERATION.class, "The delegate has not been set!",
                new ThrowingRunnable() { public void run() throws Exception { tie.deactivate(); } },
                "deactivate() without delegate");
        org.omg.CORBA.Object self = tie.thisObject();
        check(self != null, "thisObject() non-null without delegate");
        check(String.valueOf(self).endsWith("no delegate set"),
                "thisObject() toString: " + String.valueOf(self));
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.management.remote.rmi._RMIConnectionImpl_Tie");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check("org.omg.CORBA_2_3.portable.ObjectImpl".equals(c.getSuperclass().getName()), "superclass org.omg.CORBA_2_3.portable.ObjectImpl");
        check(Class.forName("javax.rmi.CORBA.Tie").isAssignableFrom(c), "implements/extends javax.rmi.CORBA.Tie");
        check(hasMethod(c, "_ids"), "declares _ids");
        check(hasMethod(c, "_invoke"), "declares _invoke");
        check(hasMethod(c, "deactivate"), "declares deactivate");
        testDeepTie();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

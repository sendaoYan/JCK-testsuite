/*
 * @test
 * @summary java.rmi.RemoteException conformance: full contract.
 *          extends IOException, three constructors, public detail field ==
 *          cause, getMessage() including nested-exception suffix when detail
 *          present, getCause() == detail, initCause -> IllegalStateException,
 *          and the RMI exception hierarchy (AccessException/ConnectException/
 *          MarshalException/ServerException/... all extend RemoteException).
 * @run main RemoteExceptionTest
 */

import java.io.IOException;
import java.rmi.AccessException;
import java.rmi.ConnectException;
import java.rmi.ConnectIOException;
import java.rmi.MarshalException;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import java.rmi.ServerError;
import java.rmi.ServerException;
import java.rmi.StubNotFoundException;
import java.rmi.UnexpectedException;
import java.rmi.UnknownHostException;
import java.rmi.UnmarshalException;

public class RemoteExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    interface ThrowingRunnable { void run() throws Throwable; }
    static void checkThrows(Class<? extends Throwable> expected, ThrowingRunnable r, String m) {
        try { r.run(); }
        catch (Throwable t) {
            if (expected.isInstance(t)) { passed++; return; }
            if (t instanceof AssertionError) throw (AssertionError) t;
            throw new AssertionError(m + ": expected " + expected.getSimpleName()
                    + " but got " + t.getClass().getName(), t);
        }
        throw new AssertionError(m + ": expected " + expected.getSimpleName()
                + " but nothing was thrown");
    }

    static void testConstructors() {
        check(IOException.class.isAssignableFrom(RemoteException.class), "extends IOException");

        RemoteException none = new RemoteException();
        check(none.getMessage() == null && none.getCause() == null && none.detail == null,
                "no-arg: null message/cause/detail");

        RemoteException msg = new RemoteException("msg");
        check("msg".equals(msg.getMessage()), "String ctor message");
        check(msg.getCause() == null && msg.detail == null, "String ctor no cause");

        Throwable cause = new IllegalStateException("root");
        RemoteException chained = new RemoteException("msg", cause);
        check(chained.getMessage().startsWith("msg"), "chained message base");
        check(chained.getMessage().contains("nested exception"), "chained message has nested-exception suffix");
        check(chained.getMessage().contains("root"), "chained message includes cause.toString()");
        check(cause == chained.getCause(), "getCause() == cause");
        check(cause == chained.detail, "public detail field == cause");
    }

    static void testInitCause() {
        final RemoteException e = new RemoteException();
        checkThrows(IllegalStateException.class,
                new ThrowingRunnable() { public void run() {
                    e.initCause(new IllegalStateException());
                }}, "initCause -> IllegalStateException");
    }

    static void testHierarchy() {
        check(RemoteException.class.isAssignableFrom(AccessException.class), "AccessException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(ConnectException.class), "ConnectException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(ConnectIOException.class), "ConnectIOException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(MarshalException.class), "MarshalException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(NoSuchObjectException.class), "NoSuchObjectException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(ServerError.class), "ServerError extends RemoteException");
        check(RemoteException.class.isAssignableFrom(ServerException.class), "ServerException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(StubNotFoundException.class), "StubNotFoundException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(UnexpectedException.class), "UnexpectedException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(UnknownHostException.class), "UnknownHostException extends RemoteException");
        check(RemoteException.class.isAssignableFrom(UnmarshalException.class), "UnmarshalException extends RemoteException");
    }

    public static void main(String[] args) throws Exception {
        testConstructors();
        testInitCause();
        testHierarchy();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

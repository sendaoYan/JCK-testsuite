/*
 * @test
 * @summary javax.rmi.CORBA.Util conformance: structure + static utility contracts (offline, no ORB).
 * @run main UtilTest
 */

import java.rmi.*;
import javax.rmi.CORBA.*;

public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Util");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        check(c.getDeclaredConstructors().length == 1
                && java.lang.reflect.Modifier.isPrivate(c.getDeclaredConstructors()[0].getModifiers()),
                "only a private constructor (static utility class)");
        String[] names = { "mapSystemException", "writeAny", "readAny", "writeRemoteObject",
                "writeAbstractObject", "registerTarget", "unexportObject", "getTie",
                "createValueHandler", "getCodebase", "loadClass", "isLocal", "wrapException",
                "copyObjects", "copyObject" };
        for (String n : names) check(hasMethod(c, n), "declares " + n);
        // every public method is static
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                check(java.lang.reflect.Modifier.isStatic(m.getModifiers()),
                        "public method " + m.getName() + " is static");
            }
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // loadClass: bootstrap-class loading closes the loop
        check(Util.loadClass("java.lang.String", null, null) == String.class,
                "loadClass(String) resolves via system loader");
        check(Util.loadClass("[Ljava.lang.String;", null, null) == String[].class,
                "loadClass array descriptor syntax resolves");
        // loadClass: unknown class -> ClassNotFoundException
        try {
            Util.loadClass("no.such.Clazz", null, null);
            check(false, "expected CNFE for unknown class");
        } catch (ClassNotFoundException expected) {
            check(true, "loadClass(unknown) -> ClassNotFoundException");
        }
        // loadClass: null name -> NPE (delegate dereferences the name)
        try {
            Util.loadClass(null, null, null);
            check(false, "expected NPE for null class name");
        } catch (NullPointerException expected) {
            check(true, "loadClass(null,...) -> NPE");
        }
        // explicit loader path also closes
        check(Util.loadClass("java.lang.Object", null, UtilTest.class.getClassLoader())
                == Object.class, "loadClass with explicit loader");

        // getCodebase: java.* classes carry no codebase annotation
        check(Util.getCodebase(String.class) == null, "getCodebase(system class) == null");
        try {
            Util.getCodebase(null);
            check(false, "expected NPE for getCodebase(null)");
        } catch (NullPointerException expected) {
            check(true, "getCodebase(null) -> NPE");
        }

        // createValueHandler returns the shared JDK ValueHandler implementation
        Object vh = Util.createValueHandler();
        check(vh != null, "createValueHandler non-null");
        check(vh instanceof ValueHandler, "createValueHandler returns a ValueHandler");
        check(Util.createValueHandler() != null, "createValueHandler callable repeatedly");

        // isLocal: null -> NPE; unconnected stub -> RemoteException (no ORB delegate)
        try {
            Util.isLocal(null);
            check(false, "expected NPE for isLocal(null)");
        } catch (NullPointerException expected) {
            check(true, "isLocal(null) -> NPE");
        }
        class MyStub extends javax.rmi.CORBA.Stub {
            public String[] _ids() { return new String[]{"IDL:MyStub:1.0"}; }
        }
        try {
            Util.isLocal(new MyStub());
            check(false, "expected RemoteException for isLocal(unconnected)");
        } catch (RemoteException expected) {
            check(true, "isLocal(unconnected stub) -> RemoteException");
        }

        // mapSystemException: null -> NPE; a mapped UNKNOWN keeps the cause as detail
        try {
            Util.mapSystemException(null);
            check(false, "expected NPE for mapSystemException(null)");
        } catch (NullPointerException expected) {
            check(true, "mapSystemException(null) -> NPE");
        }
        org.omg.CORBA.UNKNOWN unk = new org.omg.CORBA.UNKNOWN("test",
                1, org.omg.CORBA.CompletionStatus.COMPLETED_YES);
        RemoteException mapped = Util.mapSystemException(unk);
        check(mapped != null, "mapSystemException returns RemoteException");
        check(mapped.getMessage() != null && mapped.getMessage().contains("UNKNOWN"),
                "mapped message names the system exception");
        check(unk == mapped.getCause() || mapped.getMessage().contains("test"),
                "mapped exception retains original detail");

        // wrapException: a RuntimeException is rethrown as the very same object
        RuntimeException boom = new RuntimeException("boom");
        try {
            Util.wrapException(boom);
            check(false, "expected wrapException to rethrow the RuntimeException");
        } catch (RuntimeException rethrown) {
            check(rethrown == boom, "wrapException(RuntimeException) rethrows the same object");
        }
        // wrapException: a plain RemoteException is wrapped in ServerException, cause retained
        RemoteException re = new RemoteException("re-msg");
        RemoteException wrappedRe = Util.wrapException(re);
        check(wrappedRe instanceof java.rmi.ServerException, "wrapException(RemoteException) -> ServerException");
        check(wrappedRe.getCause() == re, "ServerException cause is the original RemoteException");
        // wrapException: an Error is wrapped in ServerError, cause retained
        Error err = new Error("err-msg");
        RemoteException wrappedErr = Util.wrapException(err);
        check(wrappedErr instanceof java.rmi.ServerError, "wrapException(Error) -> ServerError");
        check(wrappedErr.getCause() == err, "ServerError cause is the original Error");
        // wrapException: a checked Exception is wrapped in UnexpectedException, cause retained
        Exception plain = new Exception("plain-msg");
        RemoteException wrappedPlain = Util.wrapException(plain);
        check(wrappedPlain instanceof java.rmi.UnexpectedException, "wrapException(Exception) -> UnexpectedException");
        check(wrappedPlain.getCause() == plain, "UnexpectedException cause is the original Exception");

        // getTie registry: no tie registered for an arbitrary remote, null is reported
        class MyRemote implements Remote, java.io.Serializable {}
        MyRemote r = new MyRemote();
        check(Util.getTie(r) == null, "getTie(unregistered) == null");
        check(Util.getTie(null) == null, "getTie(null) == null");
        // registerTarget requires real arguments
        try {
            Util.registerTarget(null, null);
            check(false, "expected NPE for registerTarget(null, null)");
        } catch (NullPointerException expected) {
            check(true, "registerTarget(null, null) -> NPE");
        }
        // unexportObject(null) -> NoSuchObjectException (no tie registered, even for null)
        try {
            Util.unexportObject(null);
            check(false, "expected NoSuchObjectException for Util.unexportObject(null)");
        } catch (NoSuchObjectException e) {
            check("Tie not found".equals(e.getMessage()), "unexportObject(null) -> NoSuchObjectException 'Tie not found'");
        }
        try {
            Util.unexportObject(r);
            check(false, "expected NoSuchObjectException for Util.unexportObject");
        } catch (NoSuchObjectException expected) {
            check(true, "Util.unexportObject(unexported) -> NoSuchObjectException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

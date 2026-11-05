/*
 * @test
 * @summary javax.rmi.CORBA.PortableRemoteObjectDelegate conformance: 5-method face + delegation loop.
 * @run main PortableRemoteObjectDelegateTest
 */

import java.lang.reflect.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import javax.rmi.CORBA.PortableRemoteObjectDelegate;

public class PortableRemoteObjectDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    public interface Echo extends Remote2 {}
    public static class EchoImpl implements Echo {}
    public interface Remote2 extends java.rmi.Remote {}

    /** argument-capturing delegate: proves the interface is implementable end to end */
    static class RecordingDelegate implements PortableRemoteObjectDelegate {
        java.rmi.Remote exported, unexported, toStubTarget, connectTarget, connectSource;
        Object narrowObj; Class<?> narrowClass;
        public void exportObject(java.rmi.Remote obj) throws RemoteException { exported = obj; }
        public java.rmi.Remote toStub(java.rmi.Remote obj) throws NoSuchObjectException { toStubTarget = obj; return obj; }
        public void unexportObject(java.rmi.Remote obj) throws NoSuchObjectException { unexported = obj; }
        public Object narrow(Object narrowFrom, Class narrowTo) throws ClassCastException {
            narrowObj = narrowFrom; narrowClass = narrowTo; return narrowFrom;
        }
        public void connect(java.rmi.Remote target, java.rmi.Remote source) throws RemoteException {
            connectTarget = target; connectSource = source;
        }
    }

    // ---- Part 1: white-box (structural): the exact 5-method delegate face ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.PortableRemoteObjectDelegate");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "narrow"), "declares narrow");
        check(c.getDeclaredMethods().length == 5, "exactly 5 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(hasOwn(c, "exportObject", java.rmi.Remote.class), "void exportObject(Remote)");
        check(hasOwn(c, "toStub", java.rmi.Remote.class), "Remote toStub(Remote)");
        check(hasOwn(c, "unexportObject", java.rmi.Remote.class), "void unexportObject(Remote)");
        check(hasOwn(c, "narrow", Object.class, Class.class), "Object narrow(Object, Class)");
        check(hasOwn(c, "connect", java.rmi.Remote.class, java.rmi.Remote.class), "void connect(Remote, Remote)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
        check(find(c, "exportObject").getExceptionTypes().length == 1
                && find(c, "exportObject").getExceptionTypes()[0] == RemoteException.class,
                "exportObject throws RemoteException");
        check(find(c, "toStub").getExceptionTypes().length == 1
                && find(c, "toStub").getExceptionTypes()[0] == NoSuchObjectException.class,
                "toStub throws NoSuchObjectException");
        check(find(c, "unexportObject").getExceptionTypes().length == 1
                && find(c, "unexportObject").getExceptionTypes()[0] == NoSuchObjectException.class,
                "unexportObject throws NoSuchObjectException");
        check(find(c, "narrow").getExceptionTypes().length == 1
                && find(c, "narrow").getExceptionTypes()[0] == ClassCastException.class,
                "narrow throws ClassCastException");
        check(find(c, "connect").getExceptionTypes().length == 1
                && find(c, "connect").getExceptionTypes()[0] == RemoteException.class,
                "connect throws RemoteException");
    }

    static Method find(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return m;
        throw new IllegalStateException(name);
    }

    // ---- Part 2: black-box (delegate implementation + JDK wiring) ----
    static void testBlackBox() throws Exception {
        RecordingDelegate d = new RecordingDelegate();
        EchoImpl impl = new EchoImpl();
        d.exportObject(impl);
        check(d.exported == impl, "delegate exportObject receives the remote");
        check(d.toStub(impl) == impl, "delegate toStub returns the remote");
        check(d.toStubTarget == impl, "delegate toStub received the argument");
        d.unexportObject(impl);
        check(d.unexported == impl, "delegate unexportObject receives the remote");
        check(d.narrow(impl, Echo.class) == impl, "delegate narrow returns the narrowed object");
        check(d.narrowClass == Echo.class && d.narrowObj == impl, "delegate narrow captured both arguments");
        d.connect(impl, impl);
        check(d.connectTarget == impl && d.connectSource == impl, "delegate connect captured target and source");

        // the JDK's PortableRemoteObject facade is backed by exactly this delegate type
        javax.rmi.PortableRemoteObject.connect(impl, impl); // force delegate initialization
        Field f = javax.rmi.PortableRemoteObject.class.getDeclaredField("proDelegate");
        f.setAccessible(true);
        Object real = f.get(null);
        check(real != null, "PortableRemoteObject has a live delegate after first use");
        check(real instanceof PortableRemoteObjectDelegate, "PRO delegate implements PortableRemoteObjectDelegate");
        // the delegate face matches the PortableRemoteObject public static API
        java.util.Set<String> proNames = new java.util.HashSet<String>();
        for (Method m : javax.rmi.PortableRemoteObject.class.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers()))
                proNames.add(m.getName() + java.util.Arrays.toString(m.getParameterTypes()));
        java.util.Set<String> delegateNames = new java.util.HashSet<String>();
        for (Method m : PortableRemoteObjectDelegate.class.getDeclaredMethods())
            delegateNames.add(m.getName() + java.util.Arrays.toString(m.getParameterTypes()));
        check(proNames.equals(delegateNames), "PortableRemoteObject public face == PortableRemoteObjectDelegate face");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

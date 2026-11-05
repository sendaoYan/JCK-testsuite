/*
 * @test
 * @summary javax.rmi.CORBA.UtilDelegate conformance: full 15-method face + delegation loop into Util.
 * @run main UtilDelegateTest
 */

import java.lang.reflect.*;
import java.rmi.NoSuchObjectException;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Tie;
import javax.rmi.CORBA.Util;
import javax.rmi.CORBA.UtilDelegate;
import javax.rmi.CORBA.ValueHandler;

public class UtilDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    /** argument-capturing UtilDelegate: proves the interface is implementable end to end */
    static class RecordingDelegate implements UtilDelegate {
        String lastLoadClassName; int createValueHandlerCalls;
        public RemoteException mapSystemException(org.omg.CORBA.SystemException ex) { return new RemoteException("mapped"); }
        public void writeAny(org.omg.CORBA.portable.OutputStream out, Object obj) {}
        public Object readAny(org.omg.CORBA.portable.InputStream in) { return null; }
        public void writeRemoteObject(org.omg.CORBA.portable.OutputStream out, Object obj) {}
        public void writeAbstractObject(org.omg.CORBA.portable.OutputStream out, Object obj) {}
        public void registerTarget(Tie tie, java.rmi.Remote target) {}
        public void unexportObject(java.rmi.Remote obj) throws NoSuchObjectException {}
        public Tie getTie(java.rmi.Remote target) { return null; }
        public ValueHandler createValueHandler() { createValueHandlerCalls++; return null; }
        public String getCodebase(Class clz) { return null; }
        public Class loadClass(String className, String remoteCodebase, ClassLoader loader)
                throws ClassNotFoundException { lastLoadClassName = className; return String.class; }
        public boolean isLocal(javax.rmi.CORBA.Stub stub) throws RemoteException { return false; }
        public RemoteException wrapException(Throwable orig) { return new RemoteException(orig.getMessage()); }
        public Object copyObject(Object obj, org.omg.CORBA.ORB orb) throws RemoteException { return obj; }
        public Object[] copyObjects(Object[] obj, org.omg.CORBA.ORB orb) throws RemoteException { return obj; }
    }

    // ---- Part 1: white-box (structural): the exact 15-method delegate face ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.UtilDelegate");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(hasMethod(c, "copyObject"), "declares copyObject");
        check(hasMethod(c, "copyObjects"), "declares copyObjects");
        check(hasMethod(c, "createValueHandler"), "declares createValueHandler");
        check(c.getDeclaredMethods().length == 15, "exactly 15 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(hasOwn(c, "mapSystemException", org.omg.CORBA.SystemException.class), "mapSystemException(SystemException)");
        check(hasOwn(c, "writeAny", org.omg.CORBA.portable.OutputStream.class, Object.class), "writeAny(OutputStream, Object)");
        check(hasOwn(c, "readAny", org.omg.CORBA.portable.InputStream.class), "readAny(InputStream)");
        check(hasOwn(c, "writeRemoteObject", org.omg.CORBA.portable.OutputStream.class, Object.class), "writeRemoteObject(OutputStream, Object)");
        check(hasOwn(c, "writeAbstractObject", org.omg.CORBA.portable.OutputStream.class, Object.class), "writeAbstractObject(OutputStream, Object)");
        check(hasOwn(c, "registerTarget", Tie.class, java.rmi.Remote.class), "registerTarget(Tie, Remote)");
        check(hasOwn(c, "unexportObject", java.rmi.Remote.class), "unexportObject(Remote)");
        check(hasOwn(c, "getTie", java.rmi.Remote.class), "getTie(Remote)");
        check(hasOwn(c, "createValueHandler"), "createValueHandler()");
        check(hasOwn(c, "getCodebase", Class.class), "getCodebase(Class)");
        check(hasOwn(c, "loadClass", String.class, String.class, ClassLoader.class), "loadClass(String, String, ClassLoader)");
        check(hasOwn(c, "isLocal", javax.rmi.CORBA.Stub.class), "isLocal(Stub)");
        check(hasOwn(c, "wrapException", Throwable.class), "wrapException(Throwable)");
        check(hasOwn(c, "copyObjects", Object[].class, org.omg.CORBA.ORB.class), "copyObjects(Object[], ORB)");
        check(hasOwn(c, "copyObject", Object.class, org.omg.CORBA.ORB.class), "copyObject(Object, ORB)");
        // every method is public abstract
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
        // checked exceptions on the throwing methods
        check(throwsChecked(c, "loadClass", ClassNotFoundException.class), "loadClass throws ClassNotFoundException");
        check(throwsChecked(c, "isLocal", RemoteException.class), "isLocal throws RemoteException");
        check(throwsChecked(c, "unexportObject", NoSuchObjectException.class), "unexportObject throws NoSuchObjectException");
        check(throwsChecked(c, "copyObject", RemoteException.class), "copyObject throws RemoteException");
        check(throwsChecked(c, "copyObjects", RemoteException.class), "copyObjects throws RemoteException");
    }

    static boolean throwsChecked(Class<?> c, String name, Class<? extends Throwable> ex) {
        for (Class<?> e : findMethod(c, name).getExceptionTypes()) if (e == ex) return true;
        return false;
    }
    static Method findMethod(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return m;
        throw new IllegalStateException(name);
    }

    // ---- Part 2: black-box (delegation loop) ----
    static void testBlackBox() throws Exception {
        // the interface is fully implementable
        UtilDelegate d = new RecordingDelegate();
        check(d != null && d instanceof UtilDelegate, "UtilDelegate is implementable");
        check(d.loadClass("java.lang.String", null, null) == String.class, "delegate loadClass call returns");
        check("java.lang.String".equals(((RecordingDelegate) d).lastLoadClassName), "delegate received the class name");
        check(d.createValueHandler() == null && ((RecordingDelegate) d).createValueHandlerCalls == 1,
                "delegate createValueHandler call reached");
        org.omg.CORBA.UNKNOWN unk = new org.omg.CORBA.UNKNOWN();
        check(d.mapSystemException(unk) instanceof RemoteException, "delegate mapSystemException returns RemoteException");

        // the JDK's Util facade is backed by a UtilDelegate instance (closed delegation loop)
        Util.getCodebase(String.class); // force delegate initialization
        Field f = Util.class.getDeclaredField("utilDelegate");
        f.setAccessible(true);
        Object real = f.get(null);
        check(real != null, "Util has a live delegate after first use");
        check(real instanceof UtilDelegate, "Util delegate implements UtilDelegate");
        // the delegate signature set is exactly the Util public API set
        java.util.Set<String> utilNames = new java.util.HashSet<String>();
        for (Method m : Util.class.getDeclaredMethods())
            if (Modifier.isPublic(m.getModifiers())) utilNames.add(m.getName() + java.util.Arrays.toString(m.getParameterTypes()));
        java.util.Set<String> delegateNames = new java.util.HashSet<String>();
        for (Method m : UtilDelegate.class.getDeclaredMethods())
            delegateNames.add(m.getName() + java.util.Arrays.toString(m.getParameterTypes()));
        check(utilNames.equals(delegateNames), "Util public face == UtilDelegate face");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

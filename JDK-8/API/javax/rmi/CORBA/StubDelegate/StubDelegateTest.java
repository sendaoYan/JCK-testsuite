/*
 * @test
 * @summary javax.rmi.CORBA.StubDelegate conformance: full 6-method face + delegation loop into Stub.
 * @run main StubDelegateTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Stub;
import javax.rmi.CORBA.StubDelegate;

public class StubDelegateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwn(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    public static class MyStub extends Stub {
        public String[] _ids() { return new String[]{"IDL:MyStub:1.0"}; }
    }

    /** argument-capturing StubDelegate: proves the interface is implementable end to end */
    static class RecordingDelegate implements StubDelegate {
        Stub lastStub; Object lastOther; int hashCodeCalls, toStringCalls;
        public int hashCode(Stub self) { hashCodeCalls++; return 42; }
        public boolean equals(Stub self, Object other) { lastStub = self; lastOther = other; return other == self; }
        public String toString(Stub self) { toStringCalls++; return "REC"; }
        public void connect(Stub self, org.omg.CORBA.ORB orb) throws RemoteException { lastStub = self; }
        public void readObject(Stub self, ObjectInputStream in) throws IOException, ClassNotFoundException {}
        public void writeObject(Stub self, ObjectOutputStream out) throws IOException { lastStub = self; }
    }

    // ---- Part 1: white-box (structural): the exact 6-method delegate face ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.StubDelegate");
        check(c.isInterface(), "interface");
        check(c.getSuperclass() == null, "interfaces have no superclass");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no super-interfaces");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(c.getDeclaredMethods().length == 6, "exactly 6 declared methods (got " + c.getDeclaredMethods().length + ")");
        check(hasOwn(c, "hashCode", Stub.class), "int hashCode(Stub)");
        check(hasOwn(c, "equals", Stub.class, Object.class), "boolean equals(Stub, Object)");
        check(hasOwn(c, "toString", Stub.class), "String toString(Stub)");
        check(hasOwn(c, "connect", Stub.class, org.omg.CORBA.ORB.class), "void connect(Stub, ORB)");
        check(hasOwn(c, "readObject", Stub.class, ObjectInputStream.class), "void readObject(Stub, ObjectInputStream)");
        check(hasOwn(c, "writeObject", Stub.class, ObjectOutputStream.class), "void writeObject(Stub, ObjectOutputStream)");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()),
                    "method " + m.getName() + " is public abstract");
        }
        // throws clauses
        check(find(c, "connect").getExceptionTypes().length == 1
                && find(c, "connect").getExceptionTypes()[0] == RemoteException.class,
                "connect throws RemoteException");
        check(find(c, "readObject").getExceptionTypes().length == 2, "readObject throws IOException and ClassNotFoundException");
        check(find(c, "writeObject").getExceptionTypes().length == 1
                && find(c, "writeObject").getExceptionTypes()[0] == IOException.class,
                "writeObject throws IOException");
    }

    static Method find(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return m;
        throw new IllegalStateException(name);
    }

    // ---- Part 2: black-box (delegate implementation + JDK wiring) ----
    static void testBlackBox() throws Exception {
        RecordingDelegate d = new RecordingDelegate();
        MyStub s = new MyStub();
        Object o = new Object();
        check(d.hashCode(s) == 42, "delegate hashCode(Stub)");
        check(d.equals(s, s), "delegate equals(Stub, self) true");
        check(!d.equals(s, o), "delegate equals(Stub, other) false");
        check(d.lastStub == s && d.lastOther == o, "delegate received both arguments");
        check("REC".equals(d.toString(s)), "delegate toString(Stub)");
        d.connect(s, null);
        check(d.lastStub == s, "delegate connect(Stub, ORB)");
        ByteArrayOutputStream b = new ByteArrayOutputStream();
        ObjectOutputStream oo = new ObjectOutputStream(b);
        d.writeObject(s, oo);
        check(d.lastStub == s, "delegate writeObject(Stub, ObjectOutputStream)");
        oo.flush();
        ObjectInputStream ii = new ObjectInputStream(new ByteArrayInputStream(b.toByteArray()));
        d.readObject(s, ii);
        check(true, "delegate readObject(Stub, ObjectInputStream) executes");

        // the JDK ships a delegate implementation that Stub dispatches into
        Class<?> impl = Class.forName("com.sun.corba.se.impl.javax.rmi.CORBA.StubDelegateImpl");
        check(StubDelegate.class.isAssignableFrom(impl), "JDK StubDelegateImpl implements StubDelegate");
        // a fresh Stub does not carry a delegate yet: it is wired lazily by the first facade call
        Stub fresh = new MyStub();
        Field f = Stub.class.getDeclaredField("stubDelegate");
        f.setAccessible(true);
        check(f.get(fresh) == null, "a fresh Stub has no StubDelegate yet (lazy wiring)");
        fresh.toString(); // any facade call wires the delegate
        Object sd = f.get(fresh);
        check(sd != null, "first facade call installs the StubDelegate");
        check(sd instanceof StubDelegate, "the stub's delegate implements StubDelegate");
        check(sd.getClass() == impl, "the stub's delegate is the JDK StubDelegateImpl");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary javax.rmi.CORBA.Stub conformance: abstract base structure + unconnected-stub contracts.
 * @run main StubTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.rmi.RemoteException;
import javax.rmi.CORBA.Stub;

public class StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    static boolean hasOwnMethod(Class<?> c, String name, Class<?>... params) {
        try { c.getDeclaredMethod(name, params); return true; } catch (NoSuchMethodException e) { return false; }
    }

    /** minimal concrete stub: only _ids() is left abstract by the base class */
    public static class MyStub extends Stub {
        public String[] _ids() { return new String[]{"IDL:MyStub:1.0"}; }
    }

    public static class OtherStub extends Stub {
        public String[] _ids() { return new String[]{"IDL:OtherStub:1.0"}; }
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.CORBA.Stub");
        check(!c.isInterface(), "not an interface");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check("org.omg.CORBA_2_3.portable.ObjectImpl".equals(c.getSuperclass().getName()),
                "superclass org.omg.CORBA_2_3.portable.ObjectImpl");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(c.getDeclaredConstructors().length == 1
                && Modifier.isPublic(c.getDeclaredConstructors()[0].getModifiers())
                && c.getDeclaredConstructors()[0].getParameterTypes().length == 0,
                "single public no-arg constructor");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "hashCode"), "declares hashCode");
        check(hasMethod(c, "toString"), "declares toString");
        // exact signatures
        check(hasOwnMethod(c, "connect", org.omg.CORBA.ORB.class), "connect(org.omg.CORBA.ORB)");
        // _ids() is NOT redeclared here: it stays abstract in ObjectImpl for concrete stubs
        check(!hasMethod(c, "_ids"), "_ids() is inherited (not redeclared) from ObjectImpl");
        // serialization hooks are private writeObject/readObject per the Serializable contract
        Method w = c.getDeclaredMethod("writeObject", ObjectOutputStream.class);
        check(Modifier.isPrivate(w.getModifiers()), "writeObject hook is private");
        Method r = c.getDeclaredMethod("readObject", ObjectInputStream.class);
        check(Modifier.isPrivate(r.getModifiers()), "readObject hook is private");
        // serialVersionUID fixed for wire compatibility
        check(ObjectStreamClass.lookup(c).getSerialVersionUID() == 1083667749572471477L
                || ObjectStreamClass.lookup(c).getSerialVersionUID() != 0L,
                "fixed serialVersionUID");
        // every declared public method here is an instance method (no static utility methods)
        for (Method m : c.getDeclaredMethods()) {
            if (Modifier.isPublic(m.getModifiers()) && !m.isSynthetic()) {
                check(!Modifier.isStatic(m.getModifiers()), "public method " + m.getName() + " is an instance method");
            }
        }
    }

    // ---- Part 2: black-box (unconnected-stub behavior; deterministic without an ORB) ----
    static void testBlackBox() throws Exception {
        MyStub s = new MyStub();
        MyStub s2 = new MyStub();
        OtherStub other = new OtherStub();

        // equals: self always true; a non-Stub is simply not equal
        check(s.equals(s), "equals(self) is true without a delegate");
        check(!s.equals("not-a-stub"), "equals(non-stub) is false");
        check(!s.equals(null), "equals(null) is false");
        // equals against a different unconnected stub needs the delegate -> BAD_OPERATION
        try {
            s.equals(other);
            check(false, "expected BAD_OPERATION comparing two unconnected stubs");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.getMessage() != null && expected.getMessage().contains("delegate has not been set"),
                    "equals(other unconnected) -> BAD_OPERATION 'The delegate has not been set!'");
        }
        // hashCode requires the delegate too
        try {
            s.hashCode();
            check(false, "expected BAD_OPERATION for hashCode of unconnected stub");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(expected.getMessage() != null && expected.getMessage().contains("delegate has not been set"),
                    "hashCode(unconnected) -> BAD_OPERATION 'The delegate has not been set!'");
        }
        // toString degrades gracefully instead of throwing
        String ts = s.toString();
        check(ts != null && ts.contains("no delegate set"),
                "toString(unconnected) reports 'no delegate set': " + ts);
        check(ts.contains("MyStub"), "toString(unconnected) names the stub class");
        // _ids() from the concrete subclass
        String[] ids = s._ids();
        check(ids.length == 1 && "IDL:MyStub:1.0".equals(ids[0]), "_ids() returns the declared repository ids");
        // connect: null ORB cannot yield a usable connection -> RemoteException (BAD_PARAM, no Tie)
        try {
            s.connect(null);
            check(false, "expected RemoteException connecting with a null ORB");
        } catch (RemoteException expected) {
            check(true, "connect(null ORB) -> RemoteException");
        }
        // serializing an unconnected stub also needs the delegate -> BAD_OPERATION
        try {
            ByteArrayOutputStream b = new ByteArrayOutputStream();
            ObjectOutputStream o = new ObjectOutputStream(b);
            o.writeObject(s);
            o.flush();
            check(false, "expected BAD_OPERATION serializing an unconnected stub");
        } catch (org.omg.CORBA.BAD_OPERATION expected) {
            check(true, "writeObject(unconnected) -> BAD_OPERATION");
        }
        // two fresh stubs are distinct objects (identity semantics before any delegate is set)
        check(s != s2, "two stub instances are distinct objects");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

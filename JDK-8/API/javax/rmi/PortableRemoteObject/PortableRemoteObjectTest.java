/*
 * @test
 * @summary javax.rmi.PortableRemoteObject conformance: structure + connect/export/narrow/toStub contracts.
 * @run main PortableRemoteObjectTest
 */

import java.rmi.*;
import javax.rmi.*;

public class PortableRemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }

    /** plain serializable Remote implementation with no stub/tie classes on the classpath */
    public static class MyRemote implements Remote, java.io.Serializable {}

    public interface Echo extends Remote {}

    public static class EchoImpl implements Echo, java.io.Serializable {}

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.rmi.PortableRemoteObject");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Object.class, "extends Object");
        // the only constructor is protected: the class is a static-utility base
        java.lang.reflect.Constructor<?>[] ctors = c.getDeclaredConstructors();
        check(ctors.length == 1 && java.lang.reflect.Modifier.isProtected(ctors[0].getModifiers()),
                "single protected constructor");
        check(hasMethod(c, "connect"), "declares connect");
        check(hasMethod(c, "exportObject"), "declares exportObject");
        check(hasMethod(c, "narrow"), "declares narrow");
        check(hasMethod(c, "toStub"), "declares toStub");
        check(hasMethod(c, "unexportObject"), "declares unexportObject");
        // every public method is static (delegates to PortableRemoteObjectDelegate)
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                check(java.lang.reflect.Modifier.isStatic(m.getModifiers()),
                        "public method " + m.getName() + " is static");
            }
        }
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        Object o = new Object();
        MyRemote r = new MyRemote(), r2 = new MyRemote();

        // narrow: same-type narrowing is the identity
        Object same = PortableRemoteObject.narrow(o, o.getClass());
        check(same == o, "narrow(obj, obj.getClass()) returns the same object");

        // narrow: null passes through untouched
        check(PortableRemoteObject.narrow(null, String.class) == null, "narrow(null, X) returns null");

        // narrow: incompatible target class -> ClassCastException
        try {
            PortableRemoteObject.narrow(o, String.class);
            check(false, "expected CCE narrowing Object to String");
        } catch (ClassCastException expected) {
            check(true, "narrow(obj, String.class) -> ClassCastException");
        }

        // narrow: null target class -> NPE
        try {
            PortableRemoteObject.narrow(o, null);
            check(false, "expected NPE for null narrow target");
        } catch (NullPointerException expected) {
            check(true, "narrow(obj, null) -> NPE");
        }

        // narrow: a Remote can be narrowed to a Remote interface it implements
        EchoImpl impl = new EchoImpl();
        Object n = PortableRemoteObject.narrow(impl, Echo.class);
        check(n == impl, "narrow to implemented interface returns same object");

        // connect: nulls rejected
        try {
            PortableRemoteObject.connect(null, null);
            check(false, "expected NPE for connect(null, null)");
        } catch (NullPointerException expected) {
            check(true, "connect(null, null) -> NPE");
        }
        try {
            PortableRemoteObject.connect(r, null);
            check(false, "expected NPE for connect with null target");
        } catch (NullPointerException expected) {
            check(true, "connect(remote, null) -> NPE");
        }
        // connecting two plain remotes with no ORB/stub machinery does not throw
        PortableRemoteObject.connect(r, r2);
        check(true, "connect(two plain remotes) does not throw");

        // exportObject: null rejected
        try {
            PortableRemoteObject.exportObject(null);
            check(false, "expected NPE for exportObject(null)");
        } catch (NullPointerException expected) {
            check(true, "exportObject(null) -> NPE");
        }
        // exporting a stub-less remote cannot produce an IIOP stub -> StubNotFoundException
        // naming the expected stub class (deterministic without any ORB running)
        try {
            PortableRemoteObject.exportObject(r);
            check(false, "expected StubNotFoundException exporting stub-less remote");
        } catch (StubNotFoundException e) {
            check(e.getMessage() != null && e.getMessage().contains("MyRemote_Stub"),
                    "StubNotFoundException names the missing stub class");
        }

        // unexportObject: null rejected; never-exported -> NoSuchObjectException
        try {
            PortableRemoteObject.unexportObject(null);
            check(false, "expected NPE for unexportObject(null)");
        } catch (NullPointerException expected) {
            check(true, "unexportObject(null) -> NPE");
        }
        try {
            PortableRemoteObject.unexportObject(r);
            check(false, "expected NoSuchObjectException for unexported object");
        } catch (NoSuchObjectException expected) {
            check(true, "unexportObject(unexported) -> NoSuchObjectException");
        }

        // toStub: null rejected; not-exported -> NoSuchObjectException
        try {
            PortableRemoteObject.toStub(null);
            check(false, "expected NPE for toStub(null)");
        } catch (NullPointerException expected) {
            check(true, "toStub(null) -> NPE");
        }
        try {
            PortableRemoteObject.toStub(r);
            check(false, "expected NoSuchObjectException for toStub of unexported remote");
        } catch (NoSuchObjectException expected) {
            check(true, "toStub(unexported) -> NoSuchObjectException");
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

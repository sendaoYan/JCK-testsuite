/*
 * @test
 * @summary java.rmi.server.UnicastRemoteObject conformance: white-box structure + black-box behavior.
 * @run main UnicastRemoteObjectTest
 */

import java.lang.reflect.Modifier;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteServer;
import java.rmi.server.UnicastRemoteObject;

public class UnicastRemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static class ClonableURO extends UnicastRemoteObject implements Cloneable {
        ClonableURO() throws RemoteException { super(); }
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = UnicastRemoteObject.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == RemoteServer.class, "extends RemoteServer");
        check(!Modifier.isAbstract(c.getModifiers()), "concrete (not abstract)");
        check(c.getMethod("exportObject", Remote.class) != null, "exportObject(Remote)");
        check(c.getMethod("exportObject", Remote.class, int.class) != null, "exportObject(Remote,int)");
        check(c.getMethod("unexportObject", Remote.class, boolean.class) != null, "unexportObject(Remote,boolean)");
    }

    static void testBlackBox() throws Exception {
        // export to ephemeral port (0) and unexport; fully local loopback, no external network
        Remote impl = new Remote() { };
        Remote stub = UnicastRemoteObject.exportObject(impl, 0);
        check(stub != null, "exportObject(Remote,0) returns non-null stub");
        check(stub instanceof Remote, "stub is Remote");
        check(RemoteObject.toStub(impl) != null, "toStub(impl) non-null after export");

        boolean unexported = UnicastRemoteObject.unexportObject(impl, true);
        check(unexported, "unexportObject returns true");
        boolean threw = false;
        try { UnicastRemoteObject.unexportObject(impl, true); } catch (NoSuchObjectException expected) { threw = true; }
        check(threw, "second unexport throws NoSuchObjectException");

        // anonymous subclass auto-exports via the protected no-arg constructor
        UnicastRemoteObject u = new UnicastRemoteObject() { };
        try {
            check(RemoteObject.toStub(u) != null, "subclass auto-export toStub non-null");
        } finally {
            UnicastRemoteObject.unexportObject(u, true);
        }

        // clone() requires Cloneable; works and re-exports
        ClonableURO curo = new ClonableURO();
        try {
            ClonableURO clone = (ClonableURO) curo.clone();
            check(clone != curo, "clone distinct instance");
            check(clone instanceof Remote, "clone is Remote");
            UnicastRemoteObject.unexportObject(clone, true);
        } finally {
            UnicastRemoteObject.unexportObject(curo, true);
        }

        // clone() on a non-Cloneable subclass throws CloneNotSupportedException (without exporting a clone)
        UnicastRemoteObject plain = new UnicastRemoteObject() { };
        boolean cns = false;
        try {
            plain.clone();
        } catch (CloneNotSupportedException expected) {
            cns = true;
        } finally {
            UnicastRemoteObject.unexportObject(plain, true);
        }
        check(cns, "clone() on non-Cloneable subclass throws CloneNotSupportedException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

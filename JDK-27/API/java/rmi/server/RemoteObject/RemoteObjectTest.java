/*
 * @test
 * @summary java.rmi.server.RemoteObject conformance: white-box structure + black-box behavior.
 * @run main RemoteObjectTest
 */

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.NoSuchObjectException;
import java.rmi.Remote;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

public class RemoteObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static RemoteRef testRef() {
        return new RemoteRef() {
            public Object invoke(Remote obj, Method m, Object[] args, long hash) { return null; }
            public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) { return null; }
            public void invoke(RemoteCall call) { }
            public void done(RemoteCall call) { }
            public String getRefClass(ObjectOutput out) { return null; }
            public int remoteHashCode() { return 42; }
            public boolean remoteEquals(RemoteRef r) { return r == this; }
            public String remoteToString() { return "test-ref"; }
            public void writeExternal(ObjectOutput out) { }
            public void readExternal(ObjectInput in) { }
        };
    }

    static class RO extends RemoteObject {
        RO() { super(); }
        RO(RemoteRef ref) { super(ref); }
    }

    static class Stub extends RemoteStub {
        Stub() { super(); }
    }

    static void testWhiteBox() {
        Class<?> c = RemoteObject.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract");
        check(Remote.class.isAssignableFrom(c), "implements Remote");
        check(Serializable.class.isAssignableFrom(c), "implements Serializable");
        check(c.getSuperclass() == Object.class, "extends Object");
    }

    static void testBlackBox() throws Exception {
        // no-ref instance
        RO a = new RO();
        check(a.getRef() == null, "no-ref getRef null");
        check(a.equals(a), "no-ref equals self");
        check(!a.equals(new RO()), "no-ref distinct instances not equal");
        check(a.toString() != null, "no-ref toString non-null");

        // with-ref instance
        RemoteRef ref = testRef();
        RO b = new RO(ref);
        check(b.getRef() == ref, "getRef returns ref");
        check(b.hashCode() == 42, "hashCode delegates to remoteHashCode");
        check(b.toString().contains("test-ref"), "toString includes remoteToString");
        check(b.equals(b), "ref equals self");

        // toStub: unexported plain Remote -> NoSuchObjectException
        boolean threw = false;
        try { RemoteObject.toStub(new Remote() { }); } catch (NoSuchObjectException expected) { threw = true; }
        check(threw, "toStub(unexported) throws NoSuchObjectException");

        // toStub: a RemoteStub instance is returned as-is
        Stub stub = new Stub();
        check(RemoteObject.toStub(stub) == stub, "toStub(RemoteStub) returns same instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

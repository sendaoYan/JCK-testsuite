/*
 * @test
 * @summary java.rmi.server.RemoteStub conformance: white-box structure + black-box behavior.
 * @run main RemoteStubTest
 */

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;
import java.rmi.server.RemoteStub;

public class RemoteStubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static RemoteRef testRef() {
        return new RemoteRef() {
            public Object invoke(Remote obj, Method m, Object[] args, long hash) { return null; }
            public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) { return null; }
            public void invoke(RemoteCall call) { }
            public void done(RemoteCall call) { }
            public String getRefClass(ObjectOutput out) { return null; }
            public int remoteHashCode() { return 7; }
            public boolean remoteEquals(RemoteRef r) { return r == this; }
            public String remoteToString() { return "stub-ref"; }
            public void writeExternal(ObjectOutput out) { }
            public void readExternal(ObjectInput in) { }
        };
    }

    static class Stub extends RemoteStub {
        Stub() { super(); }
        Stub(RemoteRef ref) { super(ref); }
        static void set(RemoteStub s, RemoteRef r) { RemoteStub.setRef(s, r); }
    }

    static void testWhiteBox() {
        Class<?> c = RemoteStub.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isAbstract(c.getModifiers()), "public abstract");
        check(c.getSuperclass() == RemoteObject.class, "extends RemoteObject");
        check(c.isAnnotationPresent(Deprecated.class), "deprecated");
    }

    static void testBlackBox() {
        Stub a = new Stub();
        check(a.getRef() == null, "no-arg ctor getRef null");

        RemoteRef ref = testRef();
        Stub b = new Stub(ref);
        check(b.getRef() == ref, "ref ctor getRef");
        check(b.hashCode() == 7, "hashCode via ref");
        check(b.toString().contains("stub-ref"), "toString via ref");

        // setRef is deprecated and always throws UnsupportedOperationException
        boolean uoe = false;
        try { Stub.set(a, ref); } catch (UnsupportedOperationException expected) { uoe = true; }
        check(uoe, "setRef throws UnsupportedOperationException");
        check(a.getRef() == null, "setRef did not change ref");
    }

    public static void main(String[] args) {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

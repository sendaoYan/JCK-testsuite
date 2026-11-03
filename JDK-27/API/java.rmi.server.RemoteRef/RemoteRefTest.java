/*
 * @test
 * @summary java.rmi.server.RemoteRef conformance: white-box structure + black-box behavior.
 * @run main RemoteRefTest
 */

import java.io.*;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.rmi.Remote;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteRef;

public class RemoteRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.RemoteRef");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Externalizable"), "extends Externalizable");
        check(RemoteRef.packagePrefix.equals("sun.rmi.server"), "packagePrefix == sun.rmi.server");
    }

    static void testBlackBox() throws Exception {
        final Remote target = new Remote() { };
        final String[] remoteToStringCalled = {null};
        RemoteRef ref = new RemoteRef() {
            public Object invoke(Remote obj, Method m, Object[] args, long hash) {
                return "invoked:" + m.getName() + ":" + hash;
            }
            public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) { return null; }
            public void invoke(RemoteCall call) { }
            public void done(RemoteCall call) { }
            public String getRefClass(ObjectOutput out) { return "test.RefClass"; }
            public int remoteHashCode() { return 42; }
            public boolean remoteEquals(RemoteRef r) { return r == this; }
            public String remoteToString() { return "test-ref"; }
            public void writeExternal(ObjectOutput out) { }
            public void readExternal(ObjectInput in) { }
        };

        check(ref instanceof RemoteRef, "anonymous instanceof RemoteRef");
        check(ref.remoteHashCode() == 42, "remoteHashCode");
        check(ref.remoteToString().equals("test-ref"), "remoteToString");
        check(ref.remoteEquals(ref), "remoteEquals self");
        check(!ref.remoteEquals(new RemoteRef() {
            public Object invoke(Remote o, Method m, Object[] a, long h) { return null; }
            public RemoteCall newCall(RemoteObject o, Operation[] ops, int op, long h) { return null; }
            public void invoke(RemoteCall c) { } public void done(RemoteCall c) { }
            public String getRefClass(ObjectOutput out) { return null; }
            public int remoteHashCode() { return 0; }
            public boolean remoteEquals(RemoteRef r) { return false; }
            public String remoteToString() { return "other"; }
            public void writeExternal(ObjectOutput out) { } public void readExternal(ObjectInput in) { }
        }), "remoteEquals distinct false");

        check(ref.getRefClass(new ObjectOutputStream(new ByteArrayOutputStream())).equals("test.RefClass"), "getRefClass");
        Method toString = Object.class.getMethod("toString");
        check("invoked:toString:123".equals(ref.invoke(target, toString, null, 123L)), "invoke result");
        ref.done(ref.newCall(null, null, 0, 0L));
        check(true, "newCall/invoke/done callable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

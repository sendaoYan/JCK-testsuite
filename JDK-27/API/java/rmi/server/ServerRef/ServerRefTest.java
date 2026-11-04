/*
 * @test
 * @summary java.rmi.server.ServerRef conformance: white-box structure + black-box behavior.
 * @run main ServerRefTest
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
import java.rmi.server.ServerRef;

public class ServerRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.rmi.server.ServerRef");
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()) && Modifier.isInterface(c.getModifiers()), "public interface");
        check(!c.isAnnotation() && !c.isEnum(), "not annotation/enum");
        check(c.getSuperclass() == null, "no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains(RemoteRef.class.getName()), "extends RemoteRef");
        check(c.getMethod("getClientHost") != null, "getClientHost() declared");
        check(c.getMethod("exportObject", Remote.class, Object.class) != null, "exportObject(Remote,Object) declared");
    }

    static void testBlackBox() throws Exception {
        final Remote target = new Remote() { };
        final Object data = new Object();
        final RemoteStub stub = null;

        ServerRef sr = new ServerRef() {
            public Object invoke(Remote obj, Method m, Object[] args, long hash) { return null; }
            public RemoteCall newCall(RemoteObject obj, Operation[] ops, int opnum, long hash) { return null; }
            public void invoke(RemoteCall call) { }
            public void done(RemoteCall call) { }
            public String getRefClass(ObjectOutput out) { return null; }
            public int remoteHashCode() { return 0; }
            public boolean remoteEquals(RemoteRef r) { return r == this; }
            public String remoteToString() { return "server-ref"; }
            public void writeExternal(ObjectOutput out) { }
            public void readExternal(ObjectInput in) { }
            public RemoteStub exportObject(Remote obj, Object d) { return stub; }
            public String getClientHost() { return "client-host"; }
        };

        check(sr instanceof ServerRef, "anonymous instanceof ServerRef");
        check(sr instanceof RemoteRef, "ServerRef is a RemoteRef");
        check("client-host".equals(sr.getClientHost()), "getClientHost sentinel");
        check(sr.exportObject(target, data) == stub, "exportObject returns sentinel stub");
        check("server-ref".equals(sr.remoteToString()), "inherited remoteToString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

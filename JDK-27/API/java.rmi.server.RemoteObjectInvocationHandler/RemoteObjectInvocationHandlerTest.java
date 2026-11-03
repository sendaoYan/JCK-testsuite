/*
 * @test
 * @summary java.rmi.server.RemoteObjectInvocationHandler conformance: white-box structure + black-box behavior.
 * @run main RemoteObjectInvocationHandlerTest
 */

import java.io.*;
import java.lang.reflect.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.Operation;
import java.rmi.server.RemoteCall;
import java.rmi.server.RemoteObject;
import java.rmi.server.RemoteObjectInvocationHandler;
import java.rmi.server.RemoteRef;

public class RemoteObjectInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    interface Echo extends Remote {
        String echo(String s) throws RemoteException;
    }

    static RemoteRef testRef() {
        return new RemoteRef() {
            public Object invoke(Remote obj, Method m, Object[] args, long hash) {
                return "delegated:" + m.getName();
            }
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

    static void testWhiteBox() {
        Class<?> c = RemoteObjectInvocationHandler.class;
        check(c != null, "loads");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(c.getSuperclass() == RemoteObject.class, "extends RemoteObject");
        check(InvocationHandler.class.isAssignableFrom(c), "implements InvocationHandler");
    }

    static void testBlackBox() throws Exception {
        RemoteRef ref = testRef();
        RemoteObjectInvocationHandler h = new RemoteObjectInvocationHandler(ref);
        check(h.getRef() == ref, "getRef returns ref");

        // null ref -> NullPointerException
        boolean npe = false;
        try { new RemoteObjectInvocationHandler(null); } catch (NullPointerException expected) { npe = true; }
        check(npe, "null ref throws NullPointerException");

        // dynamic proxy
        Echo proxy = (Echo) Proxy.newProxyInstance(Echo.class.getClassLoader(), new Class<?>[]{Echo.class}, h);
        check(proxy instanceof Remote, "proxy instanceof Remote");
        check(Proxy.getInvocationHandler(proxy) == h, "proxy handler is h");

        // Object methods handled locally
        String ts = proxy.toString();
        check(ts.startsWith("Proxy[") && ts.contains("Echo") && ts.contains("test-ref"), "proxy toString");
        check(proxy.hashCode() == h.hashCode(), "proxy hashCode == handler hashCode");
        check(proxy.equals(proxy), "proxy equals self");
        Echo proxy2 = (Echo) Proxy.newProxyInstance(Echo.class.getClassLoader(), new Class<?>[]{Echo.class}, h);
        check(proxy.equals(proxy2), "proxies with same handler equal");
        Echo proxy3 = (Echo) Proxy.newProxyInstance(Echo.class.getClassLoader(), new Class<?>[]{Echo.class}, new RemoteObjectInvocationHandler(testRef()));
        check(!proxy.equals(proxy3), "proxies with different handler not equal");

        // remote method delegates to RemoteRef.invoke
        check("delegated:echo".equals(proxy.echo("hi")), "remote method delegation");

        // invoke on a non-proxy -> IllegalArgumentException
        boolean iae = false;
        try { h.invoke(new Object(), Object.class.getMethod("toString"), null); }
        catch (Throwable t) { iae = (t instanceof IllegalArgumentException); }
        check(iae, "invoke on non-proxy throws IllegalArgumentException");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

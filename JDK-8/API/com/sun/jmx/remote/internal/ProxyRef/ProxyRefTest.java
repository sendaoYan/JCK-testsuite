/*
 * @test
 * @summary com.sun.jmx.remote.internal.ProxyRef conformance: hierarchy and declared members via reflection.
 * @run main ProxyRefTest
 */

public class ProxyRefTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.jmx.remote.internal.ProxyRef", false, null);
        check(c.getName().equals("com.sun.jmx.remote.internal.ProxyRef"), "binary name");
        check(c.isInterface() == false, "is interface");
        check(c.isEnum() == false, "is enum");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> in = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.rmi.server.RemoteRef"), "implements java.rmi.server.RemoteRef");
        java.util.Set<String> ms = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) ms.add(m.getName());
        check(ms.contains("done"), "declares done");
        check(ms.contains("getRefClass"), "declares getRefClass");
        check(ms.contains("invoke"), "declares invoke");
        check(ms.contains("newCall"), "declares newCall");
        check(ms.contains("readExternal"), "declares readExternal");
        check(ms.contains("remoteEquals"), "declares remoteEquals");
        check(ms.contains("remoteHashCode"), "declares remoteHashCode");
        check(ms.contains("remoteToString"), "declares remoteToString");
        check(ms.contains("writeExternal"), "declares writeExternal");

        System.out.println("PASS (" + passed + " assertions)");
    }
}

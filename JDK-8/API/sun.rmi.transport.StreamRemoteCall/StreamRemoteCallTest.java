/*
 * @test
 * @summary sun.rmi.transport.StreamRemoteCall conformance: class and members.
 * @run main StreamRemoteCallTest
 */

public class StreamRemoteCallTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.transport.StreamRemoteCall", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.transport.StreamRemoteCall"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.RemoteCall"), "interface java.rmi.server.RemoteCall");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("discardPendingRefs"), "declares method discardPendingRefs");
        check(mnames.contains("done"), "declares method done");
        check(mnames.contains("exceptionReceivedFromServer"), "declares method exceptionReceivedFromServer");
        check(mnames.contains("executeCall"), "declares method executeCall");
        check(mnames.contains("getConnection"), "declares method getConnection");
        check(mnames.contains("getInputStream"), "declares method getInputStream");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

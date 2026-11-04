/*
 * @test
 * @summary sun.rmi.server.UnicastRef conformance: class and members.
 * @run main UnicastRefTest
 */

public class UnicastRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.UnicastRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.UnicastRef"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.RemoteRef"), "interface java.rmi.server.RemoteRef");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("done"), "declares method done");
        check(mnames.contains("free"), "declares method free");
        check(mnames.contains("getLiveRef"), "declares method getLiveRef");
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        check(mnames.contains("invoke"), "declares method invoke");
        check(mnames.contains("logClientCall"), "declares method logClientCall");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

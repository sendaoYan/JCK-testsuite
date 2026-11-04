/*
 * @test
 * @summary sun.rmi.server.ActivatableRef conformance: class and members.
 * @run main ActivatableRefTest
 */

public class ActivatableRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.ActivatableRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.ActivatableRef"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.RemoteRef"), "interface java.rmi.server.RemoteRef");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("activate"), "declares method activate");
        check(mnames.contains("done"), "declares method done");
        check(mnames.contains("getRef"), "declares method getRef");
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        check(mnames.contains("getStub"), "declares method getStub");
        check(mnames.contains("invoke"), "declares method invoke");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

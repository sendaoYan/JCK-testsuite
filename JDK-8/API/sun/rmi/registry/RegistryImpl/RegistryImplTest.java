/*
 * @test
 * @summary sun.rmi.registry.RegistryImpl conformance: class and members.
 * @run main RegistryImplTest
 */

public class RegistryImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.registry.RegistryImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.registry.RegistryImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RemoteServer"), "superclass java.rmi.server.RemoteServer");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.registry.Registry"), "interface java.rmi.registry.Registry");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("checkAccess"), "declares method checkAccess");
        check(mnames.contains("getAccessControlContext"), "declares method getAccessControlContext");
        check(mnames.contains("getID"), "declares method getID");
        check(mnames.contains("getTextResource"), "declares method getTextResource");
        check(mnames.contains("initRegistryFilter"), "declares method initRegistryFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

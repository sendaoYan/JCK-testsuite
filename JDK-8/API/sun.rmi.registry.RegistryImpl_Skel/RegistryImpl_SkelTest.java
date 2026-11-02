/*
 * @test
 * @summary sun.rmi.registry.RegistryImpl_Skel conformance: class and members.
 * @run main RegistryImpl_SkelTest
 */

public class RegistryImpl_SkelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.registry.RegistryImpl_Skel", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.registry.RegistryImpl_Skel"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.server.Skeleton"), "interface java.rmi.server.Skeleton");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("dispatch"), "declares method dispatch");
        check(mnames.contains("getOperations"), "declares method getOperations");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

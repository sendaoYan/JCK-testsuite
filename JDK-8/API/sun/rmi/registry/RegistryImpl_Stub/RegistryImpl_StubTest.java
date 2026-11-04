/*
 * @test
 * @summary sun.rmi.registry.RegistryImpl_Stub conformance: class and members.
 * @run main RegistryImpl_StubTest
 */

public class RegistryImpl_StubTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.registry.RegistryImpl_Stub", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.registry.RegistryImpl_Stub"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.server.RemoteStub"), "superclass java.rmi.server.RemoteStub");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.rmi.registry.Registry"), "interface java.rmi.registry.Registry");
        check(ifaces.contains("java.rmi.Remote"), "interface java.rmi.Remote");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("list"), "declares method list");
        check(mnames.contains("lookup"), "declares method lookup");
        check(mnames.contains("rebind"), "declares method rebind");
        check(mnames.contains("unbind"), "declares method unbind");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

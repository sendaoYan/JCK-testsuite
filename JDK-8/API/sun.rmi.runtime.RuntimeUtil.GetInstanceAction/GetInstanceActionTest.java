/*
 * @test
 * @summary sun.rmi.runtime.RuntimeUtil.GetInstanceAction conformance: class and members.
 * @run main GetInstanceActionTest
 */

public class GetInstanceActionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.runtime.RuntimeUtil$GetInstanceAction", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.runtime.RuntimeUtil$GetInstanceAction"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.security.PrivilegedAction"), "interface java.security.PrivilegedAction");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("run"), "declares method run");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

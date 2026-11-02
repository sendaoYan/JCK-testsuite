/*
 * @test
 * @summary sun.rmi.server.ActivatableServerRef conformance: class and members.
 * @run main ActivatableServerRefTest
 */

public class ActivatableServerRefTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.ActivatableServerRef", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.ActivatableServerRef"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.rmi.server.UnicastServerRef2"), "superclass sun.rmi.server.UnicastServerRef2");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getClientRef"), "declares method getClientRef");
        check(mnames.contains("getRefClass"), "declares method getRefClass");
        check(mnames.contains("writeExternal"), "declares method writeExternal");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary sun.rmi.server.ActivationGroupImpl conformance: class and members.
 * @run main ActivationGroupImplTest
 */

public class ActivationGroupImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.ActivationGroupImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.ActivationGroupImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.rmi.activation.ActivationGroup"), "superclass java.rmi.activation.ActivationGroup");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("acquireLock"), "declares method acquireLock");
        check(mnames.contains("activeObject"), "declares method activeObject");
        check(mnames.contains("checkInactiveGroup"), "declares method checkInactiveGroup");
        check(mnames.contains("covers"), "declares method covers");
        check(mnames.contains("inactiveObject"), "declares method inactiveObject");
        check(mnames.contains("newInstance"), "declares method newInstance");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

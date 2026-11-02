/*
 * @test
 * @summary sun.rmi.log.LogHandler conformance: abstract class and members.
 * @run main LogHandlerTest
 */

public class LogHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.log.LogHandler", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.log.LogHandler"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("applyUpdate"), "declares method applyUpdate");
        check(mnames.contains("initialSnapshot"), "declares method initialSnapshot");
        check(mnames.contains("readUpdate"), "declares method readUpdate");
        check(mnames.contains("recover"), "declares method recover");
        check(mnames.contains("snapshot"), "declares method snapshot");
        check(mnames.contains("writeUpdate"), "declares method writeUpdate");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

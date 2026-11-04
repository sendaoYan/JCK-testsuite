/*
 * @test
 * @summary sun.rmi.server.Activation conformance: class and members.
 * @run main ActivationTest
 */

public class ActivationTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.rmi.server.Activation", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.rmi.server.Activation"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.io.Serializable"), "interface java.io.Serializable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("Pstartgroup"), "declares method Pstartgroup");
        check(mnames.contains("Vstartgroup"), "declares method Vstartgroup");
        check(mnames.contains("activationArgs"), "declares method activationArgs");
        check(mnames.contains("addLogRecord"), "declares method addLogRecord");
        check(mnames.contains("bomb"), "declares method bomb");
        check(mnames.contains("checkArgs"), "declares method checkArgs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

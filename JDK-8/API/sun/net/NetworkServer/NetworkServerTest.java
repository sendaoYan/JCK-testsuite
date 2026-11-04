/*
 * @test
 * @summary sun.net.NetworkServer conformance: class and members.
 * @run main NetworkServerTest
 */

public class NetworkServerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.NetworkServer", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.NetworkServer"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.lang.Runnable"), "interface java.lang.Runnable");
        check(ifaces.contains("java.lang.Cloneable"), "interface java.lang.Cloneable");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("clientIsOpen"), "declares method clientIsOpen");
        check(mnames.contains("clone"), "declares method clone");
        check(mnames.contains("close"), "declares method close");
        check(mnames.contains("main"), "declares method main");
        check(mnames.contains("run"), "declares method run");
        check(mnames.contains("serviceRequest"), "declares method serviceRequest");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

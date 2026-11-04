/*
 * @test
 * @summary sun.net.PortConfig conformance: class and members.
 * @run main PortConfigTest
 */

public class PortConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.net.PortConfig", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.net.PortConfig"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getLower"), "declares method getLower");
        check(mnames.contains("getLower0"), "declares method getLower0");
        check(mnames.contains("getUpper"), "declares method getUpper");
        check(mnames.contains("getUpper0"), "declares method getUpper0");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

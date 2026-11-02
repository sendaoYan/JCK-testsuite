/*
 * @test
 * @summary sun.nio.ch.Net conformance: class and members.
 * @run main NetTest
 */

public class NetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Net", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.Net"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("anyInet4Address"), "declares method anyInet4Address");
        check(mnames.contains("asInetSocketAddress"), "declares method asInetSocketAddress");
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("bind0"), "declares method bind0");
        check(mnames.contains("block4"), "declares method block4");
        check(mnames.contains("block6"), "declares method block6");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

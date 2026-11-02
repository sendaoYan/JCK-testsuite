/*
 * @test
 * @summary sun.misc.JarIndex conformance: class and members.
 * @run main JarIndexTest
 */

public class JarIndexTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JarIndex", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JarIndex"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("add"), "declares method add");
        check(mnames.contains("addMapping"), "declares method addMapping");
        check(mnames.contains("addToList"), "declares method addToList");
        check(mnames.contains("get"), "declares method get");
        check(mnames.contains("getJarFiles"), "declares method getJarFiles");
        check(mnames.contains("getJarIndex"), "declares method getJarIndex");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

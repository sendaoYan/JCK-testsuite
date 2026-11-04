/*
 * @test
 * @summary sun.nio.ch.IOStatus conformance: class and members.
 * @run main IOStatusTest
 */

public class IOStatusTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.IOStatus", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.IOStatus"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("check"), "declares method check");
        check(mnames.contains("checkAll"), "declares method checkAll");
        check(mnames.contains("normalize"), "declares method normalize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

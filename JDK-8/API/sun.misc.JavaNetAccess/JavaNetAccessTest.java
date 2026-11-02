/*
 * @test
 * @summary sun.misc.JavaNetAccess conformance: interface and members.
 * @run main JavaNetAccessTest
 */

public class JavaNetAccessTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.JavaNetAccess", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.JavaNetAccess"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getOriginalHostName"), "declares method getOriginalHostName");
        check(mnames.contains("getURLClassPath"), "declares method getURLClassPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

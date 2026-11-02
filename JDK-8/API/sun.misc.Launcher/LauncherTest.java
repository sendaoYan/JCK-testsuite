/*
 * @test
 * @summary sun.misc.Launcher conformance: class and members.
 * @run main LauncherTest
 */

public class LauncherTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.misc.Launcher", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.misc.Launcher"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getBootstrapClassPath"), "declares method getBootstrapClassPath");
        check(mnames.contains("getClassLoader"), "declares method getClassLoader");
        check(mnames.contains("getClassPath"), "declares method getClassPath");
        check(mnames.contains("getFileURL"), "declares method getFileURL");
        check(mnames.contains("getLauncher"), "declares method getLauncher");
        check(mnames.contains("pathToURLs"), "declares method pathToURLs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

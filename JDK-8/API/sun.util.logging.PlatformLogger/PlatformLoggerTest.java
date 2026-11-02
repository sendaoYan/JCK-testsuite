/*
 * @test
 * @summary sun.util.logging.PlatformLogger conformance: class and members.
 * @run main PlatformLoggerTest
 */

public class PlatformLoggerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.PlatformLogger", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.logging.PlatformLogger"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("config"), "declares method config");
        check(mnames.contains("fine"), "declares method fine");
        check(mnames.contains("finer"), "declares method finer");
        check(mnames.contains("finest"), "declares method finest");
        check(mnames.contains("getLogger"), "declares method getLogger");
        check(mnames.contains("getName"), "declares method getName");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

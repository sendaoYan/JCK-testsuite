/*
 * @test
 * @requires os.family == "linux"
 * @summary sun.nio.ch.EPollSelectorProvider conformance: class and members.
 * @run main EPollSelectorProviderTest
 */

public class EPollSelectorProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.EPollSelectorProvider", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.EPollSelectorProvider"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.SelectorProviderImpl"), "superclass sun.nio.ch.SelectorProviderImpl");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("inheritedChannel"), "declares method inheritedChannel");
        check(mnames.contains("openSelector"), "declares method openSelector");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

/*
 * @test
 * @summary sun.nio.ch.SelectorProviderImpl conformance: abstract class and members.
 * @run main SelectorProviderImplTest
 */

public class SelectorProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectorProviderImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.SelectorProviderImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.SelectorProvider"), "superclass java.nio.channels.spi.SelectorProvider");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("openDatagramChannel"), "declares method openDatagramChannel");
        check(mnames.contains("openPipe"), "declares method openPipe");
        check(mnames.contains("openSelector"), "declares method openSelector");
        check(mnames.contains("openServerSocketChannel"), "declares method openServerSocketChannel");
        check(mnames.contains("openSocketChannel"), "declares method openSocketChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

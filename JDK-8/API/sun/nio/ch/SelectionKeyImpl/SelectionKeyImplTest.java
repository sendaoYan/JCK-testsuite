/*
 * @test
 * @summary sun.nio.ch.SelectionKeyImpl conformance: class and members.
 * @run main SelectionKeyImplTest
 */

public class SelectionKeyImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelectionKeyImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.SelectionKeyImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.channels.spi.AbstractSelectionKey"), "superclass java.nio.channels.spi.AbstractSelectionKey");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("channel"), "declares method channel");
        check(mnames.contains("ensureValid"), "declares method ensureValid");
        check(mnames.contains("getIndex"), "declares method getIndex");
        check(mnames.contains("interestOps"), "declares method interestOps");
        check(mnames.contains("nioInterestOps"), "declares method nioInterestOps");
        check(mnames.contains("nioReadyOps"), "declares method nioReadyOps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

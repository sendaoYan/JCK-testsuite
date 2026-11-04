/*
 * @test
 * @summary sun.nio.ch.SelChImpl conformance: interface and members.
 * @run main SelChImplTest
 */

public class SelChImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.SelChImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.SelChImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        check(ifaces.contains("java.nio.channels.Channel"), "interface java.nio.channels.Channel");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getFD"), "declares method getFD");
        check(mnames.contains("getFDVal"), "declares method getFDVal");
        check(mnames.contains("kill"), "declares method kill");
        check(mnames.contains("translateAndSetInterestOps"), "declares method translateAndSetInterestOps");
        check(mnames.contains("translateAndSetReadyOps"), "declares method translateAndSetReadyOps");
        check(mnames.contains("translateAndUpdateReadyOps"), "declares method translateAndUpdateReadyOps");
        System.out.println("PASS (" + passed + " assertions)");
    }
}

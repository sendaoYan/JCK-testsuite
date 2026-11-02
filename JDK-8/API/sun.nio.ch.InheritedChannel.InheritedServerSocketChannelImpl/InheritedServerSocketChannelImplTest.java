/*
 * @test
 * @summary sun.nio.ch.InheritedChannel.InheritedServerSocketChannelImpl conformance: class and members.
 * @run main InheritedServerSocketChannelImplTest
 */

public class InheritedServerSocketChannelImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.InheritedChannel$InheritedServerSocketChannelImpl", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.ch.InheritedChannel$InheritedServerSocketChannelImpl"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.nio.ch.ServerSocketChannelImpl"), "superclass sun.nio.ch.ServerSocketChannelImpl");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("accept"), "declares method accept");
        check(mnames.contains("bind"), "declares method bind");
        check(mnames.contains("getFD"), "declares method getFD");
        check(mnames.contains("getFDVal"), "declares method getFDVal");
        check(mnames.contains("getLocalAddress"), "declares method getLocalAddress");
        check(mnames.contains("getOption"), "declares method getOption");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
